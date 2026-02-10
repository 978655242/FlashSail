<script setup lang="ts">
interface Props {
  title: string
  subtitle?: string
  showBack?: boolean
}

withDefaults(defineProps<Props>(), {
  subtitle: '',
  showBack: false
})

defineEmits<{
  back: []
}>()
</script>

<template>
  <div class="page-header">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div class="flex items-center gap-4">
        <!-- 返回按钮 -->
        <button
          v-if="showBack"
          class="p-2 text-slate-400 hover:text-slate-200 hover:bg-slate-800/50 rounded-lg transition-colors"
          @click="$emit('back')"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>

        <!-- 标题 -->
        <div>
          <h1 class="text-2xl font-bold text-white">{{ title }}</h1>
          <p v-if="subtitle" class="mt-1 text-sm text-slate-400">{{ subtitle }}</p>
        </div>
      </div>

      <!-- 右侧插槽 -->
      <div class="flex items-center gap-3">
        <slot name="actions" />
      </div>
    </div>

    <!-- 底部插槽（用于筛选器等） -->
    <div v-if="$slots.default" class="mt-4">
      <slot />
    </div>
  </div>
</template>
