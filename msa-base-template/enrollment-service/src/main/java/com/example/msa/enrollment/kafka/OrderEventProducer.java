package com.example.msa.enrollment.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.order-created}")
    private String orderCreatedTopic;

    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("[Kafka Producer] order.created 발행 - orderId={}, userId={}, itemId={}",
                event.getOrderId(), event.getUserId(), event.getItemId());
        kafkaTemplate.send(orderCreatedTopic, String.valueOf(event.getItemId()), event);
    }
}
