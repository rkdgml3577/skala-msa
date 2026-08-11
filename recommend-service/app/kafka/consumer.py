import json
import logging
import threading

from kafka import KafkaConsumer, KafkaProducer

from app.config.settings import settings
from app.service.anomaly_service import anomaly_service

logger = logging.getLogger(__name__)


class EnrollmentCompletedConsumer:
    """기존 Consumer 생명주기를 재사용한 screening.completed 이상 탐지기."""

    def __init__(self):
        self.topic = settings.kafka_topic_screening_completed
        self.consumer = None
        self.producer = None
        self._running = False
        self._last_grades: dict[str, str] = {}

    def start(self):
        self._running = True
        threading.Thread(target=self._consume, daemon=True).start()
        logger.info("[KafkaConsumer] 시작 - topic=%s", self.topic)

    def stop(self):
        self._running = False
        if self.consumer:
            self.consumer.close()
        if self.producer:
            self.producer.close()

    def _consume(self):
        try:
            self.consumer = KafkaConsumer(
                self.topic,
                bootstrap_servers=settings.kafka_bootstrap_servers,
                group_id=settings.kafka_consumer_group_id,
                auto_offset_reset="earliest",
                enable_auto_commit=True,
                value_deserializer=lambda value: json.loads(value.decode("utf-8")),
                consumer_timeout_ms=1000,
            )
            self.producer = KafkaProducer(
                bootstrap_servers=settings.kafka_bootstrap_servers,
                value_serializer=lambda value: json.dumps(value).encode("utf-8"),
                key_serializer=lambda value: value.encode("utf-8"),
            )
            while self._running:
                for message in self.consumer:
                    if not self._running:
                        break
                    self._handle_message(message.value)
        except Exception as exc:
            logger.error("[KafkaConsumer] 오류 발생: %s", exc)
        finally:
            if self.consumer:
                self.consumer.close()

    def _handle_message(self, event: dict):
        if event.get("screeningType") == "ORDER" or event.get("orderId") is not None:
            return
        ticker = event.get("ticker")
        grade = event.get("grade")
        if not ticker or not grade:
            return
        previous = self._last_grades.get(ticker)
        self._last_grades[ticker] = grade

        anomaly_type = None
        reason = None
        if previous and previous != grade:
            anomaly_type = "GRADE_CHANGED"
            reason = f"종목 등급이 {previous}에서 {grade}(으)로 변경되었습니다."
        elif grade == "WATCH":
            anomaly_type = "BOUNDARY_NEAR"
            reason = "재무비율 경계값 근접 또는 중요성 업종으로 사람의 재확인이 필요합니다."

        if not anomaly_type:
            return
        anomaly = anomaly_service.record(ticker, anomaly_type, previous, grade, reason)
        if self.producer:
            self.producer.send(
                settings.kafka_topic_anomaly_detected,
                key=ticker,
                value=anomaly.model_dump(mode="json"),
            )
        logger.info("[KafkaConsumer] anomaly.detected - ticker=%s type=%s", ticker, anomaly_type)


enrollment_consumer = EnrollmentCompletedConsumer()
