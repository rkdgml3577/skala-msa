package com.lecture.enrollment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseServiceClient {

    private final WebClient.Builder webClientBuilder;

    /**
     * Course Service: 강의 존재 여부 확인 (동기 REST)
     */
    public boolean existsCourse(Long courseId) {
        try {
            Boolean exists = webClientBuilder.build()
                    .get()
                    .uri("http://course-service/api/courses/internal/exists/{id}", courseId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("[CourseServiceClient] 강의 존재 확인 실패 - courseId: {}, error: {}",
                    courseId, e.getMessage());
            throw new RuntimeException("Course Service 연결 실패");
        }
    }

    /**
     * Course Service: 강의 상세 조회
     * - 내 수강 목록 응답에 course 정보를 붙일 때 사용
     * - course-service 쪽에 GET /api/courses/internal/{id} 엔드포인트가 있어야 함
     */
    public Map<String, Object> getCourse(Long courseId) {
        try {
            Map<String, Object> responseBody = webClientBuilder.build()
                    .get()
                    .uri("http://course-service/api/courses/internal/{id}", courseId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (responseBody == null) {
                throw new RuntimeException("Course Service 응답 본문이 비어 있습니다.");
            }

            log.info("[CourseServiceClient] 강의 상세 조회 성공 - courseId: {}", courseId);
            log.debug("[CourseServiceClient] 강의 상세 응답 - courseId: {}, body: {}", courseId, responseBody);

            /*
             * 응답 형태가 다음 둘 중 하나일 수 있으므로 둘 다 처리
             *
             * 1) 래퍼 응답
             * {
             *   "success": true,
             *   "message": "성공",
             *   "data": { ...course fields... }
             * }
             *
             * 2) 바로 강의 객체 반환
             * {
             *   "id": 1,
             *   "title": "...",
             *   ...
             * }
             */
            Object data = responseBody.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                @SuppressWarnings("unchecked")
                Map<String, Object> courseMap = (Map<String, Object>) dataMap;
                return courseMap;
            }

            return responseBody;
        } catch (Exception e) {
            log.error("[CourseServiceClient] 강의 상세 조회 실패 - courseId: {}, error: {}",
                    courseId, e.getMessage());
            throw new RuntimeException("Course Service 강의 상세 조회 실패");
        }
    }

    /** 종목 상세 조회 (ticker 기반) */
    public Map<String, Object> getSecurity(String ticker) {
        return getMap("http://course-service/api/courses/internal/ticker/{ticker}", ticker);
    }

    /** 이미 계산된 샤리아 종목 등급 조회 - 실시간 주문 경로는 계산을 수행하지 않는다. */
    public GradeResult getGrade(String ticker) {
        try {
            GradeResult result = webClientBuilder.build()
                    .get()
                    .uri("http://course-service/api/courses/internal/{ticker}/grade", ticker)
                    .retrieve()
                    .bodyToMono(GradeResult.class)
                    .block();
            if (result == null || result.getGrade() == null) {
                throw new RuntimeException("종목 등급 응답이 비어 있습니다.");
            }
            return result;
        } catch (Exception e) {
            log.error("[CourseServiceClient] 종목 등급 조회 실패 - ticker: {}", ticker, e);
            throw new RuntimeException("Security Service 등급 조회 실패");
        }
    }

    private Map<String, Object> getMap(String uri, Object value) {
        try {
            Map<String, Object> body = webClientBuilder.build()
                    .get()
                    .uri(uri, value)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
            if (body == null) {
                throw new RuntimeException("Security Service 응답이 비어 있습니다.");
            }
            return body;
        } catch (Exception e) {
            log.error("[CourseServiceClient] 종목 조회 실패 - value: {}", value, e);
            throw new RuntimeException("Security Service 연결 실패");
        }
    }

    @lombok.Getter
    @lombok.NoArgsConstructor
    public static class GradeResult {
        private String ticker;
        private String grade;
        private String reason;
        private java.time.LocalDateTime lastScreenedAt;
        private String ruleVersionId;
    }

    /**
     * Course Service: 수강생 수 증가 (수강 활성화 시 호출)
     */
    public void increaseEnrollmentCount(Long courseId) {
        try {
            webClientBuilder.build()
                    .post()
                    .uri("http://course-service/api/courses/internal/{id}/enrollment-count", courseId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("[CourseServiceClient] 수강생 수 증가 완료 - courseId: {}", courseId);
        } catch (Exception e) {
            log.error("[CourseServiceClient] 수강생 수 증가 실패 - courseId: {}, error: {}",
                    courseId, e.getMessage());
        }
    }
}
