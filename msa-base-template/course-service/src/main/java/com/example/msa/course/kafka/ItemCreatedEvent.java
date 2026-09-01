package com.example.msa.course.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCreatedEvent {
    private Long itemId;
    private String code;
    private String name;
    private BigDecimal price;
}
