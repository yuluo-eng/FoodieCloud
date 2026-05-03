import axios from 'axios'

const U_KEY = 'ysh_user_token'
const M_KEY = 'ysh_merchant_token'
const R_KEY = 'ysh_rider_token'
const U_PROFILE_KEY = 'ysh_user_profile'
const M_PROFILE_KEY = 'ysh_merchant_profile'
const R_PROFILE_KEY = 'ysh_rider_profile'

const HTTP_STATUS_MAP = {
  400: '请求参数错误',
  403: '没有访问权限',
  404: '请求的资源不存在',
  409: '数据冲突，请刷新后重试',
  422: '请求无法处理',
  500: '服务器内部错误，请稍后重试',
  502: '服务暂时不可用',
  503: '服务维护中，请稍后重试',
}

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
      return Promise.reject(new Error(body.msg || HTTP_STATUS_MAP[body.code] || '请求失败'))
    }
    return res
  },
  (err) => {
    if (err.code === 'ECONNABORTED') {
      return Promise.reject(new Error('请求超时，请检查网络后重试'))
    }
    if (!err.response) {
      return Promise.reject(new Error('网络连接失败，请检查网络设置'))
    }
    const status = err.response.status
    if (status === 401) {
      handleUnauthorized()
    }
    const data = err.response?.data
    const msg =
      (typeof data === 'object' && data?.msg) ||
      HTTP_STATUS_MAP[status] ||
      err.response?.statusText ||
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
