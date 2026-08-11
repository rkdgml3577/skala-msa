-- 금융기관 거래 이상 탐지·시나리오 구독 서비스 초기 DDL
-- 기존 MSA 클래스명(Course, Enrollment, Payment)은 호환성을 위해 유지한다.

CREATE TABLE IF NOT EXISTS users (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    email              VARCHAR(255) NOT NULL UNIQUE,
    password           VARCHAR(255) NOT NULL,
    name               VARCHAR(255) NOT NULL,
    organization_name  VARCHAR(255),
    client_code        VARCHAR(40) UNIQUE,
    api_key_hash       VARCHAR(255),
    active             BOOLEAN      NOT NULL DEFAULT TRUE,
    role               VARCHAR(20)  NOT NULL COMMENT 'STUDENT=BANK_ANALYST | INSTRUCTOR=BANK_ADMIN',
    created_at         DATETIME(6),
    updated_at         DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS securities (
    id                           BIGINT          NOT NULL AUTO_INCREMENT,
    ticker                       VARCHAR(20)     NOT NULL UNIQUE,
    name                         VARCHAR(255)    NOT NULL,
    description                  TEXT,
    sector                       VARCHAR(50)     NOT NULL,
    last_price                   DECIMAL(19,4)   NOT NULL,
    market_cap                   DECIMAL(24,2)   NOT NULL,
    exchange_id                  BIGINT          NOT NULL DEFAULT 0,
    screening_count              INT             NOT NULL DEFAULT 0,
    grade                        VARCHAR(20)     NOT NULL COMMENT 'COMPLIANT | WATCH | RESTRICTED',
    interest_bearing_debt        DECIMAL(24,2)   NOT NULL DEFAULT 0,
    interest_earning_assets      DECIMAL(24,2)   NOT NULL DEFAULT 0,
    non_permissible_income       DECIMAL(24,2)   NOT NULL DEFAULT 0,
    total_revenue                DECIMAL(24,2)   NOT NULL DEFAULT 0,
    screening_reason             TEXT,
    last_screened_at             DATETIME(6),
    rule_version_id              VARCHAR(50),
    created_at                   DATETIME(6),
    updated_at                   DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_securities_sector_grade (sector, grade)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS orders (
    id                   BIGINT          NOT NULL AUTO_INCREMENT,
    client_id            BIGINT          NOT NULL,
    ticker               VARCHAR(20)     NOT NULL,
    side                 VARCHAR(10)     NOT NULL COMMENT 'BUY | SELL',
    quantity             DECIMAL(19,4)   NOT NULL,
    order_price          DECIMAL(19,4)   NOT NULL,
    status               VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '주문 호환: PENDING|APPROVED|WARNED|HELD; 구독: ACTIVE|PAUSED|CANCELLED',
    screening_reason     TEXT,
    alternative_tickers  TEXT,
    created_at           DATETIME(6),
    updated_at           DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_orders_client_created (client_id, created_at),
    INDEX idx_orders_ticker (ticker),
    INDEX idx_orders_subscription_lookup (client_id, ticker, status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS screenings (
    id                              BIGINT         NOT NULL AUTO_INCREMENT,
    screening_type                  VARCHAR(20)    NOT NULL COMMENT 'SECURITY | ORDER',
    reference_id                    VARCHAR(100)   NOT NULL,
    order_id                        BIGINT,
    client_id                       BIGINT,
    ticker                          VARCHAR(20)    NOT NULL,
    grade                           VARCHAR(20)    NOT NULL COMMENT 'COMPLIANT | WATCH | RESTRICTED',
    reason                          TEXT,
    debt_ratio                      DECIMAL(12,8),
    interest_asset_ratio            DECIMAL(12,8),
    non_permissible_income_ratio    DECIMAL(12,8),
    rule_version_id                 VARCHAR(50)    NOT NULL,
    created_at                      DATETIME(6),
    updated_at                      DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_screenings_ticker_created (ticker, created_at),
    INDEX idx_screenings_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- AI 모듈의 비동기 판정 결과. 은행사(client_id)별 감사 화면에서 조회한다.
CREATE TABLE IF NOT EXISTS ai_screening_results (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    transaction_id  VARCHAR(100) NOT NULL,
    client_id       BIGINT       NOT NULL,
    client_code     VARCHAR(40),
    model_code      VARCHAR(50)  NOT NULL,
    ticker          VARCHAR(30)  NOT NULL,
    status          VARCHAR(20)  NOT NULL COMMENT 'CLEAR | ALERT | REVIEW',
    reason          TEXT,
    rule_version    VARCHAR(80),
    created_at      DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_ai_results_client_created (client_id, created_at),
    INDEX idx_ai_results_transaction (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sprint 1 데모용 목 시세·재무데이터. 외부 API 대신 scheduler가 이 스냅샷을 재심사한다.
INSERT IGNORE INTO securities (
    ticker, name, description, sector, last_price, market_cap, exchange_id,
    screening_count, grade, interest_bearing_debt, interest_earning_assets,
    non_permissible_income, total_revenue, screening_reason, last_screened_at,
    rule_version_id, created_at, updated_at
) VALUES
('AAPL', 'Apple Inc.', '소비자 전자기기와 소프트웨어 서비스 사업', 'TECHNOLOGY', 226.40, 3400000000000, 1,
 1, 'COMPLIANT', 101000000000, 65000000000, 950000000, 391000000000,
 '금지 업종 및 AAOIFI 재무비율 3종을 통과했습니다.', NOW(), 'AAOIFI-SS21-2026.1', NOW(), NOW()),
('MSFT', 'Microsoft Corp.', '클라우드·소프트웨어·AI 플랫폼 사업', 'TECHNOLOGY', 418.79, 3110000000000, 1,
 1, 'COMPLIANT', 79000000000, 75000000000, 600000000, 245000000000,
 '금지 업종 및 AAOIFI 재무비율 3종을 통과했습니다.', NOW(), 'AAOIFI-SS21-2026.1', NOW(), NOW()),
('TSM', 'Taiwan Semiconductor', '반도체 위탁 생산 사업', 'TECHNOLOGY', 196.32, 1010000000000, 1,
 1, 'COMPLIANT', 31000000000, 22000000000, 300000000, 72000000000,
 '금지 업종 및 AAOIFI 재무비율 3종을 통과했습니다.', NOW(), 'AAOIFI-SS21-2026.1', NOW(), NOW()),
('DEBTX', 'Demo Leveraged Tech', 'AAOIFI 부채 기준 초과를 보여주는 기술기업 데모 종목', 'TECHNOLOGY', 42.10, 100000000000, 1,
 1, 'RESTRICTED', 35000000000, 4000000000, 50000000, 12000000000,
 '이자부 부채 비율 35.00%로 AAOIFI 30% 한도를 초과했습니다.', NOW(), 'AAOIFI-SS21-2026.1', NOW(), NOW()),
('BORDER', 'Demo Borderline Systems', '재무비율 경계값 근접을 보여주는 데모 종목', 'TECHNOLOGY', 73.50, 100000000000, 1,
 1, 'WATCH', 28000000000, 12000000000, 420000000, 10000000000,
 '이자부 부채 비율이 한도의 90% 이상에 근접했습니다.', NOW(), 'AAOIFI-SS21-2026.1', NOW(), NOW()),
('SUKK', 'Sukuk Finance Demo', '수쿠크 기반 이슬람 금융 서비스 데모 종목', 'ISLAMIC_FINANCE', 31.25, 38000000000, 2,
 1, 'COMPLIANT', 4000000000, 3000000000, 30000000, 4800000000,
 '이슬람 금융 업종이며 재무비율 기준을 통과했습니다.', NOW(), 'AAOIFI-SS21-2026.1', NOW(), NOW()),
('MEDIAW', 'Demo Media Group', '전통 미디어 사업으로 중요성 개별 검토 대상', 'MEDIA', 18.90, 12000000000, 1,
 1, 'WATCH', 1000000000, 600000000, 70000000, 4100000000,
 '전통 미디어는 샤리아 위원회의 중요성 개별 심사가 필요합니다.', NOW(), 'AAOIFI-SS21-2026.1', NOW(), NOW()),
('CITI', 'Conventional Bank Demo', '이자 기반 전통 금융업 데모 종목', 'CONVENTIONAL_FINANCE', 68.10, 145000000000, 1,
 1, 'RESTRICTED', 20000000000, 90000000000, 12000000000, 85000000000,
 '이자 기반 전통 금융 금지 업종에 해당합니다.', NOW(), 'AAOIFI-SS21-2026.1', NOW(), NOW());
