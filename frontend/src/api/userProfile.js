import request from './request'

export function fetchUserProfile(token) {
  return request.get('/user/profile', {
    headers: { Authorization: `Bearer ${token}` },
  })
}

export function updateUserProfile(token, payload) {
  return request.put('/user/profile', payload, {
    headers: { Authorization: `Bearer ${token}` },
  })
}

export function reverseGeocode(token, latitude, longitude) {
  return request.get('/user/geocode/reverse', {
    params: { latitude, longitude },
    headers: { Authorization: `Bearer ${token}` },
  })
}

export function uploadUserAvatar(token, file) {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/user/upload/avatar', fd, {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'multipart/form-data',
    },
  })
}
