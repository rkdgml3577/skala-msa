package com.example.msa.enrollment.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** order.created 토픽 payload. payment-service / ai-screening-service 가 구독한다. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreatedEvent {
    private Long orderId;
    private Long userId;
    private Long itemId;
    private int quantity;
    private BigDecimal amount;
}
