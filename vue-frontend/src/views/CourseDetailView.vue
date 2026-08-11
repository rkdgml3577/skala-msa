<template>
  <div class="page">
    <AppHeader />
    <main class="shell">
      <router-link to="/subscriptions" class="back">← 모델 카탈로그</router-link>
      <div v-if="model" class="layout">
        <section>
          <div class="detector-head">
            <div><span class="code">{{ detector.code }}</span><h1>{{ detector.name }}</h1><p>{{ detector.description }}</p></div>
            <span class="status" :class="`status-${subscription?.status?.toLowerCase() || 'none'}`">{{ subscriptionLabel(subscription?.status) }}</span>
          </div>
          <article class="panel">
            <div class="panel-title"><h2>모델 방법론 안내</h2><span>POLICY PROFILE</span></div>
            <p class="reason">이 모델은 기관이 연결한 종목·주문 API에 승인된 샤리아·할랄 기준을 적용합니다. 최종 기준과 임계치는 기관의 샤리아 위원회가 승인해야 합니다.</p>
            <div class="info-list">
              <div><span>모델 유형</span><strong>{{ detector.type }}</strong></div>
              <div><span>입력 데이터</span><strong>{{ detector.input }}</strong></div>
              <div><span>방법론 근거</span><strong>{{ detector.basis }}</strong></div>
              <div><span>판정 결과</span><strong>적합 · 주의 · 제한</strong></div>
            </div>
          </article>
        </section>

        <aside class="subscription-card">
          <span class="eyebrow">MODEL SUBSCRIPTION</span>
          <h2>{{ subscription ? '모델 구독 상태 관리' : '이 모델 구독' }}</h2>
          <p>{{ subscription ? 'ACTIVE 상태일 때만 이 은행사의 종목·주문 API에 모델을 적용합니다.' : '구독하면 승인된 샤리아·할랄 기준으로 종목과 주문을 판정합니다.' }}</p>
          <button v-if="!subscription" class="btn btn-primary submit" :disabled="submitting" @click="subscribe">{{ submitting ? '구독 처리 중…' : '모델 구독하기' }}</button>
          <div v-else class="subscription-actions">
            <span class="current-status" :class="`status-${subscription.status.toLowerCase()}`">{{ subscriptionLabel(subscription.status) }}</span>
            <button v-if="subscription.status === 'ACTIVE'" class="btn btn-outline submit" :disabled="submitting" @click="changeStatus('PAUSED')">일시 중지</button>
            <button v-else class="btn btn-primary submit" :disabled="submitting" @click="changeStatus('ACTIVE')">다시 시작</button>
            <button class="text-action" :disabled="submitting" @click="changeStatus('CANCELLED')">구독 해지</button>
          </div>
          <div v-if="error" class="error">{{ error }}</div>
          <div v-if="notice" class="notice"><strong>{{ notice }}</strong><router-link to="/detections">탐지 결과 확인 →</router-link></div>
        </aside>
      </div>
      <div v-else class="state">모델을 찾을 수 없습니다.</div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { enrollmentApi } from '@/api/enrollment.js'
import { detectorFor, modelCatalog, subscriptionLabel } from '@/utils/detectors.js'

const route = useRoute()
const model = computed(() => modelCatalog.find(item => item.id === Number(route.params.id)))
const detector = computed(() => detectorFor(model.value))
const subscription = ref(null)
const submitting = ref(false)
const error = ref('')
const notice = ref('')

async function loadSubscription() {
  const response = await enrollmentApi.getMySubscriptions()
  const subscriptions = response.data?.data ?? response.data ?? []
  subscription.value = subscriptions.find(item => item.ticker === model.value?.ticker && item.status !== 'CANCELLED') || null
}

