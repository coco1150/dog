package com.spring.controller;

import java.util.List;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.spring.common.ApiResponse;
import com.spring.domain.PetPlace;
import com.spring.domain.PlaceType;
import com.spring.service.PetPlaceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/local/places")
@RequiredArgsConstructor
public class PetPlaceController {

    private final PetPlaceService service;

    // 등록
    @PostMapping
    public ResponseEntity<ApiResponse<PetPlace>> create(@RequestBody PetPlace place) {
        PetPlace saved = service.create(place);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "장소 등록 성공", saved));
    }

    // 수정
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PetPlace>> update(
            @PathVariable Long id,
            @RequestBody PetPlace place) {
        PetPlace updated = service.update(id, place);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "장소 수정 성공", updated));
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "장소 삭제 성공", null));
    }

    // 단일 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PetPlace>> getById(@PathVariable Long id) {
        PetPlace result = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "단일 장소 조회 성공", result));
    }

    // 전체 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<PetPlace>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "전체 장소 조회 성공", service.getAll()));
    }

    // 유형별 조회
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<PetPlace>>> getByType(@PathVariable PlaceType type) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "유형별 장소 조회 성공", service.getByType(type)));
    }

    // 반려동물 동반 가능 장소만
    @GetMapping("/pet-friendly")
    public ResponseEntity<ApiResponse<List<PetPlace>>> getPetFriendly() {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "동반 가능 장소 조회 성공", service.getPetFriendly()));
    }
}
