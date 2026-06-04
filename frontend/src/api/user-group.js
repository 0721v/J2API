import request from './request'

// 用户分组管理 API

export function getUserGroups(params) {
  return request.get('/user-groups', { params })
}

export function getUserGroup(id) {
  return request.get(`/user-groups/${id}`)
}

export function createUserGroup(data) {
  return request.post('/user-groups', data)
}

export function updateUserGroup(id, data) {
  return request.put(`/user-groups/${id}`, data)
}

export function deleteUserGroup(id) {
  return request.delete(`/user-groups/${id}`)
}

export function assignUserToGroup(userId, groupId) {
  return request.post(`/user-groups/assign`, { userId, groupId })
}

export function getUserGroupUsers(groupId, params) {
  return request.get(`/user-groups/${groupId}/users`, { params })
}

export function getUserGroupOptions() {
  return request.get('/user-groups/options')
}