async function subscribe() {
  submitting.value = true; error.value = ''; notice.value = ''
  try {
    const response = await enrollmentApi.subscribe({ ticker: model.value.ticker })
    subscription.value = response.data?.data ?? response.data
    notice.value = '모델 구독이 시작되었습니다.'
  } catch (cause) { error.value = cause.response?.data?.message || '모델 구독에 실패했습니다.' }
  finally { submitting.value = false }
}

async function changeStatus(status) {
  submitting.value = true; error.value = ''; notice.value = ''
  try {
    await enrollmentApi.changeSubscriptionStatus(subscription.value.id, status)
    subscription.value = status === 'CANCELLED' ? null : { ...subscription.value, status }
    notice.value = ({ ACTIVE: '구독을 다시 시작했습니다.', PAUSED: '구독을 일시 중지했습니다.', CANCELLED: '구독을 해지했습니다.' })[status]
  } catch (cause) { error.value = cause.response?.data?.message || '구독 상태 변경에 실패했습니다.' }
  finally { submitting.value = false }
}

onMounted(loadSubscription)
</script>

<style scoped>
.page{min-height:100vh;background:var(--color-bg-secondary)}.shell{max-width:1180px;margin:auto;padding:30px 24px 64px}.back{display:inline-block;margin-bottom:20px;color:var(--color-text-secondary);font-size:12px}.layout{display:grid;grid-template-columns:minmax(0,1fr) 360px;gap:24px;align-items:start}.detector-head{display:flex;justify-content:space-between;gap:20px;padding:28px;color:white;background:linear-gradient(135deg,#064e3b,#0f8a63);border-radius:16px}.code,.eyebrow{font-size:10px;font-weight:800;letter-spacing:.14em}.detector-head h1{margin:5px 0 9px;font-size:28px}.detector-head p{max-width:580px;color:rgba(255,255,255,.75);font-size:13px}.status{align-self:flex-start;padding:6px 11px;border-radius:20px;background:white;font-size:11px;font-weight:700}.status-active{color:#087f5b;background:#dff7ec}.status-paused{color:#9a6700;background:#fff3bf}.status-none{color:#495057;background:#e9ecef}.panel,.subscription-card{padding:24px;background:white;border:1px solid var(--color-border);border-radius:14px}.panel{margin-top:18px}.subscription-card{position:sticky;top:88px}.panel-title{display:flex;justify-content:space-between;gap:10px}.panel-title h2,.subscription-card h2{font-size:17px}.panel-title span{color:var(--color-text-muted);font-size:10px}.reason{margin:12px 0 20px;padding:12px;background:var(--color-bg-secondary);border-radius:8px;color:var(--color-text-secondary);font-size:12px}.info-list{display:grid;grid-template-columns:repeat(2,1fr);gap:10px}.info-list div{padding:12px;background:var(--color-bg-secondary);border-radius:8px}.info-list span,.info-list strong{display:block;font-size:10px}.info-list span{color:var(--color-text-muted)}.info-list strong{margin-top:3px}.subscription-card>.eyebrow{color:var(--color-primary)}.subscription-card>p{margin:7px 0 18px;color:var(--color-text-secondary);font-size:12px;line-height:1.5}.submit{width:100%;justify-content:center}.subscription-actions{display:flex;flex-direction:column;gap:9px}.current-status{align-self:flex-start;padding:5px 9px;border-radius:14px;font-size:11px;font-weight:700}.text-action{padding:4px 0;border:0;background:transparent;color:#c92a2a;font-size:11px;text-align:left;cursor:pointer}.error,.notice{margin-top:16px;padding:14px;border-radius:10px;font-size:12px}.error{background:#ffe3e3;color:#c92a2a}.notice{background:#dff7ec;color:#087f5b}.notice strong,.notice a{display:block}.notice a{margin-top:8px;font-weight:700}.state{padding:80px;text-align:center;color:var(--color-text-muted)}@media(max-width:880px){.layout{grid-template-columns:1fr}.subscription-card{position:static}}@media(max-width:560px){.info-list{grid-template-columns:1fr}}
</style>
