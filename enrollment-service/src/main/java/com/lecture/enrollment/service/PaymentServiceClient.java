package com.lecture.enrollment.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentServiceClient {

    private final WebClient.Builder webClientBuilder;

    /**
     * 클래스명은 기존 구조 호환을 위해 유지하며, 현재 역할은 advisory-service 대체 종목 조회다.
     */
    public List<String> requestAlternatives(String ticker) {
        try {
            Map<String, Object> result = webClientBuilder.build()
                    .get()
                    .uri("http://recommend-service:8085/api/alternatives/{ticker}", ticker)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
            if (result == null || !(result.get("alternatives") instanceof List<?> alternatives)) {
                return List.of();
            }
            List<String> tickers = new ArrayList<>();
            for (Object item : alternatives) {
                if (item instanceof Map<?, ?> map && map.get("ticker") != null) {
                    tickers.add(map.get("ticker").toString());
                }
            }
            return tickers;
        } catch (Exception e) {
            log.warn("[PaymentServiceClient] 대체 종목 조회 실패 - ticker: {}, error: {}", ticker, e.getMessage());
            return List.of();
        }
    }
}
