export interface UserInfo {
  userId: number
  nickname: string
  avatar: string
  email: string
  phone: string
  subscriptionLevel: 'FREE' | 'BASIC' | 'PRO'
  subscriptionExpireDate: string | null
}

export interface LoginReq {
  phone: string
  verifyCode: string
}

export interface LoginRes {
  userId: number
  token: string
  refreshToken: string
  userInfo: UserInfo
}

export interface RegisterReq {
  phone: string
  verifyCode: string
}

export interface RegisterRes {
  userId: number
  token: string
  refreshToken: string
}

export interface RefreshReq {
  refreshToken: string
}

export interface RefreshRes {
  token: string
  refreshToken: string
}
