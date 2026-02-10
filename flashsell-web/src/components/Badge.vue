<template>
  <span :class="badgeClasses">
    <!-- Icon for status badges -->
    <svg
      v-if="showIcon && variant === 'hot'"
      class="badge-icon"
      viewBox="0 0 24 24"
      fill="currentColor"
    >
      <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
      <path d="M17.66 11.2C17.43 10.9 17.15 10.64 16.89 10.38C16.22 9.78 15.46 9.35 14.82 8.72C13.33 7.26 13 4.85 13.95 3C13 3.23 12.17 3.75 11.46 4.32C8.87 6.4 7.85 10.07 9.07 13.22C9.11 13.32 9.15 13.42 9.15 13.55C9.15 13.77 9 13.97 8.8 14.05C8.57 14.15 8.33 14.09 8.14 13.93C8.08 13.88 8.04 13.83 8 13.76C6.87 12.33 6.69 10.28 7.45 8.64C5.78 10 4.87 12.3 5 14.47C5.06 14.97 5.12 15.47 5.29 15.97C5.43 16.57 5.7 17.17 6 17.7C7.08 19.43 8.95 20.67 10.96 20.92C13.1 21.19 15.39 20.8 17.03 19.32C18.86 17.66 19.5 15 18.56 12.72L18.43 12.46C18.22 12 17.66 11.2 17.66 11.2ZM14.5 17.5C14.22 17.74 13.76 18 13.4 18.1C12.28 18.5 11.16 17.94 10.5 17.28C11.69 17 12.4 16.12 12.61 15.23C12.78 14.43 12.46 13.77 12.33 13C12.21 12.26 12.23 11.63 12.5 10.94C12.69 11.32 12.89 11.7 13.13 12C13.9 13 15.11 13.44 15.37 14.8C15.41 14.94 15.43 15.08 15.43 15.23C15.46 16.05 15.1 16.95 14.5 17.5Z" />
    </svg>
    <svg
      v-else-if="showIcon && variant === 'trending'"
      class="badge-icon"
      viewBox="0 0 24 24"
      fill="currentColor"
    >
      <path d="M16 6l2.29 2.29-4.88 4.88-4-4L2 16.59 3.41 18l6-6 4 4 6.3-6.29L22 12V6z" />
    </svg>
    <svg
      v-else-if="showIcon && variant === 'new'"
      class="badge-icon"
      viewBox="0 0 24 24"
      fill="currentColor"
    >
      <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
    </svg>
    
    <!-- Platform icons -->
    <svg
      v-if="showIcon && variant === 'amazon'"
      class="badge-icon"
      viewBox="0 0 24 24"
      fill="currentColor"
    >
      <path d="M15.93 17.09c-.18.16-.43.17-.63.06-.89-.74-1.05-1.08-1.54-1.79-1.47 1.5-2.51 1.95-4.42 1.95-2.25 0-4.01-1.39-4.01-4.17 0-2.18 1.17-3.64 2.86-4.38 1.46-.64 3.49-.76 5.04-.93V7.5c0-.66.05-1.41-.33-1.96-.32-.49-.95-.7-1.5-.7-1.02 0-1.93.53-2.15 1.61-.05.24-.25.48-.47.49l-2.6-.28c-.22-.05-.46-.22-.4-.54.6-3.15 3.45-4.1 6-4.1 1.3 0 3 .35 4.03 1.33C17.11 4.55 17 6.18 17 7.95v4.17c0 1.25.52 1.81 1.01 2.48.17.25.21.54 0 .71-.54.46-1.51 1.32-2.04 1.79l-.04-.01zM13.25 11.5c0 1.08.03 1.98-.52 2.94-.44.78-1.14 1.26-1.91 1.26-1.06 0-1.68-.81-1.68-2 0-2.36 2.12-2.79 4.11-2.79v.59z" />
      <path d="M20.16 19.54C18 21.14 14.82 22 12.1 22c-3.81 0-7.25-1.41-9.85-3.76-.2-.18-.02-.43.22-.29 2.81 1.63 6.28 2.61 9.87 2.61 2.42 0 5.08-.5 7.53-1.54.37-.16.68.24.29.52z" />
    </svg>
    <svg
      v-else-if="showIcon && variant === 'ebay'"
      class="badge-icon"
      viewBox="0 0 24 24"
      fill="currentColor"
    >
      <path d="M5.71 8.42c-2.14 0-3.71 1.03-3.71 3.21 0 1.83 1.14 2.87 3.14 2.87 1.14 0 2.14-.43 2.71-1.14v.86h1.86V9.14c0-2.14-1.43-2.72-3.43-2.72-1.71 0-3.14.57-3.43 2.14h1.86c.14-.57.71-.86 1.57-.86.86 0 1.43.29 1.43 1v.43c-.43-.29-1.14-.43-2-.43zm.14 4.86c-.86 0-1.43-.43-1.43-1.14 0-.71.57-1.14 1.43-1.14.86 0 1.43.43 1.43 1.14 0 .71-.57 1.14-1.43 1.14z" />
      <path d="M12.57 5.71v8.57h1.86v-3.43c0-1.14.71-1.71 1.71-1.71.86 0 1.43.57 1.43 1.43v3.71h1.86v-4c0-1.71-1-2.86-2.71-2.86-1 0-1.86.43-2.29 1.14V5.71h-1.86z" />
    </svg>
    <svg
      v-else-if="showIcon && variant === 'aliexpress'"
      class="badge-icon"
      viewBox="0 0 24 24"
      fill="currentColor"
    >
      <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" stroke-width="2" fill="none" />
    </svg>
    <svg
      v-else-if="showIcon && variant === 'tiktok'"
      class="badge-icon"
      viewBox="0 0 24 24"
      fill="currentColor"
    >
      <path d="M19.59 6.69a4.83 4.83 0 01-3.77-4.25V2h-3.45v13.67a2.89 2.89 0 01-5.2 1.74 2.89 2.89 0 012.31-4.64 2.93 2.93 0 01.88.13V9.4a6.84 6.84 0 00-1-.05A6.33 6.33 0 005 20.1a6.34 6.34 0 0010.86-4.43v-7a8.16 8.16 0 004.77 1.52v-3.4a4.85 4.85 0 01-1-.1z" />
    </svg>
    
    <!-- Badge text -->
    <span class="badge-text">
      <slot>{{ label }}</slot>
    </span>
  </span>
</template>

<script setup lang="ts">
/**
 * Badge Component
 * 
 * A versatile badge component for displaying status indicators and platform labels.
 * Supports status variants (hot, trending, new) and platform variants (amazon, ebay, aliexpress, tiktok).
 * 
 * Requirements: 15.4, 15.5
 * 
 * @example
 * <!-- Status badge -->
 * <Badge variant="hot">爆品</Badge>
 * <Badge variant="trending">趋势上升</Badge>
 * <Badge variant="new">新品</Badge>
 * 
 * @example
 * <!-- Platform badge -->
 * <Badge variant="amazon" size="sm">Amazon</Badge>
 * <Badge variant="ebay" size="sm">eBay</Badge>
 */
import { computed } from 'vue'

export type BadgeVariant = 
  | 'hot' 
  | 'trending' 
  | 'new' 
  | 'amazon' 
  | 'ebay' 
  | 'aliexpress' 
  | 'tiktok'
  | 'default'

export type BadgeSize = 'sm' | 'md'

export interface BadgeProps {
  /**
   * Visual variant of the badge
   * Status variants: hot (red), trending (green), new (blue)
   * Platform variants: amazon (orange), ebay (blue), aliexpress (red/pink), tiktok (black/pink)
   */
  variant?: BadgeVariant
  
  /**
   * Size of the badge
   * - sm: Small badge (for platform badges, 10px font)
   * - md: Medium badge (for status badges, 12px font) - default
   */
  size?: BadgeSize
  
  /**
   * Whether to show an icon before the text
   */
  showIcon?: boolean
  
