package com.lecture.enrollment.kafka;

import com.lecture.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentKafkaConsumer {

    private final EnrollmentService enrollmentService;

    /**
     * screening.completed 이벤트 수신
     * → 비동기 심사 결과를 주문에 멱등 반영
     *
     * payment-service 쪽은 JsonSerializer + type header 미포함으로 이벤트를 발행하므로,
     * 여기서는 특정 DTO 타입으로 바로 받지 않고 Map<String, Object> 로 받아 처리한다.
     */
    @KafkaListener(
            topics = "${kafka.topic.screening-completed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCompleted(Map<String, Object> event) {
        log.info("[Kafka Consumer] screening.completed raw event 수신: {}", event);

        try {
            Object orderIdValue = event.get("orderId");
            Object tickerValue = event.get("ticker");
            Object gradeValue = event.get("grade");

            // 종목(Level 1) 심사 결과는 course-service가 담당한다.
            if (orderIdValue == null) {
                return;
            }
            if (tickerValue == null || gradeValue == null) {
                throw new IllegalArgumentException("Kafka 이벤트에 ticker 또는 grade가 없습니다.");
            }

            Long orderId = ((Number) orderIdValue).longValue();

            enrollmentService.applyScreeningResult(
                    orderId,
                    tickerValue.toString(),
                    gradeValue.toString(),
                    event.get("reason") != null ? event.get("reason").toString() : null);

            log.info("[Kafka Consumer] 주문 심사 결과 반영 - orderId: {}, grade: {}", orderId, gradeValue);

        } catch (Exception e) {
            log.error("[Kafka Consumer] 주문 심사 결과 반영 실패 - event: {}, error: {}",
                    event, e.getMessage(), e);
        }
    }
}
