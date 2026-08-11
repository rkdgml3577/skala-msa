"""향후 ML 모델로 교체할 수 있는 3개 탐지 모델의 임시 규칙 엔진."""

from decimal import Decimal

PROHIBITED_SECTORS = {
    "CONVENTIONAL_FINANCE", "ALCOHOL", "PORK", "GAMBLING", "TOBACCO",
    "ADULT_ENTERTAINMENT", "HOTELS", "DEFENSE",
}
PROHIBITED_ACTIVITY_WORDS = {
    "alcohol", "casino", "gambling", "pork", "tobacco", "adult", "conventional finance",
}


def _result(event: dict, status: str, reason: str, rule_version: str) -> dict:
    return {
        "transactionId": event["transactionId"],
        "clientId": event["clientId"],
        "clientCode": event.get("clientCode"),
        "modelCode": event["modelCode"],
        "ticker": event["ticker"],
        "status": status,
        "reason": reason,
        "ruleVersion": rule_version,
    }


def evaluate(event: dict) -> dict:
    """각 Kafka 이벤트는 단 하나의 활성 구독 모델을 대상으로 한다."""
    model = event.get("modelCode")
    sector = (event.get("sector") or "").strip().upper()
    activity = (event.get("merchantCategory") or "").strip().lower()

    if model == "AAOIFI_CORE":
        if sector in PROHIBITED_SECTORS:
            return _result(event, "ALERT", f"AAOIFI 금지 업종 감지: {sector}", "AAOIFI-CORE-RULES-v1")
        return _result(event, "CLEAR", "입력된 업종 정보에서 AAOIFI 금지 업종이 확인되지 않았습니다", "AAOIFI-CORE-RULES-v1")

    if model == "HALAL_ACTIVITY":
        matched = next((word for word in PROHIBITED_ACTIVITY_WORDS if word in activity), None)
        if matched or sector in PROHIBITED_SECTORS:
            evidence = matched or sector
            return _result(event, "ALERT", f"할랄 사업활동 위험 신호 감지: {evidence}", "HALAL-ACTIVITY-RULES-v1")
        return _result(event, "CLEAR", "입력된 가맹점·사업활동 정보에서 금지 활동이 확인되지 않았습니다", "HALAL-ACTIVITY-RULES-v1")

    if model == "FINANCIAL_THRESHOLD":
        debt_ratio = _decimal(event.get("interestBearingDebtRatio"))
        income_ratio = _decimal(event.get("nonPermissibleIncomeRatio"))
        if debt_ratio is None or income_ratio is None:
            return _result(event, "REVIEW", "재무비율 데이터가 없어 수동 확인이 필요합니다", "FINANCIAL-THRESHOLD-RULES-v1")
        if debt_ratio > Decimal("0.33"):
            return _result(event, "ALERT", "이자부채 비율이 33% 한도를 초과했습니다", "FINANCIAL-THRESHOLD-RULES-v1")
        if income_ratio > Decimal("0.05"):
            return _result(event, "ALERT", "비허용 수익 비율이 5% 한도를 초과했습니다", "FINANCIAL-THRESHOLD-RULES-v1")
        return _result(event, "CLEAR", "이자부채 33%·비허용 수익 5% 기준 이내입니다", "FINANCIAL-THRESHOLD-RULES-v1")

    return _result(event, "REVIEW", f"지원하지 않는 구독 모델입니다: {model}", "UNSUPPORTED")


def _decimal(value):
    if value is None:
        return None
    return Decimal(str(value))
