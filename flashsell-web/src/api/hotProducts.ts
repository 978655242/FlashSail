import request, { type ApiResponse } from './request'
import type { HotProductsReq, HotProductsRes, HotProductHistoryRes, HotProductDTO } from '@/types/product'

// 获取爆品推荐列表
export function getHotProducts(params: HotProductsReq) {
  return request.get<ApiResponse<HotProductsRes>>('/hot-products', { params })
}

// 获取产品的爆品历史趋势
export function getHotProductHistory(productId: number, days: number = 7) {
  return request.get<ApiResponse<HotProductHistoryRes>>('/hot-products/history', {
    params: { productId, days }
  })
}

// 获取今日 Top 4 爆品（用于首页展示）
export function getTop4HotProducts() {
  return request.get<ApiResponse<HotProductDTO[]>>('/hot-products/top4')
}
