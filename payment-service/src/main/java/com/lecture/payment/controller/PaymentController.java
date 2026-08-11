package com.lecture.payment.controller;

import com.lecture.payment.dto.PaymentDto;
import com.lecture.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/payments", "/api/screenings"})
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/internal/securities/{ticker}")
    public ResponseEntity<PaymentDto.PaymentResponse> screenSecurity(@PathVariable String ticker) {
        return ResponseEntity.ok(paymentService.screenSecurity(ticker));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto.ApiResponse<PaymentDto.PaymentResponse>> getPayment(@PathVariable Long id) {
        return ResponseEntity.ok(PaymentDto.ApiResponse.success(paymentService.getPayment(id)));
    }

    @GetMapping("/ticker/{ticker}")
    public ResponseEntity<PaymentDto.ApiResponse<List<PaymentDto.PaymentResponse>>> getPaymentsByTicker(
            @PathVariable String ticker) {
        return ResponseEntity.ok(PaymentDto.ApiResponse.success(paymentService.getPaymentsByTicker(ticker)));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<PaymentDto.ApiResponse<List<PaymentDto.PaymentResponse>>> getPaymentsByOrder(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(PaymentDto.ApiResponse.success(paymentService.getPaymentsByOrder(orderId)));
    }
}
