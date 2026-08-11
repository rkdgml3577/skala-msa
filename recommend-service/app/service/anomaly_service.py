from datetime import datetime, timezone
from threading import Lock
from typing import List, Optional

from app.model.schemas import AnomalyResponse


class AnomalyService:
    def __init__(self):
        self._items: List[AnomalyResponse] = []
        self._lock = Lock()

    def record(
        self,
        ticker: str,
        anomaly_type: str,
        previous_grade: Optional[str],
        current_grade: str,
        reason: str,
    ) -> AnomalyResponse:
        item = AnomalyResponse(
            ticker=ticker,
            anomalyType=anomaly_type,
            previousGrade=previous_grade,
            currentGrade=current_grade,
            reason=reason,
            detectedAt=datetime.now(timezone.utc),
        )
        with self._lock:
            self._items.insert(0, item)
            del self._items[100:]
        return item

    def list(self) -> List[AnomalyResponse]:
        with self._lock:
            return list(self._items)


anomaly_service = AnomalyService()
