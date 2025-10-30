package com.spring.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.domain.PetPlace;
import com.spring.domain.PlaceType;
import com.spring.exception.BadRequestException;
import com.spring.exception.InternalServerException;
import com.spring.exception.NotFoundException;
import com.spring.repository.PetPlaceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PetPlaceService {

    private final PetPlaceRepository repository;

    // 등록
    public PetPlace create(PetPlace place) {
        if (place.getName() == null || place.getName().isBlank())
            throw new BadRequestException("장소 이름은 필수입니다.");
        if (place.getAddress() == null || place.getAddress().isBlank())
            throw new BadRequestException("주소는 필수입니다.");

        try {
            return repository.save(place);
        } catch (Exception e) {
            throw new InternalServerException("장소 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 수정
    @Transactional
    public PetPlace update(Long id, PetPlace updated) {
        if (id == null || id <= 0)
            throw new BadRequestException("유효하지 않은 장소 ID입니다.");

        if (updated == null)
            throw new BadRequestException("요청 데이터가 비어 있습니다.");

        PetPlace existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 장소를 찾을 수 없습니다."));

        try {
        	if (updated.getName() != null)
        	    existing.setName(updated.getName());
        	if (updated.getAddress() != null)
        	    existing.setAddress(updated.getAddress());
        	if (updated.getPhone() != null)
        	    existing.setPhone(updated.getPhone());
        	if (updated.getLatitude() != null)
        	    existing.setLatitude(updated.getLatitude());
        	if (updated.getLongitude() != null)
        	    existing.setLongitude(updated.getLongitude());
        	if (updated.getType() != null)
        	    existing.setType(updated.getType());
        	existing.setPetAllowed(updated.isPetAllowed());
        	if (updated.getDescription() != null)
        	    existing.setDescription(updated.getDescription());

            return repository.save(existing);
        } catch (Exception e) {
            throw new InternalServerException("장소 정보 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 삭제
    public void delete(Long id) {
        try {
            if (!repository.existsById(id)) {
                throw new NotFoundException("삭제하려는 장소가 존재하지 않습니다.");
            }
            repository.deleteById(id);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("장소 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 단일 조회
    public PetPlace getById(Long id) {
        try {
            return repository.findById(id)
                    .orElseThrow(() -> new NotFoundException("해당 장소를 찾을 수 없습니다."));
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("장소 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 전체 조회
    public List<PetPlace> getAll() {
        try {
            List<PetPlace> places = repository.findAll();
            if (places.isEmpty()) {
                throw new NotFoundException("등록된 장소가 없습니다.");
            }
            return places;
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("장소 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 유형별 조회
    public List<PetPlace> getByType(PlaceType type) {
        if (type == null) {
            throw new BadRequestException("유형을 지정해야 합니다.");
        }

        try {
            List<PetPlace> results = repository.findByType(type);
            if (results.isEmpty()) {
                throw new NotFoundException("해당 유형의 장소가 존재하지 않습니다.");
            }
            return results;
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("유형별 장소 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 반려동물 동반 가능 장소 조회
    public List<PetPlace> getPetFriendly() {
        try {
            List<PetPlace> places = repository.findByPetAllowedTrue();
            if (places.isEmpty()) {
                throw new NotFoundException("반려동물 동반이 가능한 장소가 없습니다.");
            }
            return places;
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("반려동물 동반 가능 장소 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
