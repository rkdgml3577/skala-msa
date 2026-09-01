package com.example.msa.user.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** user.created 이벤트를 발행하는 Kafka Producer 예시. */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.user-created}")
    private String userCreatedTopic;

    public void publishUserCreated(UserCreatedEvent event) {
        log.info("[Kafka Producer] user.created 발행 - userId={}, username={}",
                event.getUserId(), event.getUsername());

        kafkaTemplate.send(userCreatedTopic, event.getUsername(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka Producer] user.created 발행 실패: {}", ex.getMessage());
                    } else {
                        log.info("[Kafka Producer] user.created 발행 성공 - offset={}",
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
