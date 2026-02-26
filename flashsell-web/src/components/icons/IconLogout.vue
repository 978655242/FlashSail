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
      d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
  </AppIcon>
</template>
