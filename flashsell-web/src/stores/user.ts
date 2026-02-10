import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, LoginRes, RegisterRes } from '@/types/user'
import * as authApi from '@/api/auth'

const TOKEN_KEY = 'flashsell_token'
const REFRESH_TOKEN_KEY = 'flashsell_refresh_token'
const USER_INFO_KEY = 'flashsell_user_info'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
  const refreshToken = ref<string | null>(localStorage.getItem(REFRESH_TOKEN_KEY))
  const userInfo = ref<UserInfo | null>(
    JSON.parse(localStorage.getItem(USER_INFO_KEY) || 'null')
  )
  const isLoading = ref(false)

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const subscriptionLevel = computed(() => userInfo.value?.subscriptionLevel || 'FREE')
  const isPremium = computed(() => ['BASIC', 'PRO'].includes(subscriptionLevel.value))

  // Actions
  function setLoginData(data: LoginRes) {
    token.value = data.token
    refreshToken.value = data.refreshToken
    userInfo.value = data.userInfo

    localStorage.setItem(TOKEN_KEY, data.token)
    localStorage.setItem(REFRESH_TOKEN_KEY, data.refreshToken)
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(data.userInfo))
  }

  function setRegisterData(data: RegisterRes) {
    token.value = data.token
    refreshToken.value = data.refreshToken
    // 注册后用户信息可能不完整，设置默认值
    userInfo.value = {
      userId: data.userId,
      nickname: '',
      avatar: '',
      email: '',
      phone: '',
      subscriptionLevel: 'FREE',
      subscriptionExpireDate: null
    }

    localStorage.setItem(TOKEN_KEY, data.token)
    localStorage.setItem(REFRESH_TOKEN_KEY, data.refreshToken)
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo.value))
  }

  function updateToken(newToken: string, newRefreshToken: string) {
    token.value = newToken
    refreshToken.value = newRefreshToken

    localStorage.setItem(TOKEN_KEY, newToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, newRefreshToken)
  }

  function updateUserInfo(info: Partial<UserInfo>) {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...info }
      localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo.value))
    }
  }

  async function login(phone: string, verifyCode: string) {
    isLoading.value = true
    try {
      const { data } = await authApi.login({ phone, verifyCode })
      setLoginData(data.data)
      return data.data
    } finally {
      isLoading.value = false
    }
  }

  async function register(phone: string, verifyCode: string) {
    isLoading.value = true
    try {
      const { data } = await authApi.register({ phone, verifyCode })
      setRegisterData(data.data)
      return data.data
    } finally {
      isLoading.value = false
    }
  }

  async function refreshTokenAction() {
    if (!refreshToken.value) {
      throw new Error('No refresh token available')
    }
    try {
      const { data } = await authApi.refreshToken({ refreshToken: refreshToken.value })
      updateToken(data.data.token, data.data.refreshToken)
      return data.data
    } catch (error) {
      // 刷新失败，清除登录状态
      logout()
      throw error
    }
  }

  async function logoutAction() {
    try {
      await authApi.logout()
    } catch {
      // 即使 API 调用失败，也清除本地状态
    } finally {
      logout()
    }
  }

  function logout() {
    token.value = null
    refreshToken.value = null
    userInfo.value = null

    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
    localStorage.removeItem(USER_INFO_KEY)
  }

  // 检查 token 是否需要刷新（可选：基于 JWT 过期时间）
  function checkTokenExpiry() {
    if (!token.value) return false
    try {
      // 解析 JWT payload（不验证签名，仅用于检查过期时间）
      const payload = JSON.parse(atob(token.value.split('.')[1]))
      const exp = payload.exp * 1000 // 转换为毫秒
      const now = Date.now()
      // 如果 token 将在 5 分钟内过期，返回 true
      return exp - now < 5 * 60 * 1000
    } catch {
      return false
    }
  }

  return {
    // State
    token,
    refreshToken,
    userInfo,
    isLoading,
    // Getters
    isLoggedIn,
    subscriptionLevel,
    isPremium,
    // Actions
    setLoginData,
    setRegisterData,
    updateToken,
    updateUserInfo,
    login,
    register,
    refreshTokenAction,
    logoutAction,
    logout,
    checkTokenExpiry
  }
})
