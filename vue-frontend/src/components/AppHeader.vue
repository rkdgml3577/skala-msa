<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="logo">
        <span class="logo-mark">SG</span>
        <span><strong>ShariahGuard</strong><small>Compliance Console</small></span>
      </router-link>

      <nav v-if="auth.isAuthenticated" class="nav-links">
        <router-link to="/subscriptions" class="nav-link">탐지 시나리오</router-link>
        <router-link to="/detections" class="nav-link">탐지 결과</router-link>
        <router-link to="/mypage" class="nav-link">연동 관리</router-link>
      </nav>

      <div class="header-actions">
        <template v-if="auth.isAuthenticated">
          <span class="firm-name">{{ auth.user?.organizationName || auth.user?.name }}</span>
          <button class="btn btn-ghost btn-sm" @click="handleLogout">로그아웃</button>
        </template>
        <router-link v-else to="/login" class="btn btn-primary btn-sm">콘솔 로그인</router-link>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useAuthStore } from '@/store/auth.js'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

function handleLogout() {
  auth.logout(false)
  router.push('/')
}
</script>

<style scoped>
.app-header { position: sticky; top: 0; z-index: 100; background: rgba(255,255,255,.94); border-bottom: 1px solid var(--color-border); backdrop-filter: blur(12px); }
.header-inner { max-width: 1240px; height: 68px; margin: 0 auto; padding: 0 24px; display: flex; align-items: center; gap: 36px; }
.logo { display: flex; align-items: center; gap: 10px; }
.logo-mark { width: 38px; height: 38px; border-radius: 11px; display: grid; place-items: center; background: var(--color-primary); color: white; font-size: 13px; font-weight: 800; }
.logo strong { display: block; font-size: 15px; letter-spacing: -.2px; }
.logo small { display: block; color: var(--color-text-muted); font-size: 9px; letter-spacing: .08em; text-transform: uppercase; }
.nav-links { display: flex; gap: 4px; flex: 1; }
.nav-link { padding: 8px 13px; border-radius: 8px; font-size: 13px; color: var(--color-text-secondary); }
.nav-link:hover, .nav-link.router-link-active { color: var(--color-primary); background: var(--color-primary-light); }
.header-actions { margin-left: auto; display: flex; align-items: center; gap: 12px; }
.firm-name { font-size: 12px; color: var(--color-text-secondary); }
.btn-sm { padding: 7px 14px; font-size: 12px; }
@media (max-width: 760px) { .nav-links { display: none; } .firm-name { display: none; } }
</style>
