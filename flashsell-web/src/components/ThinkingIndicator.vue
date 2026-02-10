<script setup lang="ts">
/**
 * ThinkingIndicator Component
 * 
 * Displays AI thinking steps with progress during search operations.
 * Shows spinner animation and step completion states.
 * 
 * Requirements: 6.5
 * - Display AI thinking steps with progress
 * - Show spinner animation
 * - Implement step completion states
 */
import { computed } from 'vue'
import { useI18n } from '@/composables/useI18n'

export interface ThinkingStep {
  /** Unique step identifier */
  id: string
  /** Step label/description */
  label: string
  /** Step status */
  status: 'pending' | 'active' | 'completed'
}

export interface ThinkingIndicatorProps {
  /** Array of thinking steps */
  steps?: ThinkingStep[]
  /** Whether to show the indicator */
  show?: boolean
}

const props = withDefaults(defineProps<ThinkingIndicatorProps>(), {
  steps: () => [],
  show: true
})

const { t } = useI18n()

/** Default thinking steps if none provided */
const defaultSteps = computed<ThinkingStep[]>(() => [
  { id: 'analyzing', label: t('search.thinking.analyzing'), status: 'active' },
  { id: 'searching', label: t('search.thinking.searching'), status: 'pending' },
  { id: 'evaluating', label: t('search.thinking.evaluating'), status: 'pending' },
  { id: 'generating', label: t('search.thinking.generating'), status: 'pending' }
])

/** Steps to display */
const displaySteps = computed(() => {
  return props.steps.length > 0 ? props.steps : defaultSteps.value
})

/** Get icon for step status */
function getStepIcon(status: ThinkingStep['status']) {
  switch (status) {
    case 'completed':
      return 'check'
    case 'active':
      return 'spinner'
    default:
      return 'circle'
  }
}
</script>

<template>
  <div v-if="show" class="flex gap-3 mb-4">
    <!-- AI Avatar -->
    <div class="flex-shrink-0 w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center text-white">
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
      </svg>
    </div>

    <!-- Thinking Steps -->
    <div class="glass-card px-4 py-3 rounded-2xl rounded-tl-sm max-w-[80%]">
      <div class="space-y-2">
        <div
          v-for="step in displaySteps"
          :key="step.id"
          :class="[
            'flex items-center gap-2 text-sm transition-all duration-300',
            step.status === 'completed' ? 'text-green-400' :
            step.status === 'active' ? 'text-orange-400' :
            'text-slate-500'
          ]"
        >
          <!-- Step Icon -->
          <div class="flex-shrink-0 w-4 h-4 flex items-center justify-center">
            <!-- Completed Check -->
            <svg
              v-if="getStepIcon(step.status) === 'check'"
              class="w-4 h-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
            
            <!-- Active Spinner -->
            <svg
              v-else-if="getStepIcon(step.status) === 'spinner'"
              class="w-4 h-4 animate-spin"
              fill="none"
              viewBox="0 0 24 24"
            >
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
            </svg>
            
            <!-- Pending Circle -->
            <div
              v-else
              class="w-2 h-2 rounded-full bg-current opacity-50"
            />
          </div>

          <!-- Step Label -->
          <span :class="{ 'font-medium': step.status === 'active' }">
            {{ step.label }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
