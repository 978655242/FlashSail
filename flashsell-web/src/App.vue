<script setup lang="ts">
import { onMounted, onErrorCaptured, ref } from 'vue'
import { RouterView } from 'vue-router'
import Toast from '@/components/Toast.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import { useTheme } from '@/composables/useTheme'
import { useI18nStore } from '@/stores/i18n'

// Initialize theme system
const { theme } = useTheme()

// Initialize i18n
const i18nStore = useI18nStore()

// Toast and confirm composables
const toast = useToast()
const confirm = useConfirm()

// Global error state (can be used for error boundary display)
const _globalError = ref<Error | null>(null)

/**
 * Initialize application on mount
 * - Theme is auto-initialized by useTheme composable
 * - i18n is auto-initialized by the store
 */
onMounted(() => {
  // Log initialization for debugging
  console.log(`[FlashSell] App initialized with theme: ${theme.value}, locale: ${i18nStore.locale}`)
})

/**
 * Global error handler
 * Captures unhandled errors from child components
 */
onErrorCaptured((error: Error, _instance, info: string) => {
  console.error('[FlashSell] Global error captured:', error)
  console.error('[FlashSell] Error info:', info)
  
  // Store error for potential display
  _globalError.value = error
  
  // Show error toast to user
  toast.error(`发生错误: ${error.message || '未知错误'}`)
  
  // Return false to prevent error from propagating further
  // Return true if you want the error to continue propagating
  return false
})

/**
 * Handle global unhandled promise rejections
 */
if (typeof window !== 'undefined') {
  window.addEventListener('unhandledrejection', (event) => {
    console.error('[FlashSell] Unhandled promise rejection:', event.reason)
    toast.error('操作失败，请稍后重试')
  })
}
</script>

<template>
  <!-- Aurora Background - Applied globally -->
  <div class="aurora-background" :class="{ 'light': theme === 'light' }">
    <!-- Aurora gradient orbs -->
    <div class="aurora-orb aurora-orb-1"></div>
    <div class="aurora-orb aurora-orb-2"></div>
    <div class="aurora-orb aurora-orb-3"></div>
  </div>

  <!-- Main Application Content -->
  <div class="app-container">
    <RouterView />
  </div>
  
  <!-- 全局 Toast 通知 -->
  <Toast
    :message="toast.message.value"
    :type="toast.type.value"
    :duration="toast.duration.value"
    :visible="toast.visible.value"
    @close="toast.close"
  />

  <!-- 全局确认对话框 -->
  <ConfirmDialog
    :visible="confirm.visible.value"
    :title="confirm.title.value"
    :message="confirm.message.value"
    :confirm-text="confirm.confirmText.value"
    :cancel-text="confirm.cancelText.value"
    :type="confirm.type.value"
    :loading="confirm.loading.value"
    @confirm="confirm.confirm"
    @cancel="confirm.cancel"
  />
</template>

<style scoped>
/* Aurora Background Styles */
.aurora-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  overflow: hidden;
  background: var(--bg-dark, #0F172A);
  transition: background var(--transition-slow, 0.3s ease);
}

.aurora-background.light {
  background: var(--bg-dark, #F8FAFC);
}

/* Aurora Orbs - Animated gradient circles */
.aurora-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.6;
  animation: aurora-float 20s ease-in-out infinite;
}

.aurora-orb-1 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(249, 115, 22, 0.4) 0%, transparent 70%);
  top: -200px;
  right: -100px;
  animation-delay: 0s;
}

.aurora-orb-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.3) 0%, transparent 70%);
  bottom: -150px;
  left: -100px;
  animation-delay: -7s;
}

.aurora-orb-3 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.25) 0%, transparent 70%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -14s;
}

/* Light mode aurora adjustments */
.aurora-background.light .aurora-orb-1 {
  background: radial-gradient(circle, rgba(249, 115, 22, 0.2) 0%, transparent 70%);
}

.aurora-background.light .aurora-orb-2 {
  background: radial-gradient(circle, rgba(59, 130, 246, 0.15) 0%, transparent 70%);
}

.aurora-background.light .aurora-orb-3 {
  background: radial-gradient(circle, rgba(139, 92, 246, 0.12) 0%, transparent 70%);
}

/* Aurora float animation */
@keyframes aurora-float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(30px, -30px) scale(1.05);
  }
  50% {
    transform: translate(-20px, 20px) scale(0.95);
  }
  75% {
    transform: translate(-30px, -20px) scale(1.02);
  }
}

/* App container */
.app-container {
  position: relative;
  min-height: 100vh;
  z-index: 1;
}

/* Light mode aurora orbs - more subtle */
html.light .aurora-orb {
  opacity: 0.4;
}
</style>
