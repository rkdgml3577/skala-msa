package com.lecture.course.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    NewTopic securityUpdatedTopic(@Value("${kafka.topic.security-updated}") String topic) {
        return topic(topic);
    }

    @Bean
    NewTopic screeningCompletedTopic(@Value("${kafka.topic.screening-completed}") String topic) {
        return topic(topic);
    }

    @Bean
    NewTopic anomalyDetectedTopic(@Value("${kafka.topic.anomaly-detected}") String topic) {
        return topic(topic);
    }

    private NewTopic topic(String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }
}
