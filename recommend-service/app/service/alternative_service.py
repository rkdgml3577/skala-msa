import logging
from decimal import Decimal
from typing import Optional

from app.client.course_client import course_client
from app.client.enrollment_client import enrollment_client
from app.model.schemas import (
    AlternativeRecommendation,
    AlternativeResponse,
)

logger = logging.getLogger(__name__)


class AlternativeService:
    MAX_RECOMMEND_COUNT = 5

    async def get_alternatives(
        self,
        ticker: str,
        client_id: Optional[int] = None,
        limit: int = MAX_RECOMMEND_COUNT,
    ) -> AlternativeResponse:
        source = await course_client.get_security(ticker.upper())
        excludes = [source.ticker]
        if client_id is not None:
            history = await enrollment_client.get_enrollment_history(client_id)
            excludes.extend(history.approvedTickers)
            excludes.extend(history.heldTickers)

        candidates = await course_client.get_recommend_courses(source.sector, list(set(excludes)))
        ranked = sorted(
            candidates,
            key=lambda item: self._similarity(source.marketCap, item.marketCap),
            reverse=True,
        )[: max(1, min(limit, 20))]

        alternatives = [
            AlternativeRecommendation(
                ticker=item.ticker,
                name=item.name,
                sector=item.sector,
                grade=item.grade,
                marketCap=item.marketCap,
                similarity=self._similarity(source.marketCap, item.marketCap),
                reason="동일 업종의 COMPLIANT 종목이며 시가총액 규모가 유사합니다.",
            )
            for item in ranked
        ]
        logger.info("[AlternativeService] ticker=%s count=%s", ticker, len(alternatives))
        return AlternativeResponse(
            sourceTicker=source.ticker,
            alternatives=alternatives,
            message="보류 종목과 동일 업종인 샤리아 적합 대체 종목입니다",
        )

    def _similarity(self, source: Decimal, candidate: Decimal) -> float:
        if source <= 0 or candidate <= 0:
            return 0.0
        smaller = min(source, candidate)
        larger = max(source, candidate)
        return round(float(smaller / larger), 4)


alternative_service = AlternativeService()
