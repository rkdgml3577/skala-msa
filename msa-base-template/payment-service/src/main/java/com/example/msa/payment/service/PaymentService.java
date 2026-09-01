package com.example.msa.payment.service;

import com.example.msa.payment.entity.Payment;
import com.example.msa.payment.evaluator.Evaluator;
import com.example.msa.payment.kafka.PaymentCompletedEvent;
import com.example.msa.payment.kafka.PaymentEventProducer;
import com.example.msa.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final Evaluator evaluator;
    private final PaymentEventProducer paymentEventProducer;

    /** order.created 를 받아 심사→저장→payment.completed 발행. */
    @Transactional
    public Payment processOrder(Long orderId, Long userId, Long itemId, BigDecimal amount) {
        Evaluator.Decision decision = evaluator.evaluate(amount);

        Payment payment = paymentRepository.save(Payment.builder()
                .orderId(orderId)
                .userId(userId)
                .itemId(itemId)
                .amount(amount)
                .status(decision.approved() ? Payment.Status.APPROVED : Payment.Status.DECLINED)
                .reason(decision.reason())
                .build());

        paymentEventProducer.publishPaymentCompleted(PaymentCompletedEvent.builder()
                .paymentId(payment.getId())
                .orderId(orderId)
                .userId(userId)
                .itemId(itemId)
                .amount(amount)
                .status(payment.getStatus().name())
                .reason(payment.getReason())
                .build());

        return payment;
    }

    @Transactional(readOnly = true)
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Payment findByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("결제 내역을 찾을 수 없습니다: " + orderId));
    }
}
