<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-slate-100 dark:bg-slate-900 px-4">
    <div class="text-center">
      <h1 class="text-9xl font-bold text-gray-200 dark:text-slate-700">404</h1>
      <h2 class="text-2xl font-bold text-slate-800 dark:text-white mt-4">页面未找到</h2>
      <p class="text-slate-500 dark:text-slate-400 mt-2">抱歉，您访问的页面不存在</p>
      <button
        class="mt-6 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        @click="goHome"
      >
        返回首页
      </button>
    </div>
  </div>
</template>
