<script setup lang="ts">
import AppIcon from './AppIcon.vue'

withDefaults(defineProps<{
  size?: 'sm' | 'md' | 'lg' | 'xl' | number
  color?: string
}>(), {
  size: 'md',
  color: 'currentColor'
})
</script>

<template>
  <AppIcon :size="size" :color="color">
    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
      d="M13 10V3L4 14h7v7l9-11h-7z"/>
  </AppIcon>
</template>
