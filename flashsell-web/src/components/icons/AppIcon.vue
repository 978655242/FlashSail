<script setup lang="ts">
/**
 * AppIcon - 基础图标组件
 *
 * 提供统一的图标尺寸和颜色控制
 * 支持预设尺寸 (sm, md, lg, xl) 或自定义数字尺寸
 */
import { computed } from 'vue'

interface Props {
  /** 尺寸: 预设值或数字(像素) */
  size?: 'sm' | 'md' | 'lg' | 'xl' | number
  /** 颜色: CSS颜色值或继承当前颜色 */
  color?: string
  /** 自定义类名 */
  class?: string
  /** 是否旋转动画 */
  spin?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  color: 'currentColor',
  class: '',
  spin: false
})

// 尺寸映射
const sizeMap = {
  sm: 16,
  md: 20,
  lg: 24,
  xl: 32
}

// 计算实际尺寸
const computedSize = computed(() => {
  if (typeof props.size === 'number') {
    return props.size
  }
  return sizeMap[props.size] || sizeMap.md
})

// 计算类名
const computedClass = computed(() => {
  const classes = [props.class]
  if (props.spin) {
    classes.push('animate-spin')
  }
  return classes.join(' ')
})
</script>

<template>
  <svg
    :width="computedSize"
    :height="computedSize"
    :class="computedClass"
    :style="{ color: props.color }"
    fill="none"
    stroke="currentColor"
    viewBox="0 0 24 24"
    aria-hidden="true"
  >
    <slot />
  </svg>
</template>
