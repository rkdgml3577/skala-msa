from typing import List

from fastapi import APIRouter

from app.model.schemas import AnomalyResponse
from app.service.anomaly_service import anomaly_service

router = APIRouter(tags=["anomalies"])


@router.get("/api/anomalies", response_model=List[AnomalyResponse])
@router.get("/api/recommend/anomalies", response_model=List[AnomalyResponse])
async def get_anomalies():
    return anomaly_service.list()
