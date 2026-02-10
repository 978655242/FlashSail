import request, { type ApiResponse } from './request'
import type { FavoritesRes, BoardsRes, Board, BoardDetail, CreateBoardReq, AddToBoardReq } from '@/types/favorite'

// 添加收藏
export function addFavorite(productId: number) {
  return request.post<ApiResponse<void>>('/favorites', { productId })
}

// 取消收藏
export function removeFavorite(productId: number) {
  return request.delete<ApiResponse<void>>(`/favorites/${productId}`)
}

// 获取收藏列表
export function getFavorites(params: { page?: number; pageSize?: number } = {}) {
  return request.get<ApiResponse<FavoritesRes>>('/favorites', {
    params: { page: params.page || 1, pageSize: params.pageSize || 20 }
  })
}

// 创建看板
export function createBoard(data: CreateBoardReq) {
  return request.post<ApiResponse<Board>>('/boards', data)
}

// 删除看板
export function deleteBoard(boardId: number) {
  return request.delete<ApiResponse<void>>(`/boards/${boardId}`)
}

// 获取看板列表
export function getBoards() {
  return request.get<ApiResponse<BoardsRes>>('/boards')
}

// 获取看板详情（包含产品列表）
export function getBoardDetail(boardId: number) {
  return request.get<ApiResponse<BoardDetail>>(`/boards/${boardId}`)
}

// 添加产品到看板
export function addToBoard(boardId: number, data: AddToBoardReq) {
  return request.put<ApiResponse<void>>(`/boards/${boardId}/products`, data)
}

// 从看板移除产品
export function removeFromBoard(boardId: number, productId: number) {
  return request.delete<ApiResponse<void>>(`/boards/${boardId}/products/${productId}`)
}
