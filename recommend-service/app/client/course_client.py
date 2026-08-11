import logging
from typing import List

import httpx

from app.config.settings import settings
from app.model.schemas import CourseCategory, CourseResponse

logger = logging.getLogger(__name__)


class CourseServiceClient:
    """기존 클라이언트 구조를 재사용한 security-service REST 클라이언트."""

    def __init__(self):
        self.base_url = settings.course_service_url

    async def get_security(self, ticker: str) -> CourseResponse:
        url = f"{self.base_url}/api/courses/internal/ticker/{ticker}"
        async with httpx.AsyncClient(timeout=5.0) as client:
            response = await client.get(url)
            response.raise_for_status()
            return CourseResponse(**response.json())

    async def get_recommend_courses(
        self,
        category: CourseCategory,
        exclude_ids: List[str]
    ) -> List[CourseResponse]:
        url = f"{self.base_url}/api/courses/internal/recommend"
        params = {"sector": category.value}
        if exclude_ids:
            params["excludeTickers"] = ",".join(exclude_ids)
        try:
            async with httpx.AsyncClient(timeout=5.0) as client:
                response = await client.get(url, params=params)
                response.raise_for_status()
                return [CourseResponse(**item) for item in response.json()]
        except httpx.HTTPError as exc:
            logger.error("[CourseClient] 대체 종목 조회 실패 - sector=%s error=%s", category, exc)
            return []

    async def get_all_courses(self) -> List[CourseResponse]:
        url = f"{self.base_url}/api/courses"
        try:
            async with httpx.AsyncClient(timeout=5.0) as client:
                response = await client.get(url)
                response.raise_for_status()
                payload = response.json()
                items = payload.get("data", []) if isinstance(payload, dict) else payload
                return [CourseResponse(**item) for item in items]
        except httpx.HTTPError as exc:
            logger.error("[CourseClient] 전체 종목 조회 실패 - error=%s", exc)
            return []


course_client = CourseServiceClient()
