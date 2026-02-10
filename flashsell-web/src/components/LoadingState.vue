<script setup lang="ts">
interface Props {
  size?: 'sm' | 'md' | 'lg'
  text?: string
  fullscreen?: boolean
}

withDefaults(defineProps<Props>(), {
  size: 'md',
  text: '加载中...',
  fullscreen: false
})

const sizeClasses = {
  sm: 'w-4 h-4',
  md: 'w-8 h-8',
  lg: 'w-12 h-12'
}
</script>

<template>
  <div
    :class="[
      'flex flex-col items-center justify-center',
      fullscreen ? 'fixed inset-0 bg-slate-900/90 z-50' : 'py-8'
    ]"
  >
    <!-- 加载动画 -->
    <svg
      :class="['animate-spin text-orange-400', sizeClasses[size]]"
      xmlns="http://www.w3.org/2000/svg"
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
    <!-- 加载文字 -->
    <p v-if="text" class="mt-3 text-sm text-slate-500 dark:text-slate-400">{{ text }}</p>
  </div>
</template>
