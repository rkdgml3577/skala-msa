# 샤리아 투자 적합성 심사 서비스 변경 내역

## 1. 변경 개요

기존 `msa-lecture` 프로젝트의 MSA 구조와 서비스 이름을 유지하면서, 교육 과정·수강·결제 중심의 도메인을 샤리아 투자종목 및 매매주문 적합성 심사 도메인으로 변경했다.

새로운 마이크로서비스는 추가하지 않았으며, 기존 서비스의 내부 역할을 다음과 같이 전환했다.

| 기존 서비스 | 변경된 역할 |
|---|---|
| `user-service` | 기관·클라이언트 및 API Key 관리 |
| `course-service` | 투자 종목, 재무정보 및 샤리아 등급 관리 |
| `enrollment-service` | 매매주문 접수 및 실시간 적합성 판정 |
| `payment-service` | AAOIFI 기준 심사 엔진 및 감사 이력 관리 |
| `recommend-service` | 적합 대체 종목 추천 및 이상 탐지 |
| Eureka / API Gateway / JWT | 기존 서비스 디스커버리, 라우팅 및 인증 구조 유지 |

기존 API Gateway 경로와 클래스 이름의 호환성을 위해 `/api/courses`, `/api/enrollments`, `/api/payments`, `/api/recommend` 경로와 `Course`, `Enrollment`, `Payment` 등의 이름을 유지했다.

## 2. 주요 처리 흐름

### 2.1 Level 1: 종목 일괄 심사

1. `course-service`가 종목의 재무정보 갱신을 시작한다.
2. `security.updated` Kafka 이벤트를 발행한다.
3. `payment-service`가 업종 및 재무비율을 평가한다.
4. 심사 결과를 `screenings` 테이블에 저장한다.
5. `screening.completed` 이벤트를 발행한다.
6. `course-service`가 결과를 받아 종목의 샤리아 등급을 갱신한다.
7. `recommend-service`가 등급 변경이나 경계 상태를 감지한다.
8. 필요한 경우 `anomaly.detected` 이벤트를 발행한다.

### 2.2 Level 2: 매매주문 실시간 심사

1. 사용자가 종목, 매수·매도 구분, 수량과 가격을 입력한다.
2. `enrollment-service`가 주문을 `PENDING` 상태로 저장한다.
3. `course-service`에서 현재 종목 등급을 조회해 즉시 주문 상태를 결정한다.
4. 동시에 `order.received` Kafka 이벤트를 발행한다.
5. `payment-service`가 주문 심사를 수행하고 감사 이력을 저장한다.
6. `screening.completed` 이벤트를 통해 최종 결과를 주문에 반영한다.
7. 제한 종목이면 `recommend-service`에서 동일 업종의 적합 종목을 추천한다.

### 2.3 등급 및 주문 상태 매핑

| 종목 등급 | 주문 상태 | 의미 |
|---|---|---|
| `COMPLIANT` | `APPROVED` | 샤리아 기준 적합, 주문 승인 |
| `WATCH` | `WARNED` | 기준 한도에 근접, 경고와 함께 주문 유지 |
| `RESTRICTED` | `HELD` | 샤리아 기준 부적합, 주문 보류 |

## 3. 공통 설정 및 데이터베이스

### `readme.md`

- 샤리아 적합성 심사 서비스의 전체 구조 설명을 추가했다.
- 서비스별 역할, Kafka 이벤트 흐름, 주요 API와 실행 방법을 정리했다.
- 샤리아 등급과 매매주문 상태의 의미를 추가했다.

### `docker-compose.build.yml`

- 수정된 서비스 소스를 Docker 이미지로 직접 빌드하도록 구성했다.
- 서비스별 Kafka 의존성과 심사 토픽 환경변수를 추가했다.
- 기존 포트, 네트워크 및 서비스 이름은 유지했다.

### `docker-compose.yml`

- `course-service`가 Kafka에 연결되도록 설정했다.
- 샤리아 심사에 사용하는 Kafka 토픽 설정을 추가했다.
- 기존 사전 빌드 이미지 기반 실행 구조를 유지했다.

### `init-db/01_init.sql`

- 사용자 테이블에 기관명, 클라이언트 코드, API Key 해시 관련 필드를 추가했다.
- 다음 도메인 테이블을 구성했다.

