from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # 서버
    app_port: int = 8085
    app_name: str = "recommend-service"

    # Eureka
    eureka_server_url: str = "http://localhost:8761/eureka"
    eureka_instance_host: str = "localhost"

    # Auth Server (JWT 검증 — security.py 에서 사용)
    jwt_issuer_uri: str = "http://localhost:9000"
    jwk_set_uri: str = "http://localhost:9000/oauth2/jwks"

    # 다른 서비스 URL (Eureka 미사용 시 직접 지정)
    course_service_url: str = "http://localhost:8082"

    # Kafka
    kafka_bootstrap_servers: str = "localhost:9092"
    kafka_consumer_group_id: str = "recommend-service"
    kafka_topic_payment_completed: str = "payment.completed"

    class Config:
        env_file = ".env"


settings = Settings()
