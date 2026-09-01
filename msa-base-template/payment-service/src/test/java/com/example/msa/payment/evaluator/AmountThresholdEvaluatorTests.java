package com.example.msa.payment.evaluator;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AmountThresholdEvaluatorTests {

    private final Evaluator evaluator = new AmountThresholdEvaluator(new BigDecimal("1000000"));

    @Test
    void approvesWithinThreshold() {
        assertTrue(evaluator.evaluate(new BigDecimal("500000")).approved());
    }

    @Test
    void declinesOverThreshold() {
        assertFalse(evaluator.evaluate(new BigDecimal("2000000")).approved());
    }

    @Test
    void declinesNullAmount() {
        assertFalse(evaluator.evaluate(null).approved());
    }
}
