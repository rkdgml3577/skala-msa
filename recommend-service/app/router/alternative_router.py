from typing import Optional

from fastapi import APIRouter, Query

from app.model.schemas import AlternativeResponse
from app.service.alternative_service import alternative_service

router = APIRouter(tags=["alternatives"])


async def _alternatives(ticker: str, client_id: Optional[int], limit: int) -> AlternativeResponse:
    return await alternative_service.get_alternatives(ticker, client_id, limit)


@router.get("/api/alternatives/{ticker}", response_model=AlternativeResponse)
async def get_alternatives(
    ticker: str,
    client_id: Optional[int] = Query(default=None, alias="clientId"),
    limit: int = Query(default=5, ge=1, le=20),
):
    return await _alternatives(ticker, client_id, limit)


@router.get("/api/recommend/alternatives/{ticker}", response_model=AlternativeResponse)
async def get_alternatives_gateway(
    ticker: str,
    client_id: Optional[int] = Query(default=None, alias="clientId"),
    limit: int = Query(default=5, ge=1, le=20),
):
    return await _alternatives(ticker, client_id, limit)
