<script setup lang="ts">
import AppIcon from './AppIcon.vue'

withDefaults(defineProps<{
  size?: 'sm' | 'md' | 'lg' | 'xl' | number
  color?: string
  filled?: boolean
}>(), {
  size: 'md',
  color: 'currentColor',
  filled: false
})
</script>

<template>
  <AppIcon :size="size" :color="color">
    <path
      :fill="filled ? 'currentColor' : 'none'"
      stroke-linecap="round"
      stroke-linejoin="round"
      stroke-width="2"
      d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
  </AppIcon>
</template>
