"""order.created 를 소비→판정→screening.completed 로 재발행하는 워커."""
import json
import logging
import threading
import time

from kafka import KafkaConsumer, KafkaProducer

from app.config.settings import settings
from app.service.worker import evaluate

logger = logging.getLogger(__name__)


class OrderConsumer:
    def __init__(self):
        self.consumer = None
        self.producer = None
        self._running = False
        self.processed_count = 0
        self.last_result = None

    def start(self):
        self._running = True
        threading.Thread(target=self._consume, daemon=True).start()
        logger.info("[KafkaConsumer] 시작 - topic=%s", settings.kafka_topic_order_created)

    def stop(self):
        self._running = False
        if self.consumer:
            self.consumer.close()
        if self.producer:
            self.producer.close()

    def _consume(self):
        while self._running:
            try:
                self.consumer = KafkaConsumer(
                    settings.kafka_topic_order_created,
                    bootstrap_servers=settings.kafka_bootstrap_servers,
                    group_id=settings.kafka_consumer_group_id,
                    auto_offset_reset="latest",
                    enable_auto_commit=True,
                    value_deserializer=lambda v: json.loads(v.decode("utf-8")),
                    consumer_timeout_ms=1000,
                )
                self.producer = KafkaProducer(
                    bootstrap_servers=settings.kafka_bootstrap_servers,
                    key_serializer=lambda v: str(v).encode("utf-8"),
                    value_serializer=lambda v: json.dumps(v).encode("utf-8"),
                )
                while self._running:
                    for message in self.consumer:
                        if not self._running:
                            break
                        self._handle(message.value)
            except Exception as exc:
                logger.warning("[KafkaConsumer] 연결 오류: %s", exc)
                time.sleep(3)
            finally:
                if self.consumer:
                    self.consumer.close()
                    self.consumer = None

    def _handle(self, event: dict):
        result = evaluate(event)
        self.producer.send(
            settings.kafka_topic_screening_completed,
            key=result.get("orderId"),
            value=result,
        )
        self.producer.flush()
        self.processed_count += 1
        self.last_result = result
        logger.info(
            "[AI Screening] orderId=%s decision=%s",
            result.get("orderId"), result.get("decision"),
        )


order_consumer = OrderConsumer()
