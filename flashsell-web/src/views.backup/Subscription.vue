<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'
import type { SubscriptionPlan, SubscriptionOrder, SubscriptionStatus } from '@/api/subscription'
import * as subscriptionApi from '@/api/subscription'
import PageHeader from '@/components/PageHeader.vue'

const userStore = useUserStore()
const toast = useToast()

// 状态
const plans = ref<SubscriptionPlan[]>([])
const currentStatus = ref<SubscriptionStatus | null>(null)
const selectedPlan = ref<SubscriptionPlan | null>(null)
const currentOrder = ref<SubscriptionOrder | null>(null)
const isLoading = ref(true)
const isCreatingOrder = ref(false)
const isPolling = ref(false)
const showPaymentModal = ref(false)

// 获取套餐对应的样式类
const getPlanCardClass = (plan: SubscriptionPlan) => {
  if (plan.level === 'PRO') return 'border-purple-500 shadow-purple-200'
  if (plan.level === 'BASIC') return 'border-blue-500 shadow-blue-200'
  return 'border-gray-300'
}

const getPlanBadgeClass = (plan: SubscriptionPlan) => {
  if (plan.level === 'PRO') return 'bg-purple-100 text-purple-800'
  if (plan.level === 'BASIC') return 'bg-blue-100 text-blue-800'
  return 'bg-gray-100 text-gray-800'
}

const getButtonClass = (plan: SubscriptionPlan) => {
  if (plan.level === 'PRO') return 'bg-purple-600 hover:bg-purple-700'
  if (plan.level === 'BASIC') return 'bg-blue-600 hover:bg-blue-700'
  return 'bg-gray-600 hover:bg-gray-700'
}

// 是否已是当前套餐
const isCurrentPlan = (plan: SubscriptionPlan) => {
  return plan.level === currentStatus.value?.level
}

// 加载数据
async function loadData() {
  isLoading.value = true
  try {
    const [plansRes, statusRes] = await Promise.all([
      subscriptionApi.getPlans(),
      subscriptionApi.getSubscriptionStatus()
    ])
    plans.value = plansRes.data.data
    currentStatus.value = statusRes.data.data

    // 更新用户订阅信息
    if (currentStatus.value) {
      userStore.updateUserInfo({
        subscriptionLevel: currentStatus.value.level as 'FREE' | 'BASIC' | 'PRO',
        subscriptionExpireDate: currentStatus.value.expireDate
      })
    }
  } catch (error) {
    console.error('加载订阅数据失败:', error)
  } finally {
    isLoading.value = false
  }
}

// 选择套餐并创建订单
async function selectPlan(plan: SubscriptionPlan) {
  selectedPlan.value = plan
  isCreatingOrder.value = true

  try {
    const { data } = await subscriptionApi.createOrder(plan.id)
    currentOrder.value = data.data

    // 如果有支付URL，跳转到支付宝
    if (currentOrder.value.paymentUrl) {
      showPaymentModal.value = true
      // 开始轮询支付状态
      startPolling(currentOrder.value.orderNo)
    }
  } catch (error) {
    console.error('创建订单失败:', error)
    toast.error('创建订单失败，请稍后重试')
  } finally {
    isCreatingOrder.value = false
  }
}

// 跳转支付
function goToPayment() {
  if (currentOrder.value?.paymentUrl) {
    window.open(currentOrder.value.paymentUrl, '_blank')
  }
}

// 开始轮询支付状态
function startPolling(orderNo: string) {
  isPolling.value = true

  const pollInterval = setInterval(async () => {
    try {
      const { data } = await subscriptionApi.getOrderStatus(orderNo)
      const order = data.data

      if (order.status === 'PAID') {
        clearInterval(pollInterval)
        isPolling.value = false
        showPaymentModal.value = false
        // 刷新订阅状态
        await loadData()
        toast.success('支付成功！')
      } else if (order.status === 'CANCELLED' || order.status === 'FAILED') {
        clearInterval(pollInterval)
        isPolling.value = false
        showPaymentModal.value = false
        toast.error('支付失败或已取消')
      }
    } catch (error) {
      console.error('查询订单状态失败:', error)
    }
  }, 3000) // 每3秒轮询一次

  // 5分钟后停止轮询
  setTimeout(() => {
    clearInterval(pollInterval)
    isPolling.value = false
  }, 5 * 60 * 1000)
}

// 关闭支付弹窗
function closePaymentModal() {
  showPaymentModal.value = false
  currentOrder.value = null
  selectedPlan.value = null
}

