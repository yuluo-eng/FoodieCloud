import {defineStore} from 'pinia'
import { ref, computed } from 'vue'

const U_KEY = 'ysh_user_token'
const M_KEY = 'ysh_merchant_token'

export const useAuthStore = defineStore('auth', () => {
  const userToken = ref(localStorage.getItem(U_KEY) || '')
  const merchantToken = ref(localStorage.getItem(M_KEY) || '')

  const isUserLoggedIn = computed(() => !!userToken.value)
  const isMerchantLoggedIn = computed(() => !!merchantToken.value)

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

  function logoutUser() {
    setUserToken('')
  }

  function logoutMerchant() {
    setMerchantToken('')
  }

  return {
    userToken,
    merchantToken,
    isUserLoggedIn,
    isMerchantLoggedIn,
    setUserToken,
    setMerchantToken,
    logoutUser,
    logoutMerchant,
  }
})
