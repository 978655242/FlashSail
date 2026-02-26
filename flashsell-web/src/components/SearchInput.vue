<script setup lang="ts">
/**
 * SearchInput Component
 * 
 * Chat-style search input with auto-resize textarea, send button, and optional image upload.
 * 
 * Requirements: 6.2
 * - Text area with auto-resize
 * - Send button with active state
 * - Image upload button (optional)
 * - Character count display
 */
import { ref, computed, watch, nextTick } from 'vue'
import { useI18n } from '@/composables/useI18n'

export interface SearchInputProps {
  /** v-model value */
  modelValue: string
  /** Placeholder text */
  placeholder?: string
  /** Whether input is disabled */
  disabled?: boolean
  /** Maximum character length */
  maxLength?: number
  /** Whether to show image upload button */
  showImageUpload?: boolean
}

const props = withDefaults(defineProps<SearchInputProps>(), {
  placeholder: '',
  disabled: false,
  maxLength: 500,
  showImageUpload: false
})

const emit = defineEmits<{
  /** v-model update */
  'update:modelValue': [value: string]
  /** Submit search query */
  submit: [value: string]
  /** Image file selected */
  imageUpload: [file: File]
}>()

const { t } = useI18n()

const textareaRef = ref<HTMLTextAreaElement | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)

/** Local input value */
const inputValue = computed({
  get: () => props.modelValue,
  set: (value: string) => emit('update:modelValue', value)
})

/** Character count */
const charCount = computed(() => props.modelValue.length)

/** Whether send button should be active */
const canSend = computed(() => {
  return props.modelValue.trim().length > 0 && !props.disabled
})

/** Auto-resize textarea */
function autoResize() {
  nextTick(() => {
    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto'
      const scrollHeight = textareaRef.value.scrollHeight
      // Max height of 150px (about 6 lines)
      textareaRef.value.style.height = `${Math.min(scrollHeight, 150)}px`
    }
  })
}

/** Watch for value changes to auto-resize */
watch(() => props.modelValue, () => {
  autoResize()
})

/** Handle submit */
function handleSubmit() {
  if (!canSend.value) return
  emit('submit', props.modelValue.trim())
}

/** Handle keydown for Enter to submit */
function handleKeydown(e: KeyboardEvent) {
  // Submit on Enter (without Shift)
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSubmit()
  }
}

/** Handle image upload click */
function handleImageClick() {
  fileInputRef.value?.click()
}

/** Handle file selection */
function handleFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    emit('imageUpload', file)
    // Reset input
    target.value = ''
  }
}
</script>

<template>
  <div class="glass-card p-3 rounded-2xl">
    <div class="flex items-end gap-2">
      <!-- Image Upload Button (Optional) -->
      <button
        v-if="showImageUpload"
        type="button"
        class="flex-shrink-0 p-2 text-slate-400 hover:text-slate-300 transition-colors rounded-lg hover:bg-slate-700/50"
        :disabled="disabled"
        @click="handleImageClick"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
        </svg>
      </button>
      <input
        ref="fileInputRef"
        type="file"
        accept="image/*"
        class="hidden"
        @change="handleFileChange"
      />

      <!-- Textarea -->
      <div class="flex-1 relative">
        <textarea
          ref="textareaRef"
          v-model="inputValue"
          :placeholder="placeholder || t('search.placeholder', '描述你想找的产品...')"
          :disabled="disabled"
          :maxlength="maxLength"
          rows="1"
          class="w-full bg-transparent text-slate-200 placeholder-slate-500 resize-none outline-none text-sm leading-6 py-1 pr-12"
          @keydown="handleKeydown"
          @input="autoResize"
        />
        
        <!-- Character Count -->
        <span
          v-if="charCount > 0"
          :class="[
            'absolute right-0 bottom-0 text-xs',
            charCount >= maxLength ? 'text-red-400' : 'text-slate-500'
          ]"
        >
          {{ charCount }}/{{ maxLength }}
        </span>
      </div>

      <!-- Send Button -->
      <button
        type="button"
        :class="[
          'flex-shrink-0 p-2 rounded-lg transition-all duration-200',
          canSend
            ? 'bg-orange-500 text-white hover:bg-orange-600 shadow-lg shadow-orange-500/25'
            : 'bg-slate-700 text-slate-500 cursor-not-allowed'
        ]"
        :disabled="!canSend"
        @click="handleSubmit"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
        </svg>
      </button>
    </div>
  </div>
</template>

<style scoped>
textarea {
  scrollbar-width: thin;
  scrollbar-color: var(--border) transparent;
}

textarea::-webkit-scrollbar {
  width: 4px;
}

textarea::-webkit-scrollbar-track {
  background: transparent;
}

textarea::-webkit-scrollbar-thumb {
  background-color: var(--border);
  border-radius: 2px;
}
</style>
