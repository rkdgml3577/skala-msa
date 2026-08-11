package com.lecture.enrollment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(nullable = false, length = 20)
    private String ticker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Side side;

    @Column(nullable = false, precision = 19, scale = 4)
    private java.math.BigDecimal quantity;

    @Column(name = "order_price", nullable = false, precision = 19, scale = 4)
    private java.math.BigDecimal orderPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(name = "screening_reason", columnDefinition = "TEXT")
    private String screeningReason;

    @Column(name = "alternative_tickers", columnDefinition = "TEXT")
    private String alternativeTickers;

    public enum Side {
        BUY, SELL
    }

    public enum Status {
        PENDING,
        APPROVED,
        WARNED,
        HELD,
        ACTIVE,
        PAUSED,
        CANCELLED
    }

    public void activate() {
        this.status = Status.ACTIVE;
    }

    public void pause() {
        this.status = Status.PAUSED;
    }

    public void cancel() {
        this.status = Status.CANCELLED;
    }

    public void applyScreeningResult(Status status, String reason, java.util.List<String> alternatives) {
        this.status = status;
        this.screeningReason = reason;
        if (alternatives != null) {
            this.alternativeTickers = String.join(",", alternatives);
        }
    }

    public void changeSubscriptionStatus(Status status) {
        if (status != Status.ACTIVE && status != Status.PAUSED && status != Status.CANCELLED) {
            throw new IllegalArgumentException("구독 상태는 ACTIVE, PAUSED, CANCELLED만 사용할 수 있습니다.");
        }
        this.status = status;
    }
}
