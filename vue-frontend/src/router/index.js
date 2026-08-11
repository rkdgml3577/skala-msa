import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth.js'

const routes = [
  {
    path: '/',
    name: 'Landing',
    component: () => import('@/views/LandingView.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/callback',
    name: 'Callback',
    component: () => import('@/views/CallbackView.vue')
  },
  {
    path: '/subscriptions',
    name: 'SubscriptionList',
    component: () => import('@/views/CourseListView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/subscriptions/new',
    name: 'CourseCreate',
    component: () => import('@/views/CourseCreateView.vue'),
    meta: { requiresAuth: true, instructorOnly: true }
  },
  {
    path: '/subscriptions/:id(\\d+)',
    name: 'SubscriptionDetail',
    component: () => import('@/views/CourseDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/detections',
    name: 'DetectionResults',
    component: () => import('@/views/EnrollmentView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/mypage',
    name: 'MyPage',
    component: () => import('@/views/MyPageView.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 인증/권한 가드
router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'Login' }
  }

  if (to.meta.guestOnly && auth.isAuthenticated) {
    return { name: 'SubscriptionList' }
  }

  if (to.meta.instructorOnly && auth.user?.role !== 'INSTRUCTOR') {
    return { name: 'SubscriptionList' }
  }
})

export default router
