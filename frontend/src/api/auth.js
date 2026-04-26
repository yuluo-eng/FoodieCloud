import request, { setAuthHeader } from './request'

export async function userRegister(payload) {
  const { data } = await request.post('/auth/user/register', payload)
  return data.data
}

export async function userLogin(payload) {
  const { data } = await request.post('/auth/user/login', payload)
  return data.data
}

export async function employeeLogin(payload) {
  const { data } = await request.post('/auth/employee/login', payload)
  return data.data
}

export async function riderLogin(payload) {
  const { data } = await request.post('/auth/rider/login', payload)
  return data.data
}

export async function fetchMe(token) {
  setAuthHeader(token)
  try {
    const { data } = await request.get('/auth/me')
    return data.data
  } finally {
    setAuthHeader(null)
  }
}
