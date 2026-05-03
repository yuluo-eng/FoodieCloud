import request from './request'

export function riderMe(token) {
  return request.get('/rider/me', {
    headers: { Authorization: `Bearer ${token}` },
  })
}

export function updateRiderWorkStatus(token, workStatus) {
  return request.patch('/rider/work-status', { workStatus }, {
    headers: { Authorization: `Bearer ${token}` },
  })
}

export function fetchDispatchOrders(token, params = {}) {
  return request.get('/rider/orders/dispatch', {
    params,
    headers: { Authorization: `Bearer ${token}` },
  })
}

export function fetchCurrentOrders(token) {
  return request.get('/rider/orders/current', {
    headers: { Authorization: `Bearer ${token}` },
  })
}

export function riderAcceptOrder(token, orderId) {
  return request.patch(`/rider/orders/${orderId}/accept`, {}, {
    headers: { Authorization: `Bearer ${token}` },
  })
}

export function riderArriveShop(token, orderId) {
  return request.patch(`/rider/orders/${orderId}/arrive-shop`, {}, {
    headers: { Authorization: `Bearer ${token}` },
  })
}

export function riderPickup(token, orderId) {
  return request.patch(`/rider/orders/${orderId}/pickup`, {}, {
    headers: { Authorization: `Bearer ${token}` },
  })
}

export function riderDelivered(token, orderId) {
  return request.patch(`/rider/orders/${orderId}/delivered`, {}, {
    headers: { Authorization: `Bearer ${token}` },
  })
}

export function fetchHistoryOrders(token, params = {}) {
  return request.get('/rider/orders/history', {
    params,
    headers: { Authorization: `Bearer ${token}` },
  })
}
