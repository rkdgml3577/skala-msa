package com.lecture.enrollment.service;

import com.lecture.enrollment.entity.Enrollment;
import com.lecture.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentWriteService {

    private final EnrollmentRepository enrollmentRepository;

    /**
     * 반드시 독립 트랜잭션으로 실행
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Enrollment createPendingEnrollment(
            Long clientId,
            String ticker,
            Enrollment.Side side,
            java.math.BigDecimal quantity,
            java.math.BigDecimal orderPrice) {

        Enrollment enrollment = enrollmentRepository.save(
                Enrollment.builder()
                        .clientId(clientId)
                        .ticker(ticker)
                        .side(side)
                        .quantity(quantity)
                        .orderPrice(orderPrice)
                        .build()
        );

        log.info("[EnrollmentWriteService] PENDING 주문 생성 - orderId: {}, clientId: {}, ticker: {}",
                enrollment.getId(), clientId, ticker);

        return enrollment;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Enrollment createSubscription(Long clientId, String ticker) {
        Enrollment enrollment = enrollmentRepository.save(
                Enrollment.builder()
                        .clientId(clientId)
                        .ticker(ticker)
                        // 기존 orders 스키마의 NOT NULL 컬럼을 채우는 호환용 기본값
                        .side(Enrollment.Side.BUY)
                        .quantity(BigDecimal.ONE)
                        .orderPrice(BigDecimal.ONE)
                        .status(Enrollment.Status.ACTIVE)
                        .screeningReason("은행사에서 구독한 이상 탐지 시나리오입니다.")
                        .build()
        );
        log.info("[EnrollmentWriteService] 구독 생성 - subscriptionId: {}, clientId: {}, detector: {}",
                enrollment.getId(), clientId, ticker);
        return enrollment;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Enrollment applyScreeningResult(
            Long orderId,
            Enrollment.Status status,
            String reason,
            java.util.List<String> alternatives) {
        Enrollment enrollment = enrollmentRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
        enrollment.applyScreeningResult(status, reason, alternatives);
        return enrollment;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Enrollment changeSubscriptionStatus(Long subscriptionId, Long clientId, Enrollment.Status status) {
        Enrollment enrollment = enrollmentRepository.findByIdAndClientId(subscriptionId, clientId)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다: " + subscriptionId));
        enrollment.changeSubscriptionStatus(status);
        return enrollment;
    }
}