// 格式化日期
function formatDate(dateStr: string | null) {
  if (!dateStr) return '永久有效'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <!-- 页面标题 -->
    <PageHeader
      title="订阅套餐"
      description="选择适合您的套餐，解锁更多功能"
    />

    <!-- 当前订阅状态 -->
    <div v-if="currentStatus && !isLoading" class="mb-8 bg-orange-500/10 border border-orange-500/30 rounded-lg p-4">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-orange-400">
            当前订阅：{{ currentStatus.level === 'FREE' ? '免费版' : currentStatus.level === 'BASIC' ? '基础版' : '专业版' }}
          </p>
          <p class="text-xs text-orange-300 mt-1">
            {{ currentStatus.isActive ? '订阅生效中' : '订阅已过期' }}
            <span v-if="currentStatus.expireDate" class="ml-2">
              · 到期时间：{{ formatDate(currentStatus.expireDate) }}
            </span>
          </p>
        </div>
        <div class="text-right">
          <p class="text-2xl font-bold text-orange-400">
            {{ currentStatus.level }}
          </p>
        </div>
      </div>
    </div>

    <!-- 套餐列表 -->
    <div v-if="!isLoading" class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div
        v-for="plan in plans"
        :key="plan.id"
        :class="[
          'glass-card p-6 border-2 transition-all hover:shadow-lg cursor-pointer',
          getPlanCardClass(plan)
        ]"
      >
        <!-- 套餐标题 -->
        <div class="text-center mb-6">
          <span :class="['inline-block px-3 py-1 rounded-full text-sm font-medium mb-3', getPlanBadgeClass(plan)]">
            {{ plan.level === 'FREE' ? '免费版' : plan.level === 'BASIC' ? '基础版' : '专业版' }}
          </span>
          <div class="text-4xl font-bold text-white">
            ¥{{ plan.price }}
          </div>
          <p class="text-slate-400 text-sm mt-1">
            {{ plan.durationDays }}天有效期
          </p>
        </div>

        <!-- 套餐描述 -->
        <p class="text-slate-400 text-sm text-center mb-6">
          {{ plan.description }}
        </p>

        <!-- 功能列表 -->
        <ul class="space-y-3 mb-6">
          <li class="flex items-center text-sm">
            <svg class="w-5 h-5 text-green-500 mr-2" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
            <span class="text-slate-300">{{ plan.searchLimit }} 次/天 搜索</span>
          </li>
          <li class="flex items-center text-sm">
            <svg class="w-5 h-5 text-green-500 mr-2" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
            <span class="text-slate-300">{{ plan.exportLimit }} 次/天 导出</span>
          </li>
          <li class="flex items-center text-sm">
            <svg class="w-5 h-5 text-green-500 mr-2" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
            <span class="text-slate-300">{{ plan.boardLimit }} 个 选品板</span>
          </li>
          <li class="flex items-center text-sm">
            <svg :class="['w-5 h-5 mr-2', plan.aiAnalysisEnabled ? 'text-green-500' : 'text-slate-600']" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
            <span :class="['text-slate-300', { 'line-through opacity-50': !plan.aiAnalysisEnabled }]">
              AI 智能分析
            </span>
          </li>
          <li class="flex items-center text-sm">
            <svg :class="['w-5 h-5 mr-2', plan.apiAccessEnabled ? 'text-green-500' : 'text-slate-600']" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
            <span :class="['text-slate-300', { 'line-through opacity-50': !plan.apiAccessEnabled }]">
              API 接口访问
            </span>
          </li>
        </ul>

        <!-- 购买按钮 -->
        <button
          :disabled="isCreatingOrder || (plan.price === 0 && isCurrentPlan(plan))"
          :class="[
            'w-full py-3 px-4 rounded-lg font-medium transition-colors',
            getButtonClass(plan),
            { 'opacity-50 cursor-not-allowed': isCreatingOrder || (plan.price === 0 && isCurrentPlan(plan)) }
          ]"
          @click="selectPlan(plan)"
        >
          <span v-if="isCreatingOrder && selectedPlan?.id === plan.id">
            处理中...
          </span>
          <span v-else-if="plan.price === 0">
            {{ isCurrentPlan(plan) ? '当前套餐' : '免费使用' }}
          </span>
          <span v-else>
            {{ isCurrentPlan(plan) ? '续费' : '立即购买' }}
          </span>
        </button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else class="flex justify-center items-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-500"></div>
    </div>

    <!-- 支付弹窗 -->
    <div v-if="showPaymentModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="glass-card p-6 max-w-md w-full mx-4">
        <h3 class="text-xl font-bold text-white mb-4">
          确认支付
        </h3>
        <div class="mb-4">
          <p class="text-slate-400">
            您选择了 <span class="font-bold text-orange-400">{{ selectedPlan?.name }}</span>
          </p>
          <p class="text-2xl font-bold text-white mt-2">
            ¥{{ selectedPlan?.price }}
          </p>
        </div>
        <div class="space-y-3">
          <button
            @click="goToPayment"
            :disabled="isPolling"
            class="w-full btn-gradient-primary font-medium py-3 px-4 rounded-lg disabled:opacity-50"
          >
            <span v-if="isPolling">等待支付中...</span>
            <span v-else>前往支付宝支付</span>
          </button>
          <button
            @click="closePaymentModal"
            :disabled="isPolling"
            class="w-full bg-slate-700 hover:bg-slate-600 text-slate-200 font-medium py-3 px-4 rounded-lg disabled:opacity-50"
          >
            取消
          </button>
        </div>
        <p class="text-xs text-slate-500 mt-4 text-center">
          支付完成后系统将自动刷新，如未刷新请手动刷新页面
        </p>
      </div>
    </div>
  </div>
</template>
