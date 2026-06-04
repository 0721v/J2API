import request from './request'

// 获取OAuth提供商列表
export function getOAuthProviders() {
  return request.get('/auth/oauth/providers')
}

// 获取授权URL
export function getAuthorizationUrl(provider) {
  return request.get(`/auth/oauth/authorize/${provider}`)
}

// 绑定OAuth账户
export function bindOAuthAccount(provider, code) {
  return request.post(`/auth/oauth/bind/${provider}`, { code })
}

// 解除OAuth绑定
export function unbindOAuthAccount(provider) {
  return request.delete(`/auth/oauth/unbind/${provider}`)
}

// 获取已绑定的OAuth列表
export function getOAuthBindings() {
  return request.get('/auth/oauth/bindings')
}

// Telegram登录
export function telegramLogin(initData) {
  return request.post('/auth/oauth/telegram/login', { initData })
}
