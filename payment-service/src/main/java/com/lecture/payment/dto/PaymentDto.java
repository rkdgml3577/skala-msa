package com.lecture.payment.dto;

import com.lecture.payment.entity.Payment;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class PaymentDto {

    // security-service가 보유한 심사 입력 스냅샷
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SecuritySnapshot {
        private String ticker;
        private String name;
        private String description;
        private String sector;
        private BigDecimal marketCap;
        private BigDecimal interestBearingDebt;
        private BigDecimal interestEarningAssets;
        private BigDecimal nonPermissibleIncome;
        private BigDecimal totalRevenue;
        private String grade;
        private String reason;
        private String screeningReason;
        private String ruleVersionId;
    }

    // 심사 결과 응답
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentResponse {
        private Long screeningId;
        private Payment.ScreeningType screeningType;
        private String referenceId;
        private Long orderId;
        private Long clientId;
        private String ticker;
        private Payment.Grade grade;
        private String reason;
        private BigDecimal debtRatio;
        private BigDecimal interestAssetRatio;
        private BigDecimal nonPermissibleIncomeRatio;
        private String ruleVersionId;
        private LocalDateTime createdAt;

        public static PaymentResponse from(Payment payment) {
            return PaymentResponse.builder()
                    .screeningId(payment.getId())
                    .screeningType(payment.getScreeningType())
                    .referenceId(payment.getReferenceId())
                    .orderId(payment.getOrderId())
                    .clientId(payment.getClientId())
                    .ticker(payment.getTicker())
                    .grade(payment.getGrade())
                    .reason(payment.getReason())
                    .debtRatio(payment.getDebtRatio())
                    .interestAssetRatio(payment.getInterestAssetRatio())
                    .nonPermissibleIncomeRatio(payment.getNonPermissibleIncomeRatio())
                    .ruleVersionId(payment.getRuleVersionId())
                    .createdAt(payment.getCreatedAt())
                    .build();
        }
    }

    public record EvaluationResult(
            Payment.Grade grade,
            String reason,
            boolean terminal,
            Map<String, BigDecimal> ratios
    ) {
        public static EvaluationResult compliant(String reason) {
            return new EvaluationResult(Payment.Grade.COMPLIANT, reason, false, Map.of());
        }
    }

    public record RuleSnapshot(
            BigDecimal debtThreshold,
            BigDecimal interestAssetThreshold,
            BigDecimal nonPermissibleIncomeThreshold,
            BigDecimal watchBoundaryFactor,
            String version
    ) {
        public static RuleSnapshot aaoifiS21() {
            return new RuleSnapshot(
                    new BigDecimal("0.30"),
                    new BigDecimal("0.30"),
                    new BigDecimal("0.05"),
                    new BigDecimal("0.90"),
                    "AAOIFI-SS21-2026.1");
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
}
