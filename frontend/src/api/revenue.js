import request from '../utils/request'

// ==================== 钀ユ敹缁熻API ====================

/**
 * 鑾峰彇钀ユ敹姒傝
 */
export function getRevenueOverview() {
  return request.get('/api/admin/revenue/overview')
}

/**
 * 鑾峰彇浠婃棩钀ユ敹
 */
export function getTodayRevenue() {
  return request.get('/api/admin/revenue/today')
}

/**
 * 鑾峰彇鏈湀钀ユ敹
 */
export function getMonthRevenue() {
  return request.get('/api/admin/revenue/month')
}

/**
 * 鑾峰彇鏃ヨ惀鏀惰秼鍔? */
export function getDailyRevenueTrend(params) {
  return request.get('/api/admin/revenue/trend/daily', { params })
}

/**
 * 鑾峰彇鏈堣惀鏀惰秼鍔? */
export function getMonthlyRevenueTrend(year) {
  return request.get('/api/admin/revenue/trend/monthly', { params: { year } })
}

/**
 * 鎸夋敮浠樻笭閬撶粺璁? */
export function getRevenueByChannel(params) {
  return request.get('/api/admin/revenue/by-channel', { params })
}

/**
 * 鎸変笟鍔＄被鍨嬬粺璁? */
export function getRevenueByBusiness(params) {
  return request.get('/api/admin/revenue/by-business', { params })
}

/**
 * 鑾峰彇璁㈠崟缁熻
 */
export function getOrderStats(params) {
  return request.get('/api/admin/revenue/orders', { params })
}

/**
 * 鑾峰彇浠樿垂鐢ㄦ埛缁熻
 */
export function getPayingUserStats() {
  return request.get('/api/admin/revenue/users')
}

/**
 * 鑾峰彇Top娑堣垂鐢ㄦ埛
 */
export function getTopConsumers(limit = 10) {
  return request.get('/api/admin/revenue/top-users', { params: { limit } })
}

/**
 * 鑾峰彇浠ｇ悊鍒嗘垚缁熻
 */
export function getAgentCommissionStats() {
  return request.get('/api/admin/revenue/agent-commission')
}

/**
 * 鑾峰彇骞村害鎶ヨ〃
 */
export function getYearlyReport(year) {
  return request.get('/api/admin/revenue/yearly', { params: { year } })
}

