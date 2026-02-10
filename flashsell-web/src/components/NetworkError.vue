<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from '@/composables/useI18n'

interface Props {
  type?: 'network' | 'server' | 'timeout' | 'unknown'
  message?: string
  retryable?: boolean
  retrying?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'network',
  message: '',
  retryable: true,
  retrying: false
})

const emit = defineEmits<{
  retry: []
}>()

const { t } = useI18n()

// Internal loading state for retry button
const isRetrying = ref(false)

// Error configuration with i18n support
const errorConfig = computed(() => ({
  network: {
    title: t('common.networkError', '网络连接失败'),
    defaultMessage: t('error.networkMessage', '请检查您的网络连接后重试'),
    icon: 'wifi'
  },
  server: {
    title: t('common.serverError', '服务器错误'),
    defaultMessage: t('error.serverMessage', '服务器暂时无法响应，请稍后重试'),
    icon: 'server'
  },
  timeout: {
    title: t('common.timeout', '请求超时'),
    defaultMessage: t('error.timeoutMessage', '服务器响应时间过长，请稍后重试'),
    icon: 'clock'
  },
  unknown: {
    title: t('common.unknownError', '出错了'),
    defaultMessage: t('error.unknownMessage', '发生未知错误，请稍后重试'),
    icon: 'error'
  }
}))

// Get current error config
const currentConfig = computed(() => errorConfig.value[props.type])

// Handle retry with loading state
async function handleRetry() {
  if (isRetrying.value || props.retrying) return
  isRetrying.value = true
  emit('retry')
  // Reset after a short delay if parent doesn't control retrying state
  setTimeout(() => {
    isRetrying.value = false
  }, 2000)
}

// Check if currently retrying
const isCurrentlyRetrying = computed(() => isRetrying.value || props.retrying)
</script>

<template>
  <div class="flex flex-col items-center justify-center py-16 px-4">
    <!-- 图标 -->
    <div class="w-20 h-20 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center mb-6">
      <!-- 网络图标 -->
      <svg v-if="currentConfig.icon === 'wifi'" class="w-10 h-10 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8.111 16.404a5.5 5.5 0 017.778 0M12 20h.01m-7.08-7.071c3.904-3.905 10.236-3.905 14.141 0M1.394 9.393c5.857-5.857 15.355-5.857 21.213 0" />
      </svg>
      <!-- 服务器图标 -->
      <svg v-else-if="currentConfig.icon === 'server'" class="w-10 h-10 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M5 12h14M5 12a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v4a2 2 0 01-2 2M5 12a2 2 0 00-2 2v4a2 2 0 002 2h14a2 2 0 002-2v-4a2 2 0 00-2-2m-2-4h.01M17 16h.01" />
      </svg>
      <!-- 超时图标 -->
      <svg v-else-if="currentConfig.icon === 'clock'" class="w-10 h-10 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      <!-- 错误图标 -->
      <svg v-else class="w-10 h-10 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
      </svg>
    </div>

    <!-- 标题 -->
    <h3 class="text-xl font-semibold text-slate-900 dark:text-white mb-2">
      {{ currentConfig.title }}
    </h3>

    <!-- 错误信息 -->
    <p class="text-sm text-slate-500 dark:text-slate-400 text-center max-w-md mb-8">
      {{ message || currentConfig.defaultMessage }}
    </p>

    <!-- 重试按钮 -->
    <button
      v-if="retryable"
      :disabled="isCurrentlyRetrying"
      class="inline-flex items-center px-6 py-2.5 text-sm font-medium text-white btn-gradient-primary rounded-lg hover:opacity-90 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed"
      @click="handleRetry"
    >
      <!-- Loading spinner when retrying -->
      <svg
        v-if="isCurrentlyRetrying"
        class="w-4 h-4 mr-2 animate-spin"
        fill="none"
        viewBox="0 0 24 24"
      >
        <circle
          class="opacity-25"
          cx="12"
          cy="12"
          r="10"
          stroke="currentColor"
          stroke-width="4"
        />
        <path
          class="opacity-75"
          fill="currentColor"
          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
        />
      </svg>
      <!-- Retry icon when not retrying -->
      <svg
        v-else
        class="w-4 h-4 mr-2"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
      </svg>
      {{ isCurrentlyRetrying ? t('common.loading', '加载中...') : t('common.retry', '重新加载') }}
    </button>
  </div>
</template>
