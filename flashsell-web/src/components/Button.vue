<template>
  <button
    :class="buttonClasses"
    :disabled="disabled || loading"
    @click="handleClick"
    v-bind="$attrs"
  >
    <!-- Loading spinner -->
    <span v-if="loading" class="btn-spinner">
      <svg
        class="spinner-icon"
        viewBox="0 0 24 24"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <circle
          class="spinner-track"
          cx="12"
          cy="12"
          r="10"
          stroke="currentColor"
          stroke-width="3"
        />
        <path
          class="spinner-head"
          d="M12 2C6.47715 2 2 6.47715 2 12"
          stroke="currentColor"
          stroke-width="3"
          stroke-linecap="round"
        />
      </svg>
    </span>
    
    <!-- Button content -->
    <span :class="{ 'btn-content-hidden': loading && !$slots.default }">
      <slot />
    </span>
  </button>
</template>

<script setup lang="ts">
/**
 * Button Component
 * 
 * A reusable button component with multiple variants, sizes, and states.
 * Supports loading state with spinner animation and disabled state.
 * 
 * Requirements: 4.2, 9.6
 * 
 * @example
 * <Button variant="primary" size="md" @click="handleClick">
 *   Click me
 * </Button>
 * 
 * @example
 * <Button variant="secondary" :loading="isLoading">
 *   Submit
 * </Button>
 */
import { computed } from 'vue'

export interface ButtonProps {
  /**
   * Visual variant of the button
   * - primary: Orange gradient button (main CTA)
   * - secondary: Blue button (secondary actions)
   * - ghost: Transparent button with border (tertiary actions)
   */
  variant?: 'primary' | 'secondary' | 'ghost'
  
  /**
   * Size of the button
   * - sm: Small button (28px height)
   * - md: Medium button (36px height) - default
   * - lg: Large button (44px height)
   */
  size?: 'sm' | 'md' | 'lg'
  
  /**
   * Loading state - shows spinner and disables interaction
   */
  loading?: boolean
  
  /**
   * Disabled state - prevents interaction
   */
  disabled?: boolean
  
  /**
   * Full width button
   */
  block?: boolean
}

const props = withDefaults(defineProps<ButtonProps>(), {
  variant: 'primary',
  size: 'md',
  loading: false,
  disabled: false,
  block: false,
})

const emit = defineEmits<{
  (e: 'click', event: MouseEvent): void
}>()

/**
 * Computed CSS classes based on props
 */
const buttonClasses = computed(() => {
  const classes: string[] = ['btn']
  
  // Variant classes
  classes.push(`btn-${props.variant}`)
  
  // Size classes
  classes.push(`btn-${props.size}`)
  
  // State classes
  if (props.loading) {
    classes.push('btn-loading')
  }
  
  if (props.disabled) {
    classes.push('btn-disabled')
  }
  
  if (props.block) {
    classes.push('btn-block')
  }
  
  return classes
})

/**
 * Handle click event
 * Only emits if not loading or disabled
 */
function handleClick(event: MouseEvent) {
  if (!props.loading && !props.disabled) {
    emit('click', event)
  }
}
</script>

<style scoped>
/**
 * Base button styles
 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-family: var(--font-family-sans);
  font-weight: var(--font-weight-medium);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-normal);
  white-space: nowrap;
  user-select: none;
  position: relative;
  overflow: hidden;
}

.btn:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
}

/**
 * Size variants
 */
.btn-sm {
  height: 28px;
  padding: 0 12px;
  font-size: var(--font-size-xs);
  border-radius: var(--radius-sm);
}

.btn-md {
  height: 36px;
  padding: 0 16px;
  font-size: var(--font-size-sm);
}

.btn-lg {
  height: 44px;
  padding: 0 24px;
  font-size: var(--font-size-base);
  border-radius: var(--radius-lg);
}

/**
 * Primary variant - Orange gradient
 */
.btn-primary {
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-hover) 100%);
  color: white;
  box-shadow: var(--shadow-md), var(--shadow-glow-orange);
}

.btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--primary-hover) 0%, #C2410C 100%);
  box-shadow: var(--shadow-lg), 0 0 30px rgba(249, 115, 22, 0.3);
  transform: translateY(-1px);
}

.btn-primary:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: var(--shadow-sm), var(--shadow-glow-orange);
}

/**
 * Secondary variant - Blue
 */
.btn-secondary {
  background: linear-gradient(135deg, var(--secondary) 0%, var(--secondary-hover) 100%);
  color: white;
  box-shadow: var(--shadow-md), var(--shadow-glow-blue);
}

.btn-secondary:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--secondary-hover) 0%, #1D4ED8 100%);
  box-shadow: var(--shadow-lg), 0 0 30px rgba(59, 130, 246, 0.3);
  transform: translateY(-1px);
}

.btn-secondary:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: var(--shadow-sm), var(--shadow-glow-blue);
}

/**
 * Ghost variant - Transparent with border
 */
.btn-ghost {
  background: transparent;
  color: var(--text-primary);
  border: 1px solid var(--border);
}

.btn-ghost:hover:not(:disabled) {
  background: var(--bg-card-hover);
  border-color: var(--border-hover);
}

.btn-ghost:active:not(:disabled) {
  background: var(--bg-card);
}

/**
 * Disabled state
 */
.btn:disabled,
.btn-disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
}

.btn-primary:disabled,
.btn-primary.btn-disabled {
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-hover) 100%);
  box-shadow: none;
}

.btn-secondary:disabled,
.btn-secondary.btn-disabled {
  background: linear-gradient(135deg, var(--secondary) 0%, var(--secondary-hover) 100%);
  box-shadow: none;
}

.btn-ghost:disabled,
.btn-ghost.btn-disabled {
  background: transparent;
  border-color: var(--border);
}

/**
 * Loading state
 */
.btn-loading {
  cursor: wait;
}

.btn-spinner {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.spinner-icon {
  width: 16px;
  height: 16px;
  animation: spin 1s linear infinite;
}

.btn-sm .spinner-icon {
  width: 14px;
  height: 14px;
}

.btn-lg .spinner-icon {
  width: 18px;
  height: 18px;
}

.spinner-track {
  opacity: 0.25;
}

.spinner-head {
  opacity: 1;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.btn-content-hidden {
  visibility: hidden;
  width: 0;
  overflow: hidden;
}

/**
 * Block (full width) variant
 */
.btn-block {
  width: 100%;
}

/**
 * Light mode adjustments
 */
html.light .btn-ghost {
  color: var(--text-primary);
  border-color: var(--border);
}

html.light .btn-ghost:hover:not(:disabled) {
  background: var(--bg-card-hover);
  border-color: var(--border-hover);
}
</style>
