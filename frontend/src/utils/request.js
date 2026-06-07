import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 用于避免多个401错误同时弹出
let isRefreshing = false
let refreshSubscribers = []

// 添加刷新订阅
function subscribeTokenRefresh(callback) {
  refreshSubscribers.push(callback)
}

// 执行刷新回调
function onTokenRefreshed(token) {
  refreshSubscribers.forEach(callback => callback(token))
  refreshSubscribers = []
}

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('access_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    // code !== 200 且不是401/403/404等HTTP错误，只是业务错误
    if (res.code !== 200 && res.code !== 401 && res.code !== 403) {
      // 只对严重错误显示提示
      if (res.message && !res.message.includes('未')) {
        ElMessage.error(res.message || '请求失败')
      }
    }
    return res
  },
  error => {
    // 避免重复弹框
    if (error.__handled__) {
      return Promise.reject(error)
    }
    error.__handled__ = true

    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 只在真正未登录时跳转
          if (!isRefreshing) {
            ElMessage.error('登录已过期，请重新登录')
            localStorage.removeItem('access_token')
            localStorage.removeItem('user_role')
            router.push('/login')
          }
          break
        case 403:
          // 静默处理403，避免多个弹框
          break
        case 404:
          // 静默处理404，避免多个弹框
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          // 只显示有明确消息的错误
          const message = error.response.data?.message
          if (message && !message.includes('未')) {
            ElMessage.error(message)
          }
      }
    } else if (error.request) {
      // 网络错误，静默处理或显示一次
      console.error('Network error:', error)
    }
    return Promise.reject(error)
  }
)

export default request
