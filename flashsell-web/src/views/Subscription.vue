<script setup lang="ts">
/**
 * Subscription Page
 * 
 * Displays subscription plans (Free, Pro, Enterprise) with features and pricing.
 * Allows users to upgrade/downgrade their subscription plan.
 * 
 * Requirements: 10.1, 10.2, 10.3, 10.4, 10.5
 * - 10.1: Display available subscription plans (Free, Pro, Enterprise)
 * - 10.2: Highlight the user's current plan
 * - 10.3: Display plan features and pricing
 * - 10.4: Provide upgrade/downgrade buttons
 * - 10.5: Integrate with /api/subscription/* endpoints
 */
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/composables/useI18n'
import type { SubscriptionPlan, SubscriptionOrder, SubscriptionStatus } from '@/api/subscription'
import * as subscriptionApi from '@/api/subscription'
import GlassCard from '@/components/GlassCard.vue'
import Button from '@/components/Button.vue'
import Badge from '@/components/Badge.vue'
import PageHeader from '@/components/PageHeader.vue'

const userStore = useUserStore()
const toast = useToast()
const { t } = useI18n()

// State
const plans = ref<SubscriptionPlan[]>([])
const currentStatus = ref<SubscriptionStatus | null>(null)
const selectedPlan = ref<SubscriptionPlan | null>(null)
const currentOrder = ref<SubscriptionOrder | null>(null)
const isLoading = ref(true)
const isCreatingOrder = ref(false)
const isPolling = ref(false)
const showPaymentModal = ref(false)

/**
 * Get the display name for a plan level
 */
const getPlanDisplayName = (level: string): string => {
  switch (level) {
    case 'FREE':
      return t('subscription.plans.free.name')
    case 'BASIC':
      return t('subscription.plans.pro.name') // BASIC maps to Pro in UI
    case 'PRO':
      return t('subscription.plans.enterprise.name') // PRO maps to Enterprise in UI
    default:
      return level
  }
}

/**
 * Get the description for a plan level
 */
const getPlanDescription = (level: string): string => {
  switch (level) {
    case 'FREE':
      return t('subscription.plans.free.description')
    case 'BASIC':
      return t('subscription.plans.pro.description')
    case 'PRO':
      return t('subscription.plans.enterprise.description')
    default:
      return ''
  }
}

/**
 * Get the variant for the plan card based on level
 */
const getPlanVariant = (level: string): 'default' | 'primary' | 'secondary' => {
  switch (level) {
    case 'BASIC':
      return 'primary' // Pro plan gets orange glow
    case 'PRO':
      return 'secondary' // Enterprise plan gets blue glow
    default:
      return 'default'
  }
}

/**
 * Get the badge variant for a plan level
 */
const getBadgeVariant = (level: string): 'hot' | 'trending' | 'new' | 'default' => {
  switch (level) {
    case 'BASIC':
      return 'hot' // Pro plan badge
    case 'PRO':
      return 'trending' // Enterprise plan badge
    default:
      return 'default'
  }
}

/**
 * Get the button variant for a plan
 */
const getButtonVariant = (plan: SubscriptionPlan): 'primary' | 'secondary' | 'ghost' => {
  if (isCurrentPlan(plan)) {
    return 'ghost'
  }
  switch (plan.level) {
    case 'BASIC':
      return 'primary'
    case 'PRO':
      return 'secondary'
    default:
      return 'ghost'
  }
}

/**
 * Check if a plan is the user's current plan
 */
const isCurrentPlan = (plan: SubscriptionPlan): boolean => {
  return plan.level === currentStatus.value?.level
}

/**
 * Get the button text for a plan
 */
const getButtonText = (plan: SubscriptionPlan): string => {
  if (isCreatingOrder.value && selectedPlan.value?.id === plan.id) {
    return t('common.loading')
  }
  if (isCurrentPlan(plan)) {
    return t('subscription.current')
  }
  if (plan.price === 0) {
    return t('subscription.plans.free.name')
  }
  
  // Determine if it's an upgrade or downgrade
  const currentLevel = currentStatus.value?.level || 'FREE'
  const levelOrder = ['FREE', 'BASIC', 'PRO']
  const currentIndex = levelOrder.indexOf(currentLevel)
  const planIndex = levelOrder.indexOf(plan.level)
  
  if (planIndex > currentIndex) {
    return t('subscription.upgrade')
  } else if (planIndex < currentIndex) {
    return t('subscription.downgrade')
  }
  
  return t('subscription.upgrade')
}

/**
 * Check if the button should be disabled
 */
const isButtonDisabled = (plan: SubscriptionPlan): boolean => {
  return isCreatingOrder.value || isCurrentPlan(plan)
}

/**
 * Load subscription data from API
 */
