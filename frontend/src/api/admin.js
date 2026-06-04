import request from './request'

// ==================== 管理员API ====================

// 获取统计数据
export function getAdminStats() {
  return request.get('/admin/stats')
}

// 获取用户列表
export function getUserList(params) {
  return request.get('/admin/users', { params })
}

// 获取渠道列表
export function getChannelList(params) {
  return request.get('/admin/channels', { params })
}

// 创建渠道
export function createChannel(data) {
  return request.post('/admin/channels', data)
}

// 更新渠道
export function updateChannel(id, data) {
  return request.put(`/admin/channels/${id}`, data)
}

// 删除渠道
export function deleteChannel(id) {
  return request.delete(`/admin/channels/${id}`)
}

// 获取渠道模板列表
export function getChannelTemplates() {
  return request.get('/admin/channels/templates')
}

// 从模板创建渠道
export function createChannelFromTemplate(templateId, data) {
  return request.post(`/admin/channels/templates/${templateId}`, data)
}

// 获取渠道支持的模型
export function getChannelModels(channelId) {
  return request.get(`/admin/channels/${channelId}/models`)
}

// 获取模型列表
export function getModelList(params) {
  return request.get('/admin/models', { params })
}

// 创建模型
export function createModel(data) {
  return request.post('/admin/models', data)
}

// 更新模型
export function updateModel(id, data) {
  return request.put(`/admin/models/${id}`, data)
}

// 删除模型
export function deleteModel(id) {
  return request.delete(`/admin/models/${id}`)
}

// 获取订单列表
export function getOrderList(params) {
  return request.get('/admin/orders', { params })
}

// 获取套餐列表
export function getPackageList(params) {
  return request.get('/admin/packages', { params })
}

// 创建套餐
export function createPackage(data) {
  return request.post('/admin/packages', data)
}

// 更新套餐
export function updatePackage(id, data) {
  return request.put(`/admin/packages/${id}`, data)
}

// 删除套餐
export function deletePackage(id) {
  return request.delete(`/admin/packages/${id}`)
}

// 获取系统设置
export function getSystemSettings() {
  return request.get('/admin/settings/system')
}

// 更新系统设置
export function updateSystemSettings(data) {
  return request.put('/admin/settings/system', data)
}

// 获取用量日志
export function getUsageLogs(params) {
  return request.get('/admin/usage-logs', { params })
}
