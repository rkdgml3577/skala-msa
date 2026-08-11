import logging

import httpx

from app.config.settings import settings
from app.model.schemas import EnrollmentHistoryResponse

logger = logging.getLogger(__name__)


class EnrollmentServiceClient:
    """기존 클라이언트 구조를 재사용한 order-service 주문 이력 클라이언트."""

    def __init__(self):
        self.base_url = settings.enrollment_service_url

    async def get_enrollment_history(self, user_id: int) -> EnrollmentHistoryResponse:
        url = f"{self.base_url}/api/enrollments/internal/history/{user_id}"
        try:
            async with httpx.AsyncClient(timeout=5.0) as client:
                response = await client.get(url)
                response.raise_for_status()
                return EnrollmentHistoryResponse(**response.json())
        except httpx.HTTPError as exc:
            logger.error("[EnrollmentClient] 주문 이력 조회 실패 - clientId=%s error=%s", user_id, exc)
            return EnrollmentHistoryResponse(clientId=user_id, approvedTickers=[], heldTickers=[])


enrollment_client = EnrollmentServiceClient()
