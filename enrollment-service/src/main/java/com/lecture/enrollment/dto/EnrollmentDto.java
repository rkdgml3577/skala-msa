package com.lecture.enrollment.dto;

import com.lecture.enrollment.entity.Enrollment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;
import java.util.Arrays;

public class EnrollmentDto {

    // 매매 주문 요청 (기존 EnrollRequest 구조 재사용)
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EnrollRequest {
        private Long clientId;

        @NotBlank(message = "티커는 필수입니다")
        private String ticker;

        @NotNull(message = "매매 구분은 필수입니다")
        private Enrollment.Side side;

        @NotNull(message = "수량은 필수입니다")
        @Positive(message = "수량은 양수여야 합니다")
        private BigDecimal quantity;

        @NotNull(message = "주문 가격은 필수입니다")
        @Positive(message = "주문 가격은 양수여야 합니다")
        private BigDecimal orderPrice;
    }

    /**
     * 화면과 외부 API에서 사용하는 이상 탐지 시나리오 구독 요청.
     * 내부적으로는 기존 Enrollment 엔티티를 재사용해 스키마 변경을 최소화한다.
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubscriptionRequest {
        private Long clientId;

        @NotBlank(message = "탐지 시나리오 코드는 필수입니다")
        private String ticker;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubscriptionStatusRequest {
        @NotNull(message = "구독 상태는 필수입니다")
        private Enrollment.Status status;
    }

    // 종목 요약 정보 (주문 목록 표시용)
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CourseSummary {
        private String ticker;
        private String name;
        private String description;
        private String sector;
        private BigDecimal lastPrice;
        private BigDecimal marketCap;
        private String grade;
    }

    // 주문 심사 응답
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EnrollmentResponse {
        private Long id;
        private Long clientId;
        private String ticker;
        private Enrollment.Side side;
        private BigDecimal quantity;
        private BigDecimal orderPrice;
        private Enrollment.Status status;
        private String screeningReason;
        private List<String> alternativeTickers;
        private LocalDateTime createdAt;

        // 추가
        private CourseSummary course;

        public static EnrollmentResponse from(Enrollment enrollment) {
            return EnrollmentResponse.builder()
                    .id(enrollment.getId())
                    .clientId(enrollment.getClientId())
                    .ticker(enrollment.getTicker())
                    .side(enrollment.getSide())
                    .quantity(enrollment.getQuantity())
                    .orderPrice(enrollment.getOrderPrice())
                    .status(enrollment.getStatus())
                    .screeningReason(enrollment.getScreeningReason())
                    .alternativeTickers(parseAlternatives(enrollment.getAlternativeTickers()))
                    .createdAt(enrollment.getCreatedAt())
                    .build();
        }

        public static EnrollmentResponse from(Enrollment enrollment, CourseSummary course) {
            return EnrollmentResponse.builder()
                    .id(enrollment.getId())
                    .clientId(enrollment.getClientId())
                    .ticker(enrollment.getTicker())
                    .side(enrollment.getSide())
                    .quantity(enrollment.getQuantity())
                    .orderPrice(enrollment.getOrderPrice())
                    .status(enrollment.getStatus())
                    .screeningReason(enrollment.getScreeningReason())
                    .alternativeTickers(parseAlternatives(enrollment.getAlternativeTickers()))
                    .createdAt(enrollment.getCreatedAt())
                    .course(course)
                    .build();
        }

        private static List<String> parseAlternatives(String value) {
            if (value == null || value.isBlank()) {
                return List.of();
            }
            return Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(item -> !item.isBlank())
                    .toList();
        }
    }

    // advisory-service용 주문 이력 응답
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EnrollmentHistoryResponse {
        private Long clientId;
        private List<String> approvedTickers;
        private List<String> heldTickers;
    }

    // 공통 API 응답 래퍼
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public static <T> ApiResponse<T> success(T data) {
            return ApiResponse.<T>builder()
                    .success(true)
                    .message("성공")
                    .data(data)
                    .build();
        }

        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder()
                    .success(false)
                    .message(message)
                    .build();
        }
    }
}
