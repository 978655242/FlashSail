<script setup lang="ts">
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

const chartRef = ref<HTMLDivElement | null>(null)
let chartInstance: echarts.ECharts | null = null

// 格式化价格
const formattedPrice = computed(() => {
  if (!props.product) return ''
  return `$${props.product.currentPrice.toFixed(2)}`
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
  if (rank >= 10000) {
    return `${(rank / 10000).toFixed(1)}万`
  } else if (rank >= 1000) {
    return `${(rank / 1000).toFixed(1)}k`
  }
  return `#${rank}`
})

// 竞争评分颜色
const competitionColor = computed(() => {
  if (!props.product) return ''
  const score = props.product.competitionScore
  if (score >= 0.7) return 'text-red-500'
  if (score >= 0.4) return 'text-yellow-500'
  return 'text-green-500'
})

// 竞争评分文字
const competitionText = computed(() => {
  if (!props.product) return ''
  const score = props.product.competitionScore
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
  const current = props.product.currentPrice
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

// 初始化图表
function initChart() {
  if (!chartRef.value || !props.product?.priceHistory?.length) return

  if (chartInstance) {
    chartInstance.dispose()
  }

  chartInstance = echarts.init(chartRef.value)

  const dates = props.product.priceHistory.map(p => p.date)
  const prices = props.product.priceHistory.map(p => p.price)

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      textStyle: {
        color: '#374151'
      },
      formatter: (params: unknown) => {
        const data = (params as { data: number; axisValue: string }[])[0]
        return `<div class="text-sm">
          <div class="text-slate-500">${data.axisValue}</div>
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
      axisLine: {
        lineStyle: {
          color: '#e5e7eb'
        }
      },
      axisLabel: {
        color: '#9ca3af',
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      splitLine: {
        lineStyle: {
          color: '#f3f4f6'
        }
      },
      axisLabel: {
        color: '#9ca3af',
        fontSize: 11,
        formatter: (value: number) => `$${value}`
      }
    },
    series: [
      {
        name: '价格',
        type: 'line',
        data: prices,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          color: '#f97316',
          width: 2
        },
        itemStyle: {
          color: '#f97316',
          borderColor: '#fff',
          borderWidth: 2
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(249, 115, 22, 0.3)' },
            { offset: 1, color: 'rgba(249, 115, 22, 0.05)' }
          ])
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

// 监听产品变化
watch(() => props.product, () => {
  if (props.product && props.show) {
    setTimeout(initChart, 100)
  }
}, { deep: true })

// 监听显示状态
watch(() => props.show, (show) => {
  if (show && props.product) {
    setTimeout(initChart, 100)
  }
})

// 处理窗口大小变化
function handleResize() {
  chartInstance?.resize()
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})

function handleClose() {
  emit('close')
}

function handleFavorite() {
  if (props.product) {
    emit('favorite', props.product.id)
  }
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
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm"
        @click="handleBackdropClick"
      >
        <Transition
          enter-active-class="transition ease-out duration-300"
          enter-from-class="opacity-0 scale-95"
          enter-to-class="opacity-100 scale-100"
          leave-active-class="transition ease-in duration-200"
          leave-from-class="opacity-100 scale-100"
          leave-to-class="opacity-0 scale-95"
        >
          <div
            v-if="show"
            class="relative w-full max-w-4xl max-h-[90vh] glass-card rounded-2xl shadow-2xl overflow-hidden"
          >
            <!-- 关闭按钮 -->
            <button
              class="absolute top-4 right-4 z-10 p-2 rounded-full glass-card text-slate-500 hover:text-orange-400 dark:hover:text-orange-300 transition-colors"
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
                <p class="mt-3 text-slate-500 dark:text-slate-400">加载产品详情...</p>
              </div>
            </div>

            <!-- 产品详情 -->
            <div v-else-if="product" class="overflow-y-auto max-h-[90vh]">
              <div class="flex flex-col lg:flex-row">
                <!-- 左侧图片 -->
                <div class="lg:w-2/5 p-6 bg-slate-900/30 dark:bg-slate-800/50">
                  <div class="aspect-square rounded-xl overflow-hidden glass-card">
                    <img
                      :src="product.image || 'https://via.placeholder.com/400'"
                      :alt="product.title"
                      class="w-full h-full object-cover"
                    />
                  </div>
                </div>

                <!-- 右侧信息 -->
                <div class="lg:w-3/5 p-6">
                  <!-- 标题 -->
                  <h2 class="text-xl font-bold text-slate-800 dark:text-white mb-4 pr-8">
                    {{ product.title }}
                  </h2>

                  <!-- 价格和评分 -->
                  <div class="flex items-center gap-6 mb-6">
                    <div>
                      <span class="text-3xl font-bold text-orange-400">{{ formattedPrice }}</span>
                      <div v-if="priceChange" class="mt-1 flex items-center gap-1">
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
                        <span
                          :class="[
                            'text-sm',
                            priceChange.isUp ? 'text-red-500' : priceChange.isDown ? 'text-green-500' : 'text-slate-400'
                          ]"
                        >
                          {{ priceChange.isUp ? '+' : '' }}{{ priceChange.percentage }}%
                        </span>
                      </div>
                    </div>
                    <div class="flex items-center gap-2">
                      <svg class="w-5 h-5 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                        <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                      </svg>
                      <span class="text-lg font-medium text-slate-800 dark:text-white">{{ formattedRating }}</span>
                      <span class="text-slate-400">({{ formattedReviewCount }} 评论)</span>
                    </div>
                  </div>

                  <!-- 数据指标 -->
                  <div class="grid grid-cols-3 gap-4 mb-6">
                    <div class="p-3 bg-slate-100 dark:bg-slate-700/50 rounded-lg">
                      <p class="text-xs text-slate-500 dark:text-slate-400 mb-1">BSR 排名</p>
                      <p class="text-lg font-semibold text-slate-800 dark:text-white">{{ formattedBsr }}</p>
                    </div>
                    <div class="p-3 bg-slate-100 dark:bg-slate-700/50 rounded-lg">
                      <p class="text-xs text-slate-500 dark:text-slate-400 mb-1">竞争度</p>
                      <p :class="['text-lg font-semibold', competitionColor]">{{ competitionText }}</p>
                    </div>
                    <div class="p-3 bg-slate-100 dark:bg-slate-700/50 rounded-lg">
                      <p class="text-xs text-slate-500 dark:text-slate-400 mb-1">品类</p>
                      <p class="text-sm font-medium text-slate-800 dark:text-white truncate">{{ product.category?.name || '未分类' }}</p>
                    </div>
                  </div>

                  <!-- 价格趋势图 -->
                  <div class="mb-6">
                    <h3 class="text-sm font-medium text-slate-700 dark:text-slate-300 mb-3">价格趋势</h3>
                    <div ref="chartRef" class="h-48 w-full"></div>
                  </div>

                  <!-- AI 推荐 -->
                  <div v-if="product.aiRecommendation" class="mb-6">
                    <h3 class="text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">AI 分析</h3>
                    <div class="p-4 bg-gradient-to-r from-orange-50 to-red-50 dark:from-orange-900/20 dark:to-red-900/20 rounded-lg border border-orange-100 dark:border-orange-800">
                      <p class="text-sm text-slate-600 dark:text-slate-400">{{ product.aiRecommendation }}</p>
                    </div>
                  </div>

                  <!-- 操作按钮 -->
                  <div class="flex gap-3">
                    <button
                      class="flex-1 flex items-center justify-center gap-2 px-4 py-3 btn-gradient-primary rounded-lg hover:opacity-90 transition-opacity"
                      @click="handleFavorite"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                      </svg>
                      收藏产品
                    </button>
                    <a
                      :href="`https://www.amazon.com/dp/${product.id}`"
                      target="_blank"
                      rel="noopener noreferrer"
                      class="flex items-center justify-center gap-2 px-4 py-3 border border-slate-300 dark:border-slate-600 text-slate-700 dark:text-slate-300 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                      </svg>
                      查看原链接
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>
