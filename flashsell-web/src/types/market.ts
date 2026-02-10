import type { Category, ProductDTO } from './product'

export interface SalesDataPoint {
  date: string
  sales: number
  revenue: number
}

export interface MarketAnalysisReq {
  categoryId: number
  timeRangeDays?: number
}

export interface MarketAnalysisRes {
  category: Category
  marketSize: number
  monthlyGrowthRate: number
  competitionScore: number
  entryBarrier: number
  potentialScore: number
  salesDistribution: SalesDataPoint[]
  weekOverWeek: number
  monthOverMonth: number
  topProducts: ProductDTO[]
  analysisDate: string
  timeRangeDays: number
  trendDescription: string
  overallScore: number
}
