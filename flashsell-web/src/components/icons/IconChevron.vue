<script setup lang="ts">
import AppIcon from './AppIcon.vue'
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  size?: 'sm' | 'md' | 'lg' | 'xl' | number
  color?: string
  direction?: 'left' | 'right' | 'up' | 'down'
}>(), {
  size: 'md',
  color: 'currentColor',
  direction: 'left'
})

// 根据方向计算旋转角度
const rotationMap: Record<string, number> = {
  left: 0,
  up: 90,
  right: 180,
  down: 270
}

const rotation = computed(() => rotationMap[props.direction] || 0)
</script>

<template>
  <AppIcon
    :size="size"
    :color="color"
    :style="{ transform: `rotate(${rotation}deg)` }"
  >
    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
  </AppIcon>
</template>
