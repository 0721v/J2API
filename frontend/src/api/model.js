import request from './request'

export function getModels() {
  return request.get('/models')
}

export function getModelsPage(params) {
  return request.get('/models/page', { params })
}

export function getModelsByType(type) {
  return request.get(`/models/type/${type}`)
}

export function getModel(modelId) {
  return request.get(`/models/${modelId}`)
}

export function getModelPricing(modelId) {
  return request.get(`/models/${modelId}/pricing`)
}
