package com.example.msa.course.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 예시 카탈로그 엔티티(상품/강의/종목 등). 새 프로젝트에서 실제 도메인으로 교체한다. */
@Entity
@Table(name = "items")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private int soldCount = 0;

    public void update(String name, String description, BigDecimal price) {
        if (name != null && !name.isBlank()) this.name = name;
        if (description != null) this.description = description;
        if (price != null) this.price = price;
    }

    public void increaseSoldCount() {
        this.soldCount += 1;
    }
}
