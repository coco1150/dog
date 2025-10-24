package com.spring.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.spring.domain.PetPlace;
import com.spring.domain.PlaceType;
import com.spring.repository.PetPlaceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PetPlaceService {

    private final PetPlaceRepository repository;

    // 등록
    public PetPlace create(PetPlace place) {
        return repository.save(place);
    }

    // 수정
    public PetPlace update(Long id, PetPlace updated) {
        PetPlace existing = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 장소를 찾을 수 없습니다."));
        existing.setName(updated.getName());
        existing.setAddress(updated.getAddress());
        existing.setPhone(updated.getPhone());
        existing.setLatitude(updated.getLatitude());
        existing.setLongitude(updated.getLongitude());
        existing.setType(updated.getType());
        existing.setPetAllowed(updated.isPetAllowed());
        existing.setDescription(updated.getDescription());
        return repository.save(existing);
    }

    // 삭제
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("삭제할 장소가 존재하지 않습니다.");
        }
        repository.deleteById(id);
    }

    // 단일 조회
    public PetPlace getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 장소를 찾을 수 없습니다."));
    }

    // 전체 조회
    public List<PetPlace> getAll() {
        return repository.findAll();
    }

    // 유형별 조회
    public List<PetPlace> getByType(PlaceType type) {
        return repository.findByType(type);
    }

    // 반려동물 동반 가능 장소 조회
    public List<PetPlace> getPetFriendly() {
        return repository.findByPetAllowedTrue();
    }
}
