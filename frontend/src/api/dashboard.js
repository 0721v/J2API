import request from '../utils/request'

// ==================== 仪表盘API ====================

/**
 * 获取仪表盘概览
 */
export function getDashboardOverview() {
  return request.get('/dashboard/overview')
}

/**
 * 获取销售统计
 */
export function getSalesStats(params) {
  return request.get('/dashboard/sales', { params })
}

/**
 * 获取使用统计
 */
export function getUsageStats(params) {
  return request.get('/dashboard/usage', { params })
}

/**
 * 获取渠道分布
 */
export function getChannelDistribution(params) {
  return request.get('/dashboard/channel-distribution', { params })
}

/**
 * 获取模型分布
 */
export function getModelDistribution() {
  return request.get('/dashboard/model-distribution')
}

/**
 * 获取趋势数据
 */
export function getTrend(params) {
  return request.get('/dashboard/trend', { params })
}

/**
 * 获取实时数据
 */
export function getRealtimeData() {
  return request.get('/dashboard/realtime')
}

/**
 * 获取Top排名
 */
export function getTopRanking(params) {
  return request.get('/dashboard/top', { params })
}