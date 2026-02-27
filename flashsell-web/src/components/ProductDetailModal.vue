<script setup lang="ts">
/**
 * Product Detail Modal Component
 *
 * Displays detailed product information with:
 * - Multi-platform price comparison
 * - Sales statistics and metrics
 * - Price trend chart
 * - Competition radar chart
 * - AI analysis
 * - Action buttons
 */
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import type { ProductDetailRes } from '@/types/product'

interface Props {
  show: boolean
  product: ProductDetailRes | null
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

const emit = defineEmits<{
  close: []
  favorite: [productId: number]
}>()

const priceChartRef = ref<HTMLDivElement | null>(null)
const radarChartRef = ref<HTMLDivElement | null>(null)
let priceChartInstance: echarts.ECharts | null = null
let radarChartInstance: echarts.ECharts | null = null

// 格式化价格
const formattedPrice = computed(() => {
  if (!props.product) return ''
  return `$${(props.product.price ?? props.product.currentPrice ?? 0).toFixed(2)}`
})

// 格式化评分
const formattedRating = computed(() => {
  if (!props.product) return ''
  return props.product.rating.toFixed(1)
})

// 格式化评论数
const formattedReviewCount = computed(() => {
  if (!props.product) return ''
  const count = props.product.reviewCount
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}万`
  } else if (count >= 1000) {
    return `${(count / 1000).toFixed(1)}k`
  }
  return count.toString()
})

// 格式化 BSR 排名
const formattedBsr = computed(() => {
  if (!props.product) return ''
  const rank = props.product.bsrRank
  if (rank === null || rank === undefined) return '暂无数据'
  if (rank >= 10000) {
    return `#${(rank / 10000).toFixed(1)}万`
  } else if (rank >= 1000) {
    return `#${(rank / 1000).toFixed(1)}k`
  }
  return `#${rank}`
})

// 竞争评分颜色
const competitionColor = computed(() => {
  if (!props.product) return ''
  const score = props.product.competitionScore
  if (score === null || score === undefined) return 'text-slate-500'
  if (score >= 0.7) return 'text-red-500'
  if (score >= 0.4) return 'text-yellow-500'
  return 'text-green-500'
})

// 竞争评分文字
const competitionText = computed(() => {
  if (!props.product) return ''
  const score = props.product.competitionScore
  if (score === null || score === undefined) return '暂无数据'
  if (score >= 0.7) return '高竞争'
  if (score >= 0.4) return '中竞争'
  return '低竞争'
})

// 价格变化
const priceChange = computed(() => {
  if (!props.product || !props.product.priceHistory || props.product.priceHistory.length < 2) {
    return null
  }
  const history = props.product.priceHistory
  const current = props.product.price ?? props.product.currentPrice ?? 0
  const previous = history[history.length - 2]?.price || current
  const change = current - previous
  const percentage = ((change / previous) * 100).toFixed(1)
  return {
    value: change,
    percentage,
    isUp: change > 0,
    isDown: change < 0
  }
})

// 模拟多平台数据
const platformData = computed(() => {
  if (!props.product) return []
  const basePrice = props.product.price ?? props.product.currentPrice ?? 0
  return [
    {
      name: 'Amazon',
      price: basePrice,
      rating: props.product.rating,
      reviews: props.product.reviewCount,
      color: 'orange',
      borderColor: 'border-orange-500/30',
      bgColor: 'bg-orange-500/10',
      textColor: 'text-orange-400'
    },
    {
      name: 'eBay',
      price: basePrice * 0.95,
      rating: props.product.rating * 0.98,
      reviews: Math.floor(props.product.reviewCount * 0.7),
      color: 'blue',
      borderColor: 'border-blue-500/30',
      bgColor: 'bg-blue-500/10',
      textColor: 'text-blue-400'
    },
    {
      name: '速卖通',
      price: basePrice * 0.85,
      rating: props.product.rating * 0.95,
      reviews: Math.floor(props.product.reviewCount * 1.2),
      color: 'pink',
      borderColor: 'border-pink-500/30',
      bgColor: 'bg-pink-500/10',
      textColor: 'text-pink-400'
    },
    {
      name: 'TikTok',
      price: basePrice * 0.9,
      rating: props.product.rating * 0.92,
      reviews: Math.floor(props.product.reviewCount * 0.5),
      color: 'slate',
      borderColor: 'border-slate-500/30',
      bgColor: 'bg-slate-500/10',
      textColor: 'text-slate-400'
    }
  ]
})

// 统计数据
const statsData = computed(() => {
  if (!props.product) return []
  return [
    {
      label: '月销量',
      value: Math.floor(Math.random() * 5000 + 1000).toLocaleString(),
      icon: 'shopping-cart',
      color: 'orange'
    },
    {
      label: '平均评分',
      value: props.product.rating.toFixed(1),
      icon: 'star',
      color: 'blue'
    },
    {
      label: '总评论数',
      value: props.product.reviewCount.toLocaleString(),
      icon: 'comment',
      color: 'green'
    },
    {
      label: '竞品数量',
      value: Math.floor(Math.random() * 200 + 50).toString(),
      icon: 'users',
      color: 'purple'
    }
  ]
})

// 关键指标
const keyIndicators = computed(() => {
  if (!props.product) return []
  const competitionScore = props.product.competitionScore ?? 0.5
  return [
    {
      label: '入场建议',
      value: competitionScore < 0.5 ? '推荐入场' : competitionScore < 0.7 ? '谨慎入场' : '不建议',
      color: competitionScore < 0.5 ? 'text-green-400' : competitionScore < 0.7 ? 'text-yellow-400' : 'text-red-400'
    },
    {
      label: '竞争指数',
      value: `${Math.round(competitionScore * 100)}%`,
      color: competitionColor.value
    },
    {
      label: '利润空间',
      value: `${Math.floor(Math.random() * 30 + 20)}%`,
      color: 'text-blue-400'
    },
    {
      label: '趋势判断',
      value: Math.random() > 0.5 ? '↑ 上升' : '→ 稳定',
      color: Math.random() > 0.5 ? 'text-green-400' : 'text-slate-400'
    }
  ]
})

// 初始化价格趋势图表
function initPriceChart() {
  if (!priceChartRef.value || !props.product?.priceHistory?.length) return

  if (priceChartInstance) {
    priceChartInstance.dispose()
  }

  priceChartInstance = echarts.init(priceChartRef.value)

  const dates = props.product.priceHistory.map(p => p.date)
  const prices = props.product.priceHistory.map(p => p.price)

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30, 41, 59, 0.95)',
      borderColor: 'rgba(51, 65, 85, 0.5)',
      borderWidth: 1,
      textStyle: { color: '#f8fafc' },
      formatter: (params: unknown) => {
        const data = (params as { data: number; axisValue: string }[])[0]
        return `<div class="text-sm">
          <div class="text-slate-400">${data.axisValue}</div>
          <div class="font-semibold text-orange-400">$${data.data.toFixed(2)}</div>
        </div>`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLine: { lineStyle: { color: 'rgba(51, 65, 85, 0.5)' } },
      axisLabel: { color: '#94a3b8', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: 'rgba(51, 65, 85, 0.3)' } },
      axisLabel: { color: '#94a3b8', fontSize: 11, formatter: (value: number) => `$${value}` }
    },
    series: [{
      name: '价格',
      type: 'line',
      data: prices,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { color: '#f97316', width: 2 },
      itemStyle: { color: '#f97316', borderColor: '#fff', borderWidth: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(249, 115, 22, 0.3)' },
          { offset: 1, color: 'rgba(249, 115, 22, 0.05)' }
        ])
      }
    }]
  }

  priceChartInstance.setOption(option)
}

// 初始化雷达图
function initRadarChart() {
  if (!radarChartRef.value) return

  if (radarChartInstance) {
    radarChartInstance.dispose()
  }

  radarChartInstance = echarts.init(radarChartRef.value)

  const competitionScore = props.product?.competitionScore ?? 0.5
  const marketSize = Math.random() * 0.4 + 0.5
  const growthPotential = Math.random() * 0.3 + 0.6
  const entryBarrier = Math.random() * 0.3 + 0.4
  const overallScore = (competitionScore + marketSize + growthPotential + entryBarrier) / 4

  const option = {
    tooltip: { trigger: 'item' },
    radar: {
      indicator: [
        { name: '市场规模', max: 1 },
        { name: '增长潜力', max: 1 },
        { name: '竞争强度', max: 1 },
        { name: '进入壁垒', max: 1 },
        { name: '综合评分', max: 1 }
      ],
      radius: '65%',
      axisName: { color: '#94a3b8', fontSize: 11 },
      splitLine: { lineStyle: { color: 'rgba(51, 65, 85, 0.5)' } },
      splitArea: { areaStyle: { color: ['rgba(30, 41, 59, 0.3)'] } },
      axisLine: { lineStyle: { color: 'rgba(51, 65, 85, 0.5)' } }
    },
    series: [{
      type: 'radar',
      data: [{
        value: [marketSize, growthPotential, competitionScore, entryBarrier, overallScore],
        name: '市场指标',
        itemStyle: { color: '#f97316' },
        areaStyle: { color: 'rgba(249, 115, 22, 0.3)' },
        lineStyle: { color: '#f97316', width: 2 }
      }]
    }]
  }

  radarChartInstance.setOption(option)
}