| 테이블 | 용도 |
|---|---|
| `users` | 기관 사용자, 클라이언트 코드 및 API Key 정보 |
| `securities` | 종목 기본정보, 재무비율 및 샤리아 등급 |
| `orders` | 매매주문과 실시간 심사 상태 |
| `screenings` | 종목·주문 심사 결과와 감사 이력 |

- 다음 데모 종목을 추가했다.

| 티커 | 초기 등급 | 테스트 목적 |
|---|---|---|
| `AAPL` | `COMPLIANT` | 정상 승인 주문 |
| `MSFT` | `COMPLIANT` | 정상 적합 종목 |
| `TSM` | `COMPLIANT` | 정상 적합 종목 |
| `SUKK` | `COMPLIANT` | 정상 적합 종목 |
| `BORDER` | `WATCH` | 기준 경계 종목 |
| `MEDIAW` | `WATCH` | 주의 종목 |
| `DEBTX` | `RESTRICTED` | 부채비율 기준 초과 |
| `CITI` | `RESTRICTED` | 금지 업종 판정 |

## 4. user-service

기존 사용자 관리와 OAuth/JWT 구조를 유지하면서 기관 클라이언트 관리 기능을 추가했다.

### 변경 파일

- `user-service/build.gradle`
- `user-service/src/main/java/com/lecture/user/controller/UserController.java`
- `user-service/src/main/java/com/lecture/user/dto/UserDto.java`
- `user-service/src/main/java/com/lecture/user/entity/User.java`
- `user-service/src/main/java/com/lecture/user/service/UserService.java`
- `user-service/src/test/resources/application.yml`

### 변경 내용

- 사용자 엔티티에 다음 정보를 추가했다.
  - `organizationName`
  - `clientCode`
  - `apiKeyHash`
  - `active`
- 회원가입 시 클라이언트 API Key를 생성한다.
- API Key 원문은 최초 발급 응답에서만 전달하고 DB에는 BCrypt 해시를 저장한다.
- API Key 재발급 API를 추가했다.
- 기존 인증 호환성을 위해 내부 권한값 `STUDENT`, `INSTRUCTOR`는 유지했다.
- 테스트 시 외부 MariaDB 없이 실행할 수 있도록 H2 테스트 설정을 추가했다.

## 5. course-service

기존 `Course` 클래스와 Controller-Service-Repository 계층을 유지하면서 투자 종목 마스터 서비스로 변경했다.

### 변경 파일

- `course-service/build.gradle`
- `course-service/src/main/java/com/lecture/course/CourseServiceApplication.java`
- `course-service/src/main/java/com/lecture/course/config/KafkaConfig.java`
- `course-service/src/main/java/com/lecture/course/controller/CourseController.java`
- `course-service/src/main/java/com/lecture/course/dto/CourseDto.java`
- `course-service/src/main/java/com/lecture/course/entity/Course.java`
- `course-service/src/main/java/com/lecture/course/repository/CourseRepository.java`
- `course-service/src/main/java/com/lecture/course/service/CourseService.java`
- `course-service/src/main/java/com/lecture/course/service/CourseFinancialRefreshScheduler.java`
- `course-service/src/main/java/com/lecture/course/kafka/CourseKafkaProducer.java`
- `course-service/src/main/java/com/lecture/course/kafka/CourseKafkaConsumer.java`
- `course-service/src/main/resources/application.yml`
- `course-service/src/test/resources/application.yml`

### 변경 내용

- `Course` 엔티티를 `securities` 테이블에 매핑했다.
- 다음 종목 및 심사 정보를 관리하도록 필드를 변경했다.
  - 티커와 종목명
  - 업종
  - 현재가와 시가총액
  - 부채비율
  - 이자성 자산 비율
  - 비허용 수익 비율
  - 샤리아 등급
  - 판정 사유
  - 적용 규칙 버전
