package com.lecture.user.dto;

import com.lecture.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

public class UserDto {

    // 회원가입 요청
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterRequest {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
        private String password;

        @NotBlank(message = "이름은 필수입니다")
        private String name;

        @NotBlank(message = "금융회사명은 필수입니다")
        private String organizationName;

        private User.Role role; // STUDENT or INSTRUCTOR
    }

    // 사용자 정보 응답
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserResponse {
        private Long id;
        private String email;
        private String name;
        private User.Role role;
        private String organizationName;
        private String clientCode;
        private Boolean active;
        private String apiKey;
        private LocalDateTime createdAt;

        public static UserResponse from(User user) {
            return UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole())
                    .organizationName(user.getOrganizationName())
                    .clientCode(user.getClientCode())
                    .active(user.getActive())
                    .createdAt(user.getCreatedAt())
                    .build();
        }

        public static UserResponse from(User user, String issuedApiKey) {
            UserResponse response = from(user);
            response.apiKey = issuedApiKey;
            return response;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApiKeyResponse {
        private Long clientId;
        private String clientCode;
        private String apiKey;
        private String notice;
    }

    /** 금융회사 거래 API 수신 형식. 고객사 ID는 API 키로 식별한다. */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransactionRequest {
        @NotBlank(message = "거래 ID는 필수입니다")
        @Size(max = 100, message = "거래 ID는 100자 이하여야 합니다")
        private String transactionId;

        @NotBlank(message = "종목 코드는 필수입니다")
        @Size(max = 30, message = "종목 코드는 30자 이하여야 합니다")
        private String ticker;

        @NotBlank(message = "매매 구분은 필수입니다")
        private String side;

        @jakarta.validation.constraints.DecimalMin(value = "0.0001", message = "수량은 0보다 커야 합니다")
        private BigDecimal quantity;

        @jakarta.validation.constraints.DecimalMin(value = "0.0", message = "가격은 0 이상이어야 합니다")
        private BigDecimal price;

        private LocalDateTime occurredAt;
        private String sector;
        private String merchantCategory;

        @jakarta.validation.constraints.DecimalMin(value = "0.0", message = "부채비율은 0 이상이어야 합니다")
        private BigDecimal interestBearingDebtRatio;

        @jakarta.validation.constraints.DecimalMin(value = "0.0", message = "비허용 수익 비율은 0 이상이어야 합니다")
        private BigDecimal nonPermissibleIncomeRatio;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransactionAcceptedResponse {
        private String transactionId;
        private Long clientId;
        private String clientCode;
        private List<String> dispatchedModels;
        private boolean blocked;
        private String blockReason;
        private String ruleVersion;
        private String decision;
        private boolean processingCompleted;
        private List<AiScreeningResultResponse> results;
        private String message;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AiScreeningResultResponse {
        private Long id;
        private String transactionId;
        private String modelCode;
        private String ticker;
        private String status;
        private String reason;
        private String ruleVersion;
        private LocalDateTime createdAt;
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
