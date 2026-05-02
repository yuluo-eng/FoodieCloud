import { createRouter, createWebHistory } from 'vue-router'
import { authBeforeEach } from '@/router/guards'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue'),
      meta: { public: true },
    },
    {
      path: '/user/login',
      name: 'user-login',
      component: () => import('@/views/user/UserLoginView.vue'),
      meta: { guestOnly: 'user' },
    },
    {
      path: '/user/register',
      name: 'user-register',
      component: () => import('@/views/user/UserRegisterView.vue'),
      meta: { guestOnly: 'user' },
    },
    {
      path: '/user/profile',
      name: 'user-profile',
      component: () => import('@/views/user/UserProfileView.vue'),
      meta: { requiresUser: true },
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
      meta: { guestOnly: 'merchant' },
    },
    {
      path: '/merchant',
      name: 'merchant-home',
      component: () => import('@/views/merchant/MerchantHomeView.vue'),
      meta: { requiresMerchant: true },
    },
    {
      path: '/rider/login',
      name: 'rider-login',
      component: () => import('@/views/rider/RiderLoginView.vue'),
      meta: { guestOnly: 'rider' },
    },
    {
      path: '/rider/register',
      name: 'rider-register',
      component: () => import('@/views/rider/RiderRegisterView.vue'),
      meta: { guestOnly: 'rider' },
    },
    {
      path: '/rider',
      name: 'rider-home',
      component: () => import('@/views/rider/RiderHomeView.vue'),
      meta: { requiresRider: true },
    },
  ],
})

router.beforeEach(authBeforeEach)

export default router
