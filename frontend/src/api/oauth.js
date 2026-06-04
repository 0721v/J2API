import request from '../utils/request'

export function getOAuthProviders() {
  return request.get('/auth/oauth/providers')
}

export function getAuthorizationUrl(provider) {
  return request.get(`/auth/oauth/authorize/${provider}`)
}

export function bindOAuthAccount(provider, code) {
  return request.post(`/auth/oauth/bind/${provider}`, { code })
}

export function unbindOAuthAccount(provider) {
  return request.delete(`/auth/oauth/unbind/${provider}`)
}

export function getOAuthBindings() {
  return request.get('/auth/oauth/bindings')
}

export function telegramLogin(initData) {
  return request.post('/auth/oauth/telegram/login', { initData })
}