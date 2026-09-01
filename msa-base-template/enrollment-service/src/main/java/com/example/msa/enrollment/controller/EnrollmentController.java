package com.example.msa.enrollment.controller;

import com.example.msa.enrollment.dto.ApiResponse;
import com.example.msa.enrollment.dto.OrderDto;
import com.example.msa.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 주문(Order) REST API. 경로는 인프라 라우팅 호환을 위해 /api/enrollments 를 사용한다. */
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto.Response>> create(
            @Valid @RequestBody OrderDto.CreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(enrollmentService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDto.Response>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(enrollmentService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDto.Response>>> findByUser(
            @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success(enrollmentService.findByUser(userId)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderDto.Response>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(enrollmentService.cancel(id)));
    }
}
