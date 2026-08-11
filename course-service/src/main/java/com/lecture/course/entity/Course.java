package com.lecture.course.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "securities")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String ticker;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sector sector;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal lastPrice;

    @Column(nullable = false, precision = 24, scale = 2)
    private BigDecimal marketCap;

    // 기존 instructorId의 역할을 유지한 거래소/데이터 공급자 식별자
    @Column(nullable = false)
    private Long exchangeId;

    // 기존 enrollmentCount를 심사 횟수로 재사용
    @Column(nullable = false)
    @Builder.Default
    private Integer screeningCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Grade grade = Grade.WATCH;

    @Column(nullable = false, precision = 24, scale = 2)
    @Builder.Default
    private BigDecimal interestBearingDebt = BigDecimal.ZERO;

    @Column(nullable = false, precision = 24, scale = 2)
    @Builder.Default
    private BigDecimal interestEarningAssets = BigDecimal.ZERO;

    @Column(nullable = false, precision = 24, scale = 2)
    @Builder.Default
    private BigDecimal nonPermissibleIncome = BigDecimal.ZERO;

    @Column(nullable = false, precision = 24, scale = 2)
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String screeningReason;

    private LocalDateTime lastScreenedAt;

    @Column(length = 50)
    private String ruleVersionId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum Sector {
        TECHNOLOGY,
        HEALTHCARE,
        ENERGY,
        CONSUMER_GOODS,
        INDUSTRIALS,
        REAL_ESTATE,
        MATERIALS,
        TELECOMMUNICATION,
        UTILITIES,
        ISLAMIC_FINANCE,
        CONVENTIONAL_FINANCE,
        ALCOHOL,
        PORK,
        GAMBLING,
        TOBACCO,
        ADULT_ENTERTAINMENT,
        MEDIA,
        HOTELS,
        DEFENSE,
        OTHER
    }

    public enum Grade {
        COMPLIANT, WATCH, RESTRICTED
    }

    public void increaseEnrollmentCount() {
        this.screeningCount++;
    }

    public void applyScreening(Grade grade, String reason, String ruleVersionId) {
        this.grade = grade;
        this.screeningReason = reason;
        this.ruleVersionId = ruleVersionId;
        this.lastScreenedAt = LocalDateTime.now();
        this.screeningCount++;
    }

    public void flagForReview(String reason) {
        if (this.grade != Grade.RESTRICTED) {
            this.grade = Grade.WATCH;
        }
        this.screeningReason = reason;
    }
}
