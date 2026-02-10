<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import LoadingState from '@/components/LoadingState.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import ProductCard from '@/components/ProductCard.vue'
import PageHeader from '@/components/PageHeader.vue'
import { getMarketAnalysis, checkDataAvailability, exportMarketReport } from '@/api/market'
import type { MarketAnalysisRes } from '@/types/market'
import type { Category } from '@/types/product'
import { useToast } from '@/composables/useToast'

const toast = useToast()

// 状态管理
const isLoading = ref(true)
const error = ref<string | null>(null)
const marketData = ref<MarketAnalysisRes | null>(null)
const hasData = ref(false)

// 筛选条件
const selectedCategoryId = ref<number | null>(null)
const timeRange = ref<number>(30)
const timeRangeOptions = [
  { label: '最近30天', value: 30 },
  { label: '最近90天', value: 90 },
  { label: '最近一年', value: 365 }
]

// 品类列表（从其他地方获取，这里先用模拟数据）
const categories = ref<Category[]>([
  { id: 1, name: '工业用品', productCount: 100 },
  { id: 2, name: '节日装饰', productCount: 150 },
  { id: 3, name: '家居生活', productCount: 200 }
])

// 图表实例
let salesChart: echarts.ECharts | null = null
let competitionChart: echarts.ECharts | null = null

// 格式化数字
const formatNumber = (num: number) => {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K'
  }
  return num.toString()
}

// 格式化百分比
const formatPercent = (num: number) => {
  return num >= 0 ? `+${num.toFixed(2)}%` : `${num.toFixed(2)}%`
}

// 获取趋势颜色
const getTrendColor = (value: number) => {
  return value >= 0 ? 'text-green-600' : 'text-red-600'
}

// 获取评分颜色
const getScoreColor = (score: number) => {
  if (score >= 0.8) return 'text-green-600'
  if (score >= 0.6) return 'text-blue-600'
  if (score >= 0.4) return 'text-yellow-600'
  return 'text-red-600'
}

// 获取评分等级
const getScoreLevel = (score: number) => {
  if (score >= 0.8) return '优秀'
  if (score >= 0.6) return '良好'
  if (score >= 0.4) return '一般'
  return '较低'
}

// 加载市场分析数据
async function loadMarketData() {
  if (!selectedCategoryId.value) {
    error.value = '请选择品类'
    return
  }

  isLoading.value = true
  error.value = null

  try {
    // 检查数据可用性
    const dataCheckResponse = await checkDataAvailability(selectedCategoryId.value)
    hasData.value = dataCheckResponse.data.data || false

    if (!hasData.value) {
      error.value = '该品类暂无足够数据进行分析'
      isLoading.value = false
      return
    }

    // 获取市场分析数据
    const response = await getMarketAnalysis(selectedCategoryId.value, timeRange.value)
    
    if (response.data.code === 200 && response.data.data) {
      marketData.value = response.data.data
      
      // 渲染图表
      await renderCharts()
    } else {
      error.value = response.data.message || '获取市场分析数据失败'
    }
  } catch (err) {
    console.error('Failed to load market data:', err)
    error.value = '加载市场分析数据失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

// 渲染图表
async function renderCharts() {
  await new Promise(resolve => setTimeout(resolve, 100))
  renderSalesChart()
  renderCompetitionChart()
}

// 渲染销量分布图表
function renderSalesChart() {
  if (!marketData.value?.salesDistribution.length) return

  const chartDom = document.getElementById('sales-chart')
  if (!chartDom) return

  if (salesChart) {
    salesChart.dispose()
  }

  salesChart = echarts.init(chartDom)

  const data = marketData.value.salesDistribution
  const dates = data.map(d => d.date)
  const sales = data.map(d => d.sales)
  const revenue = data.map(d => d.revenue)

  const option: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['销量', '销售额'],
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
      data: dates,
      axisLabel: {
        color: '#666',
        rotate: 45
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '销量',
        axisLabel: {
          color: '#666',
          formatter: (value: number) => formatNumber(value)
        }
      },
      {
        type: 'value',
        name: '销售额 ($)',
        axisLabel: {
          color: '#666',
          formatter: (value: number) => '$' + formatNumber(value)
        }
      }
    ],
    series: [
      {
        name: '销量',
        type: 'line',
        data: sales,
        smooth: true,
        itemStyle: {
          color: '#3b82f6'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59, 130, 246, 0.3)' },
            { offset: 1, color: 'rgba(59, 130, 246, 0.05)' }
          ])
        }
      },
      {
        name: '销售额',
        type: 'line',
        yAxisIndex: 1,
        data: revenue,
        smooth: true,
        itemStyle: {
          color: '#10b981'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(16, 185, 129, 0.3)' },
            { offset: 1, color: 'rgba(16, 185, 129, 0.05)' }
          ])
        }
      }
    ]
  }

  salesChart.setOption(option)
}

