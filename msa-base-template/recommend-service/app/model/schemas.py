from decimal import Decimal
from typing import List, Optional

from pydantic import BaseModel


class ItemSummary(BaseModel):
    id: int
    code: str
    name: str
    price: Decimal


class RecommendResponse(BaseModel):
    userId: int
    recommendations: List[ItemSummary] = []
    message: str


class ApiResponse(BaseModel):
    success: bool
    message: str
    data: Optional[dict] = None
