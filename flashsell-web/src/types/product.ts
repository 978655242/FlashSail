export interface ProductDTO {
  id: number
  title: string
  image: string
  price: number
  bsrRank: number | null
  reviewCount: number
  rating: number
  categoryId: number | null
  categoryName: string | null
  competitionScore?: number
  category?: Category
}

export interface Category {
  id: number
  name: string
  productCount: number
}

export interface CategoryGroup {
  id: number
  name: string
  categories: Category[]
}

export interface PricePoint {
  date: string
  price: number
}

export interface ProductDetailRes {
  id: number
  title: string
  image: string
  price?: number
  currentPrice?: number
  priceHistory: PricePoint[]
  bsrRank: number | null
  reviewCount: number
  rating: number
  competitionScore?: number
  aiRecommendation?: string
  categoryId?: number | null
  categoryName?: string | null
  category?: Category
}

export interface SearchReq {
  query: string
  categoryId?: number
  priceMin?: number
  priceMax?: number
  minRating?: number
  page: number
  pageSize: number
}

export interface SearchRes {
  products: ProductDTO[]
  total: number
  page: number
  pageSize: number
  hasMore: boolean
  aiSummary: string
  dataFreshness?: string
  freshnessMessage?: string | null
}

export interface CategoriesRes {
  groups: CategoryGroup[]
}

export interface PriceHistoryRes {
  prices: PricePoint[]
}

// Hot Products Types
export interface HotProductDTO {
  product: ProductDTO
  hotScore: number
  rankInCategory: number
  daysOnList: number
  rankChange: number
  recommendation: string
  recommendDate: string
}

export interface HotProductGroup {
  categoryGroup: CategoryGroup
  products: HotProductDTO[]
}

export interface HotProductsReq {
  categoryGroupId?: number
  categoryId?: number
  date?: string
}

export interface HotProductsRes {
  date: string
  groups: HotProductGroup[]
  total: number
}

export interface HotProductHistoryPoint {
  date: string
  rank: number
  hotScore: number
}

export interface HotProductHistoryRes {
  productId: number
  history: HotProductHistoryPoint[]
}
