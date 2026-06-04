import request from '../utils/request'

// ==================== 鍏憡API ====================

/**
 * 鑾峰彇鍏憡鍒楄〃
 */
export function getAnnouncements(params) {
  return request.get('/api/announcements', { params })
}

/**
 * 鑾峰彇鍏憡璇︽儏
 */
export function getAnnouncementDetail(id) {
  return request.get(`/api/announcements/${id}`)
}

/**
 * 鑾峰彇鏈鍏憡鏁伴噺
 */
export function getUnreadAnnouncementCount() {
  return request.get('/api/announcements/unread-count')
}

/**
 * 鏍囪鍏憡宸茶
 */
export function markAnnouncementRead(id) {
  return request.post(`/api/announcements/${id}/read`)
}

// ==================== 娑堟伅API ====================

/**
 * 鑾峰彇娑堟伅鍒楄〃
 */
export function getMessages(params) {
  return request.get('/api/messages', { params })
}

/**
 * 鑾峰彇鏈娑堟伅鏁伴噺
 */
export function getUnreadMessageCount() {
  return request.get('/api/messages/unread-count')
}

/**
 * 鏍囪娑堟伅宸茶
 */
export function markMessageRead(id) {
  return request.post(`/api/messages/${id}/read`)
}

/**
 * 鏍囪鎵€鏈夋秷鎭凡璇? */
export function markAllMessagesRead() {
  return request.post('/api/messages/read-all')
}

/**
 * 鍒犻櫎娑堟伅
 */
export function deleteMessage(id) {
  return request.delete(`/api/messages/${id}`)
}

// ==================== 鎶ヨ〃API ====================

/**
 * 鑾峰彇娑堣垂璁板綍
 */
export function getConsumptionRecords(params) {
  return request.get('/api/reports/consumption', { params })
}

/**
 * 鑾峰彇娑堣垂姹囨€? */
export function getConsumptionSummary(params) {
  return request.get('/api/reports/consumption/summary', { params })
}

/**
 * 鎸夌被鍨嬬粺璁℃秷璐? */
export function getConsumptionByType(params) {
  return request.get('/api/reports/consumption/by-type', { params })
}

/**
 * 鑾峰彇娑堣垂瓒嬪娍
 */
export function getConsumptionTrend(params) {
  return request.get('/api/reports/consumption/trend', { params })
}

/**
 * 鑾峰彇姣忔棩缁熻
 */
export function getDailyStats(params) {
  return request.get('/api/reports/daily', { params })
}

/**
 * 鑾峰彇鏁翠綋缁熻
 */
export function getOverallStats() {
  return request.get('/api/reports/overall')
}

/**
 * 鑾峰彇鏈湀缁熻
 */
export function getMonthStats() {
  return request.get('/api/reports/month')
}

/**
 * 鑾峰彇浠婃棩缁熻
 */
export function getTodayStats() {
  return request.get('/api/reports/today')
}

