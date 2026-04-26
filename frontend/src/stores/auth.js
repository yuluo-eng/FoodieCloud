import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/api/request'

const U_KEY = 'ysh_user_token'
const M_KEY = 'ysh_merchant_token'
const R_KEY = 'ysh_rider_token'
const U_PROFILE_KEY = 'ysh_user_profile'
const M_PROFILE_KEY = 'ysh_merchant_profile'
const R_PROFILE_KEY = 'ysh_rider_profile'

const ROLE_LABELS = {
  SUPER_ADMIN: '超级管理员',
  SHOP_MANAGER: '店长',
  STAFF: '员工',
}

function readJson(key) {
  try {
    const raw = localStorage.getItem(key)
    if (!raw) return null
    return JSON.parse(raw)
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const userToken = ref(localStorage.getItem(U_KEY) || '')
  const merchantToken = ref(localStorage.getItem(M_KEY) || '')
  const riderToken = ref(localStorage.getItem(R_KEY) || '')
  const userProfile = ref(readJson(U_PROFILE_KEY))
  const merchantProfile = ref(readJson(M_PROFILE_KEY))
  const riderProfile = ref(readJson(R_PROFILE_KEY))

  const isUserLoggedIn = computed(() => !!userToken.value)
  const isMerchantLoggedIn = computed(() => !!merchantToken.value)
  const isRiderLoggedIn = computed(() => !!riderToken.value)

  const userDisplayLabel = computed(() => {
    const p = userProfile.value
    if (!p) return ''
    return p.displayName || p.nickname || p.username || '顾客'
  })

  const merchantDisplayLabel = computed(() => {
    const p = merchantProfile.value
    if (!p) return ''
    return p.displayName || p.realName || p.username || '员工'
  })

  const merchantRoleLabel = computed(() => {
    const code = merchantProfile.value?.roleCode
    return (code && ROLE_LABELS[code]) || code || ''
  })

  const riderDisplayLabel = computed(() => {
    const p = riderProfile.value
    if (!p) return ''
    return p.displayName || p.realName || p.username || '骑手'
  })

  /** 头像地址（相对路径补全，供 img src） */
  const userAvatarSrc = computed(() => {
    const a = userProfile.value?.avatar
    if (!a) return ''
    if (a.startsWith('http')) return a
    return a.startsWith('/') ? a : `/${a}`
  })

  function persistUserProfile() {
    if (userProfile.value) {
      localStorage.setItem(U_PROFILE_KEY, JSON.stringify(userProfile.value))
    } else {
      localStorage.removeItem(U_PROFILE_KEY)
    }
  }

  function persistMerchantProfile() {
    if (merchantProfile.value) {
      localStorage.setItem(M_PROFILE_KEY, JSON.stringify(merchantProfile.value))
    } else {
      localStorage.removeItem(M_PROFILE_KEY)
    }
  }

  function persistRiderProfile() {
    if (riderProfile.value) {
      localStorage.setItem(R_PROFILE_KEY, JSON.stringify(riderProfile.value))
    } else {
      localStorage.removeItem(R_PROFILE_KEY)
    }
  }

  function setUserToken(t) {
    userToken.value = t || ''
    if (t) localStorage.setItem(U_KEY, t)
    else localStorage.removeItem(U_KEY)
  }

  function setMerchantToken(t) {
    merchantToken.value = t || ''
    if (t) localStorage.setItem(M_KEY, t)
    else localStorage.removeItem(M_KEY)
  }

  function setRiderToken(t) {
    riderToken.value = t || ''
    if (t) localStorage.setItem(R_KEY, t)
    else localStorage.removeItem(R_KEY)
  }

  /** 顾客登录成功后写入 */
  function setUserFromLogin(userInfo) {
    if (!userInfo) return
    const displayName = userInfo.nickname || userInfo.username
    userProfile.value = {
      id: userInfo.id,
      username: userInfo.username,
      nickname: userInfo.nickname ?? null,
      displayName,
      avatar: userInfo.avatar ?? null,
    }
    persistUserProfile()
  }

  /** 商家登录成功后写入 */
  function setMerchantFromLogin(employeeInfo) {
    if (!employeeInfo) return
    const displayName = employeeInfo.realName || employeeInfo.username
    merchantProfile.value = {
      id: employeeInfo.id,
      username: employeeInfo.username,
      realName: employeeInfo.realName ?? null,
      roleCode: employeeInfo.roleCode ?? null,
      displayName,
    }
    persistMerchantProfile()
  }

  function setRiderFromLogin(riderInfo) {
    if (!riderInfo) return
    const displayName = riderInfo.realName || riderInfo.username
    riderProfile.value = {
      id: riderInfo.id,
      username: riderInfo.username,
      realName: riderInfo.realName ?? null,
      enabled: riderInfo.enabled ?? 1,
      workStatus: riderInfo.workStatus ?? 'ONLINE',
      displayName,
    }
    persistRiderProfile()
  }

  /** 拉取完整顾客资料（含头像、收货信息） */
  async function refreshUserProfile() {
    if (!userToken.value) return
    try {
      const res = await request.get('/user/profile', {
        headers: { Authorization: `Bearer ${userToken.value}` },
      })
      const p = res.data.data
      if (!p) return
      userProfile.value = {
        id: p.id,
        username: p.username,
        nickname: p.nickname ?? null,
        displayName: p.nickname || p.username,
        avatar: p.avatar ?? null,
        phone: p.phone ?? null,
        receiverName: p.receiverName ?? null,
        shippingPhone: p.shippingPhone ?? null,
        shippingAddress: p.shippingAddress ?? null,
        shippingLat: p.shippingLat ?? null,
        shippingLng: p.shippingLng ?? null,
      }
      persistUserProfile()
    } catch {
      const res = await request.get('/auth/me', {
        headers: { Authorization: `Bearer ${userToken.value}` },
      })
      const me = res.data.data
      if (me?.type !== 'USER') return
      userProfile.value = {
        id: me.id,
        username: me.username,
        nickname: me.displayName !== me.username ? me.displayName : null,
        displayName: me.displayName || me.username,
        avatar: me.avatar ?? null,
      }
      persistUserProfile()
    }
  }

  async function refreshMerchantProfile() {
    if (!merchantToken.value) return
    const res = await request.get('/auth/me', {
      headers: { Authorization: `Bearer ${merchantToken.value}` },
    })
    const me = res.data.data
    if (me?.type !== 'EMPLOYEE') return
    merchantProfile.value = {
      id: me.id,
      username: me.username,
      realName: me.displayName !== me.username ? me.displayName : null,
      roleCode: me.roleCode ?? null,
      displayName: me.displayName || me.username,
    }
    persistMerchantProfile()
  }

  async function refreshRiderProfile() {
    if (!riderToken.value) return
    const res = await request.get('/auth/me', {
      headers: { Authorization: `Bearer ${riderToken.value}` },
    })
    const me = res.data.data
    if (me?.type !== 'RIDER') return
    riderProfile.value = {
      id: me.id,
      username: me.username,
      realName: me.displayName !== me.username ? me.displayName : null,
      displayName: me.displayName || me.username,
    }
    persistRiderProfile()
  }

  function logoutUser() {
    setUserToken('')
    userProfile.value = null
    persistUserProfile()
  }

  function logoutMerchant() {
    setMerchantToken('')
    merchantProfile.value = null
    persistMerchantProfile()
  }

  function logoutRider() {
    setRiderToken('')
    riderProfile.value = null
    persistRiderProfile()
  }

  return {
    userToken,
    merchantToken,
    riderToken,
    userProfile,
    merchantProfile,
    riderProfile,
    isUserLoggedIn,
    isMerchantLoggedIn,
    isRiderLoggedIn,
    userDisplayLabel,
    userAvatarSrc,
    merchantDisplayLabel,
    merchantRoleLabel,
    riderDisplayLabel,
    setUserToken,
    setMerchantToken,
    setRiderToken,
    setUserFromLogin,
    setMerchantFromLogin,
    setRiderFromLogin,
    refreshUserProfile,
    refreshMerchantProfile,
    refreshRiderProfile,
    logoutUser,
    logoutMerchant,
    logoutRider,
  }
})
