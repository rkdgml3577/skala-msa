package com.example.msa.enrollment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

/** course-service 를 동기 REST 로 호출하는 클라이언트 예시. */
@Slf4j
@Component
@RequiredArgsConstructor
public class CourseServiceClient {

    private final WebClient.Builder webClientBuilder;

    /** 상품 상세 조회 (주문 시점 가격 스냅샷 용도) */
    public ItemView getItem(Long itemId) {
        try {
            ItemView item = webClientBuilder.build()
                    .get()
                    .uri("http://course-service/api/courses/internal/{id}", itemId)
                    .retrieve()
                    .bodyToMono(ItemView.class)
                    .block();
            if (item == null || item.getId() == null) {
                throw new IllegalArgumentException("상품 정보를 찾을 수 없습니다: " + itemId);
            }
            return item;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("[CourseServiceClient] 상품 조회 실패 - itemId={}, error={}", itemId, e.getMessage());
            throw new IllegalArgumentException("course-service 연결에 실패했습니다");
        }
    }

    /** course-service 의 내부 응답을 매핑하는 뷰 객체 */
    @lombok.Getter
    @lombok.NoArgsConstructor
    public static class ItemView {
        private Long id;
        private String code;
        private String name;
        private BigDecimal price;
    }
}
