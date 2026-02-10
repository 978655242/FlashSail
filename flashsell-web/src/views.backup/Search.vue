<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useSearchStore } from '@/stores/search'
import { useFavoritesStore } from '@/stores/favorites'
import SearchBar from '@/components/SearchBar.vue'
import CategoryFilter from '@/components/CategoryFilter.vue'
import ProductCard from '@/components/ProductCard.vue'
import CardSkeleton from '@/components/CardSkeleton.vue'
import EmptyState from '@/components/EmptyState.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import ProductDetailModal from '@/components/ProductDetailModal.vue'
import PageHeader from '@/components/PageHeader.vue'

const searchStore = useSearchStore()
const favoritesStore = useFavoritesStore()

// 筛选面板展开状态
const showFilters = ref(true)

// 价格筛选
const priceMin = ref<number | undefined>(undefined)
const priceMax = ref<number | undefined>(undefined)

// 评分筛选
const minRating = ref<number | undefined>(undefined)

// 评分选项
const ratingOptions = [
  { value: undefined, label: '全部' },
  { value: 4.5, label: '4.5+' },
  { value: 4.0, label: '4.0+' },
  { value: 3.5, label: '3.5+' },
  { value: 3.0, label: '3.0+' }
]

onMounted(() => {
  // 加载品类数据
  searchStore.fetchCategories()
  // 加载收藏列表
  favoritesStore.fetchFavorites()
})

// 监听筛选条件变化
watch([priceMin, priceMax, minRating], () => {
  searchStore.updateFilters({
    priceMin: priceMin.value,
    priceMax: priceMax.value,
    minRating: minRating.value
  })
})

function handleSearch(query: string) {
  searchStore.search(query)
}

function handleHistoryClick(query: string) {
  searchStore.search(query)
}

function handleHistoryRemove(query: string) {
  searchStore.removeFromHistory(query)
}

function handleHistoryClear() {
  searchStore.clearHistory()
}

function handleCategorySelect(categoryId: number | undefined) {
  searchStore.updateFilters({ categoryId })
  // 如果已有搜索词，重新搜索
  if (searchStore.searchQuery) {
    searchStore.search(searchStore.searchQuery)
  }
}

function handleProductClick(product: { id: number }) {
  searchStore.fetchProductDetail(product.id)
}

function handleFavorite(product: { id: number }) {
  if (favoritesStore.isFavorite(product.id)) {
    favoritesStore.removeFavorite(product.id)
  } else {
    favoritesStore.addFavorite(product.id)
  }
}

function handleLoadMore() {
  searchStore.loadMore()
}

function handleRetry() {
  if (searchStore.searchQuery) {
    searchStore.search(searchStore.searchQuery)
  }
}

function clearAllFilters() {
  priceMin.value = undefined
  priceMax.value = undefined
  minRating.value = undefined
  searchStore.resetFilters()
  if (searchStore.searchQuery) {
    searchStore.search(searchStore.searchQuery)
  }
}

function applyFilters() {
  if (searchStore.searchQuery) {
    searchStore.search(searchStore.searchQuery)
  }
}
</script>

