package com.lecture.payment.evaluator;

import com.lecture.payment.dto.PaymentDto;
import com.lecture.payment.entity.Payment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class FinancialRatioEvaluator implements Evaluator {

    @Override
    public int order() {
        return 20;
    }

    @Override
    public PaymentDto.EvaluationResult evaluate(
            PaymentDto.SecuritySnapshot security,
            PaymentDto.RuleSnapshot rules) {
        if (security.getMarketCap() == null || security.getMarketCap().signum() <= 0
                || security.getTotalRevenue() == null || security.getTotalRevenue().signum() <= 0) {
            return new PaymentDto.EvaluationResult(
                    Payment.Grade.WATCH,
                    "시가총액 또는 총수익 데이터가 없어 사람의 확인이 필요합니다.",
                    false,
                    Map.of());
        }

        BigDecimal debtRatio = ratio(security.getInterestBearingDebt(), security.getMarketCap());
        BigDecimal assetRatio = ratio(security.getInterestEarningAssets(), security.getMarketCap());
        BigDecimal incomeRatio = ratio(security.getNonPermissibleIncome(), security.getTotalRevenue());

        Map<String, BigDecimal> ratios = new LinkedHashMap<>();
        ratios.put("debtRatio", debtRatio);
        ratios.put("interestAssetRatio", assetRatio);
        ratios.put("nonPermissibleIncomeRatio", incomeRatio);

        if (atOrAbove(debtRatio, rules.debtThreshold())
                || atOrAbove(assetRatio, rules.interestAssetThreshold())
                || atOrAbove(incomeRatio, rules.nonPermissibleIncomeThreshold())) {
            return new PaymentDto.EvaluationResult(
                    Payment.Grade.RESTRICTED,
                    "AAOIFI 한도 초과: 이자부부채 " + percent(debtRatio)
                            + ", 이자수익자산 " + percent(assetRatio)
                            + ", 비적격수익 " + percent(incomeRatio) + ".",
                    false,
                    ratios);
        }

        if (near(debtRatio, rules.debtThreshold(), rules.watchBoundaryFactor())
                || near(assetRatio, rules.interestAssetThreshold(), rules.watchBoundaryFactor())
                || near(incomeRatio, rules.nonPermissibleIncomeThreshold(), rules.watchBoundaryFactor())) {
            return new PaymentDto.EvaluationResult(
                    Payment.Grade.WATCH,
                    "AAOIFI 경계값의 90% 이상에 근접하여 모니터링이 필요합니다.",
                    false,
                    ratios);
        }

        return new PaymentDto.EvaluationResult(
                Payment.Grade.COMPLIANT,
                "AAOIFI 재무비율 3종을 모두 통과했습니다. 배당 정화비율은 " + percent(incomeRatio) + "입니다.",
                false,
                ratios);
    }

    private BigDecimal ratio(BigDecimal numerator, BigDecimal denominator) {
        BigDecimal value = numerator != null ? numerator : BigDecimal.ZERO;
        return value.divide(denominator, 8, RoundingMode.HALF_UP);
    }

    private boolean atOrAbove(BigDecimal value, BigDecimal threshold) {
        return value.compareTo(threshold) >= 0;
    }

    private boolean near(BigDecimal value, BigDecimal threshold, BigDecimal factor) {
        return value.compareTo(threshold.multiply(factor)) >= 0;
    }

    private String percent(BigDecimal value) {
        return value.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP) + "%";
    }
}
