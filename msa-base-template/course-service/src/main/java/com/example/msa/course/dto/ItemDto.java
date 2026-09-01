package com.example.msa.course.dto;

import com.example.msa.course.entity.Item;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ItemDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "code 는 필수입니다")
        @Size(max = 50)
        private String code;

        @NotBlank(message = "name 은 필수입니다")
        @Size(max = 200)
        private String name;

        @Size(max = 1000)
        private String description;

        @NotNull(message = "price 는 필수입니다")
        @DecimalMin(value = "0.0", message = "price 는 0 이상이어야 합니다")
        private BigDecimal price;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        @Size(max = 200)
        private String name;

        @Size(max = 1000)
        private String description;

        @DecimalMin(value = "0.0", message = "price 는 0 이상이어야 합니다")
        private BigDecimal price;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String code;
        private String name;
        private String description;
        private BigDecimal price;
        private int soldCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(Item item) {
            return Response.builder()
                    .id(item.getId())
                    .code(item.getCode())
                    .name(item.getName())
                    .description(item.getDescription())
                    .price(item.getPrice())
                    .soldCount(item.getSoldCount())
                    .createdAt(item.getCreatedAt())
                    .updatedAt(item.getUpdatedAt())
                    .build();
        }
    }
}
