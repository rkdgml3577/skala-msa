package com.lecture.user.kafka;

import com.lecture.user.dto.UserDto;
import com.lecture.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.trade-received}")
    private String tradeReceivedTopic;

    public void publishTradeReceived(User client, String modelCode,
                                     UserDto.TransactionRequest request, String ticker) {
        TradeReceivedEvent event = new TradeReceivedEvent(
                request.getTransactionId(), client.getId(), client.getClientCode(), modelCode,
                ticker, request.getSide(), request.getQuantity(), request.getPrice(),
                request.getOccurredAt() != null ? request.getOccurredAt() : LocalDateTime.now(),
                request.getSector(), request.getMerchantCategory(),
                request.getInterestBearingDebtRatio(), request.getNonPermissibleIncomeRatio());

        try {
            // 외부 API가 즉시 판정 결과를 기다릴 수 있도록 브로커 수신까지만 확인한다.
            // 이후 AI 소비·판정·감사 저장은 기존처럼 Kafka 비동기 흐름으로 처리된다.
            var result = kafkaTemplate.send(tradeReceivedTopic, client.getId() + ":" + request.getTransactionId(), event)
                    .get(1, TimeUnit.SECONDS);
            log.info("[Kafka Producer] trade.received 발행 - transactionId: {}, model: {}, offset: {}",
                    request.getTransactionId(), modelCode, result.getRecordMetadata().offset());
        } catch (Exception e) {
            log.error("[Kafka Producer] trade.received 발행 실패 - transactionId: {}, model: {}",
                    request.getTransactionId(), modelCode, e);
            throw new IllegalStateException("거래 탐지 요청을 Kafka에 전달하지 못했습니다", e);
        }
    }

    public record TradeReceivedEvent(
            String transactionId, Long clientId, String clientCode, String modelCode,
            String ticker, String side, BigDecimal quantity, BigDecimal price,
            LocalDateTime occurredAt, String sector, String merchantCategory,
            BigDecimal interestBearingDebtRatio, BigDecimal nonPermissibleIncomeRatio
    ) {}
}
