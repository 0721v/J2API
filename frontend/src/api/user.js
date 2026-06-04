import request from './request'

export function login(loginKey, password) {
  return request.post('/auth/login', { loginKey, password })
}

export function register(username, email, password, inviteCode = null) {
  const data = { username, email, password }
  if (inviteCode) {
    data.inviteCode = inviteCode
  }
  return request.post('/auth/register', data)
}

// 邀请码相关 API
export function validateInviteCode(inviteCode) {
  return request.get('/auth/invite/validate', { params: { code: inviteCode } })
}

export function getInviteRewards() {
  return request.get('/auth/invite/rewards')
}

export function getInviteStats() {
  return request.get('/auth/invite/stats')
}

export function getInvitees(params) {
  return request.get('/auth/invite/invitees', { params })
}

export function getMyInviteCode() {
  return request.get('/auth/invite/code')
}

export function logout() {
  return request.post('/auth/logout')
}

export function getUserInfo() {
  return request.get('/auth/me')
}

export function refreshToken(refreshToken) {
  return request.post('/auth/refresh', { refreshToken })
}

export function changePassword(data) {
  return request.post('/auth/change-password', data)
}

export function resetPassword(data) {
  return request.post('/auth/reset-password', data)
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}

export function updateLanguage(language) {
  return request.put('/user/language', null, { params: { language } })
}

export function updateTheme(theme) {
  return request.put('/user/theme', null, { params: { theme } })
}

export function getBalance() {
  return request.get('/user/balance')
}

export function getUserStats(params) {
  return request.get('/user/stats', { params })
}
