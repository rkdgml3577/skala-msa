<template>
  <div class="page">
    <AppHeader />
    <main class="shell">
      <div class="head"><span>BANK API MANAGEMENT</span><h1>은행사 연동 관리</h1><p>거래 API 접근 정보와 연동용 식별자를 한곳에서 관리합니다.</p></div>
      <div class="layout">
        <section>
          <article class="profile card">
            <div class="avatar">{{ auth.user?.organizationName?.charAt(0) || auth.user?.name?.charAt(0) || '?' }}</div>
            <div><small>FINANCIAL INSTITUTION</small><h2>{{ auth.user?.organizationName || '등록된 회사명 없음' }}</h2><p>{{ auth.user?.name }} · {{ auth.user?.email }}</p></div>
            <span class="role">{{ auth.user?.role === 'INSTRUCTOR' ? 'FIRM ADMIN' : 'COMPLIANCE ANALYST' }}</span>
          </article>

          <article class="card api-card">
            <div class="card-title"><div><h2>거래 API 클라이언트</h2><p>은행사 거래 데이터 연동용 식별자와 API 키를 관리합니다.</p></div><button class="btn btn-outline" :disabled="rotating" @click="rotateKey">{{ rotating ? '발급 중…' : 'API 키 재발급' }}</button></div>
            <div class="client-grid"><div><span>Client ID</span><strong>{{ auth.user?.id || '-' }}</strong></div><div><span>Client Code</span><strong>{{ auth.user?.clientCode || '-' }}</strong></div><div><span>상태</span><strong class="active">ACTIVE</strong></div></div>
            <div v-if="issuedKey" class="issued"><span>새 API 키 · 이번에만 표시됩니다</span><code>{{ issuedKey }}</code></div>
            <pre>curl -X POST http://localhost:8081/api/users/transactions \\
  -H 'X-API-Key: YOUR_ISSUED_API_KEY' \\
  -H 'Content-Type: application/json' \\
  -d '{ "transactionId":"TX-001", "ticker":"AAPL", "side":"BUY", "quantity":10, "price":210.50, "sector":"TECHNOLOGY", "merchantCategory":"software", "interestBearingDebtRatio":0.20, "nonPermissibleIncomeRatio":0.01 }'</pre>
            <p v-if="error" class="error">{{ error }}</p>
          </article>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import { useAuthStore } from '@/store/auth.js'
import { authApi } from '@/api/auth.js'

const auth=useAuthStore(), rotating=ref(false), issuedKey=ref(''), error=ref('')
async function rotateKey(){rotating.value=true;error.value='';try{const response=await authApi.rotateApiKey(auth.user.id);issuedKey.value=response.data?.data?.apiKey||''}catch(cause){error.value=cause.response?.data?.message||'API 키 재발급에 실패했습니다.'}finally{rotating.value=false}}
</script>

<style scoped>
.page{min-height:100vh;background:var(--color-bg-secondary)}.shell{max-width:1120px;margin:auto;padding:38px 24px 64px}.head span{color:var(--color-primary);font-size:10px;font-weight:800;letter-spacing:.13em}.head h1{margin:5px 0;font-size:27px}.head p,.card p{color:var(--color-text-secondary);font-size:12px}.layout{margin-top:24px}.card{padding:22px;background:white;border:1px solid var(--color-border);border-radius:14px}.profile{display:flex;align-items:center;gap:15px}.avatar{width:48px;height:48px;display:grid;place-items:center;border-radius:13px;background:var(--color-primary);color:white;font-size:18px;font-weight:800}.profile small{font-size:8px;color:var(--color-primary);letter-spacing:.1em}.profile h2{font-size:17px}.role{margin-left:auto;padding:5px 8px;border-radius:15px;background:var(--color-primary-light);color:var(--color-primary);font-size:9px;font-weight:800}.api-card{margin-top:18px}.card-title{display:flex;justify-content:space-between;gap:15px;align-items:flex-start}.card-title h2{font-size:15px}.client-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:8px;margin:18px 0}.client-grid div{padding:11px;background:var(--color-bg-secondary);border-radius:8px}.client-grid span,.client-grid strong{display:block;font-size:9px}.client-grid strong{margin-top:3px;font-size:11px}.active{color:var(--color-primary)}pre{padding:14px;overflow:auto;border-radius:8px;background:#10231d;color:#b9ddcf;font-size:10px;line-height:1.7}.issued{margin-bottom:12px;padding:11px;background:#fff3bf;border-radius:8px}.issued span,.issued code{display:block;font-size:9px}.issued code{margin-top:4px;font-size:11px;word-break:break-all}.error{margin-top:14px;padding:10px;background:#ffe3e3;color:#c92a2a;border-radius:8px;font-size:11px}@media(max-width:800px){.client-grid{grid-template-columns:1fr}}
</style>
