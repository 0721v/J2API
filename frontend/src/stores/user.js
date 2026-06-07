import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserInfo, login as apiLogin, register as apiRegister, logout as apiLogout, getMyInviteCode, getInviteRewards } from '@/api/user'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(localStorage.getItem('access_token') || '')
  const refreshToken = ref(localStorage.getItem('refresh_token') || '')
  const userInfo = ref(null)
  const locale = ref(localStorage.getItem('locale') || 'zh-CN')
  const theme = ref(localStorage.getItem('theme') || 'light')
  const inviteCode = ref(localStorage.getItem('invite_code') || '')
  const inviteRewards = ref(null)
  
  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')
  const balance = computed(() => userInfo.value?.balance || 0)
  const myInviteCode = computed(() => userInfo.value?.inviteCode || '')
  
  // Actions
  async function login(loginKey, password) {
    try {
      const res = await apiLogin(loginKey, password)
      if (res.code === 200) {
        setToken(res.data.accessToken, res.data.refreshToken)
        await fetchUserInfo()
        return true
      }
      return false
    } catch (error) {
      console.error('Login failed:', error)
      return false
    }
  }
  
  async function register(username, email, password, inviteCodeParam = null) {
    try {
      const res = await apiRegister(username, email, password, inviteCodeParam)
      if (res.code === 200) {
        setToken(res.data.accessToken, res.data.refreshToken)
        await fetchUserInfo()
        return true
      }
      return false
    } catch (error) {
      console.error('Register failed:', error)
      return false
    }
  }
  
  async function logout() {
    try {
      await apiLogout()
    } catch (e) {
      // ignore
    }
    clearToken()
    userInfo.value = null
    router.push('/login')
  }
  
  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const res = await getUserInfo()
      if (res.code === 200) {
        userInfo.value = res.data
        // 存储用户角色到 localStorage，供路由守卫使用
        if (res.data.role) {
          localStorage.setItem('user_role', res.data.role)
        }
      }
    } catch (error) {
      console.error('Fetch user info failed:', error)
      if (error.response?.status === 401) {
        clearToken()
        localStorage.removeItem('user_role')
        router.push('/login')
      }
    }
  }
  
  function setToken(accessToken, refresh) {
    token.value = accessToken
    refreshToken.value = refresh
    localStorage.setItem('access_token', accessToken)
    localStorage.setItem('refresh_token', refresh)
  }
  
  function clearToken() {
    token.value = ''
    refreshToken.value = ''
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
    localStorage.removeItem('user_role')
  }
  
  function setLocale(newLocale) {
    locale.value = newLocale
    localStorage.setItem('locale', newLocale)
  }
  
  function setTheme(newTheme) {
    theme.value = newTheme
    localStorage.setItem('theme', newTheme)
  }
  
  // Initialize
  if (token.value) {
    fetchUserInfo()
  }
  
  return {
    token,
    refreshToken,
    userInfo,
    locale,
    theme,
    inviteCode,
    inviteRewards,
    isLoggedIn,
    username,
    balance,
    myInviteCode,
    login,
    register,
    logout,
    fetchUserInfo,
    setToken,
    clearToken,
    setLocale,
    setTheme
  }
})
