package com.spring.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.spring.domain.ToxicFood;
import com.spring.dto.ToxicFoodRequestDTO;
import com.spring.dto.ToxicFoodResponseDTO;
import com.spring.repository.ToxicFoodRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ToxicFoodService {

	private final ToxicFoodRepository toxicFoodRepository;

	// 독성 음식 등록
	public ToxicFoodResponseDTO create(ToxicFoodRequestDTO dto) {
		ToxicFood food = new ToxicFood();
		food.setName(dto.getName());
		food.setCategory(dto.getCategory());
//		food.setIngredient(dto.getIngredient());
//		food.setAdditionalIngredient(dto.getAdditionalIngredient());
		food.setToxicityLevel(dto.getToxicityLevel());
		food.setDescription(dto.getDescription());
//		food.setSymptoms(dto.getSymptoms());
//		food.setSafeDose(dto.getSafeDose());
//		food.setImageUrl(dto.getImageUrl());
		food.setNote(dto.getNote());

		// 중복 이름 검증
		if (toxicFoodRepository.existsByName(food.getName())) {
			throw new IllegalArgumentException("이미 등록된 식품 이름입니다.");
		}

		ToxicFood saved = toxicFoodRepository.save(food);
		return ToxicFoodResponseDTO.fromEntity(saved);
	}

	// 독성 식품 수정
	public ToxicFoodResponseDTO update(Long id, ToxicFoodRequestDTO dto) {
		ToxicFood existing = toxicFoodRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("해당 식품 정보가 존재하지 않습니다."));

		if (dto.getName() != null)
			existing.setName(dto.getName());
		if (dto.getCategory() != null)
			existing.setCategory(dto.getCategory());
		if (dto.getToxicityLevel() != null)
			existing.setToxicityLevel(dto.getToxicityLevel());
		if (dto.getDescription() != null)
			existing.setDescription(dto.getDescription());
		if (dto.getNote() != null)
			existing.setNote(dto.getNote());

		ToxicFood updated = toxicFoodRepository.save(existing);
		return ToxicFoodResponseDTO.fromEntity(updated);
	}

	// 독성 식품 삭제
	public void delete(Long id) {
		if (!toxicFoodRepository.existsById(id)) {
			throw new NoSuchElementException("삭제할 식품 정보가 존재하지 않습니다.");
		}
		toxicFoodRepository.deleteById(id);
	}

	// 전체 목록 조회
	public List<ToxicFoodResponseDTO> getAll() {
		return toxicFoodRepository.findAll().stream().map(ToxicFoodResponseDTO::fromEntity)
				.collect(Collectors.toList());
	}

	//ID로 조회
	public ToxicFoodResponseDTO getById(Long id) {
	    ToxicFood food = toxicFoodRepository.findById(id)
	            .orElseThrow(() -> new NoSuchElementException("해당 ID의 식품 정보가 존재하지 않습니다."));
	    return ToxicFoodResponseDTO.fromEntity(food);
	}

	// 이름으로 조회
	public ToxicFoodResponseDTO getByName(String name) {
		ToxicFood food = toxicFoodRepository.findByName(name)
				.orElseThrow(() -> new NoSuchElementException("해당 이름의 식품 정보를 찾을 수 없습니다."));
		return ToxicFoodResponseDTO.fromEntity(food);
	}

//	// 카테고리별 조회
//	public List<ToxicFoodResponseDTO> getByCategory(String categoryName) {
//		return toxicFoodRepository.findAll().stream().filter(f -> f.getCategory().name().equalsIgnoreCase(categoryName))
//				.map(ToxicFoodResponseDTO::fromEntity).collect(Collectors.toList());
//	}

}