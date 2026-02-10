<script setup lang="ts">
/**
 * Login.vue - Login/Register Page Component
 * 
 * Implements login/register form with phone and verification code inputs.
 * Features FlashSell branding, mode switching, and form validation.
 * 
 * Requirements: 4.1, 4.2, 4.5, 4.7
 * - 4.1: Display a form with phone number and verification code inputs
 * - 4.2: Provide a button to send verification code
 * - 4.5: Support switching between login and registration modes
 * - 4.7: Display FlashSell branding and logo
 */

import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/composables/useI18n'
import { sendVerifyCode } from '@/api/auth'
import LoadingState from '@/components/LoadingState.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const toast = useToast()
const { t } = useI18n()

// Form mode: login or register
const mode = ref<'login' | 'register'>('login')
const phone = ref('')
const verifyCode = ref('')
const isLoading = ref(false)
const isSendingCode = ref(false)
const countdown = ref(0)
const error = ref('')
const success = ref('')

// Computed properties
const isLoginMode = computed(() => mode.value === 'login')
const modeTitle = computed(() => isLoginMode.value ? t('login.title') : t('login.register'))
const modeButtonText = computed(() => isLoginMode.value ? t('login.loginButton') : t('login.registerButton'))
const switchModeText = computed(() => isLoginMode.value ? t('login.switchToRegister') : t('login.switchToLogin'))

// Phone validation - Chinese mobile number pattern: 1[3-9]XXXXXXXXX
const isValidPhone = computed(() => /^1[3-9]\d{9}$/.test(phone.value))

// Verification code validation
const isValidCode = computed(() => /^\d{6}$/.test(verifyCode.value))

// Form can be submitted
const canSubmit = computed(() => isValidPhone.value && isValidCode.value && !isLoading.value)

// Send code button text
const sendCodeButtonText = computed(() => {
  if (countdown.value > 0) {
    return `${countdown.value}s`
  }
  return t('login.sendCode')
})

// Switch between login and register modes
function switchMode() {
  mode.value = isLoginMode.value ? 'register' : 'login'
  error.value = ''
  success.value = ''
}

