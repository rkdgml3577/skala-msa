# ShariahGuard — 샤리아 투자종목·매매주문 적합성 심사

기존 `msa-lecture` 교육 플랫폼의 MSA 경계와 인프라를 유지하면서 내부 역할을 샤리아 컴플라이언스 도메인으로 치환한 MVP입니다. 은행사 거래 수신과 비동기 탐지를 담당하는 `ai-screening-service`를 추가했습니다.

## 기존 서비스 재사용 매핑

| 기존 디렉터리/등록명 | 현재 역할 | 핵심 책임 |
|---|---|---|
| `user-service` | client-service 역할 | 금융회사 계정, JWT 사용자, Client Code, API 키 |
| `course-service` | security-service 역할 | 종목 마스터, 재무데이터, 저장 등급, 배치 이벤트 |
| `enrollment-service` | order-service 역할 | 매매주문 접수, 실시간 등급 조회, 승인·주의·보류 |
| `payment-service` | screening-service 역할 | 금지업종 및 AAOIFI 재무비율 규칙 심사 |
| `recommend-service` | advisory-service 역할 | 동일 업종 적합 대체 종목, 이상 징후 |
| `ai-screening-service` | AI screening 역할 | 은행 거래 이벤트를 구독 모델별로 소비·판정 |
| `vue-frontend` | 컴플라이언스 콘솔 | 종목·주문·근거·감사 이력 화면 |

Eureka, API Gateway, Auth Server, JWT, MariaDB, Kafka와 기존 서비스명은 그대로 사용합니다. 배포된 Gateway 이미지가 기존 URL만 명시적으로 라우팅하므로 외부 API 경로도 `/api/courses`, `/api/enrollments`, `/api/payments`, `/api/recommend`를 유지합니다. 각 Java Controller는 도메인 의미가 분명한 `/api/securities`, `/api/orders`, `/api/screenings` 별칭도 제공합니다.

## 2단계 심사

1. Level 1 종목 심사: `course-service`가 `security.updated`를 발행하면 `payment-service`가 금지업종과 AAOIFI 30%/30%/5% 규칙을 평가해 `screening.completed`를 발행합니다. `course-service`는 결과 등급과 근거, 룰 버전을 저장합니다.
2. Level 2 주문 심사: `enrollment-service`는 주문을 PENDING으로 저장한 후 종목의 기계산 등급을 O(1) REST 조회하여 `APPROVED`, `WARNED`, `HELD`로 즉시 확정합니다. RESTRICTED 종목은 `recommend-service`에서 같은 업종의 COMPLIANT 대체 종목을 조회합니다.

Kafka 토픽은 티커를 파티션 키로 사용합니다.

- `security.updated`: course-service → payment-service
- `order.received`: enrollment-service → payment-service
- `screening.completed`: payment-service → course/enrollment/recommend-service
- `anomaly.detected`: recommend-service → course-service
- `trade.received`: user-service → ai-screening-service (활성 구독 모델별 거래 이벤트)
- `ai.screening.completed`: ai-screening-service → 후속 알림·저장 모듈 확장 지점

## 은행사 API 키 거래 연동

은행사 계정을 만들거나 **API 키 재발급**을 하면 `shr_...` 형식의 키가 한 번만 표시됩니다. DB에는 BCrypt 해시만 저장되므로 재발급 즉시 이전 키는 인증에 사용할 수 없습니다.

외부 금융회사는 Gateway JWT가 아니라 이 키로 거래를 보냅니다. 현재 Gateway 이미지의 공개 경로 정책과 분리하기 위해 수신 API는 `user-service` 포트(8081)에 직접 노출합니다.

```bash
curl -X POST http://localhost:8081/api/users/transactions \
  -H 'Content-Type: application/json' \
  -H 'X-API-Key: YOUR_ISSUED_API_KEY' \
  -d '{
    "transactionId": "bank-trade-20260811-001",
    "ticker": "AAPL",
    "side": "BUY",
    "quantity": 10,
    "price": 210.50,
    "sector": "TECHNOLOGY",
    "merchantCategory": "software",
    "interestBearingDebtRatio": 0.20,
    "nonPermissibleIncomeRatio": 0.01
  }'
```

