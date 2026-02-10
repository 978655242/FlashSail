<script setup lang="ts">
/**
 * Analysis Page (Market Analysis)
 * 
 * Displays market analysis data including:
 * - Market trend charts using ECharts
 * - Category performance data
 * - Platform comparison data (Amazon, eBay, AliExpress, TikTok)
 * - Time period and category filters
 * 
 * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5
 */
import { ref, onMounted, computed, watch } from 'vue'
import GlassCard from '@/components/GlassCard.vue'
import Badge from '@/components/Badge.vue'
import TrendChart from '@/components/TrendChart.vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import { useI18n } from '@/composables/useI18n'
import { getMarketAnalysis, checkDataAvailability } from '@/api/market'
import type { MarketAnalysisRes } from '@/types/market'
import type { Category } from '@/types/product'

const { t } = useI18n()

// State
const isLoading = ref(true)
const error = ref<string | null>(null)
const marketData = ref<MarketAnalysisRes | null>(null)

// Filters
const selectedCategoryId = ref<number | null>(null)
const selectedTimeRange = ref<number>(30)

// Time range options
const timeRangeOptions = computed(() => [
  { label: t('analysis.timeRange.week'), value: 7 },
  { label: t('analysis.timeRange.month'), value: 30 },
  { label: t('analysis.timeRange.quarter'), value: 90 },
  { label: t('analysis.timeRange.year'), value: 365 }
])

// Mock categories (in real app, fetch from API)
const categories = ref<Category[]>([
  { id: 1, name: '工业用品', productCount: 100 },
  { id: 2, name: '节日装饰', productCount: 150 },
  { id: 3, name: '家居生活', productCount: 200 },
  { id: 4, name: '电子产品', productCount: 180 },
  { id: 5, name: '服装配饰', productCount: 220 }
])


// Platform data for comparison
const platforms = computed(() => [
  {
    id: 'amazon',
    name: t('platforms.amazon'),
    icon: 'amazon',
    color: '#FF9900',
    metrics: {
      avgPrice: 45.99,
      totalSales: 125000,
      growth: 12.5,
      marketShare: 35
    }
  },
  {
    id: 'ebay',
    name: t('platforms.ebay'),
    icon: 'ebay',
    color: '#3B82F6',
    metrics: {
      avgPrice: 42.50,
      totalSales: 89000,
      growth: 8.2,
      marketShare: 25
    }
  },
  {
    id: 'aliexpress',
    name: t('platforms.aliexpress'),
    icon: 'aliexpress',
    color: '#EC4899',
    metrics: {
      avgPrice: 28.99,
      totalSales: 156000,
      growth: 18.7,
      marketShare: 28
    }
  },
  {
    id: 'tiktok',
    name: t('platforms.tiktok'),
    icon: 'tiktok',
    color: '#000000',
    metrics: {
      avgPrice: 35.50,
      totalSales: 67000,
      growth: 45.3,
      marketShare: 12
    }
  }
])

// Computed trend chart data
const trendChartData = computed(() => {
  if (!marketData.value?.salesDistribution) return []
  return marketData.value.salesDistribution.map(d => ({
    date: d.date,
    value: d.sales,
    secondaryValue: d.revenue
  }))
})

// Category performance data
const categoryPerformance = computed(() => {
  if (!marketData.value) {
    // Return mock data when no API data
    return [
      { id: 1, name: '工业用品', growth: 15.2, sales: 45000, trend: 'up' },
      { id: 2, name: '节日装饰', growth: 28.5, sales: 78000, trend: 'up' },
      { id: 3, name: '家居生活', growth: -5.3, sales: 32000, trend: 'down' },
      { id: 4, name: '电子产品', growth: 8.7, sales: 56000, trend: 'up' },
      { id: 5, name: '服装配饰', growth: 12.1, sales: 41000, trend: 'up' }
    ]
  }
  return []
})

// Format number helper
function formatNumber(num: number): string {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K'
  }
  return num.toString()
}

// Format percentage helper
function formatPercent(num: number): string {
  const sign = num >= 0 ? '+' : ''
  return `${sign}${num.toFixed(1)}%`
}

// Get trend color
function getTrendColor(value: number): string {
  return value >= 0 ? 'text-green-500' : 'text-red-500'
}