// Send verification code
async function handleSendCode() {
  if (!phone.value || countdown.value > 0) return

  if (!isValidPhone.value) {
    error.value = t('validation.phoneInvalid')
    return
  }

  try {
    isSendingCode.value = true
    error.value = ''
    await sendVerifyCode(phone.value)
    toast.success(t('login.sendCode') + ' ✓')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (err: unknown) {
    const errorMessage = err instanceof Error ? err.message : t('common.unknownError')
    error.value = errorMessage
  } finally {
    isSendingCode.value = false
  }
}

// Submit form
async function handleSubmit() {
  if (!phone.value || !verifyCode.value) {
    error.value = t('validation.required')
    return
  }

  if (!isValidPhone.value) {
    error.value = t('validation.phoneInvalid')
    return
  }

  if (!isValidCode.value) {
    error.value = t('validation.codeInvalid')
    return
  }

  try {
    isLoading.value = true
    error.value = ''
    success.value = ''

    if (isLoginMode.value) {
      await userStore.login(phone.value, verifyCode.value)
    } else {
      await userStore.register(phone.value, verifyCode.value)
      toast.success(t('login.register') + ' ✓')
    }

    // Redirect after successful login/register
    const redirect = route.query.redirect as string
    router.push(redirect || '/')
  } catch (err: unknown) {
    const errorMessage = err instanceof Error ? err.message : t('common.unknownError')
    error.value = errorMessage
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <!-- Aurora Background -->
    <div class="aurora-bg"></div>
    
    <!-- Main Content -->
    <div class="login-container">
      <!-- Logo and Branding -->
      <div class="branding">
        <div class="logo-wrapper">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="w-10 h-10">
              <path d="M13 2L3 14H12L11 22L21 10H12L13 2Z" fill="url(#lightning-gradient)" stroke="url(#lightning-gradient)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              <defs>
                <linearGradient id="lightning-gradient" x1="3" y1="2" x2="21" y2="22" gradientUnits="userSpaceOnUse">
                  <stop stop-color="#F97316"/>
                  <stop offset="1" stop-color="#EA580C"/>
                </linearGradient>
              </defs>
            </svg>
          </div>
          <span class="logo-text">FlashSell</span>
        </div>
        <p class="tagline">AI 驱动爆品选品工具</p>
      </div>

      <!-- Login/Register Form Card -->
      <div class="glass-card form-card">
        <h2 class="form-title">{{ modeTitle }}</h2>

        <!-- Error Alert -->
        <div v-if="error" class="alert alert-error">
          <svg class="alert-icon" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
          </svg>
          <span>{{ error }}</span>
        </div>

        <!-- Success Alert -->
        <div v-if="success" class="alert alert-success">
          <svg class="alert-icon" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
          </svg>
          <span>{{ success }}</span>
        </div>

        <form @submit.prevent="handleSubmit" class="login-form">
          <!-- Phone Number Input -->
          <div class="form-group">
            <label class="form-label">{{ t('login.phone') }}</label>
            <div class="input-wrapper">
              <input
                v-model="phone"
                type="tel"
                :placeholder="t('login.phonePlaceholder')"
                maxlength="11"
                class="form-input"
                :class="{ 'input-error': phone && !isValidPhone, 'input-success': phone && isValidPhone }"
              />
              <span v-if="phone && isValidPhone" class="input-icon input-icon-success">
                <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                </svg>
              </span>
              <span v-else-if="phone && !isValidPhone" class="input-icon input-icon-error">
                <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                </svg>
              </span>
            </div>
            <p v-if="phone && !isValidPhone" class="form-hint form-hint-error">
              {{ t('validation.phoneInvalid') }}
            </p>
          </div>

          <!-- Verification Code Input -->
          <div class="form-group">
            <label class="form-label">{{ t('login.verifyCode') }}</label>
            <div class="code-input-wrapper">
              <input
                v-model="verifyCode"
                type="text"
                :placeholder="t('login.verifyCodePlaceholder')"
                maxlength="6"
                class="form-input code-input"
                :class="{ 'input-error': verifyCode && !isValidCode }"
              />
              <button
                type="button"
                :disabled="countdown > 0 || isSendingCode || !isValidPhone"
                class="send-code-btn"
                @click="handleSendCode"
              >
                <LoadingState v-if="isSendingCode" size="sm" text="" />
                <span v-else>{{ sendCodeButtonText }}</span>
              </button>
            </div>
            <p v-if="verifyCode && !isValidCode" class="form-hint form-hint-error">
              {{ t('validation.codeInvalid') }}
            </p>
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            :disabled="!canSubmit"
            class="submit-btn btn-gradient-primary"
          >
            <LoadingState v-if="isLoading" size="sm" text="" />
            <span v-else>{{ modeButtonText }}</span>
          </button>
        </form>

        <!-- Mode Switch -->
        <div class="mode-switch">
          <button
            type="button"
            class="switch-btn"
            @click="switchMode"
          >
            {{ switchModeText }}
          </button>
        </div>

        <!-- Terms and Privacy -->
        <div class="terms">
          {{ t('login.agreement') }}
          <a href="/terms" target="_blank" class="terms-link">{{ t('login.termsOfService') }}</a>
          {{ t('login.and') }}
          <a href="/privacy" target="_blank" class="terms-link">{{ t('login.privacyPolicy') }}</a>
        </div>
      </div>

      <!-- Footer -->
      <div class="footer">
        <p>© 2025 FlashSell. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Login Page Container */
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  background-color: var(--bg-dark);
  position: relative;
  overflow: hidden;
}

/* Login Container */
.login-container {
  width: 100%;
  max-width: 420px;
  z-index: 1;
}

/* Branding Section */
.branding {
  text-align: center;
  margin-bottom: 2rem;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
}

.logo-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(249, 115, 22, 0.2) 0%, rgba(234, 88, 12, 0.1) 100%);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-glow-orange);
}

.logo-text {
  font-size: 2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #F97316 0%, #EA580C 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.tagline {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

/* Form Card */
.form-card {
  padding: 2rem;
}

.form-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 1.5rem;
  text-align: center;
}

