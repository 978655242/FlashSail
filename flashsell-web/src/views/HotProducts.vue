<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getHotProducts } from '@/api/hotProducts'
import { getProductDetail } from '@/api/product'
import { useFavoritesStore } from '@/stores/favorites'
import { useToast } from '@/composables/useToast'
import ProductCard from '@/components/ProductCard.vue'
import ProductDetailModal from '@/components/ProductDetailModal.vue'
import LoadingState from '@/components/LoadingState.vue'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import type { HotProductsRes, HotProductDTO, ProductDetailRes } from '@/types/product'

const favoritesStore = useFavoritesStore()
const toast = useToast()

// 状态
const loading = ref(false)
const error = ref<string | null>(null)
const hotProductsData = ref<HotProductsRes | null>(null)
const selectedCategoryGroupId = ref<number | undefined>(undefined)
const selectedDate = ref<string>(new Date().toISOString().split('T')[0])
const showProductModal = ref(false)
const selectedProductDetail = ref<ProductDetailRes | null>(null)
const loadingProductDetail = ref(false)

// 计算属性
const filteredGroups = computed(() => {
  if (!hotProductsData.value) return []
  if (!selectedCategoryGroupId.value) return hotProductsData.value.groups
  
  return hotProductsData.value.groups.filter(
    group => group.categoryGroup.id === selectedCategoryGroupId.value
  )
})

const allCategoryGroups = computed(() => {
  if (!hotProductsData.value) return []
  return hotProductsData.value.groups.map(group => group.categoryGroup)
})

// 生命周期
onMounted(async () => {
  await loadHotProducts()
  await favoritesStore.fetchFavorites()
})

// 方法
async function loadHotProducts() {
  loading.value = true
  error.value = null
  
  try {
    const { data } = await getHotProducts({
      categoryGroupId: selectedCategoryGroupId.value,
      date: selectedDate.value
    })
    
    if (data.code === 0 || data.code === 200) {
      hotProductsData.value = data.data
    } else {
      error.value = data.message || '加载爆品推荐失败'
    }
  } catch (err: any) {
    error.value = err.message || '加载爆品推荐失败'
    console.error('加载爆品推荐失败:', err)
  } finally {
    loading.value = false
  }
}

async function handleCategoryGroupChange(groupId: number | undefined) {
  selectedCategoryGroupId.value = groupId
  await loadHotProducts()
}

async function handleDateChange() {
  await loadHotProducts()
}

async function handleProductClick(product: HotProductDTO) {
  loadingProductDetail.value = true
  showProductModal.value = true
  
  try {
    const { data } = await getProductDetail(product.product.id)
    if (data.code === 0 || data.code === 200) {
      selectedProductDetail.value = data.data
    }
  } catch (err) {
    toast.error('加载产品详情失败')
    showProductModal.value = false
  } finally {
    loadingProductDetail.value = false
  }
}

function handleCloseProductModal() {
  showProductModal.value = false
  selectedProductDetail.value = null
}

async function handleFavorite(product: HotProductDTO) {
  const productId = product.product.id
  if (favoritesStore.isFavorite(productId)) {
    await favoritesStore.removeFavorite(productId)
  } else {
    await favoritesStore.addFavorite(productId)
  }
}

function getRankChangeIcon(rankChange: number) {
  if (rankChange > 0) return '↑'
  if (rankChange < 0) return '↓'
  return '—'
}

function getRankChangeColor(rankChange: number) {
  if (rankChange > 0) return 'text-green-600 dark:text-green-400'
  if (rankChange < 0) return 'text-red-600 dark:text-red-400'
  return 'text-gray-400 dark:text-gray-500'
}

function formatDate(dateStr: string) {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' })
}
</script>

