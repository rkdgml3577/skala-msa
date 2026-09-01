import logging

from fastapi import APIRouter

from app.model.schemas import RecommendResponse
from app.service import recommend_service

# 인증을 켜려면: from fastapi import Depends; from app.config.security import verify_token
# 그리고 아래 엔드포인트에 token: dict = Depends(verify_token) 를 추가한다.

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/recommend", tags=["recommend"])


@router.get("/{user_id}", response_model=RecommendResponse)
async def get_recommendations(user_id: int):
    logger.info("[Router] 추천 요청 - userId=%s", user_id)
    return await recommend_service.get_recommendations(user_id)
