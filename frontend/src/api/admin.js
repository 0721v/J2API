import request from '../utils/request'

export function getAdminStats() {
  return request.get('/admin/stats')
}

export function getUserList(params) {
  return request.get('/admin/users', { params })
}

export function getChannelList(params) {
  return request.get('/admin/channels', { params })
}

export function createChannel(data) {
  return request.post('/admin/channels', data)
}

export function updateChannel(id, data) {
  return request.put(`/admin/channels/${id}`, data)
}

export function deleteChannel(id) {
  return request.delete(`/admin/channels/${id}`)
}

export function getChannelTemplates() {
  return request.get('/admin/channel-templates/templates')
}

export function createChannelFromTemplate(templateId, data) {
  return request.post(`/admin/channel-templates/templates/${templateId}`, data)
}

export function getChannelModels(channelId) {
  return request.get(`/admin/channels/${channelId}/models`)
}

export function getModelList(params) {
  return request.get('/admin/models', { params })
}

export function createModel(data) {
  return request.post('/admin/models', data)
}

export function updateModel(id, data) {
  return request.put(`/admin/models/${id}`, data)
}

export function deleteModel(id) {
  return request.delete(`/admin/models/${id}`)
}

export function getOrderList(params) {
  return request.get('/admin/orders', { params })
}

export function getPackageList(params) {
  return request.get('/admin/packages', { params })
}

export function createPackage(data) {
  return request.post('/admin/packages', data)
}

export function updatePackage(id, data) {
  return request.put(`/admin/packages/${id}`, data)
}

export function deletePackage(id) {
  return request.delete(`/admin/packages/${id}`)
}

export function getSystemSettings() {
  return request.get('/admin/settings/system')
}

export function updateSystemSettings(data) {
  return request.put('/admin/settings/system', data)
}

export function getUsageLogs(params) {
  return request.get('/admin/usage-logs', { params })
}

export function updateUserStatus(userId, status) {
  return request.put(`/admin/users/${userId}/status`, { status })
}

export function rechargeUser(userId, amount) {
  return request.post(`/admin/users/${userId}/recharge`, { amount })
}