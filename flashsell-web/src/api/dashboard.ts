import request, { type ApiResponse } from './request'
import type { ProductDTO, HotProductDTO } from '@/types/product'

// Dashboard Overview Response
export interface DashboardOverviewRes {
  todayNewProducts: number
  potentialHotProducts: number
  favoriteCount: number
  aiAccuracyRate: number
  lastUpdateTime: string
}

// Search History DTO
export interface SearchHistoryDTO {
  id: number
  query: string
  resultCount: number
  createdAt: string
}

// Recent Activity Response
export interface RecentActivityRes {
  recentSearches: SearchHistoryDTO[]
  recentBrowsed: ProductDTO[]
}

// Hot Recommendations Response
export interface HotRecommendationsRes {
  products: HotProductDTO[]
  updateTime: string
}

// Trending Category DTO
export interface TrendingCategoryDTO {
  category: {
    id: number
    name: string
    productCount: number
  }
  trendScore: number
  weekOverWeek: number
  hotProductCount: number
}

// Trending Categories Response
export interface TrendingCategoriesRes {
  categories: TrendingCategoryDTO[]
}

// Hot Keyword DTO
export interface HotKeywordDTO {
  keyword: string
  searchCount: number
  trend: 'UP' | 'DOWN' | 'STABLE'
}

// Hot Keywords Response
export interface HotKeywordsRes {
  keywords: HotKeywordDTO[]
}

/**
 * 获取仪表盘数据概览
 */
export const getOverview = async (): Promise<DashboardOverviewRes> => {
  const response = await request.get<ApiResponse<DashboardOverviewRes>>('/dashboard/overview')
  return response.data.data
}

/**
 * 获取 AI 爆品推荐（Top 4）
 */
export const getHotRecommendations = async (): Promise<HotRecommendationsRes> => {
  const response = await request.get<ApiResponse<HotRecommendationsRes>>('/dashboard/hot-recommendations')
  return response.data.data
}

/**
 * 获取最近选品记录（搜索历史和浏览历史）
 */
export const getRecentActivity = async (): Promise<RecentActivityRes> => {
  const response = await request.get<ApiResponse<RecentActivityRes>>('/dashboard/recent-activity')
  return response.data.data
}

/**
 * 获取热门品类趋势
 */
export const getTrendingCategories = async (): Promise<TrendingCategoriesRes> => {
  const response = await request.get<ApiResponse<TrendingCategoriesRes>>('/dashboard/trending-categories')
  return response.data.data
}

/**
 * 获取热门搜索关键词
 */
export const getHotKeywords = async (): Promise<HotKeywordsRes> => {
  const response = await request.get<ApiResponse<HotKeywordsRes>>('/dashboard/hot-keywords')
  return response.data.data
}
