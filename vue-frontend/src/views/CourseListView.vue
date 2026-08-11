<template>
  <div class="page">
    <AppHeader />
    <main class="shell">
      <section class="hero-row">
        <div>
          <span class="eyebrow">ISLAMIC FINANCE · MODEL SUBSCRIPTIONS</span>
          <h1>샤리아·할랄 모델 구독</h1>
          <p>은행·증권사가 선택한 스크리닝 방법론을 구독하고, 종목·주문 API에 적용합니다.</p>
        </div>
      </section>

      <div class="summary-grid">
        <article v-for="item in summaries" :key="item.key" class="summary" :class="`summary-${item.key.toLowerCase()}`">
          <span>{{ item.label }}</span><strong>{{ item.value }}</strong><small>{{ item.note }}</small>
        </article>
      </div>

      <section class="toolbar">
        <div class="filter-group">
          <button v-for="item in filters" :key="item.key" :class="['chip', { active: selected === item.key }]" @click="selected = item.key">
            {{ item.label }} <b>{{ count(item.key) }}</b>
          </button>
        </div>
        <input v-model.trim="query" class="search" placeholder="모델명 또는 방법론 코드 검색" />
      </section>

      <p v-if="error" class="error">{{ error }}</p>
      <div v-if="filtered.length" class="security-grid">
        <CourseCard v-for="model in filtered" :key="model.id" :course="model" :subscription="subscriptionFor(model.ticker)" />
      </div>
      <div v-else class="state">조건에 맞는 모델이 없습니다.</div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import CourseCard from '@/components/CourseCard.vue'
import { enrollmentApi } from '@/api/enrollment.js'
import { detectorFor, modelCatalog } from '@/utils/detectors.js'

const selected = ref('ALL')
const query = ref('')
const subscriptions = ref([])
const error = ref('')
const filters = [
  { key: 'ALL', label: '전체' },
  { key: 'NOT_SUBSCRIBED', label: '미구독' },
  { key: 'ACTIVE', label: '구독 중' },
  { key: 'PAUSED', label: '일시 중지' },
]
const subscriptionFor = ticker => subscriptions.value.find(item => item.ticker === ticker && item.status !== 'CANCELLED')
const count = status => status === 'ALL' ? modelCatalog.length : modelCatalog.filter(item => {
  const subscription = subscriptionFor(item.ticker)
  return status === 'NOT_SUBSCRIBED' ? !subscription : subscription?.status === status
}).length
const summaries = computed(() => [
  { key: 'ACTIVE', label: '구독 중', value: count('ACTIVE'), note: '종목·주문 API에 적용' },
  { key: 'PAUSED', label: '일시 중지', value: count('PAUSED'), note: '판정 처리 미적용' },
  { key: 'NOT_SUBSCRIBED', label: '미구독', value: count('NOT_SUBSCRIBED'), note: '기관별 즉시 적용 가능' },
  { key: 'ALL', label: '전체 모델', value: count('ALL'), note: '정책 버전 관리 지원' },
])
const filtered = computed(() => modelCatalog.filter(item => {
  const subscription = subscriptionFor(item.ticker)
  const statusMatch = selected.value === 'ALL'
    || (selected.value === 'NOT_SUBSCRIBED' ? !subscription : subscription?.status === selected.value)
  const detector = detectorFor(item)
  const text = `${detector.code} ${detector.name} ${detector.type}`.toLowerCase()
  return statusMatch && text.includes(query.value.toLowerCase())
}))

onMounted(async () => {
  try {
    const response = await enrollmentApi.getMySubscriptions()
    subscriptions.value = response.data?.data ?? response.data ?? []
  } catch (cause) {
    error.value = cause.response?.data?.message || '구독 상태를 불러오지 못했습니다.'
  }
})
</script>

<style scoped>
.page { min-height: 100vh; background: var(--color-bg-secondary); }
.shell { max-width: 1240px; margin: 0 auto; padding: 38px 24px 64px; }
.hero-row { display: flex; justify-content: space-between; gap: 24px; align-items: flex-end; margin-bottom: 26px; }
.eyebrow { color: var(--color-primary); font-size: 10px; font-weight: 800; letter-spacing: .13em; }
h1 { margin-top: 7px; font-size: 28px; letter-spacing: -.5px; }
.hero-row p { margin-top: 7px; color: var(--color-text-secondary); font-size: 13px; }
.actions { display: flex; gap: 8px; }
.summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 24px; }
.summary { padding: 17px 18px; background: white; border: 1px solid var(--color-border); border-radius: 12px; }
.summary span, .summary small { display: block; color: var(--color-text-muted); font-size: 10px; }
.summary strong { display: block; margin: 4px 0; font-size: 27px; }
.summary-compliant { border-top: 3px solid #12b886; }.summary-watch { border-top: 3px solid #f59f00; }.summary-restricted { border-top: 3px solid #fa5252; }
.toolbar { display: flex; justify-content: space-between; align-items: center; gap: 16px; margin-bottom: 18px; }
.filter-group { display: flex; gap: 7px; flex-wrap: wrap; }.chip { padding: 7px 12px; border: 1px solid var(--color-border); border-radius: 20px; background: white; color: var(--color-text-secondary); }.chip b { margin-left: 4px; }.chip.active { background: var(--color-primary); color: white; border-color: var(--color-primary); }
.search { width: 260px; padding: 9px 12px; border: 1px solid var(--color-border); border-radius: 9px; outline: none; }.search:focus { border-color: var(--color-primary); }
.security-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; }.state { padding: 80px; text-align: center; color: var(--color-text-muted); }.notice,.error { margin-bottom: 16px; padding: 11px 14px; border-radius: 9px; font-size: 12px; }.notice { background: var(--color-primary-light); color: var(--color-primary-dark); }.error { background: #ffe3e3; color: #c92a2a; }
@media (max-width: 900px) { .summary-grid,.security-grid { grid-template-columns: repeat(2, 1fr); } .hero-row,.toolbar { align-items: stretch; flex-direction: column; }.search { width: 100%; } }
@media (max-width: 560px) { .summary-grid,.security-grid { grid-template-columns: 1fr; } }
</style>
