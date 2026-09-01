from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    app_port: int = 8086
    app_name: str = "ai-screening-service"

    eureka_server_url: str = "http://localhost:8761/eureka"
    eureka_instance_host: str = "localhost"

    kafka_bootstrap_servers: str = "localhost:9092"
    kafka_consumer_group_id: str = "ai-screening-service"
    kafka_topic_order_created: str = "order.created"
    kafka_topic_screening_completed: str = "screening.completed"

    class Config:
        env_file = ".env"


settings = Settings()
