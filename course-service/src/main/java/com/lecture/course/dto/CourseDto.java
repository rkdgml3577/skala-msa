package com.lecture.course.dto;

import com.lecture.course.entity.Course;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CourseDto {

    // 종목 마스터 등록 요청 (기존 CreateRequest 구조 재사용)
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {

        @NotBlank(message = "티커는 필수입니다")
        @Size(max = 20, message = "티커는 20자 이하여야 합니다")
        private String ticker;

        @NotBlank(message = "종목명은 필수입니다")
        private String name;

        private String description;

        @NotNull(message = "업종은 필수입니다")
        private Course.Sector sector;

        @NotNull(message = "현재가는 필수입니다")
        @PositiveOrZero(message = "현재가는 0 이상이어야 합니다")
        private BigDecimal lastPrice;

        @NotNull(message = "시가총액은 필수입니다")
        @Positive(message = "시가총액은 양수여야 합니다")
        private BigDecimal marketCap;

        private Long exchangeId;
        private BigDecimal interestBearingDebt;
        private BigDecimal interestEarningAssets;
        private BigDecimal nonPermissibleIncome;
        private BigDecimal totalRevenue;
    }

    // 종목 응답
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CourseResponse {
        private Long id;
        private String ticker;
        private String name;
        private String description;
        private Course.Sector sector;
        private BigDecimal lastPrice;
        private BigDecimal marketCap;
        private Long exchangeId;
        private Integer screeningCount;
        private Course.Grade grade;
        private BigDecimal interestBearingDebt;
        private BigDecimal interestEarningAssets;
        private BigDecimal nonPermissibleIncome;
        private BigDecimal totalRevenue;
        private String screeningReason;
        private LocalDateTime lastScreenedAt;
        private String ruleVersionId;
        private LocalDateTime createdAt;

        public static CourseResponse from(Course course) {
            return CourseResponse.builder()
                    .id(course.getId())
                    .ticker(course.getTicker())
                    .name(course.getName())
                    .description(course.getDescription())
                    .sector(course.getSector())
                    .lastPrice(course.getLastPrice())
                    .marketCap(course.getMarketCap())
                    .exchangeId(course.getExchangeId())
                    .screeningCount(course.getScreeningCount())
                    .grade(course.getGrade())
                    .interestBearingDebt(course.getInterestBearingDebt())
                    .interestEarningAssets(course.getInterestEarningAssets())
                    .nonPermissibleIncome(course.getNonPermissibleIncome())
                    .totalRevenue(course.getTotalRevenue())
                    .screeningReason(course.getScreeningReason())
                    .lastScreenedAt(course.getLastScreenedAt())
                    .ruleVersionId(course.getRuleVersionId())
                    .createdAt(course.getCreatedAt())
                    .build();
        }
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

    // 내부 실시간 주문 심사용 O(1) 등급 응답
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GradeResponse {
        private String ticker;
        private Course.Grade grade;
        private String reason;
        private LocalDateTime lastScreenedAt;
        private String ruleVersionId;

        public static GradeResponse from(Course course) {
            return GradeResponse.builder()
                    .ticker(course.getTicker())
                    .grade(course.getGrade())
                    .reason(course.getScreeningReason())
                    .lastScreenedAt(course.getLastScreenedAt())
                    .ruleVersionId(course.getRuleVersionId())
                    .build();
        }
    }

    // 추천 서비스용 응답 (동일 업종의 적합 종목 목록)
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendResponse {
        private List<CourseResponse> securities;
        private Course.Sector sector;
    }
}
