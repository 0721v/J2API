import request from '@/utils/request'

// 代理商 API

// 获取代理商等级列表
export function getAgentLevels() {
  return request.get('/agent/levels')
}

// 检查是否是代理商
export function checkAgent() {
  return request.get('/agent/check')
}

// 获取代理商信息
export function getAgentInfo() {
  return request.get('/agent/info')
}

// 获取代理商统计
export function getAgentStats() {
  return request.get('/agent/stats')
}

// 申请成为代理商
export function applyAgent(data) {
  return request.post('/agent/apply', data)
}

// 获取佣金记录
export function getCommissions(params) {
  return request.get('/agent/commissions', { params })
}

// 申请提现
export function applyWithdrawal(data) {
  return request.post('/agent/withdraw', data)
}

// 获取提现记录
export function getWithdrawals(params) {
  return request.get('/agent/withdrawals', { params })
}

// 获取佣金比例
export function getCommissionRate() {
  return request.get('/agent/commission-rate')
}

// 获取提现手续费率
export function getWithdrawalFeeRate() {
  return request.get('/agent/withdrawal-fee-rate')
}

// 根据邀请码获取代理商
export function getAgentByCode(code) {
  return request.get(`/agent/by-code/${code}`)
}

// ==================== 邀请分享 ====================

// 获取邀请链接
export function getInviteLink() {
  return request.get('/agent/invite-link')
}

// 获取邀请统计
export function getInviteStats() {
  return request.get('/agent/invite-stats')
}

// 获取邀请排行榜
export function getInviteLeaderboard(params) {
  return request.get('/agent/invite-leaderboard', { params })
}

// ==================== 下级管理 ====================

// 获取直属用户列表
export function getSubUsers(params) {
  return request.get('/agent/sub-users', { params })
}

// 获取下级代理商列表
export function getSubAgents(params) {
  return request.get('/agent/sub-agents', { params })
}

// ==================== 统计报表 ====================

// 获取佣金统计
export function getCommissionStats(params) {
  return request.get('/agent/commission-stats', { params })
}

// 获取月度佣金汇总
export function getMonthlyCommission(year, month) {
  return request.get(`/agent/commission-monthly?year=${year}&month=${month}`)
}

// 获取佣金趋势
export function getCommissionTrend(params) {
  return request.get('/agent/commission-trend', { params })
}

// ==================== 消息通知 ====================

// 获取通知列表
export function getNotifications(params) {
  return request.get('/agent/notifications', { params })
}

// 获取未读通知数
export function getUnreadCount() {
  return request.get('/agent/notifications/unread-count')
}

// 标记通知已读
export function markAsRead(notificationId) {
  return request.put(`/agent/notifications/${notificationId}/read`)
}

// 标记全部已读
export function markAllAsRead() {
  return request.put('/agent/notifications/read-all')
}

// ==================== 管理端 API ====================

// 获取代理商列表
export function getAgentList(params) {
  return request.get('/agent/admin/list', { params })
}

// 审核代理商申请
export function reviewAgent(agentId, data) {
  return request.post(`/agent/admin/review/${agentId}`, data)
}

// 获取代理商详情
export function getAgentDetail(agentId) {
  return request.get(`/agent/admin/${agentId}`)
}

// 获取所有提现记录
export function getAllWithdrawals(params) {
  return request.get('/agent/admin/withdrawals', { params })
}

// 处理提现申请
export function processWithdrawal(withdrawalId, data) {
  return request.post(`/agent/admin/withdraw/${withdrawalId}`, data)
}

// 获取代理商业绩统计
export function getAgentPerformance(agentId) {
  return request.get(`/agent/admin/${agentId}/performance`)
}

// 批量操作
export function batchReviewAgents(data) {
  return request.post('/agent/admin/batch-review', data)
}