- 티커 기반 종목 상세 조회 기능을 추가했다.
- 주문 서비스가 빠르게 사용할 수 있는 내부 등급 조회 API를 추가했다.
- 동일 업종의 `COMPLIANT` 종목 조회 기능을 추가했다.
- 매일 오전 2시에 전체 종목 갱신 이벤트를 발행하는 스케줄러를 추가했다.
- 수동으로 전체 종목 재심사를 시작하는 API를 추가했다.
- 트랜잭션 커밋 이후 `security.updated` 이벤트가 발행되도록 처리했다.
- `screening.completed` 이벤트를 소비해 종목 등급을 갱신한다.
- `anomaly.detected` 이벤트를 소비해 이상 상태를 반영한다.

## 6. enrollment-service

기존 `Enrollment` 클래스와 계층 구조를 유지하면서 매매주문 접수 및 실시간 적합성 판정 서비스로 변경했다.

### 변경 파일

- `enrollment-service/build.gradle`
- `enrollment-service/src/main/java/com/lecture/enrollment/config/KafkaConfig.java`
- `enrollment-service/src/main/java/com/lecture/enrollment/controller/EnrollmentController.java`
- `enrollment-service/src/main/java/com/lecture/enrollment/dto/EnrollmentDto.java`
- `enrollment-service/src/main/java/com/lecture/enrollment/entity/Enrollment.java`
- `enrollment-service/src/main/java/com/lecture/enrollment/repository/EnrollmentRepository.java`
- `enrollment-service/src/main/java/com/lecture/enrollment/service/CourseServiceClient.java`
- `enrollment-service/src/main/java/com/lecture/enrollment/service/PaymentServiceClient.java`
- `enrollment-service/src/main/java/com/lecture/enrollment/service/EnrollmentService.java`
- `enrollment-service/src/main/java/com/lecture/enrollment/service/EnrollmentWriteService.java`
- `enrollment-service/src/main/java/com/lecture/enrollment/kafka/KafkaEvent.java`
- `enrollment-service/src/main/java/com/lecture/enrollment/kafka/EnrollmentKafkaProducer.java`
- `enrollment-service/src/main/java/com/lecture/enrollment/kafka/EnrollmentKafkaConsumer.java`
- `enrollment-service/src/main/resources/application.yml`
- `enrollment-service/src/test/resources/application.yml`

### 변경 내용

- `Enrollment` 엔티티를 `orders` 테이블에 매핑했다.
- 주문에 다음 정보를 저장한다.
  - 클라이언트 ID
  - 종목 티커
  - 매수·매도 구분
  - 수량과 가격
  - 주문 상태
  - 심사 사유
  - 대체 종목 정보
- 기존 `/api/enrollments` 경로를 유지하고 `/api/orders` 별칭도 지원한다.
- 주문 접수 시 우선 `PENDING`으로 저장한다.
- `course-service`에서 현재 등급을 동기 조회해 즉시 상태를 반환한다.
- 제한 종목이면 `recommend-service`를 통해 대체 종목을 조회한다.
- 주문 접수 후 `order.received` 이벤트를 발행한다.
- `screening.completed` 이벤트를 소비해 최종 심사 결과를 반영한다.
- 동일 심사 결과가 중복 소비되어도 주문이 중복 처리되지 않도록 구성했다.

## 7. payment-service

기존 `Payment` 클래스와 구조를 유지하면서 샤리아 규칙 평가 엔진 및 감사 이력 서비스로 변경했다.

### 변경 파일

- `payment-service/build.gradle`
- `payment-service/src/main/java/com/lecture/payment/config/KafkaConfig.java`
- `payment-service/src/main/java/com/lecture/payment/config/WebClientConfig.java`
- `payment-service/src/main/java/com/lecture/payment/controller/PaymentController.java`
- `payment-service/src/main/java/com/lecture/payment/dto/PaymentDto.java`
- `payment-service/src/main/java/com/lecture/payment/entity/Payment.java`
- `payment-service/src/main/java/com/lecture/payment/repository/PaymentRepository.java`
- `payment-service/src/main/java/com/lecture/payment/evaluator/Evaluator.java`
- `payment-service/src/main/java/com/lecture/payment/evaluator/SectorComplianceEvaluator.java`
- `payment-service/src/main/java/com/lecture/payment/evaluator/FinancialRatioEvaluator.java`
- `payment-service/src/main/java/com/lecture/payment/service/CourseServiceClient.java`
- `payment-service/src/main/java/com/lecture/payment/service/PaymentService.java`
- `payment-service/src/main/java/com/lecture/payment/kafka/PaymentKafkaProducer.java`
- `payment-service/src/main/java/com/lecture/payment/kafka/PaymentKafkaConsumer.java`
- `payment-service/src/main/resources/application.yml`
- `payment-service/src/test/java/com/lecture/payment/evaluator/ScreeningEvaluatorTests.java`
- `payment-service/src/test/resources/application.yml`

