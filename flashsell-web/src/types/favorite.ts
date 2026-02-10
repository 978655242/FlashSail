import type { ProductDTO } from './product'

export interface Board {
  id: number
  name: string
  productCount: number
}

export interface BoardDetail extends Board {
  createdAt: string
  products: ProductDTO[]
}

export interface BoardsRes {
  boards: Board[]
  maxBoards: number
}

export interface FavoritesRes {
  products: ProductDTO[]
  total: number
  page: number
}

export interface CreateBoardReq {
  name: string
}

export interface AddToBoardReq {
  productIds: number[]
}
