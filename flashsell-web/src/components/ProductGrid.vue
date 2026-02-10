<script setup lang="ts">
import { computed } from 'vue'
import type { ProductDTO } from '@/types/product'
import ProductCard from './ProductCard.vue'
import CardSkeleton from './CardSkeleton.vue'
import EmptyState from './EmptyState.vue'

interface Props {
  /** Array of products to display */
  products: ProductDTO[]
  /** Whether the grid is in loading state */
  loading?: boolean
  /** Number of skeleton cards to show when loading */
  skeletonCount?: number
  /** Whether to show favorite button on cards */
  showFavorite?: boolean
  /** Set of favorite product IDs */
  favoriteIds?: Set<number>
  /** Empty state title */
  emptyTitle?: string
  /** Empty state description */
  emptyDescription?: string
  /** Empty state icon type */
  emptyIcon?: 'empty' | 'search' | 'favorite' | 'error'
  /** Empty state action button text */
  emptyActionText?: string
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  skeletonCount: 8,
  showFavorite: true,
  favoriteIds: () => new Set<number>(),
  emptyTitle: '暂无产品',
  emptyDescription: '',
  emptyIcon: 'empty',
  emptyActionText: ''
})

const emit = defineEmits<{
  /** Emitted when a product card is clicked */
  click: [product: ProductDTO]
  /** Emitted when favorite button is clicked */
  favorite: [product: ProductDTO]
  /** Emitted when empty state action button is clicked */
  emptyAction: []
}>()

/** Check if a product is favorited */
function isFavorite(productId: number): boolean {
  return props.favoriteIds.has(productId)
}

/** Handle product card click */
function handleProductClick(product: ProductDTO) {
  emit('click', product)
}

/** Handle favorite button click */
function handleFavorite(product: ProductDTO) {
  emit('favorite', product)
}

/** Handle empty state action */
function handleEmptyAction() {
  emit('emptyAction')
}

/** Whether to show empty state */
const showEmpty = computed(() => !props.loading && props.products.length === 0)

/** Whether to show product grid */
const showGrid = computed(() => !props.loading && props.products.length > 0)
</script>

<template>
  <!-- Loading State with Skeleton Cards -->
  <CardSkeleton
    v-if="loading"
    :count="skeletonCount"
    :columns="4"
  />

  <!-- Empty State -->
  <EmptyState
    v-else-if="showEmpty"
    :title="emptyTitle"
    :description="emptyDescription"
    :icon="emptyIcon"
    :action-text="emptyActionText"
    @action="handleEmptyAction"
  />

  <!-- Product Grid -->
  <div
    v-else-if="showGrid"
    class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4"
  >
    <ProductCard
      v-for="product in products"
      :key="product.id"
      :product="product"
      :show-favorite="showFavorite"
      :is-favorite="isFavorite(product.id)"
      @click="handleProductClick"
      @favorite="handleFavorite"
    />
  </div>
</template>
