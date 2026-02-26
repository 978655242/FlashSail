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
    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
  </AppIcon>
</template>
