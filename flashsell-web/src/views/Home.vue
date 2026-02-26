<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import LoadingState from '@/components/LoadingState.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import ProductCard from '@/components/ProductCard.vue'
import PageHeader from '@/components/PageHeader.vue'
import {
  getOverview,
  getHotRecommendations,
  getRecentActivity,
  getTrendingCategories,
  type DashboardOverviewRes,
  type HotRecommendationsRes,
  type RecentActivityRes,
  type TrendingCategoriesRes
} from '@/api/dashboard'
import type { ProductDTO } from '@/types/product'

const router = useRouter()

// 状态管理
const isLoading = ref(true)
const error = ref<string | null>(null)

// 数据状态
const overview = ref<DashboardOverviewRes | null>(null)
const hotRecommendations = ref<HotRecommendationsRes | null>(null)
const recentActivity = ref<RecentActivityRes | null>(null)
const trendingCategories = ref<TrendingCategoriesRes | null>(null)

// 图表实例
let trendChart: echarts.ECharts | null = null

// 格式化更新时间
const formattedUpdateTime = computed(() => {
  if (!overview.value?.lastUpdateTime) return ''
  const date = new Date(overview.value.lastUpdateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
})

// 加载所有数据
async function loadDashboardData() {
  isLoading.value = true
  error.value = null

  try {
    // 并行加载所有数据
    const [overviewData, hotRecsData, activityData, categoriesData] = await Promise.all([
      getOverview(),
      getHotRecommendations(),
      getRecentActivity(),
      getTrendingCategories()
    ])

    overview.value = overviewData
    hotRecommendations.value = hotRecsData
    recentActivity.value = activityData
    trendingCategories.value = categoriesData

    // 渲染图表
    await renderTrendChart()
  } catch (err) {
    console.error('Failed to load dashboard data:', err)
    error.value = '加载仪表盘数据失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

// 渲染热门品类趋势图表
async function renderTrendChart() {
  if (!trendingCategories.value?.categories.length) return

  await new Promise(resolve => setTimeout(resolve, 100)) // 等待 DOM 更新

  const chartDom = document.getElementById('trend-chart')
  if (!chartDom) return

  if (trendChart) {
    trendChart.dispose()
  }

  trendChart = echarts.init(chartDom)

  const categories = trendingCategories.value.categories
  const categoryNames = categories.map(c => c.category.name)
  const trendScores = categories.map(c => c.trendScore)
  const weekOverWeek = categories.map(c => c.weekOverWeek)

  const option: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['趋势评分', '周环比增长'],
      textStyle: {
        color: '#666'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: categoryNames,
      axisLabel: {
        rotate: 45,
        color: '#666'
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '趋势评分',
        axisLabel: {
          color: '#666'
        }
      },
      {
        type: 'value',
        name: '周环比 (%)',
        axisLabel: {
          formatter: '{value}%',
          color: '#666'
        }
      }
    ],
    series: [
      {
        name: '趋势评分',
        type: 'bar',
        data: trendScores,
        itemStyle: {
          color: '#3b82f6'
        }
      },
      {
        name: '周环比增长',
        type: 'line',
        yAxisIndex: 1,
        data: weekOverWeek,
        itemStyle: {
          color: '#10b981'
        },
        lineStyle: {
          width: 2
        }
      }
    ]
  }

  trendChart.setOption(option)

  // 响应式调整
  window.addEventListener('resize', () => {
    trendChart?.resize()
  })
}

// 处理产品卡片点击
function handleProductClick(product: ProductDTO) {
  // TODO: 打开产品详情弹窗或跳转到详情页
  console.log('Product clicked:', product)
}

// 处理搜索历史点击
function handleSearchHistoryClick(query: string) {
  router.push({
    name: 'Search',
    query: { q: query }
  })
}

// 重试加载
function handleRetry() {
  loadDashboardData()
}

// 组件挂载时加载数据
onMounted(() => {
  loadDashboardData()
})

// 组件卸载时销毁图表
onMounted(() => {
  return () => {
    if (trendChart) {
      trendChart.dispose()
      trendChart = null
    }
  }
})
</script>

<template>
  <div>
    <!-- 加载状态 -->
    <LoadingState v-if="isLoading" text="加载仪表盘数据..." />

    <!-- 错误状态 -->
    <ErrorMessage v-else-if="error" :message="error" @retry="handleRetry" />

    <!-- 内容 -->
    <div v-else>
      <!-- 页面标题 -->
      <PageHeader
        title="仪表盘"
        description="欢迎回来，查看今日选品数据"
      >
        <div v-if="formattedUpdateTime" class="text-sm text-slate-500">
          最后更新: {{ formattedUpdateTime }}
        </div>
      </PageHeader>

      <!-- 数据概览卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div class="glass-card p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-[var(--text-muted)]">今日新品发现</p>
              <p class="text-2xl font-bold text-[var(--text-primary)] mt-1">
                {{ overview?.todayNewProducts || 0 }}
              </p>
            </div>
            <div class="w-12 h-12 icon-box icon-box-blue rounded-lg flex items-center justify-center">
              <svg class="w-6 h-6 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
            </div>
          </div>
        </div>

        <div class="glass-card p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-[var(--text-muted)]">潜力爆品推荐</p>
              <p class="text-2xl font-bold text-[var(--text-primary)] mt-1">
                {{ overview?.potentialHotProducts || 0 }}
              </p>
            </div>
            <div class="w-12 h-12 icon-box icon-box-orange rounded-lg flex items-center justify-center">
              <svg class="w-6 h-6 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 18.657A8 8 0 016.343 7.343S7 9 9 10c0-2 .5-5 2.986-7C14 5 16.09 5.777 17.656 7.343A7.975 7.975 0 0120 13a7.975 7.975 0 01-2.343 5.657z" />
              </svg>
            </div>
          </div>
        </div>

        <div class="glass-card p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-[var(--text-muted)]">收藏产品数</p>
              <p class="text-2xl font-bold text-[var(--text-primary)] mt-1">
                {{ overview?.favoriteCount || 0 }}
              </p>
            </div>
            <div class="w-12 h-12 icon-box icon-box-pink rounded-lg flex items-center justify-center">
              <svg class="w-6 h-6 text-pink-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
              </svg>
            </div>
          </div>
        </div>

        <div class="glass-card p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-[var(--text-muted)]">AI 推荐准确率</p>
              <p class="text-2xl font-bold text-[var(--text-primary)] mt-1">
                {{ overview?.aiAccuracyRate || 0 }}%
              </p>
            </div>
            <div class="w-12 h-12 icon-box icon-box-green rounded-lg flex items-center justify-center">
              <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
          </div>
        </div>
      </div>

      <!-- AI 爆品推荐 & 最近活动 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        <!-- AI 爆品推荐 Top 4 -->
        <div class="glass-card p-6">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold text-[var(--text-primary)]">AI 爆品推荐</h2>
            <button
              @click="router.push({ name: 'HotProducts' })"
              class="text-sm text-orange-500 hover:text-orange-600"
            >
              查看更多 →
            </button>
          </div>

          <div v-if="hotRecommendations?.products.length" class="grid grid-cols-2 gap-4">
            <ProductCard
              v-for="hotProduct in hotRecommendations.products.slice(0, 4)"
              :key="hotProduct.product.id"
              :product="hotProduct.product"
              :show-favorite="false"
              @click="handleProductClick"
            />
          </div>
          <p v-else class="empty-text text-center py-8">暂无爆品推荐</p>
        </div>

        <!-- 最近活动 -->
        <div class="glass-card p-6">
          <h2 class="text-lg font-semibold text-[var(--text-primary)] mb-4">最近活动</h2>

          <!-- 最近搜索 -->
          <div v-if="recentActivity?.recentSearches.length" class="mb-6">
            <h3 class="text-sm font-medium text-[var(--text-muted)] mb-3">最近搜索</h3>
            <div class="space-y-2">
              <button
                v-for="search in recentActivity.recentSearches.slice(0, 5)"
                :key="search.id"
                @click="handleSearchHistoryClick(search.query)"
                class="search-history-item w-full flex items-center justify-between p-3 rounded-lg transition-colors text-left"
              >
                <div class="flex items-center gap-2 flex-1 min-w-0">
                  <svg class="w-4 h-4 text-slate-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                  </svg>
                  <span class="text-sm text-[var(--text-secondary)] truncate">{{ search.query }}</span>
                </div>
                <span class="text-xs text-slate-400 flex-shrink-0 ml-2">{{ search.resultCount }} 个结果</span>
              </button>
            </div>
          </div>

          <!-- 最近浏览 -->
          <div v-if="recentActivity?.recentBrowsed.length">
            <h3 class="text-sm font-medium text-[var(--text-muted)] mb-3">最近浏览</h3>
            <div class="grid grid-cols-4 gap-2">
              <button
                v-for="product in recentActivity.recentBrowsed.slice(0, 4)"
                :key="product.id"
                @click="handleProductClick(product)"
                class="aspect-square rounded-lg overflow-hidden hover:ring-2 hover:ring-orange-500 transition-all"
              >
                <img
                  :src="product.image || 'https://via.placeholder.com/100'"
                  :alt="product.title"
                  class="w-full h-full object-cover"
                />
              </button>
            </div>
          </div>

          <p v-if="!recentActivity?.recentSearches.length && !recentActivity?.recentBrowsed.length" class="empty-text text-center py-8">
            暂无最近活动
          </p>
        </div>
      </div>

      <!-- 热门品类趋势图表 -->
      <div class="glass-card p-6">
        <h2 class="text-lg font-semibold text-[var(--text-primary)] mb-4">热门品类趋势</h2>
        <div
          v-if="trendingCategories?.categories.length"
          id="trend-chart"
          class="w-full"
          style="height: 400px;"
        ></div>
        <p v-else class="empty-text text-center py-8">暂无趋势数据</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Icon boxes for stat cards */
.icon-box {
  transition: background-color var(--transition-normal);
}

.icon-box-blue {
  background-color: rgba(59, 130, 246, 0.15);
}

.icon-box-orange {
  background-color: rgba(249, 115, 22, 0.15);
}

.icon-box-pink {
  background-color: rgba(236, 72, 153, 0.15);
}

.icon-box-green {
  background-color: rgba(16, 185, 129, 0.15);
}

/* Search history items */
.search-history-item {
  background: var(--bg-card-hover);
}

.search-history-item:hover {
  background: var(--bg-card);
}

/* Empty text */
.empty-text {
  color: var(--text-muted);
}

/* Light mode overrides */
:global(html.light) .icon-box-blue {
  background-color: rgba(59, 130, 246, 0.1);
}

:global(html.light) .icon-box-orange {
  background-color: rgba(249, 115, 22, 0.1);
}

:global(html.light) .icon-box-pink {
  background-color: rgba(236, 72, 153, 0.1);
}

:global(html.light) .icon-box-green {
  background-color: rgba(16, 185, 129, 0.1);
}
</style>