  /**
   * Default label text (can be overridden by slot)
   */
  label?: string
}

const props = withDefaults(defineProps<BadgeProps>(), {
  variant: 'default',
  size: 'md',
  showIcon: false,
  label: '',
})

/**
 * Determine if this is a platform badge
 */
const isPlatformBadge = computed(() => {
  return ['amazon', 'ebay', 'aliexpress', 'tiktok'].includes(props.variant)
})

/**
 * Computed CSS classes based on props
 */
const badgeClasses = computed(() => {
  const classes: string[] = ['badge']
  
  // Variant classes
  classes.push(`badge-${props.variant}`)
  
  // Size classes
  classes.push(`badge-${props.size}`)
  
  // Platform badge specific class
  if (isPlatformBadge.value) {
    classes.push('badge-platform')
  }
  
  // Icon class
  if (props.showIcon) {
    classes.push('badge-with-icon')
  }
  
  return classes
})
</script>

<style scoped>
/**
 * Base badge styles
 */
.badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-family: var(--font-family-sans);
  font-weight: var(--font-weight-semibold);
  border-radius: var(--radius-full);
  white-space: nowrap;
  user-select: none;
  transition: all var(--transition-fast);
}

/**
 * Size variants
 */
.badge-sm {
  padding: 2px 6px;
  font-size: 10px;
  border-radius: 4px;
}

.badge-md {
  padding: 4px 10px;
  font-size: var(--font-size-xs);
  border-radius: var(--radius-full);
}

/**
 * Default variant
 */
.badge-default {
  background: var(--bg-card-hover);
  color: var(--text-secondary);
}

/**
 * Status variants
 */

/* Hot badge - Red/Orange for hot products */
.badge-hot {
  background: rgba(239, 68, 68, 0.15);
  color: #EF4444;
}

html.light .badge-hot {
  background: rgba(239, 68, 68, 0.1);
  color: #DC2626;
}

/* Trending badge - Green for trending products */
.badge-trending {
  background: rgba(16, 185, 129, 0.15);
  color: #10B981;
}

html.light .badge-trending {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
}

/* New badge - Blue for new products */
.badge-new {
  background: rgba(59, 130, 246, 0.15);
  color: #3B82F6;
}

html.light .badge-new {
  background: rgba(59, 130, 246, 0.1);
  color: #2563EB;
}

/**
 * Platform variants
 */

/* Amazon badge - Orange */
.badge-amazon {
  background: rgba(249, 115, 22, 0.2);
  color: #fb923c;
}

html.light .badge-amazon {
  background: rgba(249, 115, 22, 0.15);
  color: #EA580C;
}

/* eBay badge - Blue */
.badge-ebay {
  background: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
}

html.light .badge-ebay {
  background: rgba(59, 130, 246, 0.15);
  color: #2563EB;
}

/* AliExpress badge - Pink/Red */
.badge-aliexpress {
  background: rgba(236, 72, 153, 0.2);
  color: #f472b6;
}

html.light .badge-aliexpress {
  background: rgba(236, 72, 153, 0.15);
  color: #DB2777;
}

/* TikTok badge - Black/Gray in dark mode, Cyan in light mode */
.badge-tiktok {
  background: rgba(148, 163, 184, 0.3);
  color: #e2e8f0;
}

html.light .badge-tiktok {
  background: rgba(0, 0, 0, 0.1);
  color: #0F172A;
}

/**
 * Platform badge specific styles
 */
.badge-platform {
  font-weight: var(--font-weight-medium);
}

/**
 * Icon styles
 */
.badge-icon {
  width: 12px;
  height: 12px;
  flex-shrink: 0;
}

.badge-sm .badge-icon {
  width: 10px;
  height: 10px;
}

.badge-md .badge-icon {
  width: 12px;
  height: 12px;
}

/**
 * Badge with icon alignment
 */
.badge-with-icon {
  gap: 4px;
}

.badge-with-icon .badge-text {
  line-height: 1;
}

/**
 * Badge text
 */
.badge-text {
  line-height: 1.2;
}
</style>
