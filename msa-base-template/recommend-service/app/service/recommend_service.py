import logging

from app.client.course_client import fetch_items
from app.model.schemas import ItemSummary, RecommendResponse

logger = logging.getLogger(__name__)

MAX_RECOMMENDATIONS = 5


async def get_recommendations(user_id: int) -> RecommendResponse:
    """예시 추천 로직: course-service 의 상품을 가져와 상위 N개를 추천한다.

    실제 프로젝트에서는 사용자 이력/유사도 모델 등으로 교체한다.
    """
    items = await fetch_items()
    recommendations = [
        ItemSummary(
            id=item["id"],
            code=item["code"],
            name=item["name"],
            price=item["price"],
        )
        for item in items[:MAX_RECOMMENDATIONS]
    ]
    return RecommendResponse(
        userId=user_id,
        recommendations=recommendations,
        message=f"{len(recommendations)}개의 추천 상품",
    )
