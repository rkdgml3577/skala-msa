package com.lecture.payment.service;

import com.lecture.payment.dto.PaymentDto;
import com.lecture.payment.entity.Payment;
import com.lecture.payment.evaluator.Evaluator;
import com.lecture.payment.kafka.PaymentKafkaProducer;
import com.lecture.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentKafkaProducer kafkaProducer;
    private final CourseServiceClient courseServiceClient;
    private final List<Evaluator> evaluators;

    /** Level 1: 종목 재무데이터 변경 시 전체 규칙 심사 */
    @Transactional
    public PaymentDto.PaymentResponse screenSecurity(String ticker) {
        PaymentDto.SecuritySnapshot security = courseServiceClient.getSecurity(ticker);
        return screen(security, Payment.ScreeningType.SECURITY, ticker, null, null);
    }

    /** Level 2: 주문 이벤트는 이미 저장된 종목 등급만 읽어 O(1) 판정 */
    @Transactional
    public PaymentDto.PaymentResponse screenOrder(Long orderId, Long clientId, String ticker) {
        PaymentDto.SecuritySnapshot security = courseServiceClient.getGrade(ticker);
        Payment.Grade grade = Payment.Grade.valueOf(security.getGrade());
        String reason = security.getReason() != null
                ? security.getReason()
                : "저장된 종목 등급을 기반으로 주문을 판정했습니다.";
        return saveAndPublish(
                Payment.ScreeningType.ORDER,
                String.valueOf(orderId),
                orderId,
                clientId,
                ticker,
                grade,
                reason,
                Map.of(),
                security.getRuleVersionId() != null ? security.getRuleVersionId() : rule().version());
    }

    private PaymentDto.PaymentResponse screen(
            PaymentDto.SecuritySnapshot security,
            Payment.ScreeningType type,
            String referenceId,
            Long orderId,
            Long clientId) {

        Payment.Grade finalGrade = Payment.Grade.COMPLIANT;
        List<String> reasons = new ArrayList<>();
        Map<String, BigDecimal> ratios = new LinkedHashMap<>();

        List<Evaluator> ordered = evaluators.stream()
                .sorted(Comparator.comparingInt(Evaluator::order))
                .toList();

        for (Evaluator evaluator : ordered) {
            PaymentDto.EvaluationResult result = evaluator.evaluate(security, rule());
            if (severity(result.grade()) > severity(finalGrade)) {
                finalGrade = result.grade();
            }
            if (result.reason() != null && !result.reason().isBlank()) {
                reasons.add(result.reason());
            }
            ratios.putAll(result.ratios());
            if (result.terminal()) {
                break;
            }
        }

        return saveAndPublish(
                type, referenceId, orderId, clientId, security.getTicker(), finalGrade,
                String.join(" ", reasons), ratios, rule().version());
    }

    private PaymentDto.PaymentResponse saveAndPublish(
            Payment.ScreeningType type,
            String referenceId,
            Long orderId,
            Long clientId,
            String ticker,
            Payment.Grade grade,
            String reason,
            Map<String, BigDecimal> ratios,
            String ruleVersionId) {

        Payment payment = paymentRepository.save(Payment.builder()
                .screeningType(type)
                .referenceId(referenceId)
                .orderId(orderId)
                .clientId(clientId)
                .ticker(ticker.toUpperCase(Locale.ROOT))
                .grade(grade)
                .reason(reason)
                .debtRatio(ratios.get("debtRatio"))
                .interestAssetRatio(ratios.get("interestAssetRatio"))
                .nonPermissibleIncomeRatio(ratios.get("nonPermissibleIncomeRatio"))
                .ruleVersionId(ruleVersionId)
                .build());

        kafkaProducer.publishPaymentCompleted(
                PaymentKafkaProducer.ScreeningCompletedEvent.builder()
                        .screeningId(payment.getId())
                        .screeningType(type.name())
                        .orderId(orderId)
                        .clientId(clientId)
                        .ticker(payment.getTicker())
                        .grade(grade.name())
                        .reason(reason)
                        .ruleVersionId(ruleVersionId)
                        .ratios(ratios)
                        .build());

        log.info("[PaymentService] 심사 완료 - type: {}, ticker: {}, grade: {}", type, ticker, grade);
        return PaymentDto.PaymentResponse.from(payment);
    }

    public PaymentDto.PaymentResponse getPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("심사 정보를 찾을 수 없습니다: " + id));
        return PaymentDto.PaymentResponse.from(payment);
    }

    public List<PaymentDto.PaymentResponse> getPaymentsByTicker(String ticker) {
        return paymentRepository.findByTickerOrderByCreatedAtDesc(ticker.toUpperCase(Locale.ROOT)).stream()
                .map(PaymentDto.PaymentResponse::from)
                .toList();
    }

    public List<PaymentDto.PaymentResponse> getPaymentsByOrder(Long orderId) {
        return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .map(PaymentDto.PaymentResponse::from)
                .toList();
    }

    private PaymentDto.RuleSnapshot rule() {
        return PaymentDto.RuleSnapshot.aaoifiS21();
    }

    private int severity(Payment.Grade grade) {
        return switch (grade) {
            case COMPLIANT -> 0;
            case WATCH -> 1;
            case RESTRICTED -> 2;
        };
    }
}