<template>
  <div class="min-h-full">
    <!-- 页面标题 -->
    <PageHeader
      title="AI 爆品推荐"
      description="每日更新，AI 分析的高潜力爆品"
    />

    <!-- 筛选栏 -->
    <div class="mb-6 glass-card p-4">
      <div class="flex flex-wrap items-center gap-4">
        <!-- 日期选择 -->
        <div class="flex items-center gap-2">
          <label class="text-sm font-medium text-slate-300">日期:</label>
          <input
            v-model="selectedDate"
            type="date"
            :max="new Date().toISOString().split('T')[0]"
            class="px-3 py-2 text-sm border border-slate-600 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent bg-slate-800 text-white"
            @change="handleDateChange"
          />
        </div>

        <!-- 类目组筛选 -->
        <div class="flex items-center gap-2">
          <label class="text-sm font-medium text-slate-300">类目:</label>
          <select
            v-model="selectedCategoryGroupId"
            class="px-3 py-2 text-sm border border-slate-600 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent bg-slate-800 text-white"
            @change="handleCategoryGroupChange(selectedCategoryGroupId)"
          >
            <option :value="undefined">全部类目</option>
            <option
              v-for="group in allCategoryGroups"
              :key="group.id"
              :value="group.id"
            >
              {{ group.name }}
            </option>
          </select>
        </div>

        <!-- 数据更新时间 -->
        <div v-if="hotProductsData" class="ml-auto text-sm text-slate-500">
          数据日期: {{ formatDate(hotProductsData.date) }}
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <LoadingState v-if="loading" />

    <!-- 错误状态 -->
    <div v-else-if="error" class="glass-card p-6 text-center border-red-500/30">
      <svg class="w-12 h-12 text-red-500 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      <p class="text-red-400 mb-4">{{ error }}</p>
      <button
        class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
        @click="loadHotProducts"
      >
        重试
      </button>
    </div>

    <!-- 空状态 -->
    <EmptyState
      v-else-if="!hotProductsData || filteredGroups.length === 0"
      icon="empty"
      title="暂无爆品数据"
      description="该日期暂无爆品推荐数据，请选择其他日期"
    />

    <!-- 爆品列表 -->
    <div v-else class="space-y-8">
      <!-- 按类目组分组展示 -->
      <div
        v-for="group in filteredGroups"
        :key="group.categoryGroup.id"
        class="glass-card overflow-hidden"
      >
        <!-- 类目组标题 -->
        <div class="bg-gradient-to-r from-blue-50 to-purple-50 dark:from-blue-900/20 dark:to-purple-900/20 px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <div class="flex items-center justify-between">
            <div>
              <h2 class="text-lg font-semibold text-white">
                {{ group.categoryGroup.name }}
              </h2>
              <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
                {{ group.products.length }} 个爆品推荐
              </p>
            </div>
            <div class="flex items-center gap-2">
              <svg class="w-5 h-5 text-yellow-500" fill="currentColor" viewBox="0 0 20 20">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
              </svg>
              <span class="text-sm font-medium text-gray-600 dark:text-gray-400">
                Top {{ Math.min(group.products.length, 20) }}
              </span>
            </div>
          </div>
        </div>

        <!-- 产品网格 -->
        <div class="p-6">
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            <div
              v-for="(hotProduct, index) in group.products"
              :key="hotProduct.product.id"
              class="relative"
            >
              <!-- 排名徽章 -->
              <div class="absolute top-2 left-2 z-10">
                <div
                  :class="[
                    'w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold shadow-lg',
                    index === 0 ? 'bg-gradient-to-br from-yellow-400 to-yellow-600 text-white' :
                    index === 1 ? 'bg-gradient-to-br from-gray-300 to-gray-500 text-white' :
                    index === 2 ? 'bg-gradient-to-br from-orange-400 to-orange-600 text-white' :
                    'bg-blue-600 text-white'
                  ]"
                >
                  {{ index + 1 }}
                </div>
              </div>

              <!-- 爆品评分 -->
              <div class="absolute top-2 right-2 z-10">
                <div class="bg-gradient-to-r from-red-500 to-pink-500 text-white px-2 py-1 rounded-lg text-xs font-bold shadow-lg flex items-center gap-1">
                  <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                  </svg>
                  {{ hotProduct.hotScore.toFixed(1) }}
                </div>
              </div>

              <!-- 产品卡片 -->
              <ProductCard
                :product="hotProduct.product"
                :is-favorite="favoritesStore.isFavorite(hotProduct.product.id)"
                @click="handleProductClick(hotProduct)"
                @favorite="handleFavorite(hotProduct)"
              />

              <!-- 爆品指标 -->
              <div class="mt-2 p-3 bg-slate-800/50 rounded-lg space-y-2">
                <!-- 上榜天数 -->
                <div class="flex items-center justify-between text-xs">
                  <span class="text-gray-600 dark:text-gray-400">上榜天数</span>
                  <span class="font-medium text-white">
                    {{ hotProduct.daysOnList }} 天
                  </span>
                </div>

                <!-- 排名变化 -->
                <div class="flex items-center justify-between text-xs">
                  <span class="text-gray-600 dark:text-gray-400">排名变化</span>
                  <span :class="['font-medium flex items-center gap-1', getRankChangeColor(hotProduct.rankChange)]">
                    <span class="text-lg">{{ getRankChangeIcon(hotProduct.rankChange) }}</span>
                    <span v-if="hotProduct.rankChange !== 0">
                      {{ Math.abs(hotProduct.rankChange) }}
                    </span>
                  </span>
                </div>

                <!-- AI 推荐理由 -->
                <div v-if="hotProduct.recommendation" class="pt-2 border-t border-gray-200 dark:border-gray-600">
                  <p class="text-xs text-gray-600 dark:text-gray-400 line-clamp-2">
                    <span class="font-medium text-blue-600 dark:text-blue-400">AI:</span>
                    {{ hotProduct.recommendation }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 总计信息 -->
      <div class="text-center text-sm text-gray-500 dark:text-gray-400 py-4">
        共 {{ hotProductsData.total }} 个爆品推荐
      </div>
    </div>

    <!-- 产品详情弹窗 -->
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
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
