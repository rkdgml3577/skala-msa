<template>
  <div class="page">
    <AppHeader />
    <main class="shell">
      <div class="head">
        <div>
          <span>COMPLIANCE OPERATIONS</span>
          <h1>탐지 결과</h1>
          <p>이 은행사가 전송한 거래에 구독 모델을 적용한 적합성 판정 이력입니다.</p>
        </div>
        <router-link to="/subscriptions" class="btn btn-primary">모델 구독 관리</router-link>
      </div>

      <section class="result-card">
        <div class="card-title">
          <div><h2>AI 적합성 판정 이력</h2><p>Kafka 이벤트 처리 후 저장된 최신 100건</p></div>
          <b>{{ results.length }}</b>
        </div>

        <div v-if="loading" class="state">판정 결과를 불러오는 중…</div>
        <div v-else-if="loadError" class="state error-state">
          판정 결과를 불러오지 못했습니다.
          <button class="btn btn-outline" @click="loadResults">다시 시도</button>
        </div>
        <div v-else-if="results.length" class="result-list">
          <article v-for="result in results" :key="result.id" class="result-row">
            <div class="ticker"><strong>{{ result.ticker }}</strong><small>{{ result.transactionId }}</small></div>
            <div class="model"><span>{{ modelName(result.modelCode) }}</span><small>{{ result.modelCode }}</small></div>
            <span class="status" :class="statusClass(result.status)">{{ statusLabel(result.status) }}</span>
            <p>{{ result.reason }}</p>
            <time>{{ date(result.createdAt) }}</time>
          </article>
        </div>
        <div v-else class="state">
          <span class="empty-icon">◌</span>
          <h2>아직 판정 결과가 없습니다</h2>
          <p>연동 관리에서 발급한 API 키로 거래를 전송하면, 활성 구독 모델의 판정 결과가 이곳에 기록됩니다.</p>
          <router-link to="/mypage" class="btn btn-outline">API 연동 정보 확인</router-link>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import { authApi } from '@/api/auth.js'

const results = ref([])
const loading = ref(true)
const loadError = ref(false)

const modelNames = {
  AAOIFI_CORE: 'AAOIFI 샤리아 기준',
  HALAL_ACTIVITY: '할랄 사업활동',
  FINANCIAL_THRESHOLD: '재무비율 기준'
}

const modelName = code => modelNames[code] || code
const statusLabel = status => ({ CLEAR: '적합', ALERT: '경고', REVIEW: '검토 필요' })[status] || status
const statusClass = status => ({ CLEAR: 'clear', ALERT: 'alert', REVIEW: 'review' })[status] || 'review'
const date = value => value ? new Date(value).toLocaleString('ko-KR') : '-'

async function loadResults() {
  loading.value = true
  loadError.value = false
  try {
    const response = await authApi.getMyAiResults()
    results.value = response.data?.data || []
  } catch (error) {
    results.value = []
    loadError.value = true
  } finally {
    loading.value = false
  }
}

onMounted(loadResults)
</script>

<style scoped>
.page{min-height:100vh;background:var(--color-bg-secondary)}.shell{max-width:1120px;margin:auto;padding:38px 24px 64px}.head{display:flex;justify-content:space-between;align-items:flex-end;gap:20px}.head span{color:var(--color-primary);font-size:10px;font-weight:800;letter-spacing:.13em}.head h1{margin:6px 0;font-size:27px}.head p,.card-title p{color:var(--color-text-secondary);font-size:13px}.result-card{margin-top:25px;padding:22px;background:white;border:1px solid var(--color-border);border-radius:14px}.card-title{display:flex;justify-content:space-between;align-items:flex-start;gap:16px}.card-title h2{font-size:16px}.card-title b{display:grid;place-items:center;min-width:30px;height:30px;border-radius:50%;background:var(--color-primary-light);color:var(--color-primary);font-size:11px}.result-list{margin-top:18px;border-top:1px solid var(--color-border)}.result-row{display:grid;grid-template-columns:1fr 1.35fr auto 2fr auto;align-items:center;gap:16px;padding:15px 4px;border-bottom:1px solid var(--color-border)}.ticker strong,.model span{display:block;font-size:13px}.ticker small,.model small,time{display:block;margin-top:3px;color:var(--color-text-muted);font-size:10px}.result-row p{margin:0;color:var(--color-text-secondary);font-size:11px;line-height:1.45}.status{padding:5px 8px;border-radius:12px;font-size:10px;font-weight:800;white-space:nowrap}.status.clear{background:#d3f9d8;color:#087f23}.status.alert{background:#ffe3e3;color:#c92a2a}.status.review{background:#fff3bf;color:#9a6700}.state{display:flex;min-height:245px;flex-direction:column;align-items:center;justify-content:center;gap:12px;text-align:center;color:var(--color-text-muted);font-size:12px}.state h2{margin:0;color:var(--color-text);font-size:17px}.state p{max-width:500px;margin:0;line-height:1.65}.empty-icon{display:grid;place-items:center;width:46px;height:46px;border-radius:50%;background:var(--color-primary-light);color:var(--color-primary);font-size:25px}.error-state{color:#c92a2a}.error-state .btn{margin-top:4px}@media(max-width:800px){.head{align-items:stretch;flex-direction:column}.result-row{grid-template-columns:1fr auto;gap:8px}.result-row .model,.result-row p,.result-row time{grid-column:1/-1}.result-row p{margin-top:3px}}
</style>