// Load market data
async function loadMarketData() {
  if (!selectedCategoryId.value) {
    // Use mock data when no category selected
    isLoading.value = false
    return
  }

  isLoading.value = true
  error.value = null

  try {
    // Check data availability
    const checkRes = await checkDataAvailability(selectedCategoryId.value)
    if (!checkRes.data.data) {
      error.value = t('common.noData')
      isLoading.value = false
      return
    }

    // Fetch market analysis
    const response = await getMarketAnalysis(selectedCategoryId.value, selectedTimeRange.value)
    if (response.data.code === 200 && response.data.data) {
      marketData.value = response.data.data
    } else {
      error.value = response.data.message || t('common.error')
    }
  } catch (err) {
    console.error('Failed to load market data:', err)
    error.value = t('common.networkError')
  } finally {
    isLoading.value = false
  }
}

// Watch filter changes
watch([selectedCategoryId, selectedTimeRange], () => {
  loadMarketData()
})

// Initialize
onMounted(() => {
  // Set default category
  if (categories.value.length > 0) {
    selectedCategoryId.value = categories.value[0].id
  }
  loadMarketData()
})

</script>

<template>
  <div class="analysis-page space-y-6">
    <!-- Page Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-white">{{ t('analysis.title') }}</h1>
        <p class="mt-1 text-sm text-slate-400">{{ t('nav.analysis') }}</p>
      </div>
    </div>

    <!-- Filters Section -->
    <GlassCard class="p-6">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- Category Filter -->
        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">
            {{ t('search.filters.category') }}
          </label>
          <select
            v-model="selectedCategoryId"
            class="w-full px-4 py-2.5 bg-slate-800/50 border border-slate-600 rounded-lg 
                   text-white focus:ring-2 focus:ring-orange-500 focus:border-transparent
                   transition-all duration-200"
          >
            <option :value="null">{{ t('common.all') }}</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">
              {{ cat.name }} ({{ cat.productCount }})
            </option>
          </select>
        </div>

        <!-- Time Range Filter -->
        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">
            {{ t('analysis.timeRange.month') }}
          </label>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="option in timeRangeOptions"
              :key="option.value"
              @click="selectedTimeRange = option.value"
              :class="[
                'px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200',
                selectedTimeRange === option.value
                  ? 'bg-orange-500 text-white'
                  : 'bg-slate-700/50 text-slate-300 hover:bg-slate-600/50'
              ]"
            >
              {{ option.label }}
            </button>
          </div>
        </div>
      </div>
    </GlassCard>

    <!-- Loading State -->
    <div v-if="isLoading" class="space-y-6">
      <GlassCard class="p-6">
        <SkeletonLoader type="text" :lines="1" height="2rem" class="mb-4" />
        <SkeletonLoader type="custom" height="320px" />
      </GlassCard>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <GlassCard v-for="i in 4" :key="i" class="p-6">
          <SkeletonLoader type="text" :lines="3" />
        </GlassCard>
      </div>
    </div>

    <!-- Error State -->
    <ErrorMessage v-else-if="error" :message="error" @retry="loadMarketData" />

    <!-- Content -->
    <template v-else>
      <!-- Market Trend Chart Section -->
      <GlassCard class="p-6">
        <h2 class="text-lg font-semibold text-white mb-4">{{ t('analysis.marketTrend') }}</h2>
        <TrendChart
          :data="trendChartData"
          :primary-label="t('product.sales')"
          :secondary-label="t('product.price')"
          height="320px"
          :loading="isLoading"
        />
      </GlassCard>

      <!-- Category Performance Section -->
      <GlassCard class="p-6">
        <h2 class="text-lg font-semibold text-white mb-4">{{ t('analysis.categoryPerformance') }}</h2>
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-4">
          <div
            v-for="category in categoryPerformance"
            :key="category.id"
            class="p-4 bg-slate-800/30 rounded-xl border border-slate-700/50 
                   hover:border-orange-500/30 transition-all duration-200"
          >
            <div class="flex items-center justify-between mb-2">
              <span class="text-sm font-medium text-white">{{ category.name }}</span>
              <Badge :variant="category.trend === 'up' ? 'trending' : 'hot'" size="sm">
                {{ category.trend === 'up' ? t('analysis.growth') : t('analysis.decline') }}
              </Badge>
            </div>
            <div class="text-2xl font-bold text-white mb-1">
              {{ formatNumber(category.sales) }}
            </div>
            <div :class="['text-sm font-medium', getTrendColor(category.growth)]">
              {{ formatPercent(category.growth) }}
            </div>
          </div>
        </div>
      </GlassCard>

      <!-- Platform Comparison Section -->
      <GlassCard class="p-6">
        <h2 class="text-lg font-semibold text-white mb-4">{{ t('analysis.platformComparison') }}</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div
            v-for="platform in platforms"
            :key="platform.id"
            class="p-5 bg-slate-800/30 rounded-xl border border-slate-700/50 
                   hover:border-slate-600 transition-all duration-200"
          >
            <!-- Platform Header -->
            <div class="flex items-center gap-3 mb-4">
              <div 
                class="w-10 h-10 rounded-lg flex items-center justify-center"
                :style="{ backgroundColor: `${platform.color}20` }"
              >
                <Badge :variant="platform.icon as any" size="sm" show-icon>
                  {{ '' }}
                </Badge>
              </div>
              <div>
                <h3 class="font-semibold text-white">{{ platform.name }}</h3>
                <span class="text-xs text-slate-400">
                  {{ platform.metrics.marketShare }}% {{ t('product.sales') }}
                </span>
              </div>
            </div>

            <!-- Platform Metrics -->
            <div class="space-y-3">
              <!-- Average Price -->
              <div class="flex items-center justify-between">
                <span class="text-sm text-slate-400">{{ t('product.price') }}</span>
                <span class="text-sm font-medium text-white">
                  ${{ platform.metrics.avgPrice.toFixed(2) }}
                </span>
              </div>

              <!-- Total Sales -->
              <div class="flex items-center justify-between">
                <span class="text-sm text-slate-400">{{ t('product.sales') }}</span>
                <span class="text-sm font-medium text-white">
                  {{ formatNumber(platform.metrics.totalSales) }}
                </span>
              </div>

              <!-- Growth -->
              <div class="flex items-center justify-between">
                <span class="text-sm text-slate-400">{{ t('analysis.growth') }}</span>
                <span :class="['text-sm font-medium', getTrendColor(platform.metrics.growth)]">
                  {{ formatPercent(platform.metrics.growth) }}
                </span>
              </div>

              <!-- Progress Bar for Market Share -->
              <div class="mt-2">
                <div class="h-1.5 bg-slate-700 rounded-full overflow-hidden">
                  <div 
                    class="h-full rounded-full transition-all duration-500"
                    :style="{ 
                      width: `${platform.metrics.marketShare}%`,
                      backgroundColor: platform.color 
                    }"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </GlassCard>

      <!-- Market Data Summary (when API data available) -->
      <GlassCard v-if="marketData" class="p-6">
        <h2 class="text-lg font-semibold text-white mb-4">市场概览</h2>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <!-- Market Size -->
          <div class="text-center p-4 bg-slate-800/30 rounded-xl">
            <div class="text-2xl font-bold text-white">
              {{ formatNumber(marketData.marketSize) }}
            </div>
            <div class="text-sm text-slate-400 mt-1">市场规模</div>
          </div>

          <!-- Competition Score -->
          <div class="text-center p-4 bg-slate-800/30 rounded-xl">
            <div class="text-2xl font-bold text-orange-500">
              {{ (marketData.competitionScore * 100).toFixed(0) }}
            </div>
            <div class="text-sm text-slate-400 mt-1">竞争强度</div>
          </div>

          <!-- Potential Score -->
          <div class="text-center p-4 bg-slate-800/30 rounded-xl">
            <div class="text-2xl font-bold text-green-500">
              {{ (marketData.potentialScore * 100).toFixed(0) }}
            </div>
            <div class="text-sm text-slate-400 mt-1">潜力评分</div>
          </div>

          <!-- Overall Score -->
          <div class="text-center p-4 bg-slate-800/30 rounded-xl">
            <div class="text-2xl font-bold text-blue-500">
              {{ (marketData.overallScore * 100).toFixed(0) }}
            </div>
            <div class="text-sm text-slate-400 mt-1">综合评分</div>
          </div>
        </div>

        <!-- Trend Description -->
        <div v-if="marketData.trendDescription" class="mt-4 p-4 bg-orange-500/10 border border-orange-500/30 rounded-lg">
          <p class="text-sm text-slate-300">{{ marketData.trendDescription }}</p>
        </div>
      </GlassCard>
    </template>
  </div>
</template>

<style scoped>
.analysis-page {
  min-height: 100%;
}

/* Custom select styling */
select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 8l4 4 4-4'/%3e%3c/svg%3e");
  background-position: right 0.5rem center;
  background-repeat: no-repeat;
  background-size: 1.5em 1.5em;
  padding-right: 2.5rem;
}

select option {
  background-color: #1e293b;
  color: #f8fafc;
}
</style>
