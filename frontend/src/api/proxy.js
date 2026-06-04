import request from './request'

// API 代理管理 API

export function getProxies(params) {
  return request.get('/proxies', { params })
}

export function getProxy(id) {
  return request.get(`/proxies/${id}`)
}

export function createProxy(data) {
  return request.post('/proxies', data)
}

export function updateProxy(id, data) {
  return request.put(`/proxies/${id}`, data)
}

export function deleteProxy(id) {
  return request.delete(`/proxies/${id}`)
}

export function toggleProxy(id, enabled) {
  return request.put(`/proxies/${id}/toggle`, null, { params: { enabled } })
}

export function testProxy(id) {
  return request.post(`/proxies/${id}/test`)
}

export function getProxyLogs(proxyId, params) {
  return request.get(`/proxies/${proxyId}/logs`, { params })
}

export function getProxyStats(proxyId) {
  return request.get(`/proxies/${proxyId}/stats`)
}