<template>
  <div class="min-h-full">
    <!-- 页面标题 -->
    <PageHeader
      title="AI 选品搜索"
      description="使用自然语言搜索，AI 帮你找到高潜力爆品"
    />

    <!-- 搜索栏 -->
    <div class="mb-6">
      <SearchBar
        v-model="searchStore.searchQuery"
        :loading="searchStore.isLoading"
        :history="searchStore.searchHistory"
        @search="handleSearch"
        @history-click="handleHistoryClick"
        @history-remove="handleHistoryRemove"
        @history-clear="handleHistoryClear"
      />
    </div>

    <!-- 主内容区 -->
    <div class="flex gap-6">
      <!-- 左侧筛选面板 -->
      <aside
        :class="[
          'flex-shrink-0 transition-all duration-300',
          showFilters ? 'w-64' : 'w-0 overflow-hidden'
        ]"
      >
        <div class="space-y-4">
          <!-- 品类筛选 -->
          <CategoryFilter
            :groups="searchStore.categoryGroups"
            :selected-category-id="searchStore.filters.categoryId"
            :loading="searchStore.isCategoriesLoading"
            @select="handleCategorySelect"
          />

          <!-- 价格筛选 -->
          <div class="glass-card p-4">
            <h3 class="font-medium text-slate-300 mb-3">价格区间</h3>
            <div class="flex items-center gap-2">
              <input
                v-model.number="priceMin"
                type="number"
                placeholder="最低"
                min="0"
                class="w-full px-3 py-2 text-sm border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
              />
              <span class="text-gray-400">-</span>
              <input
                v-model.number="priceMax"
                type="number"
                placeholder="最高"
                min="0"
                class="w-full px-3 py-2 text-sm border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
              />
            </div>
          </div>

          <!-- 评分筛选 -->
          <div class="glass-card p-4">
            <h3 class="font-medium text-slate-300 mb-3">最低评分</h3>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="option in ratingOptions"
                :key="option.label"
                :class="[
                  'px-3 py-1.5 text-sm rounded-lg transition-colors',
                  minRating === option.value
                    ? 'bg-orange-500 text-white'
                    : 'bg-slate-700 text-slate-300 hover:bg-slate-600'
                ]"
                @click="minRating = option.value"
              >
                {{ option.label }}
              </button>
            </div>
          </div>

          <!-- 筛选操作按钮 -->
          <div class="flex gap-2">
            <button
              class="flex-1 px-4 py-2 text-sm btn-gradient-primary"
              @click="applyFilters"
            >
              应用筛选
            </button>
            <button
              v-if="searchStore.hasFilters"
              class="px-4 py-2 text-sm text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-white transition-colors"
              @click="clearAllFilters"
            >
              清除
            </button>
          </div>
        </div>
      </aside>

      <!-- 右侧结果区 -->
      <main class="flex-1 min-w-0">
        <!-- 筛选面板切换按钮 -->
        <button
          class="mb-4 flex items-center gap-2 text-sm text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300"
          @click="showFilters = !showFilters"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
          </svg>
          {{ showFilters ? '隐藏筛选' : '显示筛选' }}
        </button>

        <!-- AI 摘要 -->
        <div
          v-if="searchStore.aiSummary && !searchStore.isLoading"
          class="mb-4 p-4 glass-card border-orange-500/30"
        >
          <div class="flex items-start gap-3">
            <div class="flex-shrink-0 w-8 h-8 bg-orange-500/20 rounded-lg flex items-center justify-center">
              <svg class="w-5 h-5 text-orange-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
              </svg>
            </div>
            <div>
              <h4 class="text-sm font-medium text-orange-400 mb-1">AI 分析</h4>
              <p class="text-sm text-slate-400">{{ searchStore.aiSummary }}</p>
            </div>
          </div>
        </div>

        <!-- 结果统计 -->
        <div v-if="searchStore.total > 0 && !searchStore.isLoading" class="mb-4 flex items-center justify-between">
          <p class="text-sm text-gray-500 dark:text-gray-400">
            共找到 <span class="font-medium text-gray-800 dark:text-white">{{ searchStore.total }}</span> 个产品
          </p>
        </div>

        <!-- 加载状态 -->
        <CardSkeleton v-if="searchStore.isLoading" :count="8" :columns="4" />

        <!-- 错误状态 -->
        <ErrorMessage
          v-else-if="searchStore.error"
          :message="searchStore.error"
          @retry="handleRetry"
        />

        <!-- 空状态 -->
        <EmptyState
          v-else-if="searchStore.searchResults.length === 0 && searchStore.searchQuery"
          icon="search"
          title="未找到相关产品"
          description="尝试使用其他关键词或调整筛选条件"
        />

        <!-- 初始状态 -->
        <div
          v-else-if="searchStore.searchResults.length === 0"
          class="flex flex-col items-center justify-center py-16"
        >
          <div class="w-24 h-24 bg-gray-100 dark:bg-gray-800 rounded-full flex items-center justify-center mb-4">
            <svg class="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
          <h3 class="text-lg font-medium text-gray-800 dark:text-white mb-2">开始搜索</h3>
          <p class="text-sm text-gray-500 dark:text-gray-400 text-center max-w-md">
            输入产品关键词，如"蓝牙耳机"、"手机壳"等，AI 将帮你找到高潜力爆品
          </p>
        </div>

        <!-- 产品列表 -->
        <div v-else>
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            <ProductCard
              v-for="product in searchStore.searchResults"
              :key="product.id"
              :product="product"
              :is-favorite="favoritesStore.isFavorite(product.id)"
              @click="handleProductClick"
              @favorite="handleFavorite"
            />
          </div>

          <!-- 加载更多 -->
          <div v-if="searchStore.hasMore" class="mt-8 flex justify-center">
            <button
              :disabled="searchStore.isLoadingMore"
              class="px-6 py-2.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 transition-colors"
              @click="handleLoadMore"
            >
              <span v-if="searchStore.isLoadingMore" class="flex items-center gap-2">
                <svg class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                </svg>
                加载中...
              </span>
              <span v-else>加载更多</span>
            </button>
          </div>
        </div>
      </main>
    </div>

    <!-- 产品详情弹窗 -->
    <ProductDetailModal
      :show="searchStore.showProductModal"
      :product="searchStore.selectedProduct"
      :loading="searchStore.isProductDetailLoading"
      @close="searchStore.closeProductModal"
    />
  </div>
</template>
