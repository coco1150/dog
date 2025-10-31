package com.spring.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.domain.RecurrenceRule;
import com.spring.domain.RecurrenceType;
import com.spring.domain.Schedule;
import com.spring.dto.ScheduleRequestDTO;
import com.spring.dto.ScheduleResponseDTO;
import com.spring.exception.BadRequestException;
import com.spring.exception.InternalServerException;
import com.spring.exception.NotFoundException;
import com.spring.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

	private final ScheduleRepository scheduleRepository;

	// 일정 등록
	@Transactional
	public ScheduleResponseDTO create(ScheduleRequestDTO dto) {
	    boolean isRecurring = Boolean.TRUE.equals(dto.getRecurring());

	    if (isRecurring) {
	        if (dto.getRecurrenceType() == null || dto.getRecurrenceType().isBlank())
	            throw new BadRequestException("반복 일정은 반복 유형을 지정해야 합니다.");
	        if (dto.getInterval() == null || dto.getInterval() <= 0)
	            throw new BadRequestException("반복 간격(interval)은 1 이상이어야 합니다.");
	        if ("WEEKLY".equalsIgnoreCase(dto.getRecurrenceType())) {
	            if (dto.getDaysOfWeek() == null || dto.getDaysOfWeek().isBlank())
	                throw new BadRequestException("주간 반복 일정은 요일(daysOfWeek)을 지정해야 합니다.");
	        }
	        if (dto.getStartDate() == null)
	            throw new BadRequestException("반복 일정의 시작일(startDate)은 반드시 지정해야 합니다.");
	    } else {
	        if (dto.getScheduleTime() == null)
	            throw new BadRequestException("단일 일정은 scheduleTime을 지정해야 합니다.");
	    }

	    try {
	        Schedule s = new Schedule();
	        s.setMemberId(dto.getMemberId());
	        s.setTitle(dto.getTitle());
	        s.setRecurring(isRecurring);

	        if (!isRecurring) {
	            s.setScheduleTime(dto.getScheduleTime());
	        } else {
	            RecurrenceRule rule = new RecurrenceRule();
	            rule.setType(RecurrenceType.valueOf(dto.getRecurrenceType().trim().toUpperCase()));
	            rule.setInterval(dto.getInterval());

	            if (rule.getType() == RecurrenceType.WEEKLY) {
	                rule.setDaysOfWeek(parseDays(dto.getDaysOfWeek())); // String -> List<DayOfWeek>
	            }
	            if (rule.getType() == RecurrenceType.MONTHLY && dto.getDayOfMonth() != null) {
	                rule.setDayOfMonth(dto.getDayOfMonth());
	            }

	            rule.setRepeatCount(dto.getRepeatCount());
	            rule.setUntilDate(dto.getUntilDate());

	            s.setRecurrenceRule(rule);
	            s.setStartDate(dto.getStartDate());
	            s.setEndDate(dto.getEndDate());
	        }

	        Schedule saved = scheduleRepository.save(s);
	        return toResponse(saved);

	    } catch (IllegalArgumentException e) {
	        // RecurrenceType valueOf 실패 등
	        throw new BadRequestException("반복 유형 값이 올바르지 않습니다. DAILY/WEEKLY/MONTHLY 중 하나여야 합니다.");
	    } catch (Exception e) {
	        throw new InternalServerException("일정 등록 중 오류가 발생했습니다: " + e.getMessage());
	    }
	}

	// 일정 수정
	@Transactional
	public ScheduleResponseDTO update(Long id, ScheduleRequestDTO dto) {
	    Schedule existing = scheduleRepository.findById(id)
	            .orElseThrow(() -> new NotFoundException("해당 일정이 존재하지 않습니다."));

	    try {
	        if (dto.getTitle() != null && !dto.getTitle().isBlank())
	            existing.setTitle(dto.getTitle());

	        if (dto.getRecurring() != null)
	            existing.setRecurring(dto.getRecurring());

	        boolean toRecurring = Boolean.TRUE.equals(dto.getRecurring());
	        if (!toRecurring) {
	            // 단일 일정 변경
	            if (dto.getScheduleTime() != null)
	                existing.setScheduleTime(dto.getScheduleTime());
	            existing.setRecurrenceRule(null);
	            existing.setStartDate(null);
	            existing.setEndDate(null);
	        } else {
	            // 반복 일정 변경
	            RecurrenceRule rule = existing.getRecurrenceRule();
	            if (rule == null) rule = new RecurrenceRule();

	            if (dto.getRecurrenceType() != null && !dto.getRecurrenceType().isBlank())
	                rule.setType(RecurrenceType.valueOf(dto.getRecurrenceType().trim().toUpperCase()));

	            if (dto.getInterval() != null) {
	                if (dto.getInterval() <= 0) throw new BadRequestException("반복 간격은 1 이상이어야 합니다.");
	                rule.setInterval(dto.getInterval());
	            }

	            if (rule.getType() == RecurrenceType.WEEKLY) {
	                if (dto.getDaysOfWeek() == null || dto.getDaysOfWeek().isBlank())
	                    throw new BadRequestException("주간 반복 일정은 요일을 지정해야 합니다.");
	                rule.setDaysOfWeek(parseDays(dto.getDaysOfWeek()));
	            } else if (dto.getDaysOfWeek() != null) {
	                // WEEKLY가 아닌데 daysOfWeek 넘어오면 무시 혹은 초기화
	                rule.setDaysOfWeek(List.of());
	            }

	            if (rule.getType() == RecurrenceType.MONTHLY && dto.getDayOfMonth() != null)
	                rule.setDayOfMonth(dto.getDayOfMonth());

	            if (dto.getRepeatCount() != null && dto.getRepeatCount() < 0)
	                throw new BadRequestException("반복 횟수는 0 이상이어야 합니다.");
	            rule.setRepeatCount(dto.getRepeatCount());

	            if (dto.getStartDate() != null) existing.setStartDate(dto.getStartDate());
	            if (dto.getEndDate() != null) {
	                if (existing.getStartDate() != null && dto.getEndDate().isBefore(existing.getStartDate()))
	                    throw new BadRequestException("종료일은 시작일보다 빠를 수 없습니다.");
	                existing.setEndDate(dto.getEndDate());
	            }
	            if (dto.getUntilDate() != null) rule.setUntilDate(dto.getUntilDate());

	            existing.setRecurrenceRule(rule);
	        }

	        Schedule updated = scheduleRepository.save(existing);
	        return toResponse(updated);

	    } catch (IllegalArgumentException e) {
	        throw new BadRequestException("반복 유형 값이 올바르지 않습니다. DAILY/WEEKLY/MONTHLY 중 하나여야 합니다.");
	    } catch (BadRequestException | NotFoundException e) {
	        throw e;
	    } catch (Exception e) {
	        throw new InternalServerException("일정 수정 중 오류가 발생했습니다: " + e.getMessage());
	    }
	}

	// 단일 조회
	public ScheduleResponseDTO getById(Long id) {
		Schedule s = scheduleRepository.findById(id).orElseThrow(() -> new NotFoundException("해당 일정이 존재하지 않습니다."));
		return ScheduleResponseDTO.fromEntity(s);
	}

	// 회원별 조회
	public List<ScheduleResponseDTO> getByMemberId(Long memberId) {
		List<Schedule> list = scheduleRepository.findAllByMemberId(memberId);
		if (list.isEmpty())
			throw new NotFoundException("등록된 일정이 없습니다.");
		return list.stream().map(ScheduleResponseDTO::fromEntity).toList();
	}

	// 삭제
	public void delete(Long id) {
		if (!scheduleRepository.existsById(id))
			throw new NotFoundException("삭제할 일정이 존재하지 않습니다.");
		scheduleRepository.deleteById(id);
	}

	// 반복 일정 실제 발생 시점 계산 
	public List<LocalDateTime> generateOccurrences(Schedule schedule) {
	    if (schedule == null) {
	        throw new BadRequestException("일정 데이터가 없습니다.");
	    }

	    // 단일 일정인 경우
	    if (Boolean.FALSE.equals(schedule.getRecurring()) || schedule.getRecurrenceRule() == null) {
	        if (schedule.getScheduleTime() == null) {
	            throw new BadRequestException("단일 일정의 시간 정보가 없습니다.");
	        }
	        return List.of(schedule.getScheduleTime());
	    }

	    // 반복 일정인 경우
	    RecurrenceRule rule = schedule.getRecurrenceRule();
	    if (rule.getType() == null) {
	        throw new BadRequestException("반복 유형(type)이 지정되지 않았습니다.");
	    }
	    if (schedule.getScheduleTime() == null) {
	        throw new BadRequestException("반복 일정의 기준 시간이 없습니다.");
	    }

	    List<LocalDateTime> occurrences = new ArrayList<>();

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
	                    if (!d.isAfter(end))
	                        occurrences.add(LocalDateTime.of(d, schedule.getScheduleTime().toLocalTime()));
	                }
	                current = current.plusWeeks(interval);
	            }
	        }
	        case MONTHLY -> {
	            int day = rule.getDayOfMonth() != null
	                    ? rule.getDayOfMonth()
	                    : (schedule.getStartDate() != null ? schedule.getStartDate().getDayOfMonth() : 1);
	            while (!current.isAfter(end)) {
	                LocalDate d = current.withDayOfMonth(Math.min(day, current.lengthOfMonth()));
	                occurrences.add(LocalDateTime.of(d, schedule.getScheduleTime().toLocalTime()));
	                current = current.plusMonths(interval);
	            }
	        }
	        default -> throw new BadRequestException("지원하지 않는 반복 유형입니다.");
	    }

	    return occurrences;
	}
	
	// 반복 일정 발생일 계산
	public List<LocalDateTime> getOccurrences(Long id) {
	    // 일정 존재 여부 확인
	    Schedule schedule = scheduleRepository.findById(id)
	            .orElseThrow(() -> new NotFoundException("해당 일정이 존재하지 않습니다."));

	    // 반복 여부 및 규칙 유효성 검증
	    if (!Boolean.TRUE.equals(schedule.getRecurring()) || schedule.getRecurrenceRule() == null) {
	        throw new BadRequestException("이 일정은 반복 일정이 아닙니다.");
	    }

	    try {
	        RecurrenceRule rule = schedule.getRecurrenceRule();
	        LocalDate start = schedule.getStartDate();
	        LocalDate end = schedule.getEndDate();

	        if (start == null) {
	            throw new BadRequestException("반복 일정의 시작일(startDate)이 지정되어야 합니다.");
	        }

	        // 반복 일정 계산 로직
	        List<LocalDateTime> occurrences = new ArrayList<>();
	        LocalDate currentDate = start;
	        int count = 0;

	        while ((end == null || !currentDate.isAfter(end))
	                && (rule.getRepeatCount() == null || count < rule.getRepeatCount())) {

	            switch (rule.getType()) {
	                case DAILY -> {
	                    occurrences.add(currentDate.atStartOfDay());
	                    currentDate = currentDate.plusDays(rule.getInterval());
	                }
	                case WEEKLY -> {
	                    if (rule.getDaysOfWeek() != null && !rule.getDaysOfWeek().isEmpty()) {
	                        for (DayOfWeek day : rule.getDaysOfWeek()) {
	                            LocalDate next = currentDate.with(day);
	                            if ((end == null || !next.isAfter(end)) && !next.isBefore(start)) {
	                                occurrences.add(next.atStartOfDay());
	                            }
	                        }
	                    }
	                    currentDate = currentDate.plusWeeks(rule.getInterval());
	                }
	                case MONTHLY -> {
	                    int dayOfMonth = (rule.getDayOfMonth() != null) ? rule.getDayOfMonth() : 1;
	                    LocalDate next = currentDate.withDayOfMonth(
	                            Math.min(dayOfMonth, currentDate.lengthOfMonth())
	                    );
	                    occurrences.add(next.atStartOfDay());
	                    currentDate = currentDate.plusMonths(rule.getInterval());
	                }
	                default -> throw new BadRequestException("지원하지 않는 반복 유형입니다.");
	            }

	            count++;
	        }

	        if (occurrences.isEmpty()) {
	            throw new NotFoundException("해당 규칙에 따라 생성된 반복 일정이 없습니다.");
	        }

	        return occurrences;

	    } catch (BadRequestException | NotFoundException e) {
	        throw e;
	    } catch (Exception e) {
	        throw new InternalServerException("반복 일정 계산 중 오류가 발생했습니다: " + e.getMessage());
	    }
	}	

	private List<DayOfWeek> parseDays(String input) {
	    if (input == null || input.isBlank()) return List.of();
	    String[] tokens = input.split("[,\\s]+");
	    List<DayOfWeek> result = new ArrayList<>();
	    for (String t : tokens) {
	        try {
	            result.add(DayOfWeek.valueOf(t.trim().toUpperCase()));
	        } catch (IllegalArgumentException ignored) {
	            // 잘못된 요일 문자열은 무시
	        }
	    }
	    return result;
	}
	
	
	private ScheduleResponseDTO toResponse(Schedule s) {
	    if (Boolean.TRUE.equals(s.getRecurring()) && s.getRecurrenceRule() != null) {
	        var r = s.getRecurrenceRule();
	        return new ScheduleResponseDTO(
	                s.getId(),
	                s.getMemberId(),
	                s.getTitle(),
	                true,
	                null, // 단일 일정이 아니므로 null
	                r.getType(),
	                r.getInterval(),
	                r.getDaysOfWeek(),   // List<DayOfWeek> 그대로
	                r.getDayOfMonth(),
	                r.getRepeatCount(),
	                s.getStartDate(),
	                s.getEndDate(),
	                r.getUntilDate()
	        );
	    } else {
	        return new ScheduleResponseDTO(
	                s.getId(),
	                s.getMemberId(),
	                s.getTitle(),
	                false,
	                s.getScheduleTime(),
	                null, null, null, null, null,
	                null, null, null
	        );
	    }
	}

}
