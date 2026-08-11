package com.lecture.payment.service;

import com.lecture.payment.dto.PaymentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseServiceClient {

    private final WebClient.Builder webClientBuilder;

    public PaymentDto.SecuritySnapshot getSecurity(String ticker) {
        return get("http://course-service/api/courses/internal/ticker/{ticker}", ticker);
    }

    public PaymentDto.SecuritySnapshot getGrade(String ticker) {
        return get("http://course-service/api/courses/internal/{ticker}/grade", ticker);
    }

    private PaymentDto.SecuritySnapshot get(String uri, String ticker) {
        try {
            PaymentDto.SecuritySnapshot snapshot = webClientBuilder.build()
                    .get()
                    .uri(uri, ticker)
                    .retrieve()
                    .bodyToMono(PaymentDto.SecuritySnapshot.class)
                    .block();
            if (snapshot == null) {
                throw new IllegalStateException("종목 응답이 비어 있습니다.");
            }
            return snapshot;
        } catch (Exception e) {
            log.error("[CourseServiceClient] 종목 조회 실패 - ticker: {}", ticker, e);
            throw new RuntimeException("Security Service 연결 실패", e);
        }
    }
}
