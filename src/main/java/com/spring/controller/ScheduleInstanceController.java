package com.spring.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.spring.common.ApiResponse;
import com.spring.domain.ScheduleInstance;
import com.spring.service.ScheduleInstanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleInstanceController {

    private final ScheduleInstanceService instanceService;

    // 특정 스케줄의 인스턴스 생성
    @PostMapping("/{id}/generate")
    public ResponseEntity<ApiResponse<List<ScheduleInstance>>> generate(@PathVariable("id") Long id) {
        List<ScheduleInstance> instances = instanceService.generateInstances(id);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "일정 인스턴스 생성 완료", instances));
    }

    // 특정 스케줄의 인스턴스 조회
    @GetMapping("/{id}/instances")
    public ResponseEntity<ApiResponse<List<ScheduleInstance>>> getInstances(@PathVariable("id") Long id) {
        List<ScheduleInstance> instances = instanceService.getInstances(id);
        return ResponseEntity
                .ok(ApiResponse.success(HttpStatus.OK, "일정 인스턴스 조회 성공", instances));
    }

    // 특정 인스턴스 완료 처리
    @PutMapping("/instances/{instanceId}/complete")
    public ResponseEntity<ApiResponse<Void>> markCompleted(@PathVariable("instanceId") Long instanceId) {
        instanceService.markCompleted(instanceId);
        return ResponseEntity
                .ok(ApiResponse.success(HttpStatus.OK, "일정 인스턴스 완료 처리됨", null));
    }
}
