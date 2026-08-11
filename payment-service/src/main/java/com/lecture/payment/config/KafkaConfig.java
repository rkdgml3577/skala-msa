package com.lecture.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    NewTopic orderReceivedTopic(@Value("${kafka.topic.order-received}") String name) {
        return topic(name);
    }

    @Bean
    NewTopic securityUpdatedTopic(@Value("${kafka.topic.security-updated}") String name) {
        return topic(name);
    }

    @Bean
    NewTopic screeningCompletedTopic(@Value("${kafka.topic.screening-completed}") String name) {
        return topic(name);
    }

    private NewTopic topic(String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }
}
