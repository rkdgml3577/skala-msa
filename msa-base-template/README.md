# MSA Base Template

새 백엔드 프로젝트를 빠르게 시작하기 위한 **재사용 가능한 MSA 뼈대**입니다.
교수님이 주신 `msa-lecture` 구조(Eureka + API Gateway + Auth Server + Java/Python 마이크로서비스 + Kafka + MariaDB + Vue)를 그대로 유지하되, 도메인 로직을 걷어내고 **제네릭 예제**로 정리했습니다. 각 서비스를 복제·수정하면 바로 새 도메인을 얹을 수 있습니다.

> 원본 ShariahGuard(샤리아 종목 심사) 소스는 저장소 루트에 그대로 보존돼 있으며, 이 템플릿은 `msa-base-template/` 아래 독립적으로 존재합니다.

---

## 무엇이 들어 있나

| 서비스 | 기술 | 포트 | 보여주는 패턴 |
|---|---|---:|---|
| `eureka-server` | Spring Boot | 8761 | 서비스 디스커버리 |
| `user-service` | Spring Boot | 8081 | JPA CRUD + **Kafka Producer** + Security + Swagger |
| `course-service` | Spring Boot | 8082 | JPA CRUD + Kafka Producer + **Kafka Consumer** + **@Scheduled** |
| `enrollment-service` | Spring Boot | 8083 | CRUD + **서비스 간 REST 호출(WebClient/LoadBalanced)** + Kafka Producer |
| `payment-service` | Spring Boot | 8084 | **Kafka Consumer → 처리 → Producer** + **Strategy(Evaluator) 패턴** |
| `recommend-service` | FastAPI | 8085 | Python REST + **httpx 서비스 호출** + Kafka Consumer + JWT 검증 |
| `ai-screening-service` | FastAPI | 8086 | **Kafka 비동기 워커**(consume→판정→produce) |
| `vue-frontend` | Vue 3 + Vite | 3000 | Gateway(`/api`) 호출 최소 UI |
| `auth-server` *(선택)* | 사전 빌드 이미지 | 9000 | OAuth2 / JWT 발급 |
| `api-gateway` *(선택)* | 사전 빌드 이미지 | 8080 | JWT 검증 + 라우팅 |

- Java 패키지: `com.example.msa.<service>` (그룹 `com.example.msa`)
- 공통 요소: `ApiResponse<T>` 응답 래퍼, `BaseTimeEntity`(createdAt/updatedAt 자동), `GlobalExceptionHandler`, H2 기반 테스트 프로필

---

## 이벤트 흐름 (Kafka)

티커/키를 파티션 키로 사용하는 예시 흐름입니다.

```
user-service ──user.created──▶ (구독 예시 지점)

enrollment-service ──order.created──▶ payment-service ──payment.completed──▶ course-service (판매수 +1)
                          │                                              └──▶ recommend-service (구독)
                          └────────order.created───────────────────▶ ai-screening-service ──screening.completed──▶ (후속 확장)
```

| 토픽 | 발행 | 구독 |
|---|---|---|
| `user.created` | user-service | (확장 지점) |
| `order.created` | enrollment-service | payment-service, ai-screening-service |
| `payment.completed` | payment-service | course-service, recommend-service |
| `screening.completed` | ai-screening-service | (확장 지점) |

동기 REST 호출: `enrollment-service` → `user-service`(존재 확인), `course-service`(상품/가격 조회) · `recommend-service` → `course-service`(상품 목록).

---

## 실행

### 1) 핵심 스택만 (Gateway/Auth 없이)

```bash
cd msa-base-template
docker compose up -d --build
```

- 각 서비스 `SecurityConfig` 기본값이 `permitAll` 이라 Gateway/Auth 없이도 직접 호출됩니다.
- Eureka 대시보드: http://localhost:8761
- Swagger(서비스별): http://localhost:8081/swagger-ui.html (8082~8084 동일)
- 프런트엔드: http://localhost:3000
- Python 헬스체크: http://localhost:8085/health , http://localhost:8086/health

### 2) Gateway + Auth Server 까지 (사전 빌드 이미지 필요)

`auth-server`, `api-gateway` 는 소스가 아니라 사전 빌드 이미지입니다. 원본 프로젝트의 `infra-images.tar` 를 로드한 뒤 `infra` 프로필로 실행하세요.

```bash
docker load -i /path/to/infra-images.tar
docker compose --profile infra up -d --build
```

### 로컬(도커 없이) 개별 실행

```bash
# 인프라만 도커로
docker compose up -d mariadb kafka eureka-server

# Java 서비스
cd user-service && ./gradlew bootRun

# Python 서비스
cd recommend-service && pip install -r requirements.txt && uvicorn main:app --reload --port 8085
```

---

## API 빠른 예시

```bash
# 사용자 생성 (user.created 발행)
curl -X POST http://localhost:8081/api/users \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","email":"alice@example.com","displayName":"Alice"}'

# 상품 생성 (item.created 발행)
curl -X POST http://localhost:8082/api/courses \
  -H 'Content-Type: application/json' \
  -d '{"code":"ITEM-1","name":"샘플","price":1000}'

# 주문 생성 → user/course REST 확인 → order.created 발행 → payment/ai-screening 이 소비
curl -X POST http://localhost:8083/api/enrollments \
  -H 'Content-Type: application/json' \
  -d '{"userId":1,"itemId":1,"quantity":2}'

# 결제/심사 결과 조회
curl http://localhost:8084/api/payments
```

---

## 검증(테스트)

Java 테스트는 H2·auto-startup=false 로 MariaDB/Kafka 없이 실행됩니다.

```bash
for s in eureka-server user-service course-service enrollment-service payment-service; do
  (cd "$s" && ./gradlew test --console=plain)
done

python3 -m compileall -q recommend-service ai-screening-service
docker compose config --quiet
cd vue-frontend && npm install && npm run build
```

---

## 새 프로젝트를 시작하는 법

1. `msa-base-template/` 를 새 저장소로 복사합니다.
2. 필요한 서비스만 남기고 나머지 디렉터리는 삭제합니다(`docker-compose.yml` 에서도 제거).
3. 서비스 안의 예제 도메인을 실제 도메인으로 교체합니다.
   - 엔티티: `entity/User.java`, `entity/Item.java`, `entity/Order.java`, `entity/Payment.java`
   - DTO / Repository / Service / Controller 를 같은 패턴으로 수정
   - Kafka 토픽 이름은 `application.yml` 의 `kafka.topic.*` 에서 변경
4. 패키지명을 바꾸려면 `com.example.msa` 를 원하는 값으로 일괄 치환하고 `group` 도 함께 수정합니다.
5. 인증을 켜려면 각 `SecurityConfig` 의 프로덕션 예시 블록을 활성화하고 `--profile infra` 로 Gateway/Auth 를 함께 띄웁니다.

각 계층이 어디에 있고 무엇을 담당하는지는 위 표와 각 파일 상단 주석을 참고하세요.
