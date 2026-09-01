package com.example.msa.payment.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** payment.completed 토픽 payload. course-service / recommend-service 가 구독한다. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCompletedEvent {
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private Long itemId;
    private BigDecimal amount;
    private String status;
    private String reason;
}
