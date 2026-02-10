import request, { type ApiResponse } from './request'
import type { SearchReq, SearchRes, CategoriesRes } from '@/types/product'

// AI 选品搜索
export function search(data: SearchReq) {
  return request.post<ApiResponse<SearchRes>>('/search', data)
}

// 获取支持的品类列表
export function getCategories() {
  return request.get<ApiResponse<CategoriesRes>>('/categories')
}

// 获取搜索历史
export function getSearchHistory(params?: { page?: number; pageSize?: number }) {
  return request.get<ApiResponse<{ histories: Array<{ id: number; query: string; resultCount: number; createdAt: string }>; total: number }>>('/search/history', { params })
}

// 删除搜索历史
export function deleteSearchHistory(id: number) {
  return request.delete<ApiResponse<void>>(`/search/history/${id}`)
}

// 清空搜索历史
export function clearSearchHistory() {
  return request.delete<ApiResponse<void>>('/search/history')
}
