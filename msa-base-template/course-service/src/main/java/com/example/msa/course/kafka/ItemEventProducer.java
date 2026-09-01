package com.example.msa.course.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.item-created}")
    private String itemCreatedTopic;

    public void publishItemCreated(ItemCreatedEvent event) {
        log.info("[Kafka Producer] item.created 발행 - itemId={}, code={}",
                event.getItemId(), event.getCode());
        kafkaTemplate.send(itemCreatedTopic, event.getCode(), event);
    }
}
