package com.spring.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public ScheduleResponseDTO create(ScheduleRequestDTO dto) {
        if (dto == null) {
            throw new BadRequestException("일정 데이터가 비어 있습니다.");
        }

        Schedule schedule = dto.toEntity();

        // 날짜 검증
        if (schedule.getStartDate() != null && schedule.getEndDate() != null &&
            schedule.getEndDate().isBefore(schedule.getStartDate())) {
            throw new BadRequestException("종료일은 시작일보다 빠를 수 없습니다.");
        }

        // 반복 일정 검증
        if (schedule.isRecurring() && 
            (schedule.getRecurrenceType() == null || schedule.getRecurrenceType().isBlank())) {
            throw new BadRequestException("반복 일정은 반복 유형을 지정해야 합니다.");
        }

        try {
            Schedule saved = scheduleRepository.save(schedule);
            return ScheduleResponseDTO.fromEntity(saved);
        } catch (Exception e) {
            throw new InternalServerException("일정 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 일정 수정
    public ScheduleResponseDTO update(Long id, ScheduleRequestDTO dto) {
        if (dto == null) {
            throw new BadRequestException("수정할 일정 데이터가 비어 있습니다.");
        }

        try {
            Schedule existing = scheduleRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("수정하려는 일정이 존재하지 않습니다."));

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
                throw new BadRequestException("종료일은 시작일보다 빠를 수 없습니다.");
            }

            Schedule updated = scheduleRepository.save(existing);
            return ScheduleResponseDTO.fromEntity(updated);
        } catch (NotFoundException | BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("일정 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 일정 조회 (회원별)
    public List<ScheduleResponseDTO> getByMemberId(Long memberId) {
        try {
            List<Schedule> schedules = scheduleRepository.findAllByMemberId(memberId);
            if (schedules.isEmpty()) {
                throw new NotFoundException("해당 회원의 등록된 일정이 없습니다.");
            }
            return schedules.stream().map(ScheduleResponseDTO::fromEntity).toList();
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("일정 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 단일 일정 조회
    public ScheduleResponseDTO getById(Long id) {
        try {
            Schedule schedule = scheduleRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("해당 일정이 존재하지 않습니다."));
            return ScheduleResponseDTO.fromEntity(schedule);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("일정 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 일정 삭제
    public void delete(Long id) {
        try {
            if (!scheduleRepository.existsById(id)) {
                throw new NotFoundException("삭제할 일정이 존재하지 않습니다.");
            }
            scheduleRepository.deleteById(id);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("일정 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
