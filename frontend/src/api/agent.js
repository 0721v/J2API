import request from '../utils/request'

// Agent API

export function getAgentLevels() {
  return request.get('/agent/levels')
}

export function checkAgent() {
  return request.get('/agent/check')
}

export function getAgentInfo() {
  return request.get('/agent/info')
}

export function getAgentStats() {
  return request.get('/agent/stats')
}

export function applyAgent(data) {
  return request.post('/agent/apply', data)
}

export function getCommissions(params) {
  return request.get('/agent/commissions', { params })
}

export function applyWithdrawal(data) {
  return request.post('/agent/withdraw', data)
}

export function getWithdrawals(params) {
  return request.get('/agent/withdrawals', { params })
}

export function getCommissionRate() {
  return request.get('/agent/commission-rate')
}

export function getWithdrawalFeeRate() {
  return request.get('/agent/withdrawal-fee-rate')
}

export function getAgentByCode(code) {
  return request.get(`/agent/by-code/${code}`)
}

export function getInviteLink() {
  return request.get('/agent/invite-link')
}

export function getInviteStats() {
  return request.get('/agent/invite-stats')
}

export function getInviteLeaderboard(params) {
  return request.get('/agent/invite-leaderboard', { params })
}

export function getSubUsers(params) {
  return request.get('/agent/sub-users', { params })
}

export function getSubAgents(params) {
  return request.get('/agent/sub-agents', { params })
}

export function getCommissionStats(params) {
  return request.get('/agent/commission-stats', { params })
}

export function getMonthlyCommission(year, month) {
  return request.get(`/agent/commission-monthly?year=${year}&month=${month}`)
}

export function getCommissionTrend(params) {
  return request.get('/agent/commission-trend', { params })
}

export function getNotifications(params) {
  return request.get('/agent/notifications', { params })
}

export function getUnreadCount() {
  return request.get('/agent/notifications/unread-count')
}

export function markAsRead(notificationId) {
  return request.put(`/agent/notifications/${notificationId}/read`)
}

export function markAllAsRead() {
  return request.put('/agent/notifications/read-all')
}

export function getAgentList(params) {
  return request.get('/agent/admin/list', { params })
}

export function reviewAgent(agentId, data) {
  return request.post(`/agent/admin/review/${agentId}`, data)
}

export function getAgentDetail(agentId) {
  return request.get(`/agent/admin/${agentId}`)
}

export function getAllWithdrawals(params) {
  return request.get('/agent/admin/withdrawals', { params })
}

export function processWithdrawal(withdrawalId, data) {
  return request.post(`/agent/admin/withdraw/${withdrawalId}`, data)
}

export function getAgentPerformance(agentId) {
  return request.get(`/agent/admin/${agentId}/performance`)
}

export function batchReviewAgents(data) {
  return request.post('/agent/admin/batch-review', data)
}