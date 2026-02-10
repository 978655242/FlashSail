<script setup lang="ts">
/**
 * Favorites Page
 * 
 * Displays user's favorited products in a grid layout with the ability
 * to remove products from favorites.
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
import ProductGrid from '@/components/ProductGrid.vue'
import ProductDetailModal from '@/components/ProductDetailModal.vue'
import GlassCard from '@/components/GlassCard.vue'
import EmptyState from '@/components/EmptyState.vue'
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

// Computed properties
const hasMoreFavorites = computed(() => {
  return favoritesStore.favorites.length < favoritesStore.total
})

const favoritesCount = computed(() => favoritesStore.total)

const isEmpty = computed(() => {
  return !favoritesStore.isLoading && favoritesStore.favorites.length === 0
})

// Lifecycle
onMounted(async () => {
  await favoritesStore.fetchFavorites()
})

// Methods
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
</script>

<template>
  <div class="favorites-page">
    <!-- Page Header -->
    <GlassCard class="mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-slate-900 dark:text-white">
            {{ t('favorites.title') }}
          </h1>
          <p class="text-sm text-slate-500 dark:text-slate-400 mt-1">
            {{ t('favorites.total') }}: {{ favoritesCount }}
          </p>
        </div>
        <div class="flex items-center gap-2">
          <span class="px-3 py-1 bg-orange-500/10 text-orange-500 rounded-full text-sm font-medium">
            {{ favoritesCount }} {{ t('nav.favorites') }}
          </span>
        </div>
      </div>
    </GlassCard>

    <!-- Empty State -->
    <EmptyState
      v-if="isEmpty"
      :title="t('favorites.empty')"
      :description="t('favorites.emptyHint')"
      icon="favorite"
      :action-text="t('nav.search')"
      @action="handleGoToSearch"
    />

    <!-- Favorites Grid -->
    <ProductGrid
      v-else
      :products="favoritesStore.favorites"
      :loading="favoritesStore.isLoading && favoritesStore.favorites.length === 0"
      :skeleton-count="8"
      :show-favorite="true"
      :favorite-ids="favoritesStore.favoriteIds"
      :empty-title="t('favorites.empty')"
      :empty-description="t('favorites.emptyHint')"
      empty-icon="favorite"
      @click="handleProductClick"
      @favorite="handleRemoveFavorite"
    />

    <!-- Load More Button -->
    <div
      v-if="!isEmpty && hasMoreFavorites"
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
</style>
