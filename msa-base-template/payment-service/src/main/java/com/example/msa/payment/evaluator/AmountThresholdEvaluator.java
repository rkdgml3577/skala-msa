package com.example.msa.payment.evaluator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** 금액 한도 기반 예시 심사 규칙. 한도를 넘으면 수동 검토 대상(DECLINED)으로 표시한다. */
@Component
public class AmountThresholdEvaluator implements Evaluator {

    private final BigDecimal threshold;

    public AmountThresholdEvaluator(
            @Value("${payment.threshold:1000000}") BigDecimal threshold) {
        this.threshold = threshold;
    }

    @Override
    public Decision evaluate(BigDecimal amount) {
        if (amount == null) {
            return Decision.decline("결제 금액이 없습니다");
        }
        if (amount.compareTo(threshold) > 0) {
            return Decision.decline("금액이 한도(" + threshold + ")를 초과했습니다");
        }
        return Decision.approve("한도 이내 정상 승인");
    }
}
