package com.example.msa.enrollment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/** user-service 를 동기 REST 로 호출하는 클라이언트 예시. */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    /** 사용자 존재 여부 확인 */
    public boolean existsUser(Long userId) {
        try {
            Boolean exists = webClientBuilder.build()
                    .get()
                    .uri("http://user-service/api/users/internal/exists/{id}", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("[UserServiceClient] 사용자 확인 실패 - userId={}, error={}", userId, e.getMessage());
            throw new IllegalArgumentException("user-service 연결에 실패했습니다");
        }
    }
}
