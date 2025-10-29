package com.spring.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.domain.Disease;
import com.spring.exception.BadRequestException;
import com.spring.exception.InternalServerException;
import com.spring.exception.NotFoundException;
import com.spring.repository.DiseaseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DiseaseService {

    private final DiseaseRepository diseaseRepository;

    // 질병 등록
    public Disease create(Disease disease) {
        if (disease == null) {
            throw new BadRequestException("질병 데이터가 비어 있습니다.");
        }
        if (disease.getName() == null || disease.getName().isBlank()) {
            throw new BadRequestException("질병 이름은 필수 입력 항목입니다.");
        }

        try {
            return diseaseRepository.save(disease);
        } catch (Exception e) {
            throw new InternalServerException("질병 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 질병 단일 조회
    public Disease getById(Long id) {
        try {
            return diseaseRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("해당 질병이 존재하지 않습니다."));
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("질병 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 질병 전체 조회
    public List<Disease> getAll() {
        try {
            List<Disease> diseases = diseaseRepository.findAll();
            if (diseases.isEmpty()) {
                throw new NotFoundException("등록된 질병이 없습니다.");
            }
            return diseases;
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("질병 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 질병 수정
    public Disease update(Long id, Disease updatedDisease) {
        try {
            Disease existing = diseaseRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("수정하려는 질병이 존재하지 않습니다."));

            if (updatedDisease.getName() != null)
                existing.setName(updatedDisease.getName());
            if (updatedDisease.getDescription() != null)
                existing.setDescription(updatedDisease.getDescription());
            if (updatedDisease.getRelatedSymptomIds() != null)
                existing.setRelatedSymptomIds(updatedDisease.getRelatedSymptomIds());

            return diseaseRepository.save(existing);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("질병 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 질병 삭제
    public void delete(Long id) {
        try {
            if (!diseaseRepository.existsById(id)) {
                throw new NotFoundException("삭제하려는 질병이 존재하지 않습니다.");
            }
            diseaseRepository.deleteById(id);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("질병 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
