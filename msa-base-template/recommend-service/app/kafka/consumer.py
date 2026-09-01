"""payment.completed 이벤트를 구독하는 Kafka Consumer 예시 (백그라운드 스레드)."""
import json
import logging
import threading
import time

from kafka import KafkaConsumer

from app.config.settings import settings

logger = logging.getLogger(__name__)


class PaymentConsumer:
    def __init__(self):
        self.consumer = None
        self._running = False
        self.processed_count = 0

    def start(self):
        self._running = True
        threading.Thread(target=self._consume, daemon=True).start()
        logger.info("[KafkaConsumer] 시작 - topic=%s", settings.kafka_topic_payment_completed)

    def stop(self):
        self._running = False
        if self.consumer:
            self.consumer.close()

    def _consume(self):
        while self._running:
            try:
                self.consumer = KafkaConsumer(
                    settings.kafka_topic_payment_completed,
                    bootstrap_servers=settings.kafka_bootstrap_servers,
                    group_id=settings.kafka_consumer_group_id,
                    auto_offset_reset="latest",
                    enable_auto_commit=True,
                    value_deserializer=lambda v: json.loads(v.decode("utf-8")),
                    consumer_timeout_ms=1000,
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
        self.processed_count += 1
        # 실제 프로젝트에서는 여기서 추천 모델 갱신, 알림 발송 등을 수행한다.
        logger.info("[Recommend] payment.completed 수신 - event=%s", event)


payment_consumer = PaymentConsumer()
