import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue'),
    },
    {
      path: '/user/login',
      name: 'user-login',
      component: () => import('@/views/user/UserLoginView.vue'),
    },
    {
      path: '/user/register',
      name: 'user-register',
      component: () => import('@/views/user/UserRegisterView.vue'),
    },
    {
      path: '/user',
      name: 'user-home',
      component: () => import('@/views/user/UserHomeView.vue'),
      meta: { requiresUser: true },
    },
    {
      path: '/merchant/login',
      name: 'merchant-login',
      component: () => import('@/views/merchant/MerchantLoginView.vue'),
    },
    {
      path: '/merchant',
      name: 'merchant-home',
      component: () => import('@/views/merchant/MerchantHomeView.vue'),
      meta: { requiresMerchant: true },
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresUser && !auth.isUserLoggedIn) {
    return { name: 'user-login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresMerchant && !auth.isMerchantLoggedIn) {
    return { name: 'merchant-login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
