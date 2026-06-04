import request from './request'

// 系统设置 API

export function getSystemSettings() {
  return request.get('/settings/system')
}

export function updateSystemSettings(data) {
  return request.put('/settings/system', data)
}

export function getSettingByCategory(category) {
  return request.get(`/settings/category/${category}`)
}

export function updateSetting(key, value) {
  return request.put(`/settings/${key}`, { value })
}

export function resetSetting(key) {
  return request.delete(`/settings/${key}`)
}

// 自定义设置
export function getCustomizationSettings() {
  return request.get('/settings/customization')
}

export function updateCustomizationSettings(data) {
  return request.put('/settings/customization', data)
}

// SEO 设置
export function getSeoSettings() {
  return request.get('/settings/seo')
}

export function updateSeoSettings(data) {
  return request.put('/settings/seo', data)
}

// 注册设置
export function getRegistrationSettings() {
  return request.get('/settings/registration')
}

export function updateRegistrationSettings(data) {
  return request.put('/settings/registration', data)
}

// 配额设置
export function getQuotaSettings() {
  return request.get('/settings/quota')
}

export function updateQuotaSettings(data) {
  return request.put('/settings/quota', data)
}

// 代理设置
export function getProxySettings() {
  return request.get('/settings/proxy')
}

export function updateProxySettings(data) {
  return request.put('/settings/proxy', data)
}
