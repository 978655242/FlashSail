<script setup lang="ts">
interface Props {
  count?: number
  columns?: 1 | 2 | 3 | 4
}

withDefaults(defineProps<Props>(), {
  count: 4,
  columns: 4
})

const gridClasses = {
  1: 'grid-cols-1',
  2: 'grid-cols-1 sm:grid-cols-2',
  3: 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-3',
  4: 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4'
}
</script>

<template>
  <div :class="['grid gap-4', gridClasses[columns]]">
    <div
      v-for="i in count"
      :key="i"
      class="glass-card rounded-xl shadow-sm overflow-hidden animate-pulse"
    >
      <!-- 图片骨架 -->
      <div class="aspect-square bg-slate-200 dark:bg-slate-700"></div>

      <!-- 内容骨架 -->
      <div class="p-4 space-y-3">
        <!-- 标题 -->
        <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-full"></div>
        <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-3/4"></div>

        <!-- 价格和评分 -->
        <div class="flex items-center justify-between pt-2">
          <div class="h-6 bg-slate-200 dark:bg-slate-700 rounded w-20"></div>
          <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-16"></div>
        </div>

        <!-- 标签 -->
        <div class="flex gap-2 pt-1">
          <div class="h-5 bg-slate-200 dark:bg-slate-700 rounded w-12"></div>
          <div class="h-5 bg-slate-200 dark:bg-slate-700 rounded w-16"></div>
        </div>
      </div>
    </div>
  </div>
</template>
