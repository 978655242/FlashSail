<script setup lang="ts">
/**
 * Favorites Page
 *
 * Displays user's favorited products in a grid layout with the ability
 * to remove products from favorites and filter by category tabs.
 *
 * Requirements: 7.1, 7.2, 7.3, 7.5, 7.6
 * - 7.1: Display a grid of favorited product cards
 * - 7.2: Support removing products from favorites
 * - 7.3: Display empty state when no favorites exist
 * - 7.5: Update UI immediately on remove
 * - 7.6: Display total count of favorites
 */
import { ref, onMounted, computed } from 'vue'
import { useFavoritesStore } from '@/stores/favorites'
import { useI18n } from '@/composables/useI18n'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import ProductCard from '@/components/ProductCard.vue'
import ProductDetailModal from '@/components/ProductDetailModal.vue'
import GlassCard from '@/components/GlassCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import CardSkeleton from '@/components/CardSkeleton.vue'
import type { ProductDTO } from '@/types/product'
import type { ProductDetailRes } from '@/types/product'
import { getProductDetail } from '@/api/product'

const favoritesStore = useFavoritesStore()
const { t } = useI18n()
const toast = useToast()
const confirm = useConfirm()

// Product detail modal state
const selectedProductDetail = ref<ProductDetailRes | null>(null)
const showProductModal = ref(false)
const loadingProductDetail = ref(false)

// Category tab state
const selectedCategory = ref<string>('all')

// Category tabs - derived from favorites
const categoryTabs = computed(() => {
  const categories = new Map<string, { id: string; name: string; count: number }>()

  // Add "All" tab
  categories.set('all', { id: 'all', name: '全部收藏', count: favoritesStore.total })

  // Extract unique categories from favorites
  favoritesStore.favorites.forEach(product => {
    const catName = product.categoryName || '未分类'
    if (categories.has(catName)) {
      categories.get(catName)!.count++
    } else {
      categories.set(catName, { id: catName, name: catName, count: 1 })
    }
  })

  return Array.from(categories.values())
})

// Filtered favorites based on selected category
const filteredFavorites = computed(() => {
  if (selectedCategory.value === 'all') {
    return favoritesStore.favorites
  }
  return favoritesStore.favorites.filter(
    product => (product.categoryName || '未分类') === selectedCategory.value
  )
})

// Computed properties
const hasMoreFavorites = computed(() => {
  return favoritesStore.favorites.length < favoritesStore.total
})

const favoritesCount = computed(() => favoritesStore.total)

const isEmpty = computed(() => {
  return !favoritesStore.isLoading && favoritesStore.favorites.length === 0
})

const isFilteredEmpty = computed(() => {
  return !favoritesStore.isLoading && filteredFavorites.value.length === 0 && selectedCategory.value !== 'all'
})

// Lifecycle
onMounted(async () => {
  await favoritesStore.fetchFavorites()
})

// Methods
function handleCategoryChange(categoryId: string) {
  selectedCategory.value = categoryId
}

async function handleRemoveFavorite(product: ProductDTO) {
  const confirmed = await confirm.show({
    title: t('favorites.removeConfirm'),
    message: `${t('favorites.remove')} "${product.title}"?`,
    type: 'warning'
  })

  if (confirmed) {
    try {
      await favoritesStore.removeFavorite(product.id)
      toast.success(t('product.removeFromFavorites'))
    } catch (error) {
      toast.error(t('common.error'))
    }
  }
}

async function loadMoreFavorites() {
  if (!favoritesStore.isLoading && hasMoreFavorites.value) {
    await favoritesStore.fetchFavorites(favoritesStore.page + 1)
  }
}

async function handleProductClick(product: ProductDTO) {
  loadingProductDetail.value = true
  showProductModal.value = true

  try {
    const { data } = await getProductDetail(product.id)
    if (data.code === 0 || data.code === 200) {
      selectedProductDetail.value = data.data
    }
  } catch (error) {
    toast.error(t('common.error'))
    showProductModal.value = false
  } finally {
    loadingProductDetail.value = false
  }
}

function handleCloseProductModal() {
  showProductModal.value = false
  selectedProductDetail.value = null
}

function handleGoToSearch() {
  // Navigate to search page
  window.location.href = '/search'
}

function handleNewBoard() {
  toast.info('新建看板功能即将上线')
}
</script>

