import request, { type ApiResponse } from './request'
import type { ProductDetailRes, PriceHistoryRes } from '@/types/product'

// 获取产品详情
export function getProductDetail(id: number) {
  return request.get<ApiResponse<ProductDetailRes>>(`/products/${id}`)
}

// 获取价格历史
export function getPriceHistory(id: number) {
  return request.get<ApiResponse<PriceHistoryRes>>(`/products/${id}/price-history`)
}
