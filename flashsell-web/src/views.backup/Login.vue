<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'
import { sendVerifyCode } from '@/api/auth'
import LoadingState from '@/components/LoadingState.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const toast = useToast()

// 表单模式：login 或 register
const mode = ref<'login' | 'register'>('login')
const phone = ref('')
const verifyCode = ref('')
const isLoading = ref(false)
const isSendingCode = ref(false)
const countdown = ref(0)
const error = ref('')
const success = ref('')

// 计算属性
const isLoginMode = computed(() => mode.value === 'login')
const modeTitle = computed(() => isLoginMode.value ? '登录' : '注册')
const modeButtonText = computed(() => isLoginMode.value ? '登录' : '注册')
const switchModeText = computed(() => isLoginMode.value ? '没有账号？立即注册' : '已有账号？立即登录')

// 手机号验证
const isValidPhone = computed(() => /^1[3-9]\d{9}$/.test(phone.value))

// 切换模式
function switchMode() {
  mode.value = isLoginMode.value ? 'register' : 'login'
  error.value = ''
  success.value = ''
}

// 发送验证码
async function handleSendCode() {
  if (!phone.value || countdown.value > 0) return

  if (!isValidPhone.value) {
    error.value = '请输入正确的手机号'
    return
  }

  try {
    isSendingCode.value = true
    error.value = ''
    await sendVerifyCode(phone.value)
    toast.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (err: unknown) {
    const errorMessage = err instanceof Error ? err.message : '发送验证码失败，请重试'
    error.value = errorMessage
  } finally {
    isSendingCode.value = false
  }
}

// 提交表单
async function handleSubmit() {
  if (!phone.value || !verifyCode.value) {
    error.value = '请输入手机号和验证码'
    return
  }

  if (!isValidPhone.value) {
    error.value = '请输入正确的手机号'
    return
  }

  if (verifyCode.value.length !== 6) {
    error.value = '请输入6位验证码'
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
      toast.success('注册成功！')
    }

    // 登录/注册成功后跳转
    const redirect = route.query.redirect as string
    router.push(redirect || '/')
  } catch (err: unknown) {
    const errorMessage = err instanceof Error ? err.message : (isLoginMode.value ? '登录失败，请检查手机号和验证码' : '注册失败，请重试')
    error.value = errorMessage
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-slate-900 px-4">
    <div class="w-full max-w-md">
      <!-- Logo -->
      <div class="text-center mb-8">
        <div class="flex items-center justify-center gap-2 mb-2">
          <span class="text-4xl">⚡</span>
          <span class="text-3xl font-bold text-white">FlashSell</span>
        </div>
        <p class="text-slate-400">AI 驱动爆品选品工具</p>
      </div>

      <!-- 登录/注册表单 -->
      <div class="glass-card p-8">
        <h2 class="text-2xl font-bold text-white mb-6">{{ modeTitle }}</h2>

        <!-- 错误提示 -->
        <div
          v-if="error"
          class="mb-4 p-3 bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-400 text-sm rounded-lg flex items-center gap-2"
        >
          <svg class="w-5 h-5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
          </svg>
          <span>{{ error }}</span>
        </div>

        <!-- 成功提示 -->
        <div
          v-if="success"
          class="mb-4 p-3 bg-green-50 dark:bg-green-900/30 text-green-600 dark:text-green-400 text-sm rounded-lg flex items-center gap-2"
        >
          <svg class="w-5 h-5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
          </svg>
          <span>{{ success }}</span>
        </div>

        <form @submit.prevent="handleSubmit">
          <!-- 手机号 -->
          <div class="mb-4">
            <label class="block text-sm font-medium text-slate-300 mb-2">
              手机号
            </label>
            <div class="relative">
              <input
                v-model="phone"
                type="tel"
                placeholder="请输入手机号"
                maxlength="11"
                class="w-full px-4 py-3 border border-slate-600 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent bg-slate-800 text-white transition-colors"
                :class="{ 'border-red-500': phone && !isValidPhone }"
              />
              <span
                v-if="phone && isValidPhone"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-green-500"
              >
                <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                </svg>
              </span>
            </div>
            <p v-if="phone && !isValidPhone" class="mt-1 text-sm text-red-500">
              请输入正确的手机号
            </p>
          </div>

          <!-- 验证码 -->
          <div class="mb-6">
            <label class="block text-sm font-medium text-slate-300 mb-2">
              验证码
            </label>
            <div class="flex gap-3">
              <input
                v-model="verifyCode"
                type="text"
                placeholder="请输入6位验证码"
                maxlength="6"
                class="flex-1 px-4 py-3 border border-slate-600 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent bg-slate-800 text-white transition-colors"
              />
              <button
                type="button"
                :disabled="countdown > 0 || isSendingCode || !isValidPhone"
                class="px-4 py-3 text-sm font-medium text-orange-400 bg-orange-500/10 rounded-lg hover:bg-orange-500/20 disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap transition-colors min-w-[100px]"
                @click="handleSendCode"
              >
                <LoadingState v-if="isSendingCode" size="sm" text="" />
                <span v-else>{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}</span>
              </button>
            </div>
          </div>

          <!-- 提交按钮 -->
          <button
            type="submit"
            :disabled="isLoading || !isValidPhone || verifyCode.length !== 6"
            class="w-full py-3 text-white btn-gradient-primary rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors font-medium"
          >
            <LoadingState v-if="isLoading" size="sm" text="" />
            <span v-else>{{ modeButtonText }}</span>
          </button>
        </form>

        <!-- 切换登录/注册 -->
        <div class="mt-6 text-center">
          <button
            type="button"
            class="text-orange-400 hover:text-orange-300 text-sm font-medium transition-colors"
            @click="switchMode"
          >
            {{ switchModeText }}
          </button>
        </div>

        <!-- 服务条款 -->
        <div class="mt-4 text-center text-sm text-slate-500">
          {{ isLoginMode ? '登录' : '注册' }}即表示同意
          <a href="/terms" target="_blank" class="text-orange-400 hover:underline">服务条款</a>
          和
          <a href="/privacy" target="_blank" class="text-orange-400 hover:underline">隐私政策</a>
        </div>
      </div>

      <!-- 底部信息 -->
      <div class="mt-8 text-center text-sm text-slate-500">
        <p>© 2025 FlashSell. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>
