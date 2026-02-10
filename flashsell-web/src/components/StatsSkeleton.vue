<script setup lang="ts">
interface Props {
  count?: number
}

withDefaults(defineProps<Props>(), {
  count: 4
})
</script>

<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
    <div
      v-for="i in count"
      :key="i"
      class="glass-card rounded-xl p-6 animate-pulse"
    >
      <!-- 图标和标题 -->
      <div class="flex items-center gap-3 mb-4">
        <div class="w-10 h-10 bg-slate-200 dark:bg-slate-700 rounded-lg"></div>
        <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-20"></div>
      </div>

      <!-- 数值 -->
      <div class="h-8 bg-slate-200 dark:bg-slate-700 rounded w-24 mb-2"></div>

      <!-- 变化趋势 -->
      <div class="h-3 bg-slate-200 dark:bg-slate-700 rounded w-16"></div>
    </div>
  </div>
</template>
