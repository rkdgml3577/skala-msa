package com.lecture.enrollment.kafka;

import lombok.*;

/**
 * Kafka 이벤트 메시지 DTO
 */
public class KafkaEvent {

    /**
     * Screening Service → Order Service 심사 완료 이벤트
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScreeningCompletedEvent {
        private Long screeningId;
        private Long orderId;
        private Long clientId;
        private String ticker;
        private String grade;
        private String reason;
        private String ruleVersionId;
    }

    /**
     * Order Service → Screening Service 주문 접수 이벤트
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderReceivedEvent {
        private Long orderId;
        private Long clientId;
        private String ticker;
        private String side;
        private java.math.BigDecimal quantity;
        private java.math.BigDecimal orderPrice;
    }
}
