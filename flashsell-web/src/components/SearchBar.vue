<script setup lang="ts">
import { ref, watch } from 'vue'

interface Props {
  modelValue?: string
  placeholder?: string
  loading?: boolean
  showHistory?: boolean
  history?: string[]
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '输入产品关键词，AI 帮你找爆品...',
  loading: false,
  showHistory: true,
  history: () => []
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  search: [query: string]
  historyClick: [query: string]
  historyRemove: [query: string]
  historyClear: []
}>()

const inputValue = ref(props.modelValue)
const isFocused = ref(false)
const showDropdown = ref(false)

watch(() => props.modelValue, (val) => {
  inputValue.value = val
})

function handleInput(e: Event) {
  const value = (e.target as HTMLInputElement).value
  inputValue.value = value
  emit('update:modelValue', value)
}

function handleSearch() {
  if (inputValue.value.trim()) {
    emit('search', inputValue.value.trim())
    showDropdown.value = false
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter') {
    handleSearch()
  }
}

function handleFocus() {
  isFocused.value = true
  if (props.showHistory && props.history.length > 0 && !inputValue.value) {
    showDropdown.value = true
  }
}

function handleBlur() {
  isFocused.value = false
  // 延迟关闭以允许点击历史记录
  setTimeout(() => {
    showDropdown.value = false
  }, 200)
}

function handleHistoryClick(query: string) {
  inputValue.value = query
  emit('update:modelValue', query)
  emit('historyClick', query)
  showDropdown.value = false
}

function handleHistoryRemove(e: Event, query: string) {
  e.stopPropagation()
  emit('historyRemove', query)
}

function handleClearHistory(e: Event) {
  e.stopPropagation()
  emit('historyClear')
}

function clearInput() {
  inputValue.value = ''
  emit('update:modelValue', '')
}
</script>

<template>
  <div class="relative">
    <div
      :class="[
        'flex items-center gap-3 glass-card dark:bg-slate-800 rounded-xl border-2 transition-all duration-200',
        isFocused
          ? 'border-orange-500 shadow-lg shadow-orange-500/10'
          : 'border-slate-200 dark:border-slate-700'
      ]"
    >
      <!-- 搜索图标 -->
      <div class="pl-4">
        <svg class="w-5 h-5 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
      </div>

      <!-- 输入框 -->
      <input
        type="text"
        :value="inputValue"
        :placeholder="placeholder"
        class="flex-1 py-3.5 bg-transparent text-slate-800 dark:text-white placeholder-slate-400 focus:outline-none"
        @input="handleInput"
        @keydown="handleKeydown"
        @focus="handleFocus"
        @blur="handleBlur"
      />

      <!-- 清除按钮 -->
      <button
        v-if="inputValue"
        class="p-1.5 text-slate-400 hover:text-slate-600 dark:hover:text-slate-300 transition-colors"
        @click="clearInput"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>

      <!-- 搜索按钮 -->
      <button
        :disabled="loading || !inputValue.trim()"
        class="flex items-center gap-2 px-5 py-2.5 mr-1.5 btn-gradient-primary text-white rounded-lg hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed transition-opacity"
        @click="handleSearch"
      >
        <svg v-if="loading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
        </svg>
        <span>{{ loading ? '搜索中' : '搜索' }}</span>
      </button>
    </div>

    <!-- 搜索历史下拉 -->
    <Transition
      enter-active-class="transition ease-out duration-200"
      enter-from-class="opacity-0 translate-y-1"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition ease-in duration-150"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 translate-y-1"
    >
      <div
        v-if="showDropdown && showHistory && history.length > 0"
        class="absolute top-full left-0 right-0 mt-2 glass-card dark:bg-slate-800 rounded-xl shadow-lg border border-slate-200 dark:border-slate-700 z-50 overflow-hidden"
      >
        <div class="flex items-center justify-between px-4 py-2 border-b border-slate-100 dark:border-slate-700">
          <span class="text-sm text-slate-500 dark:text-slate-400">搜索历史</span>
          <button
            class="text-xs text-slate-400 hover:text-red-500 transition-colors"
            @click="handleClearHistory"
          >
            清空
          </button>
        </div>
        <ul class="max-h-64 overflow-y-auto">
          <li
            v-for="item in history"
            :key="item"
            class="flex items-center justify-between px-4 py-2.5 hover:bg-slate-50 dark:hover:bg-slate-700/50 cursor-pointer group"
            @click="handleHistoryClick(item)"
          >
            <div class="flex items-center gap-3">
              <svg class="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span class="text-sm text-slate-700 dark:text-slate-300">{{ item }}</span>
            </div>
            <button
              class="p-1 text-slate-400 hover:text-red-500 opacity-0 group-hover:opacity-100 transition-all"
              @click="handleHistoryRemove($event, item)"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </li>
        </ul>
      </div>
    </Transition>
  </div>
</template>
