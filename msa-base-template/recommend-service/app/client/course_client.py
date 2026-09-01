"""course-service 를 httpx 로 호출하는 클라이언트 예시."""
import logging
from typing import List

import httpx

from app.config.settings import settings

logger = logging.getLogger(__name__)


async def fetch_items() -> List[dict]:
    """course-service 의 상품 목록을 조회한다. 응답 래퍼({data:[...]}) 를 벗겨서 반환."""
    url = f"{settings.course_service_url}/api/courses"
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            response = await client.get(url)
            response.raise_for_status()
            body = response.json()
    except Exception as exc:
        logger.warning("[course_client] 상품 조회 실패: %s", exc)
        return []

    data = body.get("data") if isinstance(body, dict) else body
    return data or []
