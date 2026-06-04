import request from '../utils/request'

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

export function getCustomizationSettings() {
  return request.get('/settings/customization')
}

export function updateCustomizationSettings(data) {
  return request.put('/settings/customization', data)
}

export function getSeoSettings() {
  return request.get('/settings/seo')
}

export function updateSeoSettings(data) {
  return request.put('/settings/seo', data)
}

export function getRegistrationSettings() {
  return request.get('/settings/registration')
}

export function updateRegistrationSettings(data) {
  return request.put('/settings/registration', data)
}

export function getQuotaSettings() {
  return request.get('/settings/quota')
}

export function updateQuotaSettings(data) {
  return request.put('/settings/quota', data)
}

export function getProxySettings() {
  return request.get('/settings/proxy')
}

export function updateProxySettings(data) {
  return request.put('/settings/proxy', data)
}