const modelDefinitions = {
  AAOIFI_CORE: {
    name: 'AAOIFI 기반 샤리아 스크리닝', type: '기본 적합성 심사',
    description: '사업 활동과 재무비율을 함께 확인해 종목·주문의 샤리아 적합성을 판정합니다.',
    input: '발행사 사업 분류 · 재무제표 · 주문 종목', basis: 'AAOIFI Shari’ah Standard No. 21 프로필',
  },
  HALAL_ACTIVITY: {
    name: '할랄 사업활동 탐지', type: '사업활동 심사',
    description: '알코올·도박·돼지고기·전통 금융 등 비허용 사업활동 노출을 식별합니다.',
    input: '업종 코드 · 사업 설명 · 매출 구성', basis: 'MSCI·DJIM 사업활동 제외 기준 프로필',
  },
  FINANCIAL_THRESHOLD: {
    name: '재무비율 한도 탐지', type: '재무 심사',
    description: '부채, 이자성 자산, 비허용 수익 비율이 기관이 승인한 한도에 근접하거나 초과하는지 탐지합니다.',
    input: '재무제표 · 시가총액 · 승인 임계치', basis: 'MSCI·DJIM 재무비율 및 기관 승인 한도',
  },
}

// 종목 API와 독립된 구독 모델 카탈로그. ticker 필드는 기존 Enrollment 저장소와의 호환용 모델 코드다.
export const modelCatalog = Object.entries(modelDefinitions).map(([code, model], index) => ({
  id: index + 1,
  ticker: code,
  code,
  ...model,
}))

export function detectorFor(model = {}) {
  const code = model.code || model.ticker
  if (modelDefinitions[code]) {
    return { code, ...modelDefinitions[code] }
  }
  return {
    code: code || 'CUSTOM_POLICY',
    name: model.name || '맞춤 샤리아·할랄 모델',
    type: model.type || '맞춤 정책',
    description: model.description || '기관이 승인한 샤리아·할랄 적합성 기준을 적용합니다.',
    input: model.input || '기관 정책 · 종목 마스터 · 주문 API',
    basis: model.basis || '기관 승인 정책 버전',
  }
}

export const subscriptionLabel = status => ({
  ACTIVE: '구독 중',
  PAUSED: '일시 중지',
  CANCELLED: '해지됨',
}[status] || '미구독')
