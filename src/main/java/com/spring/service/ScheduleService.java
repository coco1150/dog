package com.spring.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.spring.domain.Schedule;
import com.spring.dto.ScheduleRequestDTO;
import com.spring.dto.ScheduleResponseDTO;
import com.spring.exception.BadRequestException;
import com.spring.exception.InternalServerException;
import com.spring.exception.NotFoundException;
import com.spring.repository.ScheduleRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

	private final ScheduleRepository scheduleRepository;

	// 일정 등록
	public ScheduleResponseDTO create(ScheduleRequestDTO dto) {
		// 기본 검증
		if (dto.getMemberId() == null || dto.getMemberId() <= 0)
			throw new BadRequestException("회원 ID는 필수입니다.");
		if (dto.getTitle() == null || dto.getTitle().isBlank())
			throw new BadRequestException("일정 제목은 비워둘 수 없습니다.");

		// 조건부 검증
		boolean isRecurring = Boolean.TRUE.equals(dto.getIsRecurring());
		if (isRecurring) {
			if (dto.getRecurrenceType() == null || dto.getRecurrenceType().isBlank())
				throw new BadRequestException("반복 일정은 반복 유형을 지정해야 합니다.");
			if (dto.getStartDate() == null)
				throw new BadRequestException("반복 일정의 시작일은 반드시 지정해야 합니다.");
		} else if (dto.getStartDate() != null && dto.getEndDate() != null) {
			if (dto.getEndDate().isBefore(dto.getStartDate()))
				throw new BadRequestException("종료일은 시작일보다 빠를 수 없습니다.");
		} else if (dto.getStartDate() == null && dto.getEndDate() == null) {
			if (dto.getScheduleTime() == null)
				throw new BadRequestException("하루 일정은 날짜 또는 시간을 반드시 지정해야 합니다.");
		}

		// 저장
		try {
			Schedule schedule = dto.toEntity();
			Schedule saved = scheduleRepository.save(schedule);
			return ScheduleResponseDTO.fromEntity(saved);
		} catch (DataIntegrityViolationException e) {
			throw new BadRequestException("입력 데이터가 잘못되었습니다: " + e.getMostSpecificCause().getMessage());
		} catch (Exception e) {
			throw new InternalServerException("일정 등록 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	// 일정 수정
	@Transactional
	public ScheduleResponseDTO update(Long id, ScheduleRequestDTO dto) {
	    if (id == null || id <= 0)
	        throw new BadRequestException("유효하지 않은 일정 ID입니다.");

	    if (dto == null)
	        throw new BadRequestException("요청 데이터가 비어 있습니다.");

	    Schedule existing = scheduleRepository.findById(id)
	            .orElseThrow(() -> new NotFoundException("해당 일정이 존재하지 않습니다."));

	    try {
	        if (dto.getTitle() != null)
	            existing.setTitle(dto.getTitle());
	        if (dto.getScheduleTime() != null)
	            existing.setScheduleTime(dto.getScheduleTime());
	        if (dto.getStartDate() != null)
	            existing.setStartDate(dto.getStartDate());
	        if (dto.getEndDate() != null)
	            existing.setEndDate(dto.getEndDate());
	        if (dto.getRecurrenceType() != null)
	            existing.setRecurrenceType(dto.getRecurrenceType());
	        if (dto.getIsRecurring() != null)
	            existing.setRecurring(dto.getIsRecurring());

	        // 날짜 검증
	        if (existing.getStartDate() != null && existing.getEndDate() != null &&
	            existing.getEndDate().isBefore(existing.getStartDate())) {
	            throw new BadRequestException("종료일은 시작일보다 빠를 수 없습니다.");
	        }

	        Schedule updated = scheduleRepository.save(existing);
	        return ScheduleResponseDTO.fromEntity(updated);
	    } catch (BadRequestException e) {
	        throw e;
	    } catch (Exception e) {
	        throw new InternalServerException("일정 수정 중 오류가 발생했습니다: " + e.getMessage());
	    }
	}

	// 단일 일정 조회
	public ScheduleResponseDTO getById(Long id) {
		Schedule schedule = scheduleRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("해당 일정이 존재하지 않습니다."));
		return ScheduleResponseDTO.fromEntity(schedule);
	}

	// 회원별 일정 조회
	public List<ScheduleResponseDTO> getByMemberId(Long memberId) {
		List<Schedule> schedules = scheduleRepository.findAllByMemberId(memberId);
		if (schedules.isEmpty()) {
			throw new NotFoundException("등록된 일정이 없습니다.");
		}
		return schedules.stream().map(ScheduleResponseDTO::fromEntity).toList();
	}

	// 일정 삭제
	public void delete(Long id) {
		if (!scheduleRepository.existsById(id)) {
			throw new NotFoundException("삭제할 일정이 존재하지 않습니다.");
		}
		try {
			scheduleRepository.deleteById(id);
		} catch (Exception e) {
			throw new InternalServerException("일정 삭제 중 오류가 발생했습니다: " + e.getMessage());
		}
	}
}