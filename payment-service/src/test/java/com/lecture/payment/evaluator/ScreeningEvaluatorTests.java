package com.lecture.payment.evaluator;

import com.lecture.payment.dto.PaymentDto;
import com.lecture.payment.entity.Payment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ScreeningEvaluatorTests {

    private final PaymentDto.RuleSnapshot rules = PaymentDto.RuleSnapshot.aaoifiS21();

    @Test
    void prohibitedSectorShortCircuitsAsRestricted() {
        PaymentDto.SecuritySnapshot security = snapshot("CONVENTIONAL_FINANCE", "0", "0", "0");

        PaymentDto.EvaluationResult result = new SectorComplianceEvaluator().evaluate(security, rules);

        assertThat(result.grade()).isEqualTo(Payment.Grade.RESTRICTED);
        assertThat(result.terminal()).isTrue();
        assertThat(result.reason()).contains("금지 업종");
    }

    @Test
    void ratiosBelowAaoifiLimitsAreCompliant() {
        PaymentDto.SecuritySnapshot security = snapshot("TECHNOLOGY", "20", "10", "2");

        PaymentDto.EvaluationResult result = new FinancialRatioEvaluator().evaluate(security, rules);

        assertThat(result.grade()).isEqualTo(Payment.Grade.COMPLIANT);
        assertThat(result.ratios().get("debtRatio")).isEqualByComparingTo("0.20000000");
    }

    @Test
    void debtAtThirtyPercentIsRestricted() {
        PaymentDto.SecuritySnapshot security = snapshot("TECHNOLOGY", "30", "10", "2");

        PaymentDto.EvaluationResult result = new FinancialRatioEvaluator().evaluate(security, rules);

        assertThat(result.grade()).isEqualTo(Payment.Grade.RESTRICTED);
        assertThat(result.reason()).contains("30.00%");
    }

    @Test
    void ratioNearLimitIsWatch() {
        PaymentDto.SecuritySnapshot security = snapshot("TECHNOLOGY", "28", "10", "2");

        PaymentDto.EvaluationResult result = new FinancialRatioEvaluator().evaluate(security, rules);

        assertThat(result.grade()).isEqualTo(Payment.Grade.WATCH);
    }

    private PaymentDto.SecuritySnapshot snapshot(
            String sector,
            String debt,
            String assets,
            String nonPermissibleIncome) {
        return PaymentDto.SecuritySnapshot.builder()
                .ticker("TEST")
                .name("Test Security")
                .sector(sector)
                .marketCap(new BigDecimal("100"))
                .interestBearingDebt(new BigDecimal(debt))
                .interestEarningAssets(new BigDecimal(assets))
                .nonPermissibleIncome(new BigDecimal(nonPermissibleIncome))
                .totalRevenue(new BigDecimal("100"))
                .build();
    }
}
