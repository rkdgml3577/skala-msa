package com.lecture.course.kafka;

import com.lecture.course.entity.Course;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.security-updated}")
    private String securityUpdatedTopic;

    public void publishSecurityUpdated(Course course) {
        SecurityUpdatedEvent event = new SecurityUpdatedEvent(
                course.getTicker(), course.getName(), course.getSector().name(),
                course.getMarketCap(), course.getInterestBearingDebt(),
                course.getInterestEarningAssets(), course.getNonPermissibleIncome(),
                course.getTotalRevenue(), course.getDescription());

        kafkaTemplate.send(securityUpdatedTopic, course.getTicker(), event)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("[Kafka Producer] security.updated 발행 실패 - ticker: {}", course.getTicker(), error);
                    } else {
                        log.info("[Kafka Producer] security.updated 발행 - ticker: {}, offset: {}",
                                course.getTicker(), result.getRecordMetadata().offset());
                    }
                });
    }

    public record SecurityUpdatedEvent(
            String ticker,
            String name,
            String sector,
            BigDecimal marketCap,
            BigDecimal interestBearingDebt,
            BigDecimal interestEarningAssets,
            BigDecimal nonPermissibleIncome,
            BigDecimal totalRevenue,
            String businessDescription
    ) {}
}
