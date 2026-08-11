<template>
  <div class="page">
    <AppHeader />
    <main class="shell">
      <router-link to="/subscriptions" class="back">← 탐지 시나리오 목록</router-link>
      <div class="head"><span>DETECTION CATALOG</span><h1>맞춤 탐지 시나리오 등록</h1><p>은행사에 노출할 탐지 시나리오의 기본 정보를 등록합니다. 세부 AI 모델은 추후 이 코드에 연결합니다.</p></div>
      <form class="form-card" @submit.prevent="submit">
        <section><h2>시나리오 정보</h2><div class="grid">
          <label>탐지 코드<input v-model.trim="form.ticker" placeholder="FOREIGN_TRANSFER" required maxlength="20" /></label>
          <label>시나리오명<input v-model.trim="form.name" placeholder="해외 송금 이상 탐지" required /></label>
          <label>탐지 유형<select v-model="form.sector"><option v-for="sector in sectors" :key="sector" :value="sector">{{ sector }}</option></select></label>
          <label>입력 데이터<input v-model.trim="inputData" placeholder="금액, 국가, 거래 시각" required /></label>
          <label class="wide">탐지 설명<textarea v-model="form.description" rows="4" placeholder="어떤 거래를 이상 징후로 판단하는지 설명하세요." required></textarea></label>
        </div></section>
        <div class="hint"><strong>구독 상태는 은행사별로 관리됩니다.</strong> 등록 후 은행사는 이 시나리오를 ACTIVE, PAUSED, CANCELLED 상태로 관리할 수 있습니다.</div>
        <p v-if="error" class="error">{{ error }}</p>
        <div class="buttons"><router-link to="/subscriptions" class="btn btn-ghost">취소</router-link><button class="btn btn-primary" :disabled="loading">{{ loading ? '등록 중…' : '시나리오 등록' }}</button></div>
      </form>
    </main>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { courseApi } from '@/api/course.js'

const router = useRouter(), loading = ref(false), error = ref(''), inputData = ref('금액, 거래 시각, 채널')
const sectors = ['거래 금액', '거래 빈도', '거래 상대방', '계좌 활동', '행동 패턴', '컴플라이언스', '맞춤 규칙']
const form = reactive({ ticker:'', name:'', description:'', sector:'맞춤 규칙' })
async function submit() {
  loading.value = true; error.value = ''
  try {
    await courseApi.create({
      ...form,
      // 기존 Course API의 필수 재무 필드를 채우는 호환용 값이며, 구독 화면에는 노출하지 않는다.
      sector: 'OTHER', lastPrice: 1, marketCap: 1, exchangeId: 0,
      interestBearingDebt: 0, interestEarningAssets: 0, nonPermissibleIncome: 0, totalRevenue: 1,
    })
    router.push('/subscriptions')
  } catch (cause) { error.value = cause.response?.data?.message || '시나리오 등록에 실패했습니다.' }
  finally { loading.value = false }
}
</script>

<style scoped>
.page{min-height:100vh;background:var(--color-bg-secondary)}.shell{max-width:880px;margin:auto;padding:30px 24px 64px}.back{font-size:12px;color:var(--color-text-secondary)}.head{margin:18px 0 22px}.head span{color:var(--color-primary);font-size:10px;font-weight:800;letter-spacing:.13em}.head h1{margin:5px 0;font-size:26px}.head p{color:var(--color-text-secondary);font-size:12px}.form-card{padding:26px;background:white;border:1px solid var(--color-border);border-radius:15px}h2{margin-bottom:16px;font-size:15px}.grid{display:grid;grid-template-columns:1fr 1fr;gap:14px}.wide{grid-column:1/-1}label{font-size:11px;color:var(--color-text-secondary)}input,select,textarea{width:100%;margin-top:5px;padding:10px;border:1px solid var(--color-border);border-radius:8px;font:inherit;background:white;outline:none}input:focus,select:focus,textarea:focus{border-color:var(--color-primary)}.hint{margin-top:20px;padding:13px;border-radius:9px;background:var(--color-primary-light);color:var(--color-primary-dark);font-size:11px;line-height:1.55}.hint strong{display:block}.buttons{display:flex;justify-content:flex-end;gap:8px;margin-top:25px}.error{margin-top:18px;padding:10px;background:#ffe3e3;color:#c92a2a;border-radius:8px;font-size:12px}@media(max-width:600px){.grid{grid-template-columns:1fr}.wide{grid-column:auto}}
</style>
