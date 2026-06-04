import request from '../utils/request'

export function getUsageStats(params) {
  return request.get('/user/stats', { params })
}

export function getModelRanking(params) {
  return request.get('/user/model-ranking', { params })
}

export function getChannelRanking(params) {
  return request.get('/user/channel-ranking', { params })
}

export function getDailyTrend(params) {
  return request.get('/user/daily-trend', { params })
}

export function getUsageLogs(params) {
  return request.get('/user/usage-logs', { params })
}

