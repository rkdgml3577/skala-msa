package com.lecture.enrollment.controller;

import com.lecture.enrollment.dto.EnrollmentDto;
import com.lecture.enrollment.entity.Enrollment;
import com.lecture.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 외부에는 구독이라는 도메인만 노출한다. 기존 /api/enrollments 주문 API는 호환성을 위해 보존한다.
 */
@RestController
@RequestMapping({"/api/subscriptions", "/api/enrollments/subscriptions"})
@RequiredArgsConstructor
public class SubscriptionController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<EnrollmentDto.ApiResponse<EnrollmentDto.EnrollmentResponse>> subscribe(
            @Valid @RequestBody EnrollmentDto.SubscriptionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Long clientId = userId != null ? userId : request.getClientId();
        if (clientId == null) {
            throw new IllegalArgumentException("은행사 식별자(X-User-Id 또는 clientId)가 필요합니다");
        }
        EnrollmentDto.EnrollmentResponse response = enrollmentService.subscribe(clientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(EnrollmentDto.ApiResponse.success(response));
    }

    @GetMapping("/my")
    public ResponseEntity<EnrollmentDto.ApiResponse<List<EnrollmentDto.EnrollmentResponse>>> getMySubscriptions(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(EnrollmentDto.ApiResponse.success(enrollmentService.getSubscriptionsByUser(userId)));
    }

    @PatchMapping("/{subscriptionId}/status")
    public ResponseEntity<EnrollmentDto.ApiResponse<EnrollmentDto.EnrollmentResponse>> changeStatus(
            @PathVariable Long subscriptionId,
            @Valid @RequestBody EnrollmentDto.SubscriptionStatusRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        Enrollment.Status status = request.getStatus();
        EnrollmentDto.EnrollmentResponse response = enrollmentService.changeSubscriptionStatus(userId, subscriptionId, status);
        return ResponseEntity.ok(EnrollmentDto.ApiResponse.success(response));
    }
}
