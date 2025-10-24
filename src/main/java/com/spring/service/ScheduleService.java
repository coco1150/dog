package com.spring.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.spring.domain.Schedule;
import com.spring.dto.ScheduleRequestDTO;
import com.spring.dto.ScheduleResponseDTO;
import com.spring.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

	private final ScheduleRepository scheduleRepository;

    // 일정 등록
    public ScheduleResponseDTO create(ScheduleRequestDTO dto) {
        Schedule schedule = dto.toEntity();

        // 1. 날짜 검증
        if (schedule.getStartDate() != null && schedule.getEndDate() != null &&
            schedule.getEndDate().isBefore(schedule.getStartDate())) {
            throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
        }

        // 2. 반복 유형 검증
        if (schedule.isRecurring() && (schedule.getRecurrenceType() == null || schedule.getRecurrenceType().isBlank())) {
            throw new IllegalArgumentException("반복 일정은 반복 유형을 지정해야 합니다.");
        }

        // 3. 저장
        Schedule saved = scheduleRepository.save(schedule);
        return ScheduleResponseDTO.fromEntity(saved);
    }
    
    // 일정 수정
    public ScheduleResponseDTO update(Long id, ScheduleRequestDTO dto) {
        Schedule existing = scheduleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 일정이 존재하지 않습니다."));

        // 변경사항 반영
        if (dto.getTitle() != null) existing.setTitle(dto.getTitle());
        if (dto.getScheduleTime() != null) existing.setScheduleTime(dto.getScheduleTime());
        if (dto.getStartDate() != null) existing.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) existing.setEndDate(dto.getEndDate());
        if (dto.getRecurrenceType() != null) existing.setRecurrenceType(dto.getRecurrenceType());
        if (dto.getIsRecurring() != null) existing.setRecurring(dto.getIsRecurring());

        // 날짜 검증
        if (existing.getStartDate() != null && existing.getEndDate() != null &&
            existing.getEndDate().isBefore(existing.getStartDate())) {
            throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
        }

        Schedule updated = scheduleRepository.save(existing);
        return ScheduleResponseDTO.fromEntity(updated);
    }
    
    

    // 일정 조회 (회원별)
    public List<ScheduleResponseDTO> getByMemberId(Long memberId) {
        List<Schedule> schedules = scheduleRepository.findAllByMemberId(memberId);
        if (schedules.isEmpty()) {
            throw new NoSuchElementException("등록된 일정이 없습니다.");
        }
        return schedules.stream().map(ScheduleResponseDTO::fromEntity).toList();
    }

//	// 특정 회원 + 반려동물 일정 조회
//	public List<Schedule> getByMemberAndPet(Long memberId, Long petId) {
//		return scheduleRepository.findAllByMemberIdAndPetId(memberId, petId);
//	}

	// 단일 일정 조회
	public ScheduleResponseDTO getById(Long id) {
		 Schedule schedule = scheduleRepository.findById(id).orElseThrow(() -> new NoSuchElementException("해당 일정이 존재하지 않습니다."));
		 return ScheduleResponseDTO.fromEntity(schedule);
	}

    // 일정 삭제
    public void delete(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new NoSuchElementException("삭제할 일정이 존재하지 않습니다.");
        }
        scheduleRepository.deleteById(id);
    }
}
