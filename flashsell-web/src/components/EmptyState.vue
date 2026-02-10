<script setup lang="ts">
interface Props {
  title?: string
  description?: string
  icon?: 'empty' | 'search' | 'favorite' | 'error'
  actionText?: string
}

withDefaults(defineProps<Props>(), {
  title: '暂无数据',
  description: '',
  icon: 'empty',
  actionText: ''
})

defineEmits<{
  action: []
}>()
</script>

<template>
  <div class="flex flex-col items-center justify-center py-12 px-4">
    <!-- 图标 -->
    <div class="w-20 h-20 rounded-full bg-slate-800 flex items-center justify-center mb-4">
      <!-- 空数据图标 -->
      <svg v-if="icon === 'empty'" class="w-10 h-10 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
      </svg>
      <!-- 搜索图标 -->
      <svg v-else-if="icon === 'search'" class="w-10 h-10 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
      </svg>
      <!-- 收藏图标 -->
      <svg v-else-if="icon === 'favorite'" class="w-10 h-10 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
      </svg>
      <!-- 错误图标 -->
      <svg v-else-if="icon === 'error'" class="w-10 h-10 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
      </svg>
    </div>

    <!-- 标题 -->
    <h3 class="text-lg font-medium text-slate-900 dark:text-white mb-2">{{ title }}</h3>

    <!-- 描述 -->
    <p v-if="description" class="text-sm text-slate-500 dark:text-slate-400 text-center max-w-md mb-6">
      {{ description }}
    </p>

    <!-- 操作按钮 -->
    <button
      v-if="actionText"
      class="inline-flex items-center px-4 py-2 text-sm font-medium text-white btn-gradient-primary rounded-lg hover:opacity-90 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 transition-opacity"
      @click="$emit('action')"
    >
      {{ actionText }}
    </button>
  </div>
</template>
