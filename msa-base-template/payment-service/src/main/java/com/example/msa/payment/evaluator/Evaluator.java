package com.example.msa.payment.evaluator;

import java.math.BigDecimal;

/**
 * 심사 전략 인터페이스 (Strategy 패턴).
 *
 * <p>새 심사 규칙을 추가하려면 이 인터페이스를 구현한 @Component 를 만들면 된다.
 * 여러 구현체를 List 로 주입받아 순차 평가하도록 확장할 수 있다.
 */
public interface Evaluator {

    Decision evaluate(BigDecimal amount);

    /** 심사 결과 */
    record Decision(boolean approved, String reason) {
        public static Decision approve(String reason) {
            return new Decision(true, reason);
        }

        public static Decision decline(String reason) {
            return new Decision(false, reason);
        }
    }
}
