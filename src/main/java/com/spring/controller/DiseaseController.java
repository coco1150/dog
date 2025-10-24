package com.spring.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.spring.common.ApiResponse;
import com.spring.domain.Disease;
import com.spring.service.DiseaseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/diseases")
@RequiredArgsConstructor
public class DiseaseController {

    private final DiseaseService diseaseService;

    // 등록
    @PostMapping
    public ResponseEntity<ApiResponse<Disease>> create(@RequestBody Disease disease) {
        Disease created = diseaseService.create(disease);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "질병 등록 성공", created));
    }

    // 단일 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Disease>> getById(@PathVariable("id") Long id) {
        Disease result = diseaseService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "질병 조회 성공", result));
    }

    // 전체 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<Disease>>> getAll() {
        List<Disease> list = diseaseService.getAll();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "전체 질병 목록 조회 성공", list));
    }

    // 수정
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Disease>> update(
            @PathVariable("id") Long id,
            @RequestBody Disease disease
    ) {
        Disease updated = diseaseService.update(id, disease);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "질병 수정 성공", updated));
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        diseaseService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "질병 삭제 성공", null));
    }
}
