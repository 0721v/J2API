import request from '../utils/request'

// ==================== 公告API ====================

/**
 * 获取公告列表
 */
export function getAnnouncements(params) {
  return request.get('/announcements', { params })
}

/**
 * 获取公告详情
 */
export function getAnnouncementDetail(id) {
  return request.get(`/announcements/${id}`)
}

/**
 * 获取未读公告数量
 */
export function getUnreadAnnouncementCount() {
  return request.get('/announcements/unread-count')
}

/**
 * 标记公告已读
 */
export function markAnnouncementRead(id) {
  return request.post(`/announcements/${id}/read`)
}

// ==================== 消息API ====================

/**
 * 获取消息列表
 */
export function getMessages(params) {
  return request.get('/messages', { params })
}

/**
 * 获取未读消息数量
 */
export function getUnreadMessageCount() {
  return request.get('/messages/unread-count')
}

/**
 * 标记消息已读
 */
export function markMessageRead(id) {
  return request.post(`/messages/${id}/read`)
}

/**
 * 标记所有消息已读
 */
export function markAllMessagesRead() {
  return request.post('/messages/read-all')
}

/**
 * 删除消息
 */
export function deleteMessage(id) {
  return request.delete(`/messages/${id}`)
}

// ==================== 报表API ====================

/**
 * 获取消费记录
 */
export function getConsumptionRecords(params) {
  return request.get('/reports/consumption', { params })
}

/**
 * 获取消费汇总
 */
export function getConsumptionSummary(params) {
  return request.get('/reports/consumption/summary', { params })
}

/**
 * 按类型统计消费
 */
export function getConsumptionByType(params) {
  return request.get('/reports/consumption/by-type', { params })
}

/**
 * 获取消费趋势
 */
export function getConsumptionTrend(params) {
  return request.get('/reports/consumption/trend', { params })
}

/**
 * 获取每日统计
 */
export function getDailyStats(params) {
  return request.get('/reports/daily', { params })
}

/**
 * 获取整体统计
 */
export function getOverallStats() {
  return request.get('/reports/overall')
}

/**
 * 获取本月统计
 */
export function getMonthStats() {
  return request.get('/reports/month')
}

/**
 * 获取今日统计
 */
export function getTodayStats() {
  return request.get('/reports/today')
}
