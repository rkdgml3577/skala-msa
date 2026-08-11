from datetime import datetime
from decimal import Decimal
from enum import Enum
from typing import List, Optional

from pydantic import BaseModel


class CourseCategory(str, Enum):
    TECHNOLOGY = "TECHNOLOGY"
    HEALTHCARE = "HEALTHCARE"
    ENERGY = "ENERGY"
    CONSUMER_GOODS = "CONSUMER_GOODS"
    INDUSTRIALS = "INDUSTRIALS"
    REAL_ESTATE = "REAL_ESTATE"
    MATERIALS = "MATERIALS"
    TELECOMMUNICATION = "TELECOMMUNICATION"
    UTILITIES = "UTILITIES"
    ISLAMIC_FINANCE = "ISLAMIC_FINANCE"
    CONVENTIONAL_FINANCE = "CONVENTIONAL_FINANCE"
    ALCOHOL = "ALCOHOL"
    PORK = "PORK"
    GAMBLING = "GAMBLING"
    TOBACCO = "TOBACCO"
    ADULT_ENTERTAINMENT = "ADULT_ENTERTAINMENT"
    MEDIA = "MEDIA"
    HOTELS = "HOTELS"
    DEFENSE = "DEFENSE"
    OTHER = "OTHER"


class CourseResponse(BaseModel):
    id: int
    ticker: str
    name: str
    description: Optional[str] = None
    sector: CourseCategory
    lastPrice: Decimal
    marketCap: Decimal
    exchangeId: int
    screeningCount: int = 0
    grade: str
    screeningReason: Optional[str] = None
    lastScreenedAt: Optional[datetime] = None
    ruleVersionId: Optional[str] = None


class EnrollmentHistoryResponse(BaseModel):
    clientId: int
    approvedTickers: List[str] = []
    heldTickers: List[str] = []


class AlternativeRecommendation(BaseModel):
    ticker: str
    name: str
    sector: CourseCategory
    grade: str
    marketCap: Decimal
    similarity: float
    reason: str


class AlternativeResponse(BaseModel):
    sourceTicker: str
    alternatives: List[AlternativeRecommendation]
    message: str


class RecommendResponse(BaseModel):
    clientId: int
    recommendedSecurities: List[CourseResponse]
    basedOnSector: Optional[CourseCategory] = None
    message: str


class AnomalyResponse(BaseModel):
    ticker: str
    anomalyType: str
    previousGrade: Optional[str] = None
    currentGrade: str
    reason: str
    detectedAt: datetime


class ApiResponse(BaseModel):
    success: bool
    message: str
    data: Optional[dict] = None
