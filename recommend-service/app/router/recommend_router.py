import logging

from fastapi import APIRouter, Depends

from app.config.security import verify_token
from app.model.schemas import RecommendResponse
from app.service.recommend_service import recommend_service

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/recommend", tags=["advisory"])


@router.get("/{user_id}", response_model=RecommendResponse)
async def get_recommendations(
    user_id: int,
    token_payload: dict = Depends(verify_token),
):
    logger.info("[Router] 적합 종목 추천 요청 - clientId=%s", user_id)
    return await recommend_service.get_recommendations(user_id)
