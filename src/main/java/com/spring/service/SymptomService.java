package com.spring.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.domain.Disease;
import com.spring.domain.Symptom;
import com.spring.dto.SymptomRequestDTO;
import com.spring.dto.SymptomResponseDTO;
import com.spring.exception.BadRequestException;
import com.spring.exception.InternalServerException;
import com.spring.exception.NotFoundException;
import com.spring.repository.DiseaseRepository;
import com.spring.repository.SymptomRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SymptomService {

	private final SymptomRepository symptomRepository;
	private final DiseaseRepository diseaseRepository;
	
	private final ObjectMapper objectMapper = new ObjectMapper();


	
	// 증상 기록 등록
	@Transactional
	public SymptomResponseDTO create(SymptomRequestDTO dto) {
	    if (dto == null)
	        throw new BadRequestException("요청 데이터가 비어 있습니다.");

	    if (dto.getMemberId() == null)
	        throw new BadRequestException("회원 ID는 필수입니다.");
	    if (dto.getPetId() == null)
	        throw new BadRequestException("반려동물 ID는 필수입니다.");
	    if (dto.getSelectedSymptomIds() == null || dto.getSelectedSymptomIds().isBlank())
	        throw new BadRequestException("선택된 증상 목록이 비어 있습니다.");

	    Symptom symptom = dto.toEntity();
	    symptom.setCreatedAt(LocalDateTime.now());

	    try {
	        List<Long> selectedIds = objectMapper.readValue(dto.getSelectedSymptomIds(), new TypeReference<>() {});
	        List<Disease> suspected = selectedIds.stream()
	                .<Disease>flatMap(id -> diseaseRepository.findBySymptomIds(id).stream())
	                .distinct().toList();

	        if (suspected.isEmpty())
	            throw new NotFoundException("해당 증상과 일치하는 질병이 없습니다.");

	        String json = objectMapper.writeValueAsString(
	                suspected.stream().map(Disease::getId).toList()
	        );
	        symptom.setSuspectedDiseaseIds(json);

	        Symptom saved = symptomRepository.save(symptom);
	        return SymptomResponseDTO.fromEntity(saved);

	    } catch (BadRequestException | NotFoundException e) {
	        throw e;
	    } catch (Exception e) {
	        throw new InternalServerException("증상 분석 또는 저장 중 오류가 발생했습니다: " + e.getMessage());
	    }
	}

	// 증상 기록 수정
	@Transactional
	public SymptomResponseDTO update(Long id, SymptomRequestDTO dto) {
	    if (id == null || id <= 0)
	        throw new BadRequestException("유효하지 않은 증상 기록 ID입니다.");

	    if (dto == null)
	        throw new BadRequestException("요청 데이터가 비어 있습니다.");

	    Symptom existing = symptomRepository.findById(id)
	            .orElseThrow(() -> new NotFoundException("해당 증상 기록이 존재하지 않습니다."));

	    try {
	        if (dto.getDescription() != null && !dto.getDescription().isBlank())
	            existing.setDescription(dto.getDescription());
	        if (dto.getSymptomDate() != null)
	            existing.setSymptomDate(dto.getSymptomDate());
	        if (dto.getSelectedSymptomIds() != null && !dto.getSelectedSymptomIds().isBlank())
	            existing.setSelectedSymptomIds(dto.getSelectedSymptomIds());

	        // 증상 변경 시 의심 질병 재계산
	        if (dto.getSelectedSymptomIds() != null && !dto.getSelectedSymptomIds().isBlank()) {
	            List<Long> selectedIds = objectMapper.readValue(dto.getSelectedSymptomIds(), new TypeReference<>() {});
	            List<Disease> suspected = selectedIds.stream()
	                    .flatMap(symptomId -> diseaseRepository.findBySymptomIds(symptomId).stream())
	                    .distinct().toList();

	            String json = objectMapper.writeValueAsString(
	                    suspected.stream().map(Disease::getId).toList()
	            );
	            existing.setSuspectedDiseaseIds(json);
	        }

	        Symptom updated = symptomRepository.save(existing);
	        return SymptomResponseDTO.fromEntity(updated);
	    } catch (NotFoundException | BadRequestException e) {
	        throw e;
	    } catch (Exception e) {
	        throw new InternalServerException("증상 기록 수정 중 오류가 발생했습니다: " + e.getMessage());
	    }
	}

	// 증상 기록 삭제
	@Transactional
	public void delete(Long id) {
	    try {
	        if (!symptomRepository.existsById(id)) {
	            throw new NotFoundException("삭제하려는 증상 기록이 존재하지 않습니다.");
	        }
	        symptomRepository.deleteById(id);
	    } catch (NotFoundException e) {
	        throw e;
	    } catch (Exception e) {
	        throw new InternalServerException("증상 기록 삭제 중 오류가 발생했습니다: " + e.getMessage());
	    }
	}

//    // 반려동물별 증상 조회
//    public List<SymptomResponseDTO> getByPetId(Long petId) {
//        List<Symptom> symptoms = symptomRepository.findAllByPetId(petId);
//        if (symptoms.isEmpty()) {
//            throw new NoSuchElementException("등록된 증상 기록이 없습니다.");
//        }
//        return symptoms.stream()
//                       .map(SymptomResponseDTO::fromEntity)
//                       .collect(Collectors.toList());
//    }

	// 단일 증상 기록 조회
	public SymptomResponseDTO getById(Long id) {
	    try {
	        Symptom symptom = symptomRepository.findById(id)
	                .orElseThrow(() -> new NotFoundException("해당 증상 기록이 존재하지 않습니다."));
	        return SymptomResponseDTO.fromEntity(symptom);
	    } catch (NotFoundException e) {
	        throw e;
	    } catch (Exception e) {
	        throw new InternalServerException("증상 기록 조회 중 오류가 발생했습니다: " + e.getMessage());
	    }
	}

    // 특정 회원의 모든 증상 기록 조회
	public List<SymptomResponseDTO> getByMember(Long memberId) {
	    try {
	        List<Symptom> symptoms = symptomRepository.findAllByMemberId(memberId);
	        if (symptoms.isEmpty()) {
	            throw new NotFoundException("해당 회원의 증상 기록이 존재하지 않습니다.");
	        }
	        return symptoms.stream()
	                .map(SymptomResponseDTO::fromEntity)
	                .toList();
	    } catch (NotFoundException e) {
	        throw e;
	    } catch (Exception e) {
	        throw new InternalServerException("회원 증상 기록 조회 중 오류가 발생했습니다: " + e.getMessage());
	    }
	}

//    // 특정 회원 + 반려동물의 증상 기록 조회
//    public List<SymptomResponseDTO> getByMemberAndPet(Long memberId, Long petId) {
//        return symptomRepository.findAllByMemberIdAndPetId(memberId, petId)
//                .stream()
//                .map(SymptomResponseDTO::fromEntity)
//                .toList();
//    }
}