// 监听产品变化
watch(() => props.product, () => {
  if (props.product && props.show) {
    setTimeout(() => {
      initPriceChart()
      initRadarChart()
    }, 100)
  }
}, { deep: true })

// 监听显示状态
watch(() => props.show, (show) => {
  if (show && props.product) {
    setTimeout(() => {
      initPriceChart()
      initRadarChart()
    }, 100)
  }
})

// 处理窗口大小变化
function handleResize() {
  priceChartInstance?.resize()
  radarChartInstance?.resize()
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  priceChartInstance?.dispose()
  radarChartInstance?.dispose()
})

function handleClose() {
  emit('close')
}

function handleFavorite() {
  if (props.product) {
    emit('favorite', props.product.id)
  }
}

function handleShare() {
  if (navigator.share && props.product) {
    navigator.share({
      title: props.product.title,
      text: `查看这个产品: ${props.product.title}`,
      url: window.location.href
    })
  } else {
    navigator.clipboard.writeText(window.location.href)
  }
}

function handleExport() {
  // 导出功能
  console.log('Export product:', props.product?.id)
}

function handleReport() {
  // 生成报告
  console.log('Generate report:', props.product?.id)
}

function handleBackdropClick(e: MouseEvent) {
  if (e.target === e.currentTarget) {
    handleClose()
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition ease-out duration-300"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition ease-in duration-200"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="show"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm"
        @click="handleBackdropClick"
      >
        <Transition
          enter-active-class="transition ease-out duration-300"
          enter-from-class="opacity-0 scale-95 translate-y-4"
          enter-to-class="opacity-100 scale-100 translate-y-0"
          leave-active-class="transition ease-in duration-200"
          leave-from-class="opacity-100 scale-100 translate-y-0"
          leave-to-class="opacity-0 scale-95 translate-y-4"
        >
          <div
            v-if="show"
            class="product-modal relative w-full max-w-[1100px] max-h-[90vh] glass-card overflow-hidden"
          >
            <!-- 关闭按钮 -->
            <button
              class="absolute top-4 right-4 z-10 p-2 rounded-full bg-slate-800/80 text-slate-400 hover:text-white hover:bg-slate-700 transition-colors"
              @click="handleClose"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>

            <!-- 加载状态 -->
            <div v-if="loading" class="flex items-center justify-center h-96">
              <div class="flex flex-col items-center">
                <svg class="w-10 h-10 text-orange-400 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                </svg>
                <p class="mt-3 text-slate-400">加载产品详情...</p>
              </div>
            </div>

            <!-- 产品详情 -->
            <div v-else-if="product" class="overflow-y-auto max-h-[90vh]">
              <!-- 顶部核心信息区 -->
              <div class="p-6 border-b border-slate-700/50">
                <div class="flex gap-6">
                  <!-- 产品图片 -->
                  <div class="w-32 h-32 flex-shrink-0 rounded-xl overflow-hidden bg-slate-800">
                    <img
                      :src="product.image || 'https://via.placeholder.com/200'"
                      :alt="product.title"
                      class="w-full h-full object-cover"
                    />
                  </div>
                  <!-- 标题和价格 -->
                  <div class="flex-1 min-w-0">
                    <h2 class="text-xl font-bold text-white mb-2 pr-8 line-clamp-2">
                      {{ product.title }}
                    </h2>
                    <div class="flex items-center gap-4 mb-3">
                      <span class="text-3xl font-bold text-orange-400">{{ formattedPrice }}</span>
                      <div v-if="priceChange" class="flex items-center gap-1">
                        <svg
                          :class="[
                            'w-4 h-4',
                            priceChange.isUp ? 'text-red-500' : priceChange.isDown ? 'text-green-500' : 'text-slate-400'
                          ]"
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                        >
                          <path
                            v-if="priceChange.isUp"
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            stroke-width="2"
                            d="M5 15l7-7 7 7"
                          />
                          <path
                            v-else-if="priceChange.isDown"
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            stroke-width="2"
                            d="M19 9l-7 7-7-7"
                          />
                        </svg>
                        <span :class="['text-sm', priceChange.isUp ? 'text-red-500' : 'text-green-500']">
                          {{ priceChange.isUp ? '+' : '' }}{{ priceChange.percentage }}%
                        </span>
                      </div>
                    </div>
                    <div class="flex items-center gap-4">
                      <div class="flex items-center gap-1">
                        <svg class="w-5 h-5 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                          <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                        </svg>
                        <span class="text-white font-medium">{{ formattedRating }}</span>
                        <span class="text-slate-400">({{ formattedReviewCount }} 评论)</span>
                      </div>
                      <span class="px-2 py-1 rounded text-xs font-medium bg-orange-500/20 text-orange-400">
                        BSR {{ formattedBsr }}
                      </span>
                      <span class="px-2 py-1 rounded text-xs font-medium bg-slate-700 text-slate-300">
                        {{ product.categoryName || '未分类' }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 全平台综合数据区 - 4列彩色统计卡片 -->
              <div class="p-6 border-b border-slate-700/50">
                <h3 class="text-sm font-medium text-slate-400 mb-4">全平台综合数据</h3>
                <div class="grid grid-cols-4 gap-4">
                  <div v-for="stat in statsData" :key="stat.label"
                       class="p-4 rounded-xl bg-slate-800/50 border border-slate-700/50">
                    <div class="flex items-center justify-between mb-2">
                      <span class="text-xs text-slate-500">{{ stat.label }}</span>
                      <div :class="[
                        'w-8 h-8 rounded-lg flex items-center justify-center',
                        stat.color === 'orange' ? 'bg-orange-500/20' : '',
                        stat.color === 'blue' ? 'bg-blue-500/20' : '',
                        stat.color === 'green' ? 'bg-green-500/20' : '',
                        stat.color === 'purple' ? 'bg-purple-500/20' : ''
                      ]">
                        <svg v-if="stat.icon === 'shopping-cart'" class="w-4 h-4 text-orange-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                        </svg>
                        <svg v-else-if="stat.icon === 'star'" class="w-4 h-4 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
                        </svg>
                        <svg v-else-if="stat.icon === 'comment'" class="w-4 h-4 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
                        </svg>
                        <svg v-else class="w-4 h-4 text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                        </svg>
                      </div>
                    </div>
                    <div class="text-2xl font-bold text-white">{{ stat.value }}</div>
                  </div>
                </div>
              </div>

              <!-- 多平台对比区 - 4列平台卡片 -->
              <div class="p-6 border-b border-slate-700/50">
                <h3 class="text-sm font-medium text-slate-400 mb-4">多平台价格对比</h3>
                <div class="grid grid-cols-4 gap-4">
                  <div
                    v-for="platform in platformData"
                    :key="platform.name"
                    :class="[
                      'p-4 rounded-xl border bg-slate-800/30 transition-all hover:bg-slate-800/50',
                      platform.borderColor
                    ]"
                  >
                    <div class="flex items-center justify-between mb-3">
                      <span class="text-sm font-medium text-white">{{ platform.name }}</span>
                      <span :class="['w-2 h-2 rounded-full', platform.bgColor]"></span>
                    </div>
                    <div :class="['text-xl font-bold mb-2', platform.textColor]">
                      ${{ platform.price.toFixed(2) }}
                    </div>
                    <div class="flex items-center gap-2 text-xs text-slate-400">
                      <svg class="w-3 h-3 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                        <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                      </svg>
                      <span>{{ platform.rating.toFixed(1) }}</span>
                      <span class="text-slate-600">|</span>
                      <span>{{ platform.reviews }} 评论</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 中部主体区 - 价格趋势 + 雷达图 + 关键指标 -->
              <div class="p-6 grid grid-cols-3 gap-6">
                <!-- 左侧: 价格趋势 + 雷达图 -->
                <div class="col-span-2 space-y-6">
                  <!-- 价格趋势图 -->
                  <div>
                    <h3 class="text-sm font-medium text-slate-400 mb-3">价格趋势</h3>
                    <div ref="priceChartRef" class="h-48 w-full bg-slate-800/30 rounded-xl"></div>
                  </div>

                  <!-- 竞争力雷达图 -->
                  <div>
                    <h3 class="text-sm font-medium text-slate-400 mb-3">市场竞争力分析</h3>
                    <div ref="radarChartRef" class="h-56 w-full bg-slate-800/30 rounded-xl"></div>
                  </div>
                </div>

                <!-- 右侧: 关键指标 -->
                <div>
                  <h3 class="text-sm font-medium text-slate-400 mb-3">关键指标</h3>
                  <div class="space-y-3">
                    <div
                      v-for="indicator in keyIndicators"
                      :key="indicator.label"
                      class="flex items-center justify-between p-3 bg-slate-800/30 rounded-lg"
                    >
                      <span class="text-sm text-slate-400">{{ indicator.label }}</span>
                      <span :class="['text-sm font-medium', indicator.color]">{{ indicator.value }}</span>
                    </div>
                  </div>

                  <!-- 品类信息 -->
                  <div class="mt-4 p-3 bg-slate-800/30 rounded-lg">
                    <div class="text-xs text-slate-500 mb-1">所属品类</div>
                    <div class="text-sm font-medium text-white">{{ product.categoryName || product.category?.name || '未分类' }}</div>
                  </div>

                  <!-- 竞争度 -->
                  <div class="mt-3 p-3 bg-slate-800/30 rounded-lg">
                    <div class="text-xs text-slate-500 mb-1">竞争度</div>
                    <div :class="['text-sm font-medium', competitionColor]">{{ competitionText }}</div>
                  </div>
                </div>
              </div>

              <!-- AI智能分析横条 -->
              <div v-if="product.aiRecommendation" class="mx-6 mb-6 p-4 ai-analysis-bar rounded-xl">
                <div class="flex items-start gap-3">
                  <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center flex-shrink-0">
                    <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
                    </svg>
                  </div>
                  <div class="flex-1">
                    <div class="text-xs text-orange-400 font-medium mb-1">AI 智能分析</div>
                    <p class="text-sm text-slate-300">{{ product.aiRecommendation }}</p>
                  </div>
                </div>
              </div>

              <!-- 操作按钮区 - 5个按钮 -->
              <div class="p-6 border-t border-slate-700/50 flex gap-3">
                <button
                  class="flex-1 flex items-center justify-center gap-2 px-4 py-3 btn-gradient-primary rounded-xl hover:opacity-90 transition-opacity"
                  @click="handleFavorite"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                  </svg>
                  收藏产品
                </button>
                <button
                  class="flex items-center justify-center gap-2 px-4 py-3 bg-slate-700/50 text-slate-300 rounded-xl hover:bg-slate-700 transition-colors"
                  @click="handleShare"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z" />
                  </svg>
                  分享
                </button>
                <button
                  class="flex items-center justify-center gap-2 px-4 py-3 bg-slate-700/50 text-slate-300 rounded-xl hover:bg-slate-700 transition-colors"
                  @click="handleExport"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                  导出
                </button>
                <button
                  class="flex items-center justify-center gap-2 px-4 py-3 bg-slate-700/50 text-slate-300 rounded-xl hover:bg-slate-700 transition-colors"
                  @click="handleReport"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                  报告
                </button>
                <a
                  :href="`https://www.amazon.com/dp/${product.id}`"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="flex items-center justify-center gap-2 px-4 py-3 border border-slate-600 text-slate-300 rounded-xl hover:bg-slate-700 transition-colors"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                  </svg>
                  原链接
                </a>
              </div>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.product-modal {
  background: rgba(30, 41, 59, 0.95);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(51, 65, 85, 0.5);
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

.ai-analysis-bar {
  background: linear-gradient(135deg, rgba(249, 115, 22, 0.1) 0%, rgba(239, 68, 68, 0.1) 100%);
  border: 1px solid rgba(249, 115, 22, 0.2);
}

.btn-gradient-primary {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  color: white;
  font-weight: 600;
}

/* 滚动条样式 */
.product-modal::-webkit-scrollbar {
  width: 8px;
}

.product-modal::-webkit-scrollbar-track {
  background: rgba(30, 41, 59, 0.5);
  border-radius: 4px;
}

.product-modal::-webkit-scrollbar-thumb {
  background: rgba(51, 65, 85, 0.8);
  border-radius: 4px;
}

.product-modal::-webkit-scrollbar-thumb:hover {
  background: rgba(71, 85, 105, 0.8);
}
</style>
