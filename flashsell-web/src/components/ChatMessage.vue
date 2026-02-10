<script setup lang="ts">
/**
 * ChatMessage Component
 * 
 * Displays chat messages in the AI search interface.
 * Supports user messages (right-aligned, orange) and AI messages (left-aligned, with avatar).
 * 
 * Requirements: 6.4
 * - Display user query as message bubble
 * - Display AI response with avatar
 * - Support typing indicator
 */
import { computed } from 'vue'
import type { ProductDTO } from '@/types/product'

export interface ChatMessageProps {
  /** Message type: 'user' or 'ai' */
  type: 'user' | 'ai'
  /** Message content text */
  content: string
  /** Optional timestamp for the message */
  timestamp?: Date
  /** Optional products to display (for AI responses) */
  products?: ProductDTO[]
  /** Whether to show typing indicator */
  isTyping?: boolean
}

const props = withDefaults(defineProps<ChatMessageProps>(), {
  timestamp: undefined,
  products: () => [],
  isTyping: false
})

// Emit for product click events (used by parent components)
defineEmits<{
  /** Emitted when a product is clicked */
  productClick: [product: ProductDTO]
}>()

/** Format timestamp for display */
const formattedTime = computed(() => {
  if (!props.timestamp) return ''
  const date = props.timestamp
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
})
</script>

<template>
  <div
    :class="[
      'flex gap-3 mb-4',
      type === 'user' ? 'flex-row-reverse' : 'flex-row'
    ]"
  >
    <!-- Avatar -->
    <div
      :class="[
        'flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center',
        type === 'user' 
          ? 'bg-orange-500 text-white' 
          : 'bg-gradient-to-br from-blue-500 to-purple-600 text-white'
      ]"
    >
      <!-- User Icon -->
      <svg v-if="type === 'user'" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
      </svg>
      <!-- AI Icon -->
      <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
      </svg>
    </div>

    <!-- Message Content -->
    <div
      :class="[
        'max-w-[80%] flex flex-col',
        type === 'user' ? 'items-end' : 'items-start'
      ]"
    >
      <!-- Message Bubble -->
      <div
        :class="[
          'px-4 py-3 rounded-2xl',
          type === 'user'
            ? 'bg-orange-500 text-white rounded-tr-sm'
            : 'glass-card rounded-tl-sm'
        ]"
      >
        <!-- Typing Indicator -->
        <div v-if="isTyping" class="flex items-center gap-1">
          <span class="typing-dot"></span>
          <span class="typing-dot animation-delay-200"></span>
          <span class="typing-dot animation-delay-400"></span>
        </div>
        
        <!-- Message Text -->
        <p v-else class="text-sm whitespace-pre-wrap">{{ content }}</p>
      </div>

      <!-- Timestamp -->
      <span
        v-if="formattedTime"
        class="text-xs text-slate-500 mt-1 px-1"
      >
        {{ formattedTime }}
      </span>
    </div>
  </div>
</template>

<style scoped>
.typing-dot {
  width: 0.5rem;
  height: 0.5rem;
  background-color: rgb(148 163 184);
  border-radius: 9999px;
  animation: bounce 1s infinite;
}

.animation-delay-200 {
  animation-delay: 0.2s;
}

.animation-delay-400 {
  animation-delay: 0.4s;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(-25%);
    animation-timing-function: cubic-bezier(0.8, 0, 1, 1);
  }
  50% {
    transform: translateY(0);
    animation-timing-function: cubic-bezier(0, 0, 0.2, 1);
  }
}
</style>
