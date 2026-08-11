package com.lecture.payment.kafka;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.screening-completed}")
    private String paymentCompletedTopic;

    /**
     * screening.completed 이벤트 발행 → order/security/advisory 서비스가 각 그룹으로 수신
     *
     * 개발/검증 단계에서는 전송 성공 여부를 즉시 확인하기 위해 동기적으로 기다린다.
     */
    public void publishPaymentCompleted(ScreeningCompletedEvent event) {
        log.info("[Kafka Producer] screening.completed 발행 - screeningId: {}, ticker: {}, grade: {}",
                event.getScreeningId(), event.getTicker(), event.getGrade());

        try {
            SendResult<String, Object> result = kafkaTemplate
                    .send(paymentCompletedTopic, event.getTicker(), event)
                    .get(10, TimeUnit.SECONDS);

            log.info("[Kafka Producer] screening.completed 발행 성공 - topic: {}, partition: {}, offset: {}",
                    paymentCompletedTopic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

        } catch (Exception e) {
            log.error("[Kafka Producer] screening.completed 발행 실패 - topic: {}, screeningId: {}, ticker: {}, error: {}",
                    paymentCompletedTopic,
                    event.getScreeningId(),
                    event.getTicker(),
                    e.getMessage(),
                    e);

            throw new RuntimeException("screening.completed Kafka 발행 실패", e);
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScreeningCompletedEvent {
        private Long screeningId;
        private String screeningType;
        private Long orderId;
        private Long clientId;
        private String ticker;
        private String grade;
        private String reason;
        private String ruleVersionId;
        private Map<String, BigDecimal> ratios;
    }
}
