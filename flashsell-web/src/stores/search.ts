import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { ProductDTO, SearchReq, SearchRes, CategoryGroup, ProductDetailRes } from '@/types/product'
import { search as searchApi, getCategories } from '@/api/search'
import { getProductDetail } from '@/api/product'

export const useSearchStore = defineStore('search', () => {
  // State
  const searchQuery = ref('')
  const searchResults = ref<ProductDTO[]>([])
  const total = ref(0)
  const page = ref(1)
  const pageSize = ref(20)
  const hasMore = ref(false)
  const aiSummary = ref('')
  const isLoading = ref(false)
  const isLoadingMore = ref(false)
  const error = ref<string | null>(null)

  // 品类数据
  const categoryGroups = ref<CategoryGroup[]>([])
  const isCategoriesLoading = ref(false)

  // 产品详情
  const selectedProduct = ref<ProductDetailRes | null>(null)
  const isProductDetailLoading = ref(false)
  const showProductModal = ref(false)

  // 筛选条件
  const filters = ref<Partial<SearchReq>>({
    categoryId: undefined,
    priceMin: undefined,
    priceMax: undefined,
    minRating: undefined
  })

  // 搜索历史（本地存储）
  const searchHistory = ref<string[]>(
    JSON.parse(localStorage.getItem('searchHistory') || '[]')
  )

  // Computed
  const hasFilters = computed(() => {
    return filters.value.categoryId !== undefined ||
      filters.value.priceMin !== undefined ||
      filters.value.priceMax !== undefined ||
      filters.value.minRating !== undefined
  })

  const selectedCategory = computed(() => {
    if (!filters.value.categoryId) return null
    for (const group of categoryGroups.value) {
      const category = group.categories.find(c => c.id === filters.value.categoryId)
      if (category) return category
    }
    return null
  })

  // Actions
  async function search(query: string, resetPage = true) {
    if (!query.trim()) return

    searchQuery.value = query
    if (resetPage) {
      page.value = 1
      searchResults.value = []
    }

    isLoading.value = resetPage
    isLoadingMore.value = !resetPage
    error.value = null

    try {
      const { data } = await searchApi({
        query,
        page: page.value,
        pageSize: pageSize.value,
        ...filters.value
      })

      if (data.code === 0 || data.code === 200) {
        if (resetPage) {
          setSearchResults(data.data)
        } else {
          appendSearchResults(data.data)
        }
        // 保存搜索历史
        addToHistory(query)
      } else {
        error.value = data.message || '搜索失败'
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : '搜索失败，请稍后重试'
    } finally {
      isLoading.value = false
      isLoadingMore.value = false
    }
  }

  async function loadMore() {
    if (!hasMore.value || isLoadingMore.value) return
    page.value++
    await search(searchQuery.value, false)
  }

  async function fetchCategories() {
    if (categoryGroups.value.length > 0) return

    isCategoriesLoading.value = true
    try {
      const { data } = await getCategories()
      if (data.code === 0 || data.code === 200) {
        categoryGroups.value = data.data.groups
      }
    } catch (err) {
      console.error('Failed to fetch categories:', err)
    } finally {
      isCategoriesLoading.value = false
    }
  }

  async function fetchProductDetail(productId: number) {
    isProductDetailLoading.value = true
    showProductModal.value = true
    selectedProduct.value = null

    try {
      const { data } = await getProductDetail(productId)
      if (data.code === 0 || data.code === 200) {
        selectedProduct.value = data.data
      }
    } catch (err) {
      console.error('Failed to fetch product detail:', err)
    } finally {
      isProductDetailLoading.value = false
    }
  }

  function closeProductModal() {
    showProductModal.value = false
    selectedProduct.value = null
  }

  function setSearchResults(data: SearchRes) {
    searchResults.value = data.products
    total.value = data.total
    page.value = data.page
    hasMore.value = data.hasMore
    aiSummary.value = data.aiSummary
  }

  function appendSearchResults(data: SearchRes) {
    searchResults.value = [...searchResults.value, ...data.products]
    total.value = data.total
    page.value = data.page
    hasMore.value = data.hasMore
  }

  function setLoading(loading: boolean) {
    isLoading.value = loading
  }

  function setError(err: string | null) {
    error.value = err
  }

  function updateFilters(newFilters: Partial<SearchReq>) {
    filters.value = { ...filters.value, ...newFilters }
  }

  function resetFilters() {
    filters.value = {
      categoryId: undefined,
      priceMin: undefined,
      priceMax: undefined,
      minRating: undefined
    }
  }

  function clearResults() {
    searchResults.value = []
    total.value = 0
    page.value = 1
    hasMore.value = false
    aiSummary.value = ''
    error.value = null
  }

  function addToHistory(query: string) {
    const trimmed = query.trim()
    if (!trimmed) return

    // 移除重复项
    const filtered = searchHistory.value.filter(h => h !== trimmed)
    // 添加到开头
    searchHistory.value = [trimmed, ...filtered].slice(0, 10)
    // 保存到本地存储
    localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
  }

  function removeFromHistory(query: string) {
    searchHistory.value = searchHistory.value.filter(h => h !== query)
    localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
  }

  function clearHistory() {
    searchHistory.value = []
    localStorage.removeItem('searchHistory')
  }

  return {
    // State
    searchQuery,
    searchResults,
    total,
    page,
    pageSize,
    hasMore,
    aiSummary,
    isLoading,
    isLoadingMore,
    error,
    filters,
    categoryGroups,
    isCategoriesLoading,
    selectedProduct,
    isProductDetailLoading,
    showProductModal,
    searchHistory,
    // Computed
    hasFilters,
    selectedCategory,
    // Actions
    search,
    loadMore,
    fetchCategories,
    fetchProductDetail,
    closeProductModal,
    setSearchResults,
    appendSearchResults,
    setLoading,
    setError,
    updateFilters,
    resetFilters,
    clearResults,
    addToHistory,
    removeFromHistory,
    clearHistory
  }
})
