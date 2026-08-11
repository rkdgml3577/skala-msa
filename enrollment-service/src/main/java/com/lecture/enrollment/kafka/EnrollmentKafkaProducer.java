package com.lecture.enrollment.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.order-received}")
    private String orderReceivedTopic;

    /**
     * order.received 이벤트 발행 → screening-service가 비동기 감사 심사
     */
    public void publishEnrollmentCompleted(KafkaEvent.OrderReceivedEvent event) {
        log.info("[Kafka Producer] order.received 발행 - orderId: {}, clientId: {}, ticker: {}",
                event.getOrderId(), event.getClientId(), event.getTicker());

        kafkaTemplate.send(orderReceivedTopic, event.getTicker(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka Producer] order.received 발행 실패: {}", ex.getMessage());
                    } else {
                        log.info("[Kafka Producer] order.received 발행 성공 - offset: {}",
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
