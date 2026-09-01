package com.example.msa.payment.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.payment-completed}")
    private String paymentCompletedTopic;

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        log.info("[Kafka Producer] payment.completed 발행 - paymentId={}, status={}",
                event.getPaymentId(), event.getStatus());
        kafkaTemplate.send(paymentCompletedTopic, String.valueOf(event.getOrderId()), event);
    }
}