### 변경 내용

- `Payment` 엔티티를 `screenings` 테이블에 매핑했다.
- 종목 심사와 주문 심사를 모두 감사 이력으로 저장한다.
- 규칙 추가가 가능하도록 `Evaluator` 인터페이스를 도입했다.
- `SectorComplianceEvaluator`에서 금지 업종을 먼저 판정한다.
- 금지 업종이면 재무비율 심사를 생략하고 `RESTRICTED`로 단락 평가한다.
- `FinancialRatioEvaluator`에 다음 AAOIFI 기준을 적용했다.

| 평가 항목 | 제한 기준 | WATCH 기준 |
|---|---:|---:|
| 부채 비율 | 30% 초과 | 제한값의 90% 이상 |
| 이자성 자산 비율 | 30% 초과 | 제한값의 90% 이상 |
| 비허용 수익 비율 | 5% 초과 | 제한값의 90% 이상 |

- `security.updated` 이벤트를 소비해 종목을 심사한다.
- `order.received` 이벤트를 소비해 매매주문을 심사한다.
- 심사 이후 `screening.completed` 이벤트를 발행한다.
- 심사 이력과 상세 결과 조회 API를 추가했다.
- 적합, 경계, 제한, 금지 업종 판정을 검증하는 단위 테스트를 추가했다.

## 8. recommend-service

기존 FastAPI 추천 서비스를 유지하면서 샤리아 적합 대체 종목 추천과 이상 탐지 기능을 추가했다.

### 변경 파일

- `recommend-service/.env`
- `recommend-service/app/client/course_client.py`
- `recommend-service/app/client/enrollment_client.py`
- `recommend-service/app/config/settings.py`
- `recommend-service/app/kafka/consumer.py`
- `recommend-service/app/model/schemas.py`
- `recommend-service/app/router/alternative_router.py`
- `recommend-service/app/router/anomaly_router.py`
- `recommend-service/app/router/recommend_router.py`
- `recommend-service/app/service/alternative_service.py`
- `recommend-service/app/service/anomaly_service.py`
- `recommend-service/app/service/recommend_service.py`
- `recommend-service/main.py`

### 변경 내용

- 제한 종목과 동일 업종에 속하는 `COMPLIANT` 종목만 대체 후보로 선택한다.
- 원본 종목과 시가총액 비율이 유사한 순서로 후보를 정렬한다.
- `screening.completed` 이벤트를 소비한다.
- 종목 등급 변경과 `WATCH` 경계 상태를 이상 징후로 탐지한다.
- 이상 징후 발견 시 `anomaly.detected` 이벤트를 발행한다.
- 기존 추천 API와 샤리아 대체 종목 API가 함께 동작하도록 정적 라우터 등록 순서를 조정했다.
- Gateway 호환 경로와 내부 서비스 직접 호출 경로를 모두 지원한다.

## 9. Vue 프런트엔드

기존 강좌 UI의 파일명과 라우터 구조를 최대한 유지하면서 샤리아 투자 적합성 심사 콘솔로 변경했다.

### 변경 파일

- `vue-frontend/index.html`
- `vue-frontend/src/api/auth.js`
- `vue-frontend/src/api/course.js`
- `vue-frontend/src/api/enrollment.js`
- `vue-frontend/src/assets/styles/global.css`
- `vue-frontend/src/components/AppHeader.vue`
- `vue-frontend/src/components/CourseCard.vue`
- `vue-frontend/src/router/index.js`
- `vue-frontend/src/store/course.js`
- `vue-frontend/src/views/LandingView.vue`
- `vue-frontend/src/views/LoginView.vue`
- `vue-frontend/src/views/CourseListView.vue`
- `vue-frontend/src/views/CourseDetailView.vue`
- `vue-frontend/src/views/CourseCreateView.vue`
- `vue-frontend/src/views/EnrollmentView.vue`
- `vue-frontend/src/views/MyPageView.vue`

