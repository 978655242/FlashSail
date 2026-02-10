<script setup lang="ts">
interface Props {
  count?: number
  showAvatar?: boolean
  showImage?: boolean
}

withDefaults(defineProps<Props>(), {
  count: 5,
  showAvatar: false,
  showImage: false
})
</script>

<template>
  <div class="space-y-4">
    <div
      v-for="i in count"
      :key="i"
      class="flex items-center gap-4 p-4 glass-card dark:bg-slate-800 rounded-lg border border-slate-200 dark:border-slate-700 animate-pulse"
    >
      <!-- 头像/图片 -->
      <div
        v-if="showAvatar"
        class="flex-shrink-0 w-10 h-10 bg-slate-200 dark:bg-slate-700 rounded-full"
      ></div>
      <div
        v-else-if="showImage"
        class="flex-shrink-0 w-16 h-16 bg-slate-200 dark:bg-slate-700 rounded-lg"
      ></div>

      <!-- 内容 -->
      <div class="flex-1 space-y-2">
        <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-3/4"></div>
        <div class="h-3 bg-slate-200 dark:bg-slate-700 rounded w-1/2"></div>
      </div>

      <!-- 右侧操作 -->
      <div class="flex-shrink-0 w-20 h-8 bg-slate-200 dark:bg-slate-700 rounded"></div>
    </div>
  </div>
</template>
