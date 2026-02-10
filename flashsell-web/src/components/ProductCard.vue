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
  const price = props.product.currentPrice
  return price ? `$${price.toFixed(2)}` : '$0.00'
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
  if (!rank) return '#N/A'
  if (rank >= 10000) {
    return `${(rank / 10000).toFixed(1)}万`
  } else if (rank >= 1000) {
    return `${(rank / 1000).toFixed(1)}k`
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
    @click="handleClick"
  >
    <!-- 图片区域 -->
    <div class="relative aspect-square bg-slate-100 dark:bg-slate-700 overflow-hidden">
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
            : 'bg-white/80 dark:bg-slate-800/80 text-slate-400 hover:text-red-500 opacity-0 group-hover:opacity-100'
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
      <h3 class="text-sm font-medium text-slate-800 dark:text-white line-clamp-2 mb-2 min-h-[2.5rem]">
        {{ product.title }}
      </h3>

      <!-- 价格和评分 -->
      <div class="flex items-center justify-between mb-3">
        <span class="text-lg font-bold text-orange-400">{{ formattedPrice }}</span>
        <div class="flex items-center gap-1">
          <svg class="w-4 h-4 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
            <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
          </svg>
          <span class="text-sm text-slate-600 dark:text-slate-400">{{ formattedRating }}</span>
          <span class="text-xs text-slate-400">({{ formattedReviewCount }})</span>
        </div>
      </div>

      <!-- 标签 -->
      <div class="flex flex-wrap gap-2">
        <!-- 品类标签 -->
        <span class="px-2 py-0.5 text-xs bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-400 rounded">
          {{ product.category?.name || '未分类' }}
        </span>
        <!-- 竞争度标签 -->
        <span :class="['px-2 py-0.5 text-xs rounded', competitionColor, 'bg-opacity-10']">
          {{ competitionText }}
        </span>
      </div>
    </div>
  </div>
</template>