async function loadData() {
  isLoading.value = true
  try {
    const [plansRes, statusRes] = await Promise.all([
      subscriptionApi.getPlans(),
      subscriptionApi.getSubscriptionStatus()
    ])
    plans.value = plansRes.data.data
    currentStatus.value = statusRes.data.data

    // Update user subscription info in store
    if (currentStatus.value) {
      userStore.updateUserInfo({
        subscriptionLevel: currentStatus.value.level as 'FREE' | 'BASIC' | 'PRO',
        subscriptionExpireDate: currentStatus.value.expireDate
      })
    }
  } catch (error) {
    console.error('Failed to load subscription data:', error)
    toast.error(t('common.error'))
  } finally {
    isLoading.value = false
  }
}

/**
 * Select a plan and create an order
 */
async function selectPlan(plan: SubscriptionPlan) {
  if (isCurrentPlan(plan)) return
  
  selectedPlan.value = plan
  isCreatingOrder.value = true

  try {
    const { data } = await subscriptionApi.createOrder(plan.id)
    currentOrder.value = data.data

    // If there's a payment URL, show the payment modal
    if (currentOrder.value.paymentUrl) {
      showPaymentModal.value = true
      // Start polling for payment status
      startPolling(currentOrder.value.orderNo)
    }
  } catch (error) {
    console.error('Failed to create order:', error)
    toast.error(t('common.error'))
  } finally {
    isCreatingOrder.value = false
  }
}

/**
 * Navigate to payment page
 */
function goToPayment() {
  if (currentOrder.value?.paymentUrl) {
    window.open(currentOrder.value.paymentUrl, '_blank')
  }
}

/**
 * Start polling for payment status
 */
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
        // Refresh subscription status
        await loadData()
        toast.success(t('common.ok'))
      } else if (order.status === 'CANCELLED' || order.status === 'FAILED') {
        clearInterval(pollInterval)
        isPolling.value = false
        showPaymentModal.value = false
        toast.error(t('common.error'))
      }
    } catch (error) {
      console.error('Failed to check order status:', error)
    }
  }, 3000) // Poll every 3 seconds

  // Stop polling after 5 minutes
  setTimeout(() => {
    clearInterval(pollInterval)
    isPolling.value = false
  }, 5 * 60 * 1000)
}

/**
 * Close the payment modal
 */
function closePaymentModal() {
  showPaymentModal.value = false
  currentOrder.value = null
  selectedPlan.value = null
}

/**
 * Format date for display
 */
function formatDate(dateStr: string | null): string {
  if (!dateStr) return t('common.none')
  return new Date(dateStr).toLocaleDateString()
}

/**
 * Get the current plan display info
 */
const currentPlanInfo = computed(() => {
  if (!currentStatus.value) return null
  return {
    name: getPlanDisplayName(currentStatus.value.level),
    isActive: currentStatus.value.isActive,
    expireDate: currentStatus.value.expireDate
  }
})

