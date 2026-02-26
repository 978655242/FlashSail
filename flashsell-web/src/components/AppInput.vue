<script setup lang="ts">
/**
 * AppInput Component
 *
 * A unified input component with glass-card styling, supporting various states,
 * sizes, and features including validation feedback, clearable, and password toggle.
 */
import { ref, computed, useSlots } from 'vue'

// Generate unique ID for accessibility
let idCounter = 0
const generateId = () => `app-input-${++idCounter}`

export interface AppInputProps {
  /** v-model binding value */
  modelValue?: string | number
  /** Input type */
  type?: 'text' | 'password' | 'email' | 'tel' | 'number'
  /** Placeholder text */
  placeholder?: string
  /** Label text displayed above input */
  label?: string
  /** Hint text displayed below input */
  hint?: string
  /** Error state - displays error styling */
  error?: boolean
  /** Error message to display (overrides hint when present) */
  errorMessage?: string
  /** Success state - displays success styling */
  success?: boolean
  /** Disabled state */
  disabled?: boolean
  /** Readonly state */
  readonly?: boolean
  /** Maximum character length */
  maxlength?: number
  /** Show clear button when input has value */
  clearable?: boolean
  /** Show password visibility toggle for password type */
  showPasswordToggle?: boolean
  /** Show state icon (success/error) */
  showStateIcon?: boolean
  /** Input size */
  size?: 'sm' | 'md' | 'lg'
}

const props = withDefaults(defineProps<AppInputProps>(), {
  modelValue: '',
  type: 'text',
  placeholder: '',
  label: '',
  hint: '',
  error: false,
  errorMessage: '',
  success: false,
  disabled: false,
  readonly: false,
  maxlength: undefined,
  clearable: false,
  showPasswordToggle: true,
  showStateIcon: true,
  size: 'md',
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | number): void
  (e: 'focus', event: FocusEvent): void
  (e: 'blur', event: FocusEvent): void
  (e: 'clear'): void
}>()

const slots = useSlots()

// State
const inputRef = ref<HTMLInputElement | null>(null)
const inputId = generateId()
const isFocused = ref(false)
const showPassword = ref(false)

// Computed
const inputValue = computed({
  get: () => props.modelValue,
  set: (value: string | number) => emit('update:modelValue', value),
})

const computedType = computed(() => {
  if (props.type === 'password' && showPassword.value) {
    return 'text'
  }
  return props.type
})

const containerClasses = computed(() => {
  const classes: string[] = [`app-input__container--${props.size}`]

  if (props.error) {
    classes.push('app-input__container--error')
  } else if (props.success) {
    classes.push('app-input__container--success')
  }

  if (isFocused.value) {
    classes.push('app-input__container--focused')
  }

  if (slots.prefix) {
    classes.push('app-input__container--has-prefix')
  }

  if (slots.suffix || props.clearable || props.showPasswordToggle || props.showStateIcon) {
    classes.push('app-input__container--has-suffix')
  }

  return classes
})

const hintClass = computed(() => {
  if (props.error || props.errorMessage) {
    return 'app-input__hint--error'
  }
  if (props.success) {
    return 'app-input__hint--success'
  }
  return ''
})

const stateIconClass = computed(() => {
  if (props.success) return 'app-input__state-icon--success'
  if (props.error) return 'app-input__state-icon--error'
  return ''
})

// Methods
function handleFocus(event: FocusEvent) {
  isFocused.value = true
  emit('focus', event)
}

function handleBlur(event: FocusEvent) {
  isFocused.value = false
  emit('blur', event)
}

function handleClear() {
  emit('update:modelValue', '')
  emit('clear')
  inputRef.value?.focus()
}

function togglePasswordVisibility() {
  showPassword.value = !showPassword.value
}

// Expose methods for parent components
defineExpose({
  focus: () => inputRef.value?.focus(),
  blur: () => inputRef.value?.blur(),
  select: () => inputRef.value?.select(),
})
</script>

