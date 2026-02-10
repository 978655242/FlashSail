<script setup lang="ts">
/**
 * TrendChart Component
 * 
 * A reusable ECharts-based line chart component for displaying market trends over time.
 * Supports theme-aware styling and responsive resizing.
 * 
 * Requirements: 8.1
 * - Display market trend data over time using ECharts
 */
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import { useTheme } from '@/composables/useTheme'
import { useI18n } from '@/composables/useI18n'

export interface TrendDataPoint {
  date: string
  value: number
  secondaryValue?: number
}

export interface TrendChartProps {
  /** Chart data points */
  data: TrendDataPoint[]
  /** Primary series name */
  primaryLabel?: string
  /** Secondary series name (optional) */
  secondaryLabel?: string
  /** Chart height */
  height?: string
  /** Show area fill under the line */
  showArea?: boolean
  /** Primary color */
  primaryColor?: string
  /** Secondary color */
  secondaryColor?: string
  /** Loading state */
  loading?: boolean
}

const props = withDefaults(defineProps<TrendChartProps>(), {
  primaryLabel: 'Value',
  secondaryLabel: '',
  height: '320px',
  showArea: true,
  primaryColor: '#3B82F6',
  secondaryColor: '#10B981',
  loading: false
})

const { isDark } = useTheme()
const { t } = useI18n()

const chartRef = ref<HTMLDivElement | null>(null)
let chartInstance: echarts.ECharts | null = null

// Computed chart options based on theme and data
const chartOptions = computed<EChartsOption>(() => {
  const textColor = isDark.value ? '#94A3B8' : '#64748B'
  const gridLineColor = isDark.value ? 'rgba(51, 65, 85, 0.5)' : 'rgba(203, 213, 225, 0.5)'
  const tooltipBg = isDark.value ? 'rgba(30, 41, 59, 0.95)' : 'rgba(255, 255, 255, 0.95)'
  const tooltipBorder = isDark.value ? '#334155' : '#E2E8F0'

  const dates = props.data.map(d => d.date)
  const primaryValues = props.data.map(d => d.value)
  const hasSecondary = props.data.some(d => d.secondaryValue !== undefined)
  const secondaryValues = hasSecondary ? props.data.map(d => d.secondaryValue ?? 0) : []

  const series: echarts.SeriesOption[] = [
    {
      name: props.primaryLabel,
      type: 'line',
      data: primaryValues,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      itemStyle: {
        color: props.primaryColor
      },
      lineStyle: {
        width: 2,
        color: props.primaryColor
      },
      areaStyle: props.showArea ? {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: `${props.primaryColor}40` },
          { offset: 1, color: `${props.primaryColor}05` }
        ])
      } : undefined
    }
  ]

  if (hasSecondary && props.secondaryLabel) {
    series.push({
      name: props.secondaryLabel,
      type: 'line',
      yAxisIndex: 1,
      data: secondaryValues,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      itemStyle: {
        color: props.secondaryColor
      },
      lineStyle: {
        width: 2,
        color: props.secondaryColor
      },
      areaStyle: props.showArea ? {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: `${props.secondaryColor}40` },
          { offset: 1, color: `${props.secondaryColor}05` }
        ])
      } : undefined
    })
  }

  const yAxis: echarts.YAXisComponentOption[] = [
    {
      type: 'value',
      name: props.primaryLabel,
      nameTextStyle: {
        color: textColor,
        fontSize: 12
      },
      axisLabel: {
        color: textColor,
        fontSize: 11,
        formatter: (value: number) => formatNumber(value)
      },
      axisLine: {
        show: false
      },
      splitLine: {
        lineStyle: {
          color: gridLineColor,
          type: 'dashed'
        }
      }
    }
  ]

  if (hasSecondary && props.secondaryLabel) {
    yAxis.push({
      type: 'value',
      name: props.secondaryLabel,
      nameTextStyle: {
        color: textColor,
        fontSize: 12
      },
      axisLabel: {
        color: textColor,
        fontSize: 11,
        formatter: (value: number) => formatNumber(value)
      },
      axisLine: {
        show: false
      },
      splitLine: {
        show: false
      }
    })
  }

  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: tooltipBg,
      borderColor: tooltipBorder,
      borderWidth: 1,
      textStyle: {
        color: isDark.value ? '#F8FAFC' : '#0F172A',
        fontSize: 12
      },
      axisPointer: {
        type: 'cross',
        crossStyle: {
          color: textColor
        }
      }
    },
    legend: {
      data: hasSecondary && props.secondaryLabel 
        ? [props.primaryLabel, props.secondaryLabel] 
        : [props.primaryLabel],
      textStyle: {
        color: textColor,
        fontSize: 12
      },
      top: 0,
      right: 0
    },
    grid: {
      left: '3%',
      right: hasSecondary ? '4%' : '3%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLabel: {
        color: textColor,
        fontSize: 11,
        rotate: dates.length > 15 ? 45 : 0
      },
      axisLine: {
        lineStyle: {
          color: gridLineColor
        }
      },
      axisTick: {
        show: false
      }
    },
    yAxis,
    series
  }
})

// Format large numbers
function formatNumber(num: number): string {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K'
  }
  return num.toString()
}

// Initialize chart
function initChart() {
  if (!chartRef.value) return

  if (chartInstance) {
    chartInstance.dispose()
  }

  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption(chartOptions.value)
}

// Handle window resize
function handleResize() {
  chartInstance?.resize()
}

// Watch for data and theme changes
watch([() => props.data, isDark], () => {
  if (chartInstance) {
    chartInstance.setOption(chartOptions.value)
  }
}, { deep: true })

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<template>
  <div class="trend-chart-container">
    <!-- Loading state -->
    <div v-if="loading" class="chart-loading" :style="{ height }">
      <div class="loading-spinner">
        <svg class="animate-spin w-8 h-8 text-orange-500" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
        </svg>
        <span class="mt-2 text-sm text-slate-400">{{ t('common.loading') }}</span>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else-if="!data || data.length === 0" class="chart-empty" :style="{ height }">
      <svg class="w-12 h-12 text-slate-500 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
      </svg>
      <span class="text-sm text-slate-400">{{ t('common.noData') }}</span>
    </div>

    <!-- Chart -->
    <div v-else ref="chartRef" :style="{ height, width: '100%' }" />
  </div>
</template>

<style scoped>
.trend-chart-container {
  width: 100%;
  position: relative;
}

.chart-loading,
.chart-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
}
</style>