### 변경 내용

- 메인 화면을 샤리아 투자 심사 서비스 소개 화면으로 변경했다.
- 종목 목록을 등급별로 확인할 수 있는 대시보드를 구현했다.
- 종목 상세 화면에 재무비율, 기준값과 판정 사유를 표시한다.
- 종목 상세 화면에서 매수·매도 주문을 입력할 수 있도록 했다.
- 주문 결과를 `APPROVED`, `WARNED`, `HELD`로 표시한다.
- 제한된 주문에는 추천 대체 종목을 표시한다.
- 주문 및 심사 이력 화면을 구현했다.
- 기관 정보, API Key 재발급과 이상 탐지 내역을 마이페이지에 표시한다.
- 샘플 데이터가 아닌 실제 백엔드 API를 호출하도록 Axios 연동을 변경했다.
- 기존 OAuth Authorization Code 및 JWT 로그인 흐름은 유지했다.

## 10. Kafka 토픽

| 토픽 | 발행 서비스 | 소비 서비스 | 용도 |
|---|---|---|---|
| `security.updated` | `course-service` | `payment-service` | 종목 재무정보 갱신 및 재심사 요청 |
| `order.received` | `enrollment-service` | `payment-service` | 신규 매매주문 심사 요청 |
| `screening.completed` | `payment-service` | `course-service`, `enrollment-service`, `recommend-service` | 심사 결과 전파 |
| `anomaly.detected` | `recommend-service` | `course-service` | 등급 변경 및 경계 상태 알림 |

## 11. 유지한 기존 구조

- 기존 마이크로서비스 이름
- Controller-Service-Repository 계층
- Eureka 서비스 디스커버리
- API Gateway 라우팅
- OAuth Authorization Code 및 JWT 인증
- Kafka Producer/Consumer 방식
- Docker Compose 실행 방식
- 기존 서비스 포트와 네트워크
- `Course`, `Enrollment`, `Payment` 등 핵심 클래스 이름
- Gateway가 사용하는 `/api/courses`, `/api/enrollments`, `/api/payments`, `/api/recommend` 경로

## 12. 검증 결과

### 빌드 및 단위 테스트

- `eureka-server`: Gradle 테스트 통과
- `user-service`: Gradle 테스트 통과
- `course-service`: Gradle 테스트 통과
- `enrollment-service`: Gradle 테스트 통과
- `payment-service`: Gradle 테스트 및 심사 규칙 단위 테스트 통과
- `recommend-service`: Python 구문 컴파일 검사 통과
- `vue-frontend`: `npm run build` 통과
- `docker-compose.yml`: Compose 설정 검증 통과
- `docker-compose.build.yml`: Compose 설정 검증 통과

### Docker 연동 검증

- MariaDB, Kafka, Eureka, 인증 서버, Gateway 및 업무 서비스 기동 확인
- `security.updated` → 심사 → `screening.completed` 종목 일괄 심사 흐름 확인
- `AAPL` 주문이 `APPROVED`로 처리되는 것 확인
- `BORDER` 주문이 `WARNED`로 처리되는 것 확인
- `DEBTX` 주문이 `HELD`로 처리되는 것 확인
- 제한 주문에 `TSM`, `MSFT`, `AAPL` 대체 종목이 반환되는 것 확인
- 주문 심사 결과가 `screenings` 감사 이력에 저장되는 것 확인
- `WATCH` 경계 종목에 대한 이상 탐지 결과 확인

## 13. 참고 사항

- 현재 프로젝트 폴더에는 Git 메타데이터가 없어 원본 대비 `git diff`를 생성할 수 없다.
- 이 문서는 실제 수정 또는 추가한 소스 파일을 기준으로 작성했다.
- `build/`, `bin/`, `.gradle/`, `node_modules/`, `__pycache__/` 등의 생성 파일은 변경 파일 목록에서 제외했다.
- 기존 데이터가 들어 있는 Docker 볼륨을 삭제하려면 별도 확인이 필요하다. 일반 종료 시에는 `docker compose down`을 사용하고, 데이터 보존이 필요하면 `-v` 옵션을 사용하지 않는다.
