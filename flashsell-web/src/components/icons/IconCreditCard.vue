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
      d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"/>
  </AppIcon>
</template>
