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
import com.spring.repository.DiseaseRepository;
import com.spring.repository.SymptomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SymptomService {

	private final SymptomRepository symptomRepository;
	private final DiseaseRepository diseaseRepository;
	
	private final ObjectMapper objectMapper = new ObjectMapper();


	
	// 증상 기록 등록
	public SymptomResponseDTO create(SymptomRequestDTO dto) {
	    Symptom symptom = dto.toEntity();
	    symptom.setCreatedAt(LocalDateTime.now());

	    try {
	        List<Long> selectedIds = objectMapper.readValue(
	            dto.getSelectedSymptomIds(),
	            new TypeReference<List<Long>>() {}
	        );

	        // DB에서는 문자열 LIKE 검색으로 질병 조회
	        List<Disease> suspectedDiseases = selectedIds.stream()
	                .flatMap(id -> diseaseRepository.findBySymptomIds(id.toString()).stream())
	                .distinct()
	                .toList();

	        // 의심 질병 ID만 JSON으로 저장
	        List<Long> diseaseIds = suspectedDiseases.stream()
	                .map(Disease::getId)
	                .toList();

	        String json = objectMapper.writeValueAsString(diseaseIds);
	        symptom.setSuspectedDiseaseIds(json);

	    } catch (Exception e) {
	        symptom.setSuspectedDiseaseIds("[]");
	    }

	    Symptom saved = symptomRepository.save(symptom);
	    return SymptomResponseDTO.fromEntity(saved);
	}

	// 증상 기록 수정
	public SymptomResponseDTO update(Long id, SymptomRequestDTO dto) {
	    Symptom existing = symptomRepository.findById(id)
	            .orElseThrow(() -> new NoSuchElementException("해당 증상 기록이 존재하지 않습니다."));

	    if (dto.getDescription() != null)
	        existing.setDescription(dto.getDescription());
	    if (dto.getSymptomDate() != null)
	        existing.setSymptomDate(dto.getSymptomDate());
	    if (dto.getSelectedSymptomIds() != null)
	        existing.setSelectedSymptomIds(dto.getSelectedSymptomIds());
	 // 증상 변경 시 의심 질병 재계산
	    if (dto.getSelectedSymptomIds() != null && !dto.getSelectedSymptomIds().isEmpty()) {
	        try {
	            List<Long> selectedIds = objectMapper.readValue(
	                dto.getSelectedSymptomIds(),
	                new TypeReference<List<Long>>() {}
	            );
	            // 여러 증상 ID 각각에 대해 질병 검색
	            List<Disease> suspectedDiseases = selectedIds.stream()
	                    .flatMap(symptomId -> diseaseRepository.findBySymptomIds(id.toString()).stream())
	                    .distinct()
	                    .toList();

	            List<Long> diseaseIds = suspectedDiseases.stream()
	                    .map(Disease::getId)
	                    .toList();

	            String json = objectMapper.writeValueAsString(diseaseIds);
	            existing.setSuspectedDiseaseIds(json);

	        } catch (Exception e) {
	            existing.setSuspectedDiseaseIds("[]");
	        }
	    }

	    Symptom updated = symptomRepository.save(existing);
	    return SymptomResponseDTO.fromEntity(updated);
	}

	// 증상 기록 삭제
    public void delete(Long id) {
        if (!symptomRepository.existsById(id)) {
            throw new NoSuchElementException("삭제하려는 증상 기록이 존재하지 않습니다.");
        }
        symptomRepository.deleteById(id);
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
        Symptom symptom = symptomRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 증상 기록이 존재하지 않습니다."));
        return SymptomResponseDTO.fromEntity(symptom);
    }

    // 특정 회원의 모든 증상 기록 조회
    public List<SymptomResponseDTO> getByMember(Long memberId) {
        List<Symptom> symptoms = symptomRepository.findAllByMemberId(memberId);
        return symptoms.stream()
                .map(SymptomResponseDTO::fromEntity)
                .toList();
    }

//    // 특정 회원 + 반려동물의 증상 기록 조회
//    public List<SymptomResponseDTO> getByMemberAndPet(Long memberId, Long petId) {
//        return symptomRepository.findAllByMemberIdAndPetId(memberId, petId)
//                .stream()
//                .map(SymptomResponseDTO::fromEntity)
//                .toList();
//    }
}
