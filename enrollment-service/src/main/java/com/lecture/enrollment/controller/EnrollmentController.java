package com.lecture.enrollment.controller;

import com.lecture.enrollment.dto.EnrollmentDto;
import com.lecture.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/enrollments", "/api/orders"})
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * POST /enrollments - 매매주문 사전 심사
     */
    @PostMapping
    public ResponseEntity<EnrollmentDto.ApiResponse<EnrollmentDto.EnrollmentResponse>> enroll(
            @Valid @RequestBody EnrollmentDto.EnrollRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        Long clientId = userId != null ? userId : request.getClientId();
        if (clientId == null) {
            throw new IllegalArgumentException("고객사 식별자(X-User-Id 또는 clientId)가 필요합니다");
        }
        EnrollmentDto.EnrollmentResponse response = enrollmentService.placeOrder(clientId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EnrollmentDto.ApiResponse.success(response));
    }

    /**
     * GET /enrollments/my - 고객사의 주문 심사 이력
     * Gateway가 전달한 X-User-Id 헤더를 사용
     */
    @GetMapping("/my")
    public ResponseEntity<EnrollmentDto.ApiResponse<List<EnrollmentDto.EnrollmentResponse>>> getMyEnrollments(
            @RequestHeader("X-User-Id") Long userId) {

        List<EnrollmentDto.EnrollmentResponse> response =
                enrollmentService.getEnrollmentsByUser(userId);
        return ResponseEntity.ok(EnrollmentDto.ApiResponse.success(response));
    }

    /**
     * GET /enrollments/user/{userId} - 특정 고객사 주문 이력
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<EnrollmentDto.ApiResponse<List<EnrollmentDto.EnrollmentResponse>>> getEnrollments(
            @PathVariable Long userId) {

        List<EnrollmentDto.EnrollmentResponse> response =
                enrollmentService.getEnrollmentsByUser(userId);
        return ResponseEntity.ok(EnrollmentDto.ApiResponse.success(response));
    }

    /**
     * GET /enrollments/internal/history/{userId} - 보유/보류 티커 이력
     */
    @GetMapping("/internal/history/{userId}")
    public ResponseEntity<EnrollmentDto.EnrollmentHistoryResponse> getEnrollmentHistory(
            @PathVariable Long userId) {

        return ResponseEntity.ok(enrollmentService.getEnrollmentHistory(userId));
    }
}
