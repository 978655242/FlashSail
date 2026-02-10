<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from '@/composables/useI18n'

interface Props {
  title?: string
  message: string
  retryable?: boolean
  retrying?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  title: '',
  retryable: true,
  retrying: false
})

const emit = defineEmits<{
  retry: []
}>()

const { t } = useI18n()

// Internal loading state for retry button
const isRetrying = ref(false)

// Computed title with i18n fallback
const displayTitle = () => props.title || t('common.error', '出错了')

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
const isCurrentlyRetrying = () => isRetrying.value || props.retrying
</script>

<template>
  <div class="flex flex-col items-center justify-center py-12 px-4">
    <!-- 错误图标 -->
    <div class="w-16 h-16 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center mb-4">
      <svg class="w-8 h-8 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
        />
      </svg>
    </div>

    <!-- 错误标题 -->
    <h3 class="text-lg font-medium text-slate-900 dark:text-white mb-2">{{ displayTitle() }}</h3>

    <!-- 错误信息 -->
    <p class="text-sm text-slate-500 dark:text-slate-400 text-center max-w-md mb-6">{{ message }}</p>

    <!-- 重试按钮 -->
    <button
      v-if="retryable"
      :disabled="isCurrentlyRetrying()"
      class="inline-flex items-center px-4 py-2 text-sm font-medium text-white btn-gradient-primary rounded-lg hover:opacity-90 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed"
      @click="handleRetry"
    >
      <!-- Loading spinner when retrying -->
      <svg
        v-if="isCurrentlyRetrying()"
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
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
        />
      </svg>
      {{ isCurrentlyRetrying() ? t('common.loading', '加载中...') : t('common.retry', '重试') }}
    </button>
  </div>
</template>
