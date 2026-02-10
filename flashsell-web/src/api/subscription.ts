import request from './request'

// 订阅套餐
export interface SubscriptionPlan {
  id: number
  name: string
  description: string
  price: number
  durationDays: number
  searchLimit: number
  exportLimit: number
  boardLimit: number
  aiAnalysisEnabled: boolean
  apiAccessEnabled: boolean
  level: string
}

// 订阅订单
export interface SubscriptionOrder {
  id: number
  orderNo: string
  planId: number
  amount: number
  status: string
  alipayTradeNo: string | null
  paidAt: string | null
  createdAt: string
  paymentUrl?: string
}

// 订阅状态
export interface SubscriptionStatus {
  level: string
  expireDate: string | null
  isActive: boolean
}

/**
 * 获取所有可用套餐
 */
export function getPlans() {
  return request<{ data: SubscriptionPlan[] }>({
    url: '/subscription/plans',
    method: 'GET'
  })
}

/**
 * 创建订阅订单
 */
export function createOrder(planId: number) {
  return request<{ data: SubscriptionOrder }>({
    url: '/subscription/orders',
    method: 'POST',
    data: { planId }
  })
}

/**
 * 查询订单状态
 */
export function getOrderStatus(orderNo: string) {
  return request<{ data: SubscriptionOrder }>({
    url: '/subscription/orders/status',
    method: 'GET',
    params: { orderNo }
  })
}

/**
 * 获取用户订阅状态
 */
export function getSubscriptionStatus() {
  return request<{ data: SubscriptionStatus }>({
    url: '/subscription/status',
    method: 'GET'
  })
}

export default {
  getPlans,
  createOrder,
  getOrderStatus,
  getSubscriptionStatus
}
