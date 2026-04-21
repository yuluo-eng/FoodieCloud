import { useAuthStore } from '@/stores/auth'

/**
 * 全局前置守卫：
 * - requiresUser / requiresMerchant：未登录则跳转对应登录页，并带上 redirect 便于登录后回跳
 * - guestOnly：已登录则离开登录/注册页，进入对应主页（避免重复登录）
 *
 * 说明：仅提升体验与防误操作，真实鉴权仍以接口 JWT 校验为准。
 */
export function authBeforeEach(to) {
  const auth = useAuthStore()

  if (to.meta.requiresUser && !auth.isUserLoggedIn) {
    return {
      name: 'user-login',
      query: { redirect: to.fullPath },
      replace: true,
    }
  }
  if (to.meta.requiresMerchant && !auth.isMerchantLoggedIn) {
    return {
      name: 'merchant-login',
      query: { redirect: to.fullPath },
      replace: true,
    }
  }

  if (to.meta.guestOnly === 'user' && auth.isUserLoggedIn) {
    return { path: '/user', replace: true }
  }
  if (to.meta.guestOnly === 'merchant' && auth.isMerchantLoggedIn) {
    return { path: '/merchant', replace: true }
  }

  return true
}
