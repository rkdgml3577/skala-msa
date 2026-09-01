package com.example.msa.enrollment.dto;

import com.example.msa.enrollment.entity.Order;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotNull(message = "userId 는 필수입니다")
        private Long userId;

        @NotNull(message = "itemId 는 필수입니다")
        private Long itemId;

        @NotNull(message = "quantity 는 필수입니다")
        @Min(value = 1, message = "quantity 는 1 이상이어야 합니다")
        private Integer quantity;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long userId;
        private Long itemId;
        private int quantity;
        private BigDecimal unitPrice;
        private String status;
        private LocalDateTime createdAt;

        public static Response from(Order order) {
            return Response.builder()
                    .id(order.getId())
                    .userId(order.getUserId())
                    .itemId(order.getItemId())
                    .quantity(order.getQuantity())
                    .unitPrice(order.getUnitPrice())
                    .status(order.getStatus().name())
                    .createdAt(order.getCreatedAt())
                    .build();
        }
    }
}
