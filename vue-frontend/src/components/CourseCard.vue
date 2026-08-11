<template>
  <router-link :to="`/subscriptions/${course.id}`" class="security-card">
    <div class="card-head">
      <div>
        <span class="ticker">{{ detector.code }}</span>
        <h3>{{ detector.name }}</h3>
      </div>
      <span class="grade" :class="`status-${subscription?.status?.toLowerCase() || 'none'}`">{{ statusLabel }}</span>
    </div>
    <p class="sector">{{ detector.type }}</p>
    <div class="metrics">
      <div><span>입력 데이터</span><strong>종목·주문 API</strong></div>
      <div><span>적용 기준</span><strong>{{ detector.type }}</strong></div>
    </div>
    <p class="reason">{{ detector.description }}</p>
    <div class="card-foot">
      <span>{{ subscription ? '구독 설정 보기' : '모델 구독하기' }}</span>
      <span>상세 보기 →</span>
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue'
import { detectorFor, subscriptionLabel } from '@/utils/detectors.js'

const props = defineProps({
  course: { type: Object, required: true },
  subscription: { type: Object, default: null },
})

const detector = computed(() => detectorFor(props.course))
const statusLabel = computed(() => subscriptionLabel(props.subscription?.status))
</script>

<style scoped>
.security-card { display: flex; flex-direction: column; gap: 14px; min-height: 250px; padding: 20px; background: white; border: 1px solid var(--color-border); border-radius: var(--radius-lg); transition: var(--transition); }
.security-card:hover { transform: translateY(-3px); box-shadow: var(--shadow-md); border-color: #a9cbbb; }
.card-head { display: flex; justify-content: space-between; gap: 12px; align-items: flex-start; }
.ticker { font-size: 12px; font-weight: 800; color: var(--color-primary); letter-spacing: .08em; }
h3 { margin-top: 3px; font-size: 16px; line-height: 1.35; }
.grade { padding: 5px 9px; border-radius: 20px; font-size: 11px; font-weight: 700; white-space: nowrap; }
.status-active { color: #087f5b; background: #dff7ec; }
.status-paused { color: #9a6700; background: #fff3bf; }
.status-cancelled { color: #c92a2a; background: #ffe3e3; }
.status-none { color: #495057; background: #e9ecef; }
.sector { font-size: 11px; color: var(--color-text-muted); text-transform: uppercase; letter-spacing: .06em; }
.metrics { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.metrics div { padding: 10px; border-radius: 9px; background: var(--color-bg-secondary); }
.metrics span { display: block; color: var(--color-text-muted); font-size: 10px; }
.metrics strong { display: block; margin-top: 3px; font-size: 13px; }
.reason { flex: 1; font-size: 12px; color: var(--color-text-secondary); line-height: 1.55; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-foot { display: flex; justify-content: space-between; padding-top: 12px; border-top: 1px solid var(--color-border); font-size: 10px; color: var(--color-text-muted); }
</style>
