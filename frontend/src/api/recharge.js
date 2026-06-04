import request from './request'

export function getAmountOptions() {
  return request.get('/recharge/amount-options')
}

export function getPaymentMethods() {
  return request.get('/recharge/payment-methods')
}

export function createRechargeOrder(data) {
  return request.post('/recharge/create', data)
}

export function getOrderStatus(orderNo) {
  return request.get(`/recharge/status/${orderNo}`)
}

export function getRechargeHistory(params) {
  return request.get('/recharge/history', { params })
}

export function getRechargeStats(params) {
  return request.get('/recharge/stats', { params })
}
