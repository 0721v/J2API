import request from './request'

// ==================== 管理员公告API ====================

/**
 * 获取公告列表（管理端）
 */
export function getAdminAnnouncements(params) {
  return request.get('/api/announcements/admin/list', { params })
}

/**
 * 获取历史公告（管理端）
 */
export function getAdminAnnouncementHistory(params) {
  return request.get('/api/announcements/admin/history', { params })
}

/**
 * 创建公告
 */
export function createAnnouncement(data) {
  return request.post('/api/announcements/admin', data)
}

/**
 * 更新公告
 */
export function updateAnnouncement(id, data) {
  return request.put(`/api/announcements/admin/${id}`, data)
}

/**
 * 删除公告
 */
export function deleteAnnouncement(id) {
  return request.delete(`/api/announcements/admin/${id}`)
}

/**
 * 发布公告
 */
export function publishAnnouncement(id) {
  return request.post(`/api/announcements/admin/${id}/publish`)
}

/**
 * 归档公告
 */
export function archiveAnnouncement(id) {
  return request.post(`/api/announcements/admin/${id}/archive`)
}

// ==================== 管理员消息API ====================

/**
 * 发送消息
 */
export function sendMessage(data) {
  return request.post('/api/messages/admin/send', data)
}