// 渲染竞争强度雷达图
function renderCompetitionChart() {
  if (!marketData.value) return

  const chartDom = document.getElementById('competition-chart')
  if (!chartDom) return

  if (competitionChart) {
    competitionChart.dispose()
  }

  competitionChart = echarts.init(chartDom)

  const data = marketData.value

  const option: EChartsOption = {
    tooltip: {
      trigger: 'item'
    },
    radar: {
      indicator: [
        { name: '市场规模', max: 1 },
        { name: '增长潜力', max: 1 },
        { name: '竞争强度', max: 1 },
        { name: '进入壁垒', max: 1 },
        { name: '综合评分', max: 1 }
      ],
      radius: '60%'
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: [
              Math.min(data.marketSize / 1000000, 1),
              data.potentialScore,
              data.competitionScore,
              data.entryBarrier,
              data.overallScore
            ],
            name: '市场指标',
            itemStyle: {
              color: '#3b82f6'
            },
            areaStyle: {
              color: 'rgba(59, 130, 246, 0.3)'
            }
          }
        ]
      }
    ]
  }

  competitionChart.setOption(option)
}

// 导出报告
async function handleExportReport() {
  if (!selectedCategoryId.value) {
    toast.warning('请先选择品类')
    return
  }

  try {
    const response = await exportMarketReport(selectedCategoryId.value, timeRange.value)
    
    if (response.data.code === 200 && response.data.data) {
      window.open(response.data.data, '_blank')
      toast.success('报告导出成功')
    } else {
      toast.error(response.data.message || '报告导出失败')
    }
  } catch (err) {
    console.error('Failed to export report:', err)
    toast.error('报告导出失败，请稍后重试')
  }
}

// 监听品类和时间范围变化
watch([selectedCategoryId, timeRange], () => {
  if (selectedCategoryId.value) {
    loadMarketData()
  }
})

// 窗口大小变化时重新渲染图表
function handleResize() {
  salesChart?.resize()
  competitionChart?.resize()
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  
  if (categories.value.length > 0) {
    selectedCategoryId.value = categories.value[0].id
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  salesChart?.dispose()
  competitionChart?.dispose()
})
</script>

