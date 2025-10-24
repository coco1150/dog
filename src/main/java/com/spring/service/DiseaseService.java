package com.spring.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.domain.Disease;
import com.spring.repository.DiseaseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DiseaseService {

    private final DiseaseRepository diseaseRepository;

    // 질병 등록
    public Disease create(Disease disease) {
        return diseaseRepository.save(disease);
    }

    // 질병 단일 조회
    public Disease getById(Long id) {
        return diseaseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 질병이 존재하지 않습니다."));
    }

    // 질병 전체 조회
    public List<Disease> getAll() {
        return diseaseRepository.findAll();
    }

    // 질병 수정
    public Disease update(Long id, Disease updatedDisease) {
        Disease existing = getById(id);
        if (updatedDisease.getName() != null)
            existing.setName(updatedDisease.getName());
        if (updatedDisease.getDescription() != null)
            existing.setDescription(updatedDisease.getDescription());
        if (updatedDisease.getRelatedSymptomIds() != null)
            existing.setRelatedSymptomIds(updatedDisease.getRelatedSymptomIds());

        return diseaseRepository.save(existing);
    }

    // 질병 삭제
    public void delete(Long id) {
        if (!diseaseRepository.existsById(id)) {
            throw new NoSuchElementException("삭제하려는 질병이 존재하지 않습니다.");
        }
        diseaseRepository.deleteById(id);
    }
}
