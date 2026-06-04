import request from './request'

// ==================== 营收统计API ====================

/**
 * 获取营收概览
 */
export function getRevenueOverview() {
  return request.get('/api/admin/revenue/overview')
}

/**
 * 获取今日营收
 */
export function getTodayRevenue() {
  return request.get('/api/admin/revenue/today')
}

/**
 * 获取本月营收
 */
export function getMonthRevenue() {
  return request.get('/api/admin/revenue/month')
}

/**
 * 获取日营收趋势
 */
export function getDailyRevenueTrend(params) {
  return request.get('/api/admin/revenue/trend/daily', { params })
}

/**
 * 获取月营收趋势
 */
export function getMonthlyRevenueTrend(year) {
  return request.get('/api/admin/revenue/trend/monthly', { params: { year } })
}

/**
 * 按支付渠道统计
 */
export function getRevenueByChannel(params) {
  return request.get('/api/admin/revenue/by-channel', { params })
}

/**
 * 按业务类型统计
 */
export function getRevenueByBusiness(params) {
  return request.get('/api/admin/revenue/by-business', { params })
}

/**
 * 获取订单统计
 */
export function getOrderStats(params) {
  return request.get('/api/admin/revenue/orders', { params })
}

/**
 * 获取付费用户统计
 */
export function getPayingUserStats() {
  return request.get('/api/admin/revenue/users')
}

/**
 * 获取Top消费用户
 */
export function getTopConsumers(limit = 10) {
  return request.get('/api/admin/revenue/top-users', { params: { limit } })
}

/**
 * 获取代理分成统计
 */
export function getAgentCommissionStats() {
  return request.get('/api/admin/revenue/agent-commission')
}

/**
 * 获取年度报表
 */
export function getYearlyReport(year) {
  return request.get('/api/admin/revenue/yearly', { params: { year } })
}