<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <PageHeader
      title="市场分析"
      description="深度洞察市场趋势和竞争态势"
    >
      <button
        @click="handleExportReport"
        :disabled="!marketData"
        class="px-4 py-2 btn-gradient-primary text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <svg class="w-5 h-5 inline-block mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        导出报告
      </button>
    </PageHeader>

    <!-- 筛选条件 -->
    <div class="glass-card p-6">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- 品类选择 -->
        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">
            选择品类
          </label>
          <select
            v-model="selectedCategoryId"
            class="w-full px-4 py-2 border border-slate-600 rounded-lg focus:ring-2 focus:ring-orange-500 bg-slate-800 text-white"
          >
            <option :value="null">请选择品类</option>
            <option v-for="category in categories" :key="category.id" :value="category.id">
              {{ category.name }} ({{ category.productCount }} 个产品)
            </option>
          </select>
        </div>

        <!-- 时间范围选择 -->
        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">
            时间范围
          </label>
          <select
            v-model="timeRange"
            class="w-full px-4 py-2 border border-slate-600 rounded-lg focus:ring-2 focus:ring-orange-500 bg-slate-800 text-white"
          >
            <option v-for="option in timeRangeOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <LoadingState v-if="isLoading" message="加载市场分析数据中..." />

    <!-- 错误提示 -->
    <ErrorMessage v-else-if="error" :message="error" @retry="loadMarketData" />

    <!-- 市场分析内容 -->
    <template v-else-if="marketData">
      <!-- 核心指标卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <!-- 市场规模 -->
        <div class="glass-card p-6">
          <div class="flex items-center justify-between mb-2">
            <span class="text-sm text-slate-400">市场规模</span>
            <svg class="w-5 h-5 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
            </svg>
          </div>
          <div class="text-2xl font-bold text-white">
            {{ formatNumber(marketData.marketSize) }}
          </div>
          <div class="text-sm mt-1" :class="getTrendColor(marketData.monthlyGrowthRate)">
            月增长 {{ formatPercent(marketData.monthlyGrowthRate) }}
          </div>
        </div>

        <!-- 竞争强度 -->
        <div class="glass-card p-6">
          <div class="flex items-center justify-between mb-2">
            <span class="text-sm text-slate-400">竞争强度</span>
            <svg class="w-5 h-5 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
          </div>
          <div class="text-2xl font-bold" :class="getScoreColor(marketData.competitionScore)">
            {{ (marketData.competitionScore * 100).toFixed(0) }}
          </div>
          <div class="text-sm text-slate-400 mt-1">
            {{ getScoreLevel(marketData.competitionScore) }}
          </div>
        </div>

        <!-- 进入壁垒 -->
        <div class="glass-card p-6">
          <div class="flex items-center justify-between mb-2">
            <span class="text-sm text-slate-400">进入壁垒</span>
            <svg class="w-5 h-5 text-purple-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
          </div>
          <div class="text-2xl font-bold" :class="getScoreColor(marketData.entryBarrier)">
            {{ (marketData.entryBarrier * 100).toFixed(0) }}
          </div>
          <div class="text-sm text-slate-400 mt-1">
            {{ getScoreLevel(marketData.entryBarrier) }}
          </div>
        </div>

        <!-- 综合评分 -->
        <div class="glass-card p-6">
          <div class="flex items-center justify-between mb-2">
            <span class="text-sm text-slate-400">综合评分</span>
            <svg class="w-5 h-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
            </svg>
          </div>
          <div class="text-2xl font-bold" :class="getScoreColor(marketData.overallScore)">
            {{ (marketData.overallScore * 100).toFixed(0) }}
          </div>
          <div class="text-sm text-slate-400 mt-1">
            {{ getScoreLevel(marketData.overallScore) }}
          </div>
        </div>
      </div>

      <!-- 趋势指标 -->
      <div class="glass-card p-6">
        <h2 class="text-lg font-semibold text-white mb-4">趋势分析</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div>
            <div class="text-sm text-slate-400 mb-1">周环比</div>
            <div class="text-xl font-bold" :class="getTrendColor(marketData.weekOverWeek)">
              {{ formatPercent(marketData.weekOverWeek) }}
            </div>
          </div>
          <div>
            <div class="text-sm text-slate-400 mb-1">月环比</div>
            <div class="text-xl font-bold" :class="getTrendColor(marketData.monthOverMonth)">
              {{ formatPercent(marketData.monthOverMonth) }}
            </div>
          </div>
          <div>
            <div class="text-sm text-slate-400 mb-1">潜力评分</div>
            <div class="text-xl font-bold" :class="getScoreColor(marketData.potentialScore)">
              {{ (marketData.potentialScore * 100).toFixed(0) }}
            </div>
          </div>
        </div>
        <div v-if="marketData.trendDescription" class="mt-4 p-4 bg-orange-500/10 border border-orange-500/30 rounded-lg">
          <p class="text-sm text-slate-300">{{ marketData.trendDescription }}</p>
        </div>
      </div>

      <!-- 销量分布图表 -->
      <div class="glass-card p-6">
        <h2 class="text-lg font-semibold text-white mb-4">销量分布趋势</h2>
        <div id="sales-chart" class="w-full h-80"></div>
      </div>

      <!-- 竞争强度雷达图 -->
      <div class="glass-card p-6">
        <h2 class="text-lg font-semibold text-white mb-4">市场竞争分析</h2>
        <div id="competition-chart" class="w-full h-80"></div>
      </div>

      <!-- 热门产品列表 -->
      <div v-if="marketData.topProducts.length > 0" class="glass-card p-6">
        <h2 class="text-lg font-semibold text-white mb-4">
          热门产品 Top {{ marketData.topProducts.length }}
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-4">
          <ProductCard
            v-for="product in marketData.topProducts"
            :key="product.id"
            :product="product"
          />
        </div>
      </div>

      <!-- 分析信息 -->
      <div class="glass-card p-4">
        <div class="flex items-center justify-between text-sm text-slate-400">
          <span>分析日期: {{ marketData.analysisDate }}</span>
          <span>时间范围: {{ marketData.timeRangeDays }} 天</span>
        </div>
      </div>
    </template>

    <!-- 无数据提示 -->
    <div v-else class="glass-card p-12 text-center">
      <svg class="w-16 h-16 mx-auto text-slate-500 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
      </svg>
      <p class="text-slate-400">请选择品类查看市场分析</p>
    </div>
  </div>
</template>
