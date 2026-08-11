package com.lecture.payment.kafka;

import com.lecture.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentKafkaConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "${kafka.topic.security-updated}", groupId = "screening-service-security")
    public void handleSecurityUpdated(Map<String, Object> event) {
        try {
            paymentService.screenSecurity(requiredString(event, "ticker"));
        } catch (Exception e) {
            log.error("[Kafka Consumer] security.updated 심사 실패 - event: {}", event, e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.order-received}", groupId = "screening-service-order")
    public void handleOrderReceived(Map<String, Object> event) {
        try {
            Long orderId = requiredLong(event, "orderId");
            Long clientId = requiredLong(event, "clientId");
            paymentService.screenOrder(orderId, clientId, requiredString(event, "ticker"));
        } catch (Exception e) {
            log.error("[Kafka Consumer] order.received 심사 실패 - event: {}", event, e);
        }
    }

    private String requiredString(Map<String, Object> event, String key) {
        Object value = event.get(key);
        if (value == null) throw new IllegalArgumentException(key + " 값이 없습니다.");
        return value.toString();
    }

    private Long requiredLong(Map<String, Object> event, String key) {
        Object value = event.get(key);
        if (!(value instanceof Number number)) throw new IllegalArgumentException(key + " 값이 없습니다.");
        return number.longValue();
    }
}
