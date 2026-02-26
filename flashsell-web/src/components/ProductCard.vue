<script setup lang="ts">
import { computed } from 'vue'
import type { ProductDTO } from '@/types/product'

interface Props {
  product: ProductDTO
  showFavorite?: boolean
  isFavorite?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showFavorite: true,
  isFavorite: false
})

const emit = defineEmits<{
  click: [product: ProductDTO]
  favorite: [product: ProductDTO]
}>()

// 格式化价格
const formattedPrice = computed(() => {
  const price = props.product.price
  return price != null ? `$${price.toFixed(2)}` : '$0.00'
})

// 格式化评分
const formattedRating = computed(() => {
  const rating = props.product.rating
  return rating ? rating.toFixed(1) : '0.0'
})

// 格式化评论数
const formattedReviewCount = computed(() => {
  const count = props.product.reviewCount ?? 0
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}万`
  } else if (count >= 1000) {
    return `${(count / 1000).toFixed(1)}k`
  }
  return count.toString()
})

// 格式化 BSR 排名
const formattedBsr = computed(() => {
  const rank = props.product.bsrRank
  if (rank === null || rank === undefined) return '暂无'
  if (rank >= 10000) {
    return `#${(rank / 10000).toFixed(1)}万`
  } else if (rank >= 1000) {
    return `#${(rank / 1000).toFixed(1)}k`
  }
  return `#${rank}`
})

// 竞争评分颜色
const competitionColor = computed(() => {
  const score = props.product.competitionScore
  if (score === undefined || score === null) return 'text-slate-500'
  if (score >= 0.7) return 'text-red-500'
  if (score >= 0.4) return 'text-yellow-500'
  return 'text-green-500'
})

// 竞争评分文字
const competitionText = computed(() => {
  const score = props.product.competitionScore
  if (score === undefined || score === null) return '未知'
  if (score >= 0.7) return '高竞争'
  if (score >= 0.4) return '中竞争'
  return '低竞争'
})

function handleClick() {
  emit('click', props.product)
}

function handleFavorite(e: Event) {
  e.stopPropagation()
  emit('favorite', props.product)
}
</script>

<template>
  <div
    class="group glass-card rounded-xl overflow-hidden cursor-pointer hover:shadow-md hover:border-orange-500/50 transition-all duration-200"
    tabindex="0"
    role="button"
    :aria-label="`查看产品详情: ${product.title}`"
    @click="handleClick"
    @keydown.enter="handleClick"
    @keydown.space.prevent="handleClick"
  >
    <!-- 图片区域 -->
    <div class="relative aspect-square product-image-bg overflow-hidden">
      <img
        :src="product.image || 'https://via.placeholder.com/300'"
        :alt="product.title"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
        loading="lazy"
      />

      <!-- 收藏按钮 -->
      <button
        v-if="showFavorite"
        :class="[
          'absolute top-2 right-2 p-2 rounded-full transition-all duration-200',
          isFavorite
            ? 'bg-red-500 text-white'
            : 'favorite-btn text-slate-400 hover:text-red-500 opacity-0 group-hover:opacity-100'
        ]"
        @click="handleFavorite"
      >
        <svg
          class="w-5 h-5"
          :fill="isFavorite ? 'currentColor' : 'none'"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
        </svg>
      </button>

      <!-- BSR 排名标签 -->
      <div class="absolute bottom-2 left-2 px-2 py-1 bg-black/60 text-white text-xs rounded">
        BSR {{ formattedBsr }}
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="p-4">
      <!-- 标题 -->
      <h3 class="product-title text-sm font-medium line-clamp-2 mb-2 min-h-[2.5rem]">
        {{ product.title }}
      </h3>

      <!-- 价格和评分 -->
      <div class="flex items-center justify-between mb-3">
        <span class="text-lg font-bold text-orange-400">{{ formattedPrice }}</span>
        <div class="flex items-center gap-1">
          <svg class="w-4 h-4 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
            <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
          </svg>
          <span class="text-sm text-[var(--text-secondary)]">{{ formattedRating }}</span>
          <span class="text-xs text-slate-400">({{ formattedReviewCount }})</span>
        </div>
      </div>

      <!-- 标签 -->
      <div class="flex flex-wrap gap-2">
        <!-- 品类标签 -->
        <span class="category-tag px-2 py-0.5 text-xs rounded">
          {{ product.categoryName || product.category?.name || '未分类' }}
        </span>
        <!-- 竞争度标签 -->
        <span :class="['px-2 py-0.5 text-xs rounded', competitionColor, 'bg-opacity-10']">
          {{ competitionText }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-image-bg {
  background-color: var(--bg-card-hover);
}

.favorite-btn {
  background: var(--glass-bg-dark);
}

.favorite-btn:hover {
  background: var(--bg-card);
}

.product-title {
  color: var(--text-primary);
}

.category-tag {
  background: var(--bg-card-hover);
  color: var(--text-secondary);
}

/* Light mode overrides */
:global(html.light) .product-image-bg {
  background-color: #f1f5f9;
}

:global(html.light) .favorite-btn {
  background: rgba(255, 255, 255, 0.9);
}

:global(html.light) .category-tag {
  background: #f1f5f9;
  color: #475569;
}
</style>