/* Alert Styles */
.alert {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

.alert-icon {
  width: 1.25rem;
  height: 1.25rem;
  flex-shrink: 0;
}

.alert-error {
  background: rgba(239, 68, 68, 0.1);
  color: #f87171;
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.alert-success {
  background: rgba(16, 185, 129, 0.1);
  color: #34d399;
  border: 1px solid rgba(16, 185, 129, 0.2);
}

/* Form Styles */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-secondary);
}

.input-wrapper {
  position: relative;
}

.form-input {
  width: 100%;
  padding: 0.875rem 1rem;
  padding-right: 2.5rem;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-size: 1rem;
  transition: border-color var(--transition-normal), box-shadow var(--transition-normal);
}

.form-input::placeholder {
  color: var(--text-muted);
}

.form-input:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
}

.form-input.input-error {
  border-color: var(--danger);
}

.form-input.input-error:focus {
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.form-input.input-success {
  border-color: var(--success);
}

.input-icon {
  position: absolute;
  right: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
}

.input-icon-success {
  color: var(--success);
}

.input-icon-error {
  color: var(--danger);
}

.form-hint {
  font-size: 0.75rem;
  margin-top: 0.25rem;
}

.form-hint-error {
  color: #f87171;
}

/* Verification Code Input */
.code-input-wrapper {
  display: flex;
  gap: 0.75rem;
}

.code-input {
  flex: 1;
  padding-right: 1rem;
}

.send-code-btn {
  padding: 0.875rem 1rem;
  background: rgba(249, 115, 22, 0.1);
  color: var(--primary);
  border: 1px solid rgba(249, 115, 22, 0.3);
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  font-weight: 500;
  white-space: nowrap;
  min-width: 100px;
  transition: background-color var(--transition-normal), border-color var(--transition-normal);
}

.send-code-btn:hover:not(:disabled) {
  background: rgba(249, 115, 22, 0.2);
  border-color: rgba(249, 115, 22, 0.5);
}

.send-code-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Submit Button */
.submit-btn {
  width: 100%;
  padding: 0.875rem 1.5rem;
  margin-top: 0.5rem;
  font-size: 1rem;
}

/* Mode Switch */
.mode-switch {
  text-align: center;
  margin-top: 1.5rem;
}

.switch-btn {
  color: var(--primary);
  font-size: 0.875rem;
  font-weight: 500;
  background: none;
  border: none;
  transition: color var(--transition-normal);
}

.switch-btn:hover {
  color: var(--primary-hover);
}

/* Terms */
.terms {
  text-align: center;
  margin-top: 1rem;
  font-size: 0.75rem;
  color: var(--text-muted);
}

.terms-link {
  color: var(--primary);
  text-decoration: none;
  transition: color var(--transition-normal);
}

.terms-link:hover {
  color: var(--primary-hover);
  text-decoration: underline;
}

/* Footer */
.footer {
  text-align: center;
  margin-top: 2rem;
  font-size: 0.75rem;
  color: var(--text-muted);
}

/* Light Mode Overrides */
html.light .login-page {
  background-color: var(--bg-dark);
}

html.light .form-input {
  background: rgba(255, 255, 255, 0.8);
  border-color: var(--border);
}

html.light .form-input:focus {
  background: rgba(255, 255, 255, 0.95);
}

html.light .send-code-btn {
  background: rgba(249, 115, 22, 0.08);
}

html.light .send-code-btn:hover:not(:disabled) {
  background: rgba(249, 115, 22, 0.15);
}

/* Responsive Adjustments */
@media (max-width: 480px) {
  .login-container {
    max-width: 100%;
  }
  
  .form-card {
    padding: 1.5rem;
  }
  
  .logo-text {
    font-size: 1.75rem;
  }
  
  .code-input-wrapper {
    flex-direction: column;
  }
  
  .send-code-btn {
    width: 100%;
  }
}

/* Utility Classes */
.w-5 {
  width: 1.25rem;
}

.h-5 {
  height: 1.25rem;
}

.w-10 {
  width: 2.5rem;
}

.h-10 {
  height: 2.5rem;
}
</style>
