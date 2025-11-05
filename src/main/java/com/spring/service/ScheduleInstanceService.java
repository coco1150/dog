package com.spring.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.domain.RecurrenceRule;
import com.spring.domain.Schedule;
import com.spring.domain.ScheduleInstance;
import com.spring.exception.InternalServerException;
import com.spring.exception.NotFoundException;
import com.spring.repository.ScheduleInstanceRepository;
import com.spring.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleInstanceService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleInstanceRepository instanceRepository;
    

    // 특정 일정의 발생 인스턴스 생성
    public List<ScheduleInstance> generateInstances(Schedule schedule) {
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule이 null입니다.");
        }

        instanceRepository.deleteAllByScheduleId(schedule.getId());

        List<LocalDateTime> occurrenceTimes = generateOccurrences(schedule);

        for (LocalDateTime time : occurrenceTimes) {
            ScheduleInstance instance = new ScheduleInstance();
            instance.setSchedule(schedule);
            instance.setOccurrenceTime(time);
            instanceRepository.save(instance);
        }

        return instanceRepository.findAllByScheduleId(schedule.getId());
    }
    
    
    // 일정에서 발생 시간을 계산하는 내부 메소드 (scheduleService 대신 직접 계산)
    private List<LocalDateTime> generateOccurrences(Schedule schedule) {
        List<LocalDateTime> occurrences = new ArrayList<>();
        RecurrenceRule rule = schedule.getRecurrenceRule();
        if (rule == null) return occurrences;

        LocalDate current = schedule.getStartDate();
        LocalDate end = schedule.getEndDate() != null ? schedule.getEndDate() : current.plusMonths(3);
        int interval = rule.getInterval() != null ? rule.getInterval() : 1;

        switch (rule.getType()) {
            case DAILY -> {
                while (!current.isAfter(end)) {
                    occurrences.add(LocalDateTime.of(current, schedule.getScheduleTime().toLocalTime()));
                    current = current.plusDays(interval);
                }
            }
            case WEEKLY -> {
                List<DayOfWeek> days = rule.getDaysOfWeek() != null ? rule.getDaysOfWeek() : List.of();
                while (!current.isAfter(end)) {
                    for (DayOfWeek day : days) {
                        LocalDate d = current.with(java.time.temporal.TemporalAdjusters.nextOrSame(day));
                        if (!d.isAfter(end)) {
                            occurrences.add(LocalDateTime.of(d, schedule.getScheduleTime().toLocalTime()));
                        }
                    }
                    current = current.plusWeeks(interval);
                }
            }
            case MONTHLY -> {
                int day = rule.getDayOfMonth() != null ? rule.getDayOfMonth()
                        : schedule.getStartDate().getDayOfMonth();
                while (!current.isAfter(end)) {
                    LocalDate d = current.withDayOfMonth(Math.min(day, current.lengthOfMonth()));
                    occurrences.add(LocalDateTime.of(d, schedule.getScheduleTime().toLocalTime()));
                    current = current.plusMonths(interval);
                }
            }
            default -> throw new IllegalArgumentException("지원하지 않는 반복 유형입니다.");
        }

        return occurrences;
    }   
    

    // 특정 일정의 모든 발생 인스턴스 조회
    @Transactional(readOnly = true)
    public List<ScheduleInstance> getInstances(Long scheduleId) {
        return instanceRepository.findAllByScheduleId(scheduleId);
    }

    // 특정 인스턴스 완료 처리 (추후 확장용)
    public void markCompleted(Long instanceId) {
        ScheduleInstance instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new NotFoundException("해당 인스턴스가 존재하지 않습니다."));
        instance.setCompleted(true);
        instanceRepository.save(instance);
    }
    
    @Transactional
    public void deleteAllByScheduleId(Long scheduleId) {
        if (scheduleId == null)
            throw new IllegalArgumentException("scheduleId가 비어있습니다.");
        instanceRepository.deleteAllByScheduleId(scheduleId);
    }
    
    @Transactional
    public void regenerateInstances(Schedule schedule) {
        deleteAllByScheduleId(schedule.getId());
        generateInstances(schedule);
    }
}
