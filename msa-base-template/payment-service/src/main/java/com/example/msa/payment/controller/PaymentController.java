package com.example.msa.payment.controller;

import com.example.msa.payment.dto.ApiResponse;
import com.example.msa.payment.dto.PaymentDto;
import com.example.msa.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 결제/심사 조회 REST API. 경로는 인프라 라우팅 호환을 위해 /api/payments 를 사용한다. */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentDto.Response>>> findAll() {
        List<PaymentDto.Response> body = paymentService.findAll().stream()
                .map(PaymentDto.Response::from).toList();
        return ResponseEntity.ok(ApiResponse.success(body));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentDto.Response>> findByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(
                PaymentDto.Response.from(paymentService.findByOrderId(orderId))));
    }
}
