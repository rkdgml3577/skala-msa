"""이벤트 판정 로직. 지금은 단순 규칙이며, 이후 ML 모델 추론으로 교체할 수 있다."""
from decimal import Decimal

# 예시 임계값: 금액이 크면 REVIEW, 그 외 CLEAR
REVIEW_THRESHOLD = Decimal("1000000")


def evaluate(event: dict) -> dict:
    order_id = event.get("orderId")
    amount = _decimal(event.get("amount"))

    if amount is None:
        decision, reason = "REVIEW", "금액 정보가 없어 수동 확인이 필요합니다"
    elif amount > REVIEW_THRESHOLD:
        decision, reason = "REVIEW", f"금액이 임계값({REVIEW_THRESHOLD})을 초과했습니다"
    else:
        decision, reason = "CLEAR", "임계값 이내 정상"

    return {
        "orderId": order_id,
        "userId": event.get("userId"),
        "itemId": event.get("itemId"),
        "decision": decision,
        "reason": reason,
        "ruleVersion": "RULES-v1",
    }


def _decimal(value):
    if value is None:
        return None
    return Decimal(str(value))
