import logging
from contextlib import asynccontextmanager

import py_eureka_client.eureka_client as eureka_client
from fastapi import FastAPI

from app.config.settings import settings
from app.kafka.consumer import trade_consumer

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s - %(message)s",
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(_: FastAPI):
    logger.info("[%s] 서비스 시작", settings.app_name)
    try:
        await eureka_client.init_async(
            eureka_server=settings.eureka_server_url,
            app_name=settings.app_name,
            instance_port=settings.app_port,
            instance_host=settings.eureka_instance_host,
        )
        logger.info("[Eureka] 서비스 등록 완료")
    except Exception as exc:
        logger.warning("[Eureka] 등록 실패: %s", exc)

    trade_consumer.start()
    yield
    trade_consumer.stop()
    await eureka_client.stop_async()


app = FastAPI(
    title="Shariah AI Screening Service",
    description="은행사 거래를 구독 모델별로 비동기 판정하는 규칙 기반 AI 모듈",
    version="1.0.0",
    lifespan=lifespan,
)


@app.get("/health")
async def health():
    return {
        "status": "UP",
        "service": settings.app_name,
        "processedCount": trade_consumer.processed_count,
        "lastResult": trade_consumer.last_result,
    }
