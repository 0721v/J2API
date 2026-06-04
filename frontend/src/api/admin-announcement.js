import request from '../utils/request'

// ==================== 绠＄悊鍛樺叕鍛夾PI ====================

/**
 * 鑾峰彇鍏憡鍒楄〃锛堢鐞嗙锛? */
export function getAdminAnnouncements(params) {
  return request.get('/api/announcements/admin/list', { params })
}

/**
 * 鑾峰彇鍘嗗彶鍏憡锛堢鐞嗙锛? */
export function getAdminAnnouncementHistory(params) {
  return request.get('/api/announcements/admin/history', { params })
}

/**
 * 鍒涘缓鍏憡
 */
export function createAnnouncement(data) {
  return request.post('/api/announcements/admin', data)
}

/**
 * 鏇存柊鍏憡
 */
export function updateAnnouncement(id, data) {
  return request.put(`/api/announcements/admin/${id}`, data)
}

/**
 * 鍒犻櫎鍏憡
 */
export function deleteAnnouncement(id) {
  return request.delete(`/api/announcements/admin/${id}`)
}

/**
 * 鍙戝竷鍏憡
 */
export function publishAnnouncement(id) {
  return request.post(`/api/announcements/admin/${id}/publish`)
}

/**
 * 褰掓。鍏憡
 */
export function archiveAnnouncement(id) {
  return request.post(`/api/announcements/admin/${id}/archive`)
}

// ==================== 绠＄悊鍛樻秷鎭疉PI ====================

/**
 * 鍙戦€佹秷鎭? */
export function sendMessage(data) {
  return request.post('/api/messages/admin/send', data)
}

