package com.lecture.course.kafka;

import com.lecture.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseKafkaConsumer {

    private final CourseService courseService;

    @KafkaListener(topics = "${kafka.topic.screening-completed}", groupId = "course-service")
    public void handleScreeningCompleted(Map<String, Object> event) {
        try {
            if ("ORDER".equals(string(event.get("screeningType")))) {
                return;
            }
            String ticker = string(event.get("ticker"));
            String grade = string(event.get("grade"));
            if (ticker == null || grade == null) {
                return;
            }
            courseService.applyScreeningResult(
                    ticker, grade, string(event.get("reason")), string(event.get("ruleVersionId")));
            log.info("[Kafka Consumer] 종목 등급 반영 - ticker: {}, grade: {}", ticker, grade);
        } catch (Exception e) {
            log.error("[Kafka Consumer] screening.completed 처리 실패 - event: {}", event, e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.anomaly-detected}", groupId = "course-service-anomaly")
    public void handleAnomalyDetected(Map<String, Object> event) {
        try {
            String ticker = string(event.get("ticker"));
            if (ticker != null) {
                courseService.flagForReview(ticker, "이상 징후 감지: " + string(event.get("reason")));
            }
        } catch (Exception e) {
            log.error("[Kafka Consumer] anomaly.detected 처리 실패 - event: {}", event, e);
        }
    }

    private String string(Object value) {
        return value != null ? value.toString() : null;
    }
}
