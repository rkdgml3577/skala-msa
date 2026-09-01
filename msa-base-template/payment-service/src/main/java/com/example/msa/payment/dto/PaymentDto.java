package com.example.msa.payment.dto;

import com.example.msa.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long orderId;
        private Long userId;
        private Long itemId;
        private BigDecimal amount;
        private String status;
        private String reason;
        private LocalDateTime createdAt;

        public static Response from(Payment payment) {
            return Response.builder()
                    .id(payment.getId())
                    .orderId(payment.getOrderId())
                    .userId(payment.getUserId())
                    .itemId(payment.getItemId())
                    .amount(payment.getAmount())
                    .status(payment.getStatus().name())
                    .reason(payment.getReason())
                    .createdAt(payment.getCreatedAt())
                    .build();
        }
    }
}