<template>
  <div class="favorites-page">
    <!-- Page Header -->
    <div class="flex justify-between items-center mb-8">
      <div>
        <h2 class="text-2xl font-bold text-white">{{ t('favorites.title') }}</h2>
        <p class="text-slate-400 mt-1">{{ t('favorites.total') }}: {{ favoritesCount }}</p>
      </div>
      <div class="flex items-center gap-4">
        <span class="px-3 py-1.5 rounded-lg bg-slate-800/50 text-sm font-medium text-orange-400">
          {{ favoritesCount }} {{ t('nav.favorites') }}
        </span>
        <button
          class="btn-ghost flex items-center gap-2"
          @click="handleNewBoard"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
          </svg>
          新建看板
        </button>
      </div>
    </div>

    <!-- Category Tabs -->
    <div v-if="!isEmpty" class="tabs mb-6">
      <button
        v-for="tab in categoryTabs"
        :key="tab.id"
        :class="['tab', { active: selectedCategory === tab.id }]"
        @click="handleCategoryChange(tab.id)"
      >
        {{ tab.name }}
        <span class="ml-1 text-xs opacity-70">({{ tab.count }})</span>
      </button>
    </div>

    <!-- Empty State -->
    <EmptyState
      v-if="isEmpty"
      :title="t('favorites.empty')"
      :description="t('favorites.emptyHint')"
      icon="favorite"
      :action-text="t('nav.search')"
      @action="handleGoToSearch"
    />

    <!-- Filtered Empty State -->
    <GlassCard v-else-if="isFilteredEmpty" class="p-8 text-center">
      <svg class="w-12 h-12 text-slate-500 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
      </svg>
      <p class="text-slate-400">该分类下暂无收藏</p>
      <button
        class="mt-4 px-4 py-2 text-sm text-orange-400 hover:text-orange-300 transition-colors"
        @click="handleCategoryChange('all')"
      >
        查看全部收藏
      </button>
    </GlassCard>

    <!-- Loading State -->
    <CardSkeleton
      v-else-if="favoritesStore.isLoading && favoritesStore.favorites.length === 0"
      :count="8"
      :columns="4"
    />

    <!-- Favorites Grid - 5 columns as per design spec -->
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5 gap-6">
      <ProductCard
        v-for="product in filteredFavorites"
        :key="product.id"
        :product="product"
        :show-favorite="true"
        :is-favorite="true"
        @click="handleProductClick"
        @favorite="handleRemoveFavorite"
      />
    </div>

    <!-- Load More Button -->
    <div
      v-if="!isEmpty && !isFilteredEmpty && hasMoreFavorites"
      class="mt-8 text-center"
    >
      <button
        class="px-6 py-3 glass-card text-slate-700 dark:text-slate-300 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors"
        @click="loadMoreFavorites"
        :disabled="favoritesStore.isLoading"
      >
        {{ favoritesStore.isLoading ? t('common.loading') : t('common.more') }}
      </button>
    </div>

    <!-- Product Detail Modal -->
    <ProductDetailModal
      v-if="showProductModal"
      :show="showProductModal"
      :product="selectedProductDetail"
      :loading="loadingProductDetail"
      @close="handleCloseProductModal"
    />
  </div>
</template>

<style scoped>
.favorites-page {
  min-height: calc(100vh - 200px);
}

/* Tabs - matching design spec */
.tabs {
  display: flex;
  gap: 4px;
  background: rgba(15, 23, 42, 0.5);
  padding: 4px;
  border-radius: 12px;
  width: fit-content;
  flex-wrap: wrap;
}

.tab {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  border: none;
  background: transparent;
  transition: all 0.2s ease;
}

.tab:hover {
  background: rgba(51, 65, 85, 0.5);
  color: var(--text-primary);
}

.tab.active {
  background: rgba(249, 115, 22, 0.15);
  color: var(--primary);
}

/* Ghost button */
.btn-ghost {
  background: transparent;
  color: var(--text-secondary);
  padding: 10px 16px;
  border-radius: 10px;
  font-weight: 500;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-ghost:hover {
  background: rgba(255, 255, 255, 0.05);
  color: var(--text-primary);
}

/* Light mode */
html.light .tabs {
  background: rgba(0, 0, 0, 0.05);
}

html.light .tab:hover {
  background: rgba(0, 0, 0, 0.05);
}

html.light .tab.active {
  background: rgba(249, 115, 22, 0.1);
}
</style>
