import logging

from app.client.course_client import course_client
from app.model.schemas import RecommendResponse

logger = logging.getLogger(__name__)


class RecommendService:
    """기존 추천 진입점을 유지하는 적합 종목 추천 서비스."""

    MAX_RECOMMEND_COUNT = 5

    async def get_recommendations(self, user_id: int) -> RecommendResponse:
        securities = await course_client.get_all_courses()
        compliant = sorted(
            (item for item in securities if item.grade == "COMPLIANT"),
            key=lambda item: item.marketCap,
            reverse=True,
        )[: self.MAX_RECOMMEND_COUNT]
        return RecommendResponse(
            clientId=user_id,
            recommendedSecurities=compliant,
            basedOnSector=None,
            message="시가총액 기준 샤리아 적합 종목입니다",
        )


recommend_service = RecommendService()
