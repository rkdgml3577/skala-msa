package com.example.msa.course.kafka;

import com.example.msa.course.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * payment.completed 이벤트를 소비해 해당 상품의 판매 수를 증가시키는 Consumer 예시.
 * (다른 서비스가 발행한 이벤트를 구독하는 패턴)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCompletedConsumer {

    private final ItemService itemService;

    @KafkaListener(topics = "${kafka.topic.payment-completed}", groupId = "course-service")
    public void onPaymentCompleted(Map<String, Object> event) {
        log.info("[Kafka Consumer] payment.completed 수신: {}", event);
        Object itemId = event.get("itemId");
        if (itemId != null) {
            try {
                itemService.increaseSoldCount(Long.valueOf(String.valueOf(itemId)));
            } catch (Exception e) {
                log.warn("[Kafka Consumer] 판매 수 증가 실패 - itemId={}, error={}", itemId, e.getMessage());
            }
        }
    }
}
