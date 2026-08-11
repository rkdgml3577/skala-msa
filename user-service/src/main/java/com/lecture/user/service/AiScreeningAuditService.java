package com.lecture.user.service;

import com.lecture.user.dto.UserDto;
import com.lecture.user.entity.AiScreeningResult;
import com.lecture.user.repository.AiScreeningResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiScreeningAuditService {

    private final AiScreeningResultRepository resultRepository;

    @Transactional
    public void record(Map<String, Object> event) {
        Long clientId = asLong(event.get("clientId"));
        String transactionId = asText(event.get("transactionId"));
        String modelCode = asText(event.get("modelCode"));
        String ticker = asText(event.get("ticker"));
        String status = asText(event.get("status"));

        if (clientId == null || transactionId == null || modelCode == null || ticker == null || status == null) {
            log.warn("[AI Audit] 필수 값이 없는 결과 이벤트를 무시합니다: {}", event);
            return;
        }

        resultRepository.save(AiScreeningResult.builder()
                .transactionId(transactionId)
                .clientId(clientId)
                .clientCode(asText(event.get("clientCode")))
                .modelCode(modelCode)
                .ticker(ticker)
                .status(status)
                .reason(asText(event.get("reason")))
                .ruleVersion(asText(event.get("ruleVersion")))
                .build());
        log.info("[AI Audit] 결과 저장 - clientId: {}, transactionId: {}, model: {}", clientId, transactionId, modelCode);
    }

    public List<UserDto.AiScreeningResultResponse> getResults(Long clientId) {
        return resultRepository.findTop100ByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Kafka 비동기 처리는 그대로 유지하되, 외부 거래 API에는 짧은 시간 안의 판정 결과를 돌려준다.
     * 동일 transactionId의 과거 이력이 섞이지 않도록 이번 전송 시점 이후 결과만 조회한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, readOnly = true)
    public List<UserDto.AiScreeningResultResponse> awaitTransactionResults(
            Long clientId, String transactionId, LocalDateTime submittedAt,
            List<String> expectedModels, Duration timeout) {
        LocalDateTime from = submittedAt.minusSeconds(1);
        long deadline = System.nanoTime() + timeout.toNanos();
        List<UserDto.AiScreeningResultResponse> latest = List.of();

        while (System.nanoTime() < deadline) {
            latest = resultRepository
                    .findByClientIdAndTransactionIdAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(
                            clientId, transactionId, from)
                    .stream()
                    .filter(result -> expectedModels.contains(result.getModelCode()))
                    .map(this::toResponse)
                    .toList();

            boolean alertFound = latest.stream().anyMatch(result -> "ALERT".equals(result.getStatus()));
            boolean allModelsCompleted = latest.stream()
                    .map(UserDto.AiScreeningResultResponse::getModelCode)
                    .distinct()
                    .count() == expectedModels.size();
            if (alertFound || allModelsCompleted) {
                return latest;
            }

            try {
                Thread.sleep(75);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return latest;
    }

    private UserDto.AiScreeningResultResponse toResponse(AiScreeningResult result) {
        return UserDto.AiScreeningResultResponse.builder()
                .id(result.getId())
                .transactionId(result.getTransactionId())
                .modelCode(result.getModelCode())
                .ticker(result.getTicker())
                .status(result.getStatus())
                .reason(result.getReason())
                .ruleVersion(result.getRuleVersion())
                .createdAt(result.getCreatedAt())
                .build();
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) return number.longValue();
        try {
            return value == null ? null : Long.valueOf(value.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String asText(Object value) {
        if (value == null) return null;
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }
}
