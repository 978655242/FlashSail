import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { ProductDTO } from '@/types/product'
import type { Board, BoardDetail } from '@/types/favorite'
import { 
  getFavorites, 
  addFavorite as addFavoriteApi, 
  removeFavorite as removeFavoriteApi,
  getBoards as getBoardsApi,
  getBoardDetail as getBoardDetailApi,
  createBoard as createBoardApi,
  deleteBoard as deleteBoardApi,
  addToBoard as addToBoardApi,
  removeFromBoard as removeFromBoardApi
} from '@/api/favorites'

export const useFavoritesStore = defineStore('favorites', () => {
  // State
  const favorites = ref<ProductDTO[]>([])
  const favoriteIds = ref<Set<number>>(new Set())
  const boards = ref<Board[]>([])
  const currentBoardDetail = ref<BoardDetail | null>(null)
  const maxBoards = ref(10)
  const total = ref(0)
  const page = ref(1)
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  // Computed
  const favoriteCount = computed(() => favoriteIds.value.size)
  const canCreateBoard = computed(() => boards.value.length < maxBoards.value)

  // Actions
  function isFavorite(productId: number): boolean {
    return favoriteIds.value.has(productId)
  }

  async function fetchFavorites(pageNum = 1, pageSize = 20) {
    isLoading.value = true
    error.value = null

    try {
      const { data } = await getFavorites({ page: pageNum, pageSize })
      if (data.code === 0 || data.code === 200) {
        if (pageNum === 1) {
          setFavorites(data.data.products, data.data.total, pageNum)
        } else {
          appendFavorites(data.data.products, data.data.total, pageNum)
        }
        // 更新收藏 ID 集合
        data.data.products.forEach(p => favoriteIds.value.add(p.id))
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : '加载收藏失败'
    } finally {
      isLoading.value = false
    }
  }

  async function addFavorite(productId: number) {
    try {
      const { data } = await addFavoriteApi(productId)
      if (data.code === 0 || data.code === 200) {
        favoriteIds.value.add(productId)
        total.value++
      }
    } catch (err) {
      console.error('Failed to add favorite:', err)
      throw err
    }
  }

  async function removeFavorite(productId: number) {
    try {
      const { data } = await removeFavoriteApi(productId)
      if (data.code === 0 || data.code === 200) {
        favoriteIds.value.delete(productId)
        favorites.value = favorites.value.filter(p => p.id !== productId)
        total.value--
        
        // 如果当前看板详情中有这个产品，也要移除
        if (currentBoardDetail.value) {
          currentBoardDetail.value.products = currentBoardDetail.value.products.filter(p => p.id !== productId)
          currentBoardDetail.value.productCount--
        }
      }
    } catch (err) {
      console.error('Failed to remove favorite:', err)
      throw err
    }
  }

  async function fetchBoards() {
    isLoading.value = true
    error.value = null

    try {
      const { data } = await getBoardsApi()
      if (data.code === 0 || data.code === 200) {
        setBoards(data.data.boards, data.data.maxBoards)
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : '加载看板失败'
    } finally {
      isLoading.value = false
    }
  }

  async function fetchBoardDetail(boardId: number) {
    isLoading.value = true
    error.value = null

    try {
      const { data } = await getBoardDetailApi(boardId)
      if (data.code === 0 || data.code === 200) {
        currentBoardDetail.value = data.data
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : '加载看板详情失败'
    } finally {
      isLoading.value = false
    }
  }

  async function createBoard(name: string) {
    try {
      const { data } = await createBoardApi({ name })
      if (data.code === 0 || data.code === 200) {
        addBoard(data.data)
        return data.data
      }
    } catch (err) {
      console.error('Failed to create board:', err)
      throw err
    }
  }

  async function deleteBoard(boardId: number) {
    try {
      const { data } = await deleteBoardApi(boardId)
      if (data.code === 0 || data.code === 200) {
        removeBoard(boardId)
        if (currentBoardDetail.value?.id === boardId) {
          currentBoardDetail.value = null
        }
      }
    } catch (err) {
      console.error('Failed to delete board:', err)
      throw err
    }
  }

  async function addProductsToBoard(boardId: number, productIds: number[]) {
    try {
      const { data } = await addToBoardApi(boardId, { productIds })
      if (data.code === 0 || data.code === 200) {
        // 更新看板产品数量
        const board = boards.value.find(b => b.id === boardId)
        if (board) {
          board.productCount += productIds.length
        }
        // 如果当前看板详情是这个看板，刷新详情
        if (currentBoardDetail.value?.id === boardId) {
          await fetchBoardDetail(boardId)
        }
      }
    } catch (err) {
      console.error('Failed to add products to board:', err)
      throw err
    }
  }

  async function removeProductFromBoard(boardId: number, productId: number) {
    try {
      const { data } = await removeFromBoardApi(boardId, productId)
      if (data.code === 0 || data.code === 200) {
        // 更新看板产品数量
        const board = boards.value.find(b => b.id === boardId)
        if (board && board.productCount > 0) {
          board.productCount--
        }
        // 如果当前看板详情是这个看板，移除产品
        if (currentBoardDetail.value?.id === boardId) {
          currentBoardDetail.value.products = currentBoardDetail.value.products.filter(p => p.id !== productId)
          currentBoardDetail.value.productCount--
        }
      }
    } catch (err) {
      console.error('Failed to remove product from board:', err)
      throw err
    }
  }

  function setFavorites(products: ProductDTO[], totalCount: number, currentPage: number) {
    favorites.value = products
    total.value = totalCount
    page.value = currentPage
    // 更新收藏 ID 集合
    favoriteIds.value = new Set(products.map(p => p.id))
  }

  function appendFavorites(products: ProductDTO[], totalCount: number, currentPage: number) {
    favorites.value = [...favorites.value, ...products]
    total.value = totalCount
    page.value = currentPage
  }

  function setBoards(boardList: Board[], max: number) {
    boards.value = boardList
    maxBoards.value = max
  }

  function addBoard(board: Board) {
    boards.value.push(board)
  }

  function removeBoard(boardId: number) {
    boards.value = boards.value.filter(b => b.id !== boardId)
  }

  function updateBoard(boardId: number, updates: Partial<Board>) {
    const index = boards.value.findIndex(b => b.id === boardId)
    if (index !== -1) {
      boards.value[index] = { ...boards.value[index], ...updates }
    }
  }

  function setLoading(loading: boolean) {
    isLoading.value = loading
  }

  function setError(err: string | null) {
    error.value = err
  }

  function clearCurrentBoard() {
    currentBoardDetail.value = null
  }

  return {
    favorites,
    favoriteIds,
    boards,
    currentBoardDetail,
    maxBoards,
    total,
    page,
    isLoading,
    error,
    favoriteCount,
    canCreateBoard,
    isFavorite,
    fetchFavorites,
    setFavorites,
    appendFavorites,
    addFavorite,
    removeFavorite,
    fetchBoards,
    fetchBoardDetail,
    createBoard,
    deleteBoard,
    addProductsToBoard,
    removeProductFromBoard,
    setBoards,
    addBoard,
    removeBoard,
    updateBoard,
    setLoading,
    setError,
    clearCurrentBoard
  }
})