`202 Accepted` 응답에는 실제로 발행한 활성 구독 모델 목록이 포함됩니다. 일시중지·해지 모델은 발행 대상에서 제외되며, AI 결과는 은행사별 `연동·감사` 탭에 저장·표시됩니다.

현재 AI 모듈은 나중에 ML 모델로 바꿀 수 있도록 분리된 임시 규칙 엔진입니다.

- `AAOIFI_CORE`: 금지 업종을 탐지합니다.
- `HALAL_ACTIVITY`: 업종·가맹점 활동의 할랄 위험 키워드를 탐지합니다.
- `FINANCIAL_THRESHOLD`: 이자부채 33%, 비허용 수익 5% 한도를 판정합니다.

## 주요 API

Gateway 기준 URL은 `http://localhost:8080`입니다.

| Method | URL | 설명 |
|---|---|---|
| GET | `/api/courses` | 전체 종목과 최신 등급 조회 |
| GET | `/api/courses/{id}` | 종목 재무비율·판단 근거 상세 |
| POST | `/api/courses` | 심사 대상 종목 등록 및 이벤트 발행 |
| POST | `/api/courses/internal/refresh` | 전 종목 재심사 이벤트 수동 발행 |
| POST | `/api/enrollments` | 매매주문 사전 심사 |
| GET | `/api/enrollments/my` | 로그인 고객사의 주문 심사 이력 |
| GET | `/api/payments/ticker/{ticker}` | 종목 심사 감사 이력 |
| GET | `/api/recommend/alternatives/{ticker}` | 같은 업종 COMPLIANT 대체 종목 |
| GET | `/api/recommend/anomalies` | 등급 변화·경계값 이상 징후 |

주문 요청 예시:

```json
{
  "ticker": "DEBTX",
  "side": "BUY",
  "quantity": 10,
  "orderPrice": 42.10
}
```

`DEBTX`는 AAOIFI 부채 한도 초과 데모 종목이므로 `HELD`와 함께 `AAPL`, `MSFT`, `TSM` 등의 대체 티커가 반환됩니다. `BORDER`는 경계값 근접 `WARNED`, `AAPL`은 `APPROVED` 흐름을 보여줍니다.

## 실행

Auth Server와 API Gateway 공통 이미지가 없다면 먼저 로드합니다.

```bash
docker load -i infra-images.tar
docker compose up -d --build
```

프런트엔드는 별도 터미널에서 실행합니다.

```bash
cd vue-frontend
npm install
npm run dev
```

- 컴플라이언스 콘솔: `http://localhost:3000`
- Eureka: `http://localhost:8761`
- 개별 Swagger: `http://localhost:8081~8084/swagger-ui.html`
- AI 모듈 상태: `http://localhost:8086/health`

초기 DDL은 새 MariaDB 볼륨이 만들어질 때 실행됩니다. 이전 온라인 강의용 볼륨이 남아 있다면 새 스키마를 정확히 확인하려고 개발 데이터 볼륨을 재생성할 수 있지만, 볼륨 삭제는 기존 데이터를 제거하므로 필요한 경우에만 직접 수행하십시오.

## 검증

```bash
for service_dir in eureka-server user-service course-service enrollment-service payment-service; do
  (cd "$service_dir" && /bin/bash gradlew test --console=plain)
done

cd vue-frontend && npm run build
python3 -m compileall -q recommend-service ai-screening-service
docker compose config --quiet
```

Java 테스트는 H2 테스트 프로필을 사용하므로 MariaDB·Kafka를 띄우지 않아도 실행됩니다. payment-service에는 금지업종 short-circuit, 적합 비율, 30% 한도 초과, 경계값 WATCH 단위 테스트가 포함되어 있습니다.

## Sprint 2 확장점

Sprint 1은 규칙 기반 코어만 구현합니다. `Evaluator` 인터페이스에 사업보고서 LLM 정성 심사 구현체를 추가하고, advisory-service의 메모리 이상 징후 저장소를 영속 저장소·통계 모델로 교체하며, 대체 종목 정렬을 리스크 프로파일 유사도 모델로 교체할 수 있습니다.
