import request, { type ApiResponse } from './request'
import type { MarketAnalysisRes } from '@/types/market'

/**
 * 获取市场分析
 * @param categoryId 品类ID
 * @param timeRangeDays 时间范围（天数：30、90、365），默认30天
 */
export function getMarketAnalysis(categoryId: number, timeRangeDays: number = 30) {
  return request.get<ApiResponse<MarketAnalysisRes>>('/market/analysis', {
    params: {
      categoryId,
      timeRangeDays
    }
  })
}

/**
 * 刷新市场分析
 * 强制重新生成市场分析数据
 * @param categoryId 品类ID
 * @param timeRangeDays 时间范围（天数）
 */
export function refreshMarketAnalysis(categoryId: number, timeRangeDays: number = 30) {
  return request.post<ApiResponse<MarketAnalysisRes>>('/market/analysis/refresh', {
    categoryId,
    timeRangeDays
  })
}

/**
 * 检查品类是否有足够的数据进行分析
 * @param categoryId 品类ID
 */
export function checkDataAvailability(categoryId: number) {
  return request.get<ApiResponse<boolean>>('/market/check-data', {
    params: {
      categoryId
    }
  })
}

/**
 * 导出市场分析报告
 * @param categoryId 品类ID
 * @param timeRangeDays 时间范围（天数）
 */
export function exportMarketReport(categoryId: number, timeRangeDays: number = 30) {
  return request.get<ApiResponse<string>>('/market/export', {
    params: {
      categoryId,
      timeRangeDays
    }
  })
}
