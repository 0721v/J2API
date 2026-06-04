import request from './request'

export function getTokens(params) {
  return request.get('/tokens', { params })
}

export function getToken(id) {
  return request.get(`/tokens/${id}`)
}

export function createToken(data) {
  return request.post('/tokens', data)
}

export function updateToken(id, data) {
  return request.put(`/tokens/${id}`, data)
}

export function deleteToken(id) {
  return request.delete(`/tokens/${id}`)
}

export function enableToken(id) {
  return request.post(`/tokens/${id}/enable`)
}

export function disableToken(id) {
  return request.post(`/tokens/${id}/disable`)
}

export function renewToken(id) {
  return request.post(`/tokens/${id}/renew`)
}

export function getTokenUsage(id) {
  return request.get(`/tokens/${id}/usage`)
}
