import axios from 'axios'

const U_KEY = 'ysh_user_token'
const M_KEY = 'ysh_merchant_token'
const R_KEY = 'ysh_rider_token'
const U_PROFILE_KEY = 'ysh_user_profile'
const M_PROFILE_KEY = 'ysh_merchant_profile'
const R_PROFILE_KEY = 'ysh_rider_profile'

function handleUnauthorized() {
  const p = window.location.pathname || ''
  if (p.startsWith('/merchant')) {
    localStorage.removeItem(M_KEY)
    localStorage.removeItem(M_PROFILE_KEY)
    window.location.href = '/merchant/login'
  } else if (p.startsWith('/rider')) {
    localStorage.removeItem(R_KEY)
    localStorage.removeItem(R_PROFILE_KEY)
    window.location.href = '/rider/login'
  } else if (p.startsWith('/user')) {
    localStorage.removeItem(U_KEY)
    localStorage.removeItem(U_PROFILE_KEY)
    window.location.href = '/user/login'
  } else {
    localStorage.removeItem(U_KEY)
    localStorage.removeItem(M_KEY)
    localStorage.removeItem(U_PROFILE_KEY)
    localStorage.removeItem(M_PROFILE_KEY)
    localStorage.removeItem(R_PROFILE_KEY)
    window.location.href = '/user/login'
  }
}

const instance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

instance.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body.code === 'number' && body.code !== 200) {
      if (body.code === 401) {
        handleUnauthorized()
      }
      return Promise.reject(new Error(body.msg || '请求失败'))
    }
    return res
  },
  (err) => {
    if (err.response?.status === 401) {
      handleUnauthorized()
    }
    const data = err.response?.data
    const msg =
      (typeof data === 'object' && data?.msg) ||
      err.response?.statusText ||
      err.message ||
      '网络错误'
    return Promise.reject(new Error(msg))
  }
)

export function setAuthHeader(token) {
  if (token) {
    instance.defaults.headers.common.Authorization = `Bearer ${token}`
  } else {
    delete instance.defaults.headers.common.Authorization
  }
}

export default instance
