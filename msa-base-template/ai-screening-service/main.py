import logging
from contextlib import asynccontextmanager

import py_eureka_client.eureka_client as eureka_client
from fastapi import FastAPI

from app.config.settings import settings
from app.kafka.consumer import order_consumer

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
        logger.warning("[Eureka] 등록 실패 (무시 가능): %s", exc)

    order_consumer.start()
    yield
    order_consumer.stop()
    try:
        await eureka_client.stop_async()
    except Exception:
        pass


app = FastAPI(
    title="AI Screening Service (Kafka Worker 예시)",
    description="이벤트를 소비→판정→재발행하는 비동기 워커 패턴 템플릿. 규칙을 나중에 ML 모델로 교체 가능.",
    version="1.0.0",
    lifespan=lifespan,
)


@app.get("/health")
async def health():
    return {
        "status": "UP",
        "service": settings.app_name,
        "processedCount": order_consumer.processed_count,
        "lastResult": order_consumer.last_result,
    }
