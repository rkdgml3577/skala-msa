package com.lecture.payment.evaluator;

import com.lecture.payment.dto.PaymentDto;

public interface Evaluator {
    int order();
    PaymentDto.EvaluationResult evaluate(
            PaymentDto.SecuritySnapshot security,
            PaymentDto.RuleSnapshot rules);
}
