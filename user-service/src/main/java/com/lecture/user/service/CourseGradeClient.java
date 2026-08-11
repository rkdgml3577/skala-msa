package com.lecture.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;

/**
 * 기존 종목 마스터가 이미 판정해 둔 등급을 조회한다.
 * 신규 종목(404)은 과거 제한 이력이 없는 것으로 보고 AI 모듈로 전달한다.
 */
@Component
public class CourseGradeClient {

    @Value("${service.course-service.url:http://course-service:8082}")
    private String courseServiceUrl;

    public Optional<GradeResult> findExistingGrade(String ticker) {
        try {
            JsonNode response = RestClient.create(courseServiceUrl)
                    .get()
                    .uri("/api/courses/internal/{ticker}/grade", ticker)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || response.path("grade").isMissingNode()) {
                throw new IllegalStateException("기존 종목 등급 응답이 올바르지 않습니다");
            }
            return Optional.of(new GradeResult(
                    response.path("grade").asText(),
                    response.path("reason").asText("기존 종목 심사에서 제한되었습니다."),
                    response.path("ruleVersionId").asText(null)));
        } catch (RestClientResponseException e) {
            // 기존 course-service는 없는 종목을 404 대신 400으로 반환한다.
            // 이는 "과거 제한 이력 없음"이므로 신규 거래를 AI 판정으로 넘긴다.
            if (e.getStatusCode().value() == 404
                    || (e.getStatusCode().value() == 400
                    && e.getResponseBodyAsString().contains("종목을 찾을 수 없습니다"))) {
                return Optional.empty();
            }
            throw new IllegalStateException("기존 종목 제한 정보를 확인할 수 없습니다", e);
        }
    }

    public record GradeResult(String grade, String reason, String ruleVersionId) {
        public boolean isRestricted() {
            return "RESTRICTED".equalsIgnoreCase(grade);
        }
    }
}