// Load data on mount
onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="subscription-page max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <!-- Page Header -->
    <PageHeader
      :title="t('subscription.title')"
      :description="t('subscription.plans.free.description')"
    />

    <!-- Current Subscription Status -->
    <GlassCard 
      v-if="currentPlanInfo && !isLoading" 
      variant="primary"
      class="current-plan-card mb-8 p-6"
    >
      <div class="flex items-center justify-between flex-wrap gap-4">
        <div>
          <p class="text-sm text-orange-400 mb-1">
            {{ t('subscription.currentPlan') }}
          </p>
          <h3 class="text-xl font-bold text-white">
            {{ currentPlanInfo.name }}
          </h3>
          <p class="text-xs text-slate-400 mt-1">
            <span v-if="currentPlanInfo.isActive" class="text-green-400">
              ● {{ t('common.ok') }}
            </span>
            <span v-else class="text-red-400">
              ● {{ t('common.error') }}
            </span>
            <span v-if="currentPlanInfo.expireDate" class="ml-2">
              · {{ formatDate(currentPlanInfo.expireDate) }}
            </span>
          </p>
        </div>
        <Badge 
          :variant="getBadgeVariant(currentStatus?.level || 'FREE')"
          size="md"
        >
          {{ currentStatus?.level }}
        </Badge>
      </div>
    </GlassCard>

    <!-- Subscription Plans Grid -->
    <div v-if="!isLoading" class="plans-grid grid grid-cols-1 md:grid-cols-3 gap-6">
      <GlassCard
        v-for="plan in plans"
        :key="plan.id"
        :variant="getPlanVariant(plan.level)"
        :hover="!isCurrentPlan(plan)"
        class="plan-card p-6 flex flex-col"
        :class="{ 'ring-2 ring-orange-500': isCurrentPlan(plan) }"
        :data-plan-level="plan.level"
        :data-is-current="isCurrentPlan(plan)"
      >
        <!-- Plan Header -->
        <div class="text-center mb-6">
          <Badge 
            :variant="getBadgeVariant(plan.level)"
            size="md"
            class="mb-3"
          >
            {{ getPlanDisplayName(plan.level) }}
          </Badge>
          
          <div class="text-4xl font-bold text-white mt-2">
            <span v-if="plan.price === 0">{{ t('subscription.plans.free.name') }}</span>
            <span v-else>
              ¥{{ plan.price }}
              <span class="text-sm font-normal text-slate-400">{{ t('subscription.perMonth') }}</span>
            </span>
          </div>
          
          <p class="text-slate-400 text-sm mt-2">
            {{ plan.durationDays }} {{ t('common.all') }}
          </p>
        </div>

        <!-- Plan Description -->
        <p class="text-slate-400 text-sm text-center mb-6">
          {{ getPlanDescription(plan.level) }}
        </p>

        <!-- Features List -->
        <ul class="features-list space-y-3 mb-6 flex-grow">
          <!-- Search Limit -->
          <li class="flex items-center text-sm">
            <svg class="w-5 h-5 text-green-500 mr-2 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
            <span class="text-slate-300">{{ plan.searchLimit }} {{ t('search.title') }}</span>
          </li>
          
          <!-- Export Limit -->
          <li class="flex items-center text-sm">
            <svg class="w-5 h-5 text-green-500 mr-2 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
            <span class="text-slate-300">{{ plan.exportLimit }} {{ t('common.more') }}</span>
          </li>
          
          <!-- Board Limit -->
          <li class="flex items-center text-sm">
            <svg class="w-5 h-5 text-green-500 mr-2 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
            <span class="text-slate-300">{{ plan.boardLimit }} {{ t('favorites.title') }}</span>
          </li>
          
          <!-- AI Analysis -->
          <li class="flex items-center text-sm">
            <svg 
              :class="['w-5 h-5 mr-2 flex-shrink-0', plan.aiAnalysisEnabled ? 'text-green-500' : 'text-slate-600']" 
              fill="currentColor" 
              viewBox="0 0 20 20"
            >
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
            <span :class="['text-slate-300', { 'line-through opacity-50': !plan.aiAnalysisEnabled }]">
              {{ t('product.aiAnalysis') }}
            </span>
          </li>
          
          <!-- API Access -->
          <li class="flex items-center text-sm">
            <svg 
              :class="['w-5 h-5 mr-2 flex-shrink-0', plan.apiAccessEnabled ? 'text-green-500' : 'text-slate-600']" 
              fill="currentColor" 
              viewBox="0 0 20 20"
            >
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
            <span :class="['text-slate-300', { 'line-through opacity-50': !plan.apiAccessEnabled }]">
              API {{ t('common.ok') }}
            </span>
          </li>
        </ul>

        <!-- Action Button -->
        <Button
          :variant="getButtonVariant(plan)"
          :disabled="isButtonDisabled(plan)"
          :loading="isCreatingOrder && selectedPlan?.id === plan.id"
          block
          @click="selectPlan(plan)"
        >
          {{ getButtonText(plan) }}
        </Button>
      </GlassCard>
    </div>

    <!-- Loading State -->
    <div v-else class="flex justify-center items-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-500"></div>
    </div>

    <!-- Payment Modal -->
    <Teleport to="body">
      <div 
        v-if="showPaymentModal" 
        class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50"
        @click.self="closePaymentModal"
      >
        <GlassCard class="payment-modal p-6 max-w-md w-full mx-4">
          <h3 class="text-xl font-bold text-white mb-4">
            {{ t('common.confirm') }}
          </h3>
          
          <div class="mb-6">
            <p class="text-slate-400">
              {{ t('subscription.plans.pro.description') }}
              <span class="font-bold text-orange-400">{{ selectedPlan?.name }}</span>
            </p>
            <p class="text-3xl font-bold text-white mt-2">
              ¥{{ selectedPlan?.price }}
            </p>
          </div>
          
          <div class="space-y-3">
            <Button
              variant="primary"
              :disabled="isPolling"
              :loading="isPolling"
              block
              @click="goToPayment"
            >
              {{ isPolling ? t('common.loading') : t('common.confirm') }}
            </Button>
            
            <Button
              variant="ghost"
              :disabled="isPolling"
              block
              @click="closePaymentModal"
            >
              {{ t('common.cancel') }}
            </Button>
          </div>
          
          <p class="text-xs text-slate-500 mt-4 text-center">
            {{ t('common.noData') }}
          </p>
        </GlassCard>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.subscription-page {
  min-height: calc(100vh - 200px);
}

.current-plan-card {
  background: linear-gradient(135deg, rgba(249, 115, 22, 0.1) 0%, rgba(249, 115, 22, 0.05) 100%);
  border: 1px solid rgba(249, 115, 22, 0.3);
}

.plan-card {
  transition: all var(--transition-normal);
}

.plan-card:hover:not([data-is-current="true"]) {
  transform: translateY(-4px);
}

.features-list li {
  transition: opacity var(--transition-fast);
}

.payment-modal {
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .plans-grid {
    gap: 1rem;
  }
  
  .plan-card {
    padding: 1.25rem;
  }
}
</style>
