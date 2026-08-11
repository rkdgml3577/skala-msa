package com.lecture.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "screenings")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "screening_type", nullable = false, length = 20)
    private ScreeningType screeningType;

    @Column(name = "reference_id", nullable = false, length = 100)
    private String referenceId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "client_id")
    private Long clientId;

    @Column(nullable = false, length = 20)
    private String ticker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Grade grade;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "debt_ratio", precision = 12, scale = 8)
    private BigDecimal debtRatio;

    @Column(name = "interest_asset_ratio", precision = 12, scale = 8)
    private BigDecimal interestAssetRatio;

    @Column(name = "non_permissible_income_ratio", precision = 12, scale = 8)
    private BigDecimal nonPermissibleIncomeRatio;

    @Column(name = "rule_version_id", nullable = false, length = 50)
    private String ruleVersionId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum ScreeningType {
        SECURITY, ORDER
    }

    public enum Grade {
        COMPLIANT, WATCH, RESTRICTED
    }
}
