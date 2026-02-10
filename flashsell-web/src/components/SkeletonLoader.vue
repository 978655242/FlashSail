<script setup lang="ts">
interface Props {
  type?: 'text' | 'card' | 'avatar' | 'image' | 'custom'
  lines?: number
  width?: string
  height?: string
}

withDefaults(defineProps<Props>(), {
  type: 'text',
  lines: 3,
  width: '100%',
  height: '1rem'
})
</script>

<template>
  <!-- 文本骨架 -->
  <div v-if="type === 'text'" class="space-y-2">
    <div
      v-for="i in lines"
      :key="i"
      :class="[
        'animate-pulse bg-slate-200 dark:bg-slate-700 rounded',
        i === lines ? 'w-3/4' : 'w-full'
      ]"
      :style="{ height }"
    />
  </div>

  <!-- 卡片骨架 -->
  <div v-else-if="type === 'card'" class="animate-pulse">
    <div class="bg-slate-200 dark:bg-slate-700 rounded-lg h-40 mb-4" />
    <div class="space-y-2">
      <div class="bg-slate-200 dark:bg-slate-700 rounded h-4 w-3/4" />
      <div class="bg-slate-200 dark:bg-slate-700 rounded h-4 w-1/2" />
    </div>
  </div>

  <!-- 头像骨架 -->
  <div
    v-else-if="type === 'avatar'"
    class="animate-pulse bg-slate-200 dark:bg-slate-700 rounded-full"
    :style="{ width, height }"
  />

  <!-- 图片骨架 -->
  <div
    v-else-if="type === 'image'"
    class="animate-pulse bg-slate-200 dark:bg-slate-700 rounded-lg"
    :style="{ width, height }"
  />

  <!-- 自定义骨架 -->
  <div
    v-else
    class="animate-pulse bg-slate-200 dark:bg-slate-700 rounded"
    :style="{ width, height }"
  />
</template>
