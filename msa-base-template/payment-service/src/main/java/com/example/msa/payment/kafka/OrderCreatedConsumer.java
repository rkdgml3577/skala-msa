package com.example.msa.payment.kafka;

import com.example.msa.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/** order.created 이벤트를 소비해 결제 심사를 수행하는 Consumer. */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "${kafka.topic.order-created}", groupId = "payment-service")
    public void onOrderCreated(Map<String, Object> event) {
        log.info("[Kafka Consumer] order.created 수신: {}", event);
        try {
            paymentService.processOrder(
                    toLong(event.get("orderId")),
                    toLong(event.get("userId")),
                    toLong(event.get("itemId")),
                    new BigDecimal(String.valueOf(event.get("amount")))
            );
        } catch (Exception e) {
            log.error("[Kafka Consumer] 결제 심사 실패: {}", e.getMessage(), e);
        }
    }

    private Long toLong(Object value) {
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }
}
