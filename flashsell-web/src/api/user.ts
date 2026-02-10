import request from './request'

// 用户资料
export interface UserProfile {
  id: number
  phone: string
  nickname: string
  avatarUrl: string
  email: string
  subscriptionLevel: string
  subscriptionExpireDate: string | null
  phoneVerified: boolean
  twoFactorEnabled: boolean
  notificationEnabled: boolean
  emailSubscribed: boolean
  lastLoginTime: string
  createdAt: string
}

// 用户设置
export interface UserSettings {
  notificationEnabled: boolean
  emailSubscribed: boolean
}

// 使用统计
export interface UserUsageStats {
  searchCount: number
  exportCount: number
  favoriteCount: number
  boardCount: number
  searchLimit: number
  exportLimit: number
  boardLimit: number
  favoriteLimit: number
}

// 邀请信息
export interface InviteInfo {
  inviteCode: string
  inviteUrl: string
}

/**
 * 获取用户资料
 */
export function getProfile() {
  return request<{ data: UserProfile }>({
    url: '/user/profile',
    method: 'GET'
  })
}

/**
 * 更新用户资料
 */
export function updateProfile(data: { nickname?: string; avatarUrl?: string; email?: string }) {
  return request<{ data: UserProfile }>({
    url: '/user/profile',
    method: 'PUT',
    data
  })
}

/**
 * 获取使用统计
 */
export function getUsage() {
  return request<{ data: UserUsageStats }>({
    url: '/user/usage',
    method: 'GET'
  })
}

/**
 * 获取邀请码
 */
export function getInviteCode() {
  return request<{ data: InviteInfo }>({
    url: '/user/invite-code',
    method: 'GET'
  })
}

/**
 * 更新用户设置
 */
export function updateSettings(data: UserSettings) {
  return request({
    url: '/user/settings',
    method: 'PUT',
    data
  })
}

export default {
  getProfile,
  updateProfile,
  getUsage,
  getInviteCode,
  updateSettings
}
