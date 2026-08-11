package com.lecture.payment.evaluator;

import com.lecture.payment.dto.PaymentDto;
import com.lecture.payment.entity.Payment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class SectorComplianceEvaluator implements Evaluator {

    private static final Set<String> PROHIBITED = Set.of(
            "CONVENTIONAL_FINANCE", "ALCOHOL", "PORK", "GAMBLING",
            "TOBACCO", "ADULT_ENTERTAINMENT");
    private static final Set<String> MATERIAL_REVIEW = Set.of("MEDIA", "HOTELS", "DEFENSE");

    @Override
    public int order() {
        return 10;
    }

    @Override
    public PaymentDto.EvaluationResult evaluate(
            PaymentDto.SecuritySnapshot security,
            PaymentDto.RuleSnapshot rules) {
        String sector = security.getSector() != null ? security.getSector() : "OTHER";
        if (PROHIBITED.contains(sector)) {
            return new PaymentDto.EvaluationResult(
                    Payment.Grade.RESTRICTED,
                    "금지 업종(" + sector + ")에 해당하여 즉시 부적격 처리했습니다.",
                    true,
                    Map.of());
        }
        if (MATERIAL_REVIEW.contains(sector)) {
            return new PaymentDto.EvaluationResult(
                    Payment.Grade.WATCH,
                    "중요성 개별 심사 업종(" + sector + ")이므로 주의 등급입니다.",
                    false,
                    Map.of());
        }
        return PaymentDto.EvaluationResult.compliant("금지 업종 필터를 통과했습니다.");
    }
}
