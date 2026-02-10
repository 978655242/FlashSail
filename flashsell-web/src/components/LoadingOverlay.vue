<script setup lang="ts">
interface Props {
  visible: boolean
  text?: string
  blur?: boolean
}

withDefaults(defineProps<Props>(), {
  text: '加载中...',
  blur: true
})
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition ease-out duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition ease-in duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="visible"
        :class="[
          'fixed inset-0 z-50 flex flex-col items-center justify-center',
          blur ? 'bg-slate-900/90 backdrop-blur-sm' : 'bg-slate-900/95'
        ]"
      >
        <!-- 加载动画 -->
        <div class="relative">
          <!-- 外圈 -->
          <div class="w-16 h-16 border-4 border-orange-200 dark:border-orange-900 rounded-full"></div>
          <!-- 旋转圈 -->
          <div class="absolute top-0 left-0 w-16 h-16 border-4 border-transparent border-t-orange-500 rounded-full animate-spin"></div>
        </div>

        <!-- 加载文字 -->
        <p class="mt-4 text-sm font-medium text-slate-300">{{ text }}</p>

        <!-- 进度点动画 -->
        <div class="flex gap-1 mt-2">
          <span class="w-1.5 h-1.5 bg-orange-500 rounded-full animate-bounce" style="animation-delay: 0ms"></span>
          <span class="w-1.5 h-1.5 bg-orange-500 rounded-full animate-bounce" style="animation-delay: 150ms"></span>
          <span class="w-1.5 h-1.5 bg-orange-500 rounded-full animate-bounce" style="animation-delay: 300ms"></span>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
