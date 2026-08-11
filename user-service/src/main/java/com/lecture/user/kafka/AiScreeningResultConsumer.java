package com.lecture.user.kafka;

import com.lecture.user.service.AiScreeningAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiScreeningResultConsumer {

    private final AiScreeningAuditService auditService;

    @KafkaListener(topics = "${kafka.topic.ai-screening-completed}")
    public void consume(Map<String, Object> event) {
        try {
            auditService.record(event);
        } catch (Exception error) {
            log.error("[AI Audit] ai.screening.completed 처리 실패: {}", event, error);
        }
    }
}
