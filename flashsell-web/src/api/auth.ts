import request, { type ApiResponse } from './request'
import type { LoginReq, LoginRes, RegisterReq, RegisterRes, RefreshReq, RefreshRes } from '@/types/user'

// 用户注册
export function register(data: RegisterReq) {
  return request.post<ApiResponse<RegisterRes>>('/auth/register', data)
}

// 用户登录
export function login(data: LoginReq) {
  return request.post<ApiResponse<LoginRes>>('/auth/login', data)
}

// 刷新令牌
export function refreshToken(data: RefreshReq) {
  return request.post<ApiResponse<RefreshRes>>('/auth/refresh', data)
}

// 用户登出
export function logout() {
  return request.post<ApiResponse<void>>('/auth/logout')
}

// 发送验证码
export function sendVerifyCode(phone: string) {
  return request.post<ApiResponse<void>>('/auth/send-code', { phone })
}