<template>
  <div class="app-input-wrapper" :class="{ 'app-input--disabled': disabled }">
    <!-- Label -->
    <label v-if="label" :for="inputId" class="app-input__label">
      {{ label }}
    </label>

    <!-- Input Container -->
    <div class="app-input__container" :class="containerClasses">
      <!-- Prefix Slot -->
      <span v-if="$slots.prefix" class="app-input__prefix">
        <slot name="prefix" />
      </span>

      <!-- Input Element -->
      <input
        :id="inputId"
        ref="inputRef"
        v-model="inputValue"
        :type="computedType"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        :maxlength="maxlength"
        class="app-input__field"
        v-bind="$attrs"
        @focus="handleFocus"
        @blur="handleBlur"
      />

      <!-- Suffix Slot -->
      <span v-if="$slots.suffix" class="app-input__suffix">
        <slot name="suffix" />
      </span>

      <!-- Clear Button -->
      <button
        v-if="clearable && inputValue && !disabled"
        type="button"
        class="app-input__clear"
        @click="handleClear"
      >
        <svg class="app-input__clear-icon" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
        </svg>
      </button>

      <!-- Password Toggle -->
      <button
        v-if="showPasswordToggle && type === 'password'"
        type="button"
        class="app-input__toggle"
        @click="togglePasswordVisibility"
      >
        <svg v-if="!showPassword" class="app-input__toggle-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
        </svg>
        <svg v-else class="app-input__toggle-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
        </svg>
      </button>

      <!-- State Icon -->
      <span v-if="showStateIcon && (success || error)" class="app-input__state-icon" :class="stateIconClass">
        <!-- Success Icon -->
        <svg v-if="success" class="app-input__icon" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
        </svg>
        <!-- Error Icon -->
        <svg v-else-if="error" class="app-input__icon" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
        </svg>
      </span>
    </div>

    <!-- Hint / Error Message -->
    <p v-if="hint || errorMessage" class="app-input__hint" :class="hintClass">
      {{ errorMessage || hint }}
    </p>
  </div>
</template>

<style scoped>
/* Wrapper */
.app-input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  width: 100%;
}

.app-input--disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Label */
.app-input__label {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-secondary);
  user-select: none;
}

/* Input Container - Glass Card Style */
.app-input__container {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.app-input__container--focused {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
}

.app-input__container--error {
  border-color: var(--danger);
}

.app-input__container--error.app-input__container--focused {
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.app-input__container--success {
  border-color: var(--success);
}

.app-input__container--success.app-input__container--focused {
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

/* Size Variants */
.app-input__container--sm { height: 32px; }
.app-input__container--md { height: 40px; }
.app-input__container--lg { height: 48px; }

/* Input Field */
.app-input__field {
  flex: 1;
  width: 100%;
  height: 100%;
  padding: 0 1rem;
  background: transparent;
  border: none;
  outline: none;
  color: var(--text-primary);
  font-size: 1rem;
  font-family: var(--font-family-sans);
}

.app-input__container--sm .app-input__field {
  font-size: 0.875rem;
  padding: 0 0.75rem;
}

.app-input__container--lg .app-input__field {
  font-size: 1.125rem;
}

.app-input__field::placeholder {
  color: var(--text-muted);
}

.app-input__field:disabled {
  cursor: not-allowed;
}

/* Remove number input spinners */
.app-input__field[type="number"]::-webkit-outer-spin-button,
.app-input__field[type="number"]::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.app-input__field[type="number"] {
  -moz-appearance: textfield;
}

/* Prefix / Suffix */
.app-input__prefix,
.app-input__suffix {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: var(--text-muted);
}

.app-input__prefix { padding-left: 1rem; }
.app-input__suffix { padding-right: 1rem; }

/* Clear Button */
.app-input__clear {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 0.5rem;
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  transition: color 0.15s ease;
}

.app-input__clear:hover {
  color: var(--text-primary);
}

.app-input__clear-icon {
  width: 1rem;
  height: 1rem;
}

/* Password Toggle */
.app-input__toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 0.5rem;
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  transition: color 0.15s ease;
}

.app-input__toggle:hover {
  color: var(--text-primary);
}

.app-input__toggle-icon {
  width: 1.25rem;
  height: 1.25rem;
}

/* State Icon */
.app-input__state-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-right: 0.75rem;
}

.app-input__icon {
  width: 1.25rem;
  height: 1.25rem;
}

.app-input__state-icon--success { color: var(--success); }
.app-input__state-icon--error { color: var(--danger); }

/* Hint / Error Message */
.app-input__hint {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin: 0;
}

.app-input__hint--error { color: #f87171; }
.app-input__hint--success { color: var(--success); }

/* Light Mode Overrides */
html.light .app-input__container {
  background: rgba(255, 255, 255, 0.8);
}

html.light .app-input__container--focused {
  background: rgba(255, 255, 255, 0.95);
}

/* Reduced Motion Support */
@media (prefers-reduced-motion: reduce) {
  .app-input__container,
  .app-input__clear,
  .app-input__toggle {
    transition: none;
  }
}
</style>
