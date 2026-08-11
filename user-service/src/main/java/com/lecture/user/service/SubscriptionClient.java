package com.lecture.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/** 기존 구독 조회 API를 내부 호출해 고객사의 ACTIVE 모델 코드만 가져온다. */
@Component
@RequiredArgsConstructor
public class SubscriptionClient {

    @Value("${service.enrollment-service.url:http://enrollment-service:8083}")
    private String enrollmentServiceUrl;

    public List<String> getActiveModelCodes(Long clientId) {
        try {
            JsonNode response = RestClient.create(enrollmentServiceUrl)
                    .get()
                    .uri("/api/enrollments/subscriptions/my")
                    .header("X-User-Id", String.valueOf(clientId))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.path("success").asBoolean()) {
                throw new IllegalStateException("구독 정보를 확인할 수 없습니다");
            }
            List<String> modelCodes = new java.util.ArrayList<>();
            for (JsonNode subscription : response.path("data")) {
                if ("ACTIVE".equals(subscription.path("status").asText())) {
                    String modelCode = subscription.path("ticker").asText();
                    if (!modelCode.isBlank()) {
                        modelCodes.add(modelCode);
                    }
                }
            }
            return modelCodes.stream()
                    .distinct()
                    .toList();
        } catch (Exception e) {
            throw new IllegalStateException("구독 정보를 확인할 수 없습니다", e);
        }
    }
}
