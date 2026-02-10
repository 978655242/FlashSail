<script setup lang="ts">
interface Props {
  visible: boolean
  title?: string
  message: string
  confirmText?: string
  cancelText?: string
  type?: 'info' | 'warning' | 'danger'
  loading?: boolean
}

withDefaults(defineProps<Props>(), {
  title: '确认操作',
  confirmText: '确认',
  cancelText: '取消',
  type: 'info',
  loading: false
})

defineEmits<{
  confirm: []
  cancel: []
}>()

const typeStyles = {
  info: {
    icon: 'bg-orange-100 dark:bg-orange-900/30',
    iconColor: 'text-orange-500',
    button: 'bg-orange-500 hover:bg-orange-600 focus:ring-orange-500'
  },
  warning: {
    icon: 'bg-yellow-100 dark:bg-yellow-900/30',
    iconColor: 'text-yellow-500',
    button: 'bg-yellow-500 hover:bg-yellow-600 focus:ring-yellow-500'
  },
  danger: {
    icon: 'bg-red-100 dark:bg-red-900/30',
    iconColor: 'text-red-500',
    button: 'bg-red-500 hover:bg-red-600 focus:ring-red-500'
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition ease-out duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition ease-in duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="visible"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50"
        @click.self="$emit('cancel')"
      >
        <Transition
          enter-active-class="transition ease-out duration-200"
          enter-from-class="opacity-0 scale-95"
          enter-to-class="opacity-100 scale-100"
          leave-active-class="transition ease-in duration-150"
          leave-from-class="opacity-100 scale-100"
          leave-to-class="opacity-0 scale-95"
        >
          <div
            v-if="visible"
            class="w-full max-w-md glass-card rounded-xl shadow-xl"
          >
            <div class="p-6">
              <div class="flex items-start gap-4">
                <!-- 图标 -->
                <div :class="['flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center', typeStyles[type].icon]">
                  <!-- 信息图标 -->
                  <svg v-if="type === 'info'" :class="['w-5 h-5', typeStyles[type].iconColor]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <!-- 警告图标 -->
                  <svg v-else-if="type === 'warning'" :class="['w-5 h-5', typeStyles[type].iconColor]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                  </svg>
                  <!-- 危险图标 -->
                  <svg v-else :class="['w-5 h-5', typeStyles[type].iconColor]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                  </svg>
                </div>

                <div class="flex-1">
                  <!-- 标题 -->
                  <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ title }}</h3>
                  <!-- 消息 -->
                  <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">{{ message }}</p>
                </div>
              </div>
            </div>

            <!-- 按钮区域 -->
            <div class="flex justify-end gap-3 px-6 py-4 bg-slate-900/30 dark:bg-slate-800/50 rounded-b-xl border-t border-slate-200 dark:border-slate-700">
              <button
                type="button"
                class="px-4 py-2 text-sm font-medium text-slate-700 dark:text-slate-200 glass-card border border-slate-300 dark:border-slate-600 rounded-lg hover:border-slate-400 dark:hover:border-slate-500 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 transition-colors"
                :disabled="loading"
                @click="$emit('cancel')"
              >
                {{ cancelText }}
              </button>
              <button
                type="button"
                :class="[
                  'px-4 py-2 text-sm font-medium text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed',
                  typeStyles[type].button
                ]"
                :disabled="loading"
                @click="$emit('confirm')"
              >
                <span v-if="loading" class="flex items-center gap-2">
                  <svg class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                  </svg>
                  处理中...
                </span>
                <span v-else>{{ confirmText }}</span>
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>
