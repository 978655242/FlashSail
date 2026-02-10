<template>
  <div
    :class="cardClasses"
    v-bind="$attrs"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
/**
 * GlassCard Component
 * 
 * A reusable glassmorphism card component with configurable props.
 * Uses CSS classes from src/styles/glassmorphism.css for styling.
 * 
 * Requirements: 2.2, 2.4
 * 
 * @example
 * <GlassCard size="md" variant="default" hover>
 *   <p>Card content</p>
 * </GlassCard>
 */
import { computed } from 'vue'

export interface GlassCardProps {
  /**
   * Size variant of the card
   * - sm: Small card with tighter border radius (radius-md)
   * - md: Medium card with standard border radius (radius-lg) - default
   * - lg: Large card with larger border radius (radius-xl)
   */
  size?: 'sm' | 'md' | 'lg'
  
  /**
   * Visual variant of the card
   * - default: Standard glass card with neutral styling
   * - primary: Glass card with orange glow accent
   * - secondary: Glass card with blue glow accent
   */
  variant?: 'default' | 'primary' | 'secondary'
  
  /**
   * Enable hover effects with enhanced visual feedback
   * When true, adds border color change and shadow on hover
   */
  hover?: boolean
  
  /**
   * Enable lift effect on hover
   * When true, adds upward translation animation on hover
   */
  lift?: boolean
  
  /**
   * Additional CSS classes to apply to the card
   */
  class?: string | string[] | Record<string, boolean>
}

const props = withDefaults(defineProps<GlassCardProps>(), {
  size: 'md',
  variant: 'default',
  hover: false,
  lift: false,
})

/**
 * Computed CSS classes based on props
 * Maps props to the appropriate glassmorphism CSS classes
 */
const cardClasses = computed(() => {
  const classes: string[] = []
  
  // Determine base class based on variant and effects
  if (props.variant === 'primary') {
    // Primary variant uses orange glow
    classes.push('glass-card-glow-orange')
  } else if (props.variant === 'secondary') {
    // Secondary variant uses blue glow
    classes.push('glass-card-glow-blue')
  } else if (props.lift) {
    // Lift effect takes precedence over hover for default variant
    classes.push('glass-card-lift')
  } else if (props.hover) {
    // Hover effect for default variant
    classes.push('glass-card-hover')
  } else {
    // Base glass card without effects
    // Apply size-specific class
    if (props.size === 'sm') {
      classes.push('glass-card-sm')
    } else if (props.size === 'lg') {
      classes.push('glass-card-xl')
    } else {
      classes.push('glass-card')
    }
  }
  
  // For variants with effects, we need to handle size separately
  // since the glow/lift/hover classes use radius-lg by default
  if (props.variant !== 'default' || props.lift || props.hover) {
    if (props.size === 'sm') {
      classes.push('glass-card--sm')
    } else if (props.size === 'lg') {
      classes.push('glass-card--lg')
    }
  }
  
  return classes
})
</script>

<style scoped>
/**
 * Size modifier classes for cards with effects
 * These override the default border-radius when size prop is used
 * with hover, lift, or variant props
 */
.glass-card--sm {
  border-radius: var(--radius-md) !important;
}

.glass-card--lg {
  border-radius: var(--radius-xl) !important;
}

/**
 * Ensure the card is a block-level element by default
 * and inherits proper box-sizing
 */
:deep(.glass-card),
:deep(.glass-card-hover),
:deep(.glass-card-lift),
:deep(.glass-card-sm),
:deep(.glass-card-xl),
:deep(.glass-card-glow-orange),
:deep(.glass-card-glow-blue) {
  box-sizing: border-box;
}
</style>
