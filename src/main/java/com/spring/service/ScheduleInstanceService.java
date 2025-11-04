package com.spring.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ScheduleService scheduleService;

    // 특정 일정의 발생 인스턴스 생성
    public List<ScheduleInstance> generateInstances(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("해당 ID의 일정이 존재하지 않습니다."));

        // 기존 인스턴스 삭제 후 다시 생성 (중복 방지)
        instanceRepository.deleteAllByScheduleId(scheduleId);

        List<LocalDateTime> occurrenceTimes = scheduleService.generateOccurrences(schedule);

        for (LocalDateTime time : occurrenceTimes) {
            ScheduleInstance instance = new ScheduleInstance();
            instance.setSchedule(schedule);
            instance.setOccurrenceTime(time);
            instanceRepository.save(instance);
        }

        return instanceRepository.findAllByScheduleId(scheduleId);
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
            throw new IllegalArgumentException("scheduleId가 null입니다.");

        try {
            instanceRepository.deleteAllByScheduleId(scheduleId);
        } catch (Exception e) {
            throw new InternalServerException("스케줄 인스턴스 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
