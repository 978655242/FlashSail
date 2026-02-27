<script setup lang="ts">
/**
 * Profile Page Component
 *
 * Displays user profile information, account statistics, and provides
 * functionality for editing nickname and logging out.
 *
 * Requirements: 11.1, 11.2, 11.3, 11.4, 11.5
 * - 11.1: Display user information (name, phone, avatar)
 * - 11.2: Allow editing user nickname
 * - 11.3: Display account statistics
 * - 11.4: Provide a logout button
 * - 11.5: Integrate with /api/user/* endpoints
 */
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import * as userApi from '@/api/user'
import type { UserProfile, UserUsageStats } from '@/api/user'
import GlassCard from '@/components/GlassCard.vue'
import Button from '@/components/Button.vue'
import Badge from '@/components/Badge.vue'
import { useI18n } from '@/composables/useI18n'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const userStore = useUserStore()
const { t } = useI18n()
const toast = useToast()

// State
const profile = ref<UserProfile | null>(null)
const usageStats = ref<UserUsageStats | null>(null)
const isLoading = ref(true)
const isEditing = ref(false)
const isSaving = ref(false)

// Edit form
const editForm = ref({
  nickname: ''
})

// Computed properties
const displayName = computed(() => {
  return profile.value?.nickname || profile.value?.phone || t('profile.nickname')
})

const avatarInitial = computed(() => {
  if (profile.value?.nickname) {
    return profile.value.nickname.charAt(0).toUpperCase()
  }
  if (profile.value?.phone) {
    return profile.value.phone.charAt(0)
  }
  return 'U'
})

const subscriptionBadgeVariant = computed(() => {
  const level = profile.value?.subscriptionLevel
  if (level === 'PRO') return 'hot'
  if (level === 'BASIC') return 'trending'
  return 'new'
})

const subscriptionLabel = computed(() => {
  const level = profile.value?.subscriptionLevel
  if (level === 'PRO') return t('subscription.plans.enterprise.name')
  if (level === 'BASIC') return t('subscription.plans.pro.name')
  return t('subscription.plans.free.name')
})

const usagePercentage = computed(() => {
  if (!usageStats.value) return {
    search: 0,
    export: 0,
    favorite: 0,
    board: 0
  }
  return {
    search: Math.min((usageStats.value.searchCount / usageStats.value.searchLimit) * 100, 100),
    export: Math.min((usageStats.value.exportCount / usageStats.value.exportLimit) * 100, 100),
    favorite: Math.min((usageStats.value.favoriteCount / usageStats.value.favoriteLimit) * 100, 100),
    board: Math.min((usageStats.value.boardCount / usageStats.value.boardLimit) * 100, 100)
  }
})

// Get progress bar color based on percentage
function getProgressColor(percentage: number): string {
  if (percentage >= 90) return 'bg-red-500'
  if (percentage >= 70) return 'bg-yellow-500'
  return 'bg-green-500'
}

// Format date for display
function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString()
}

// Load user data
async function loadData() {
  isLoading.value = true
  try {
    const [profileRes, statsRes] = await Promise.all([
      userApi.getProfile(),
      userApi.getUsage()
    ])

    profile.value = profileRes.data.data
    usageStats.value = statsRes.data.data

    // Initialize edit form
    editForm.value = {
      nickname: profile.value?.nickname || ''
    }
  } catch (error) {
    console.error('Failed to load user data:', error)
    toast.error(t('common.error'))
  } finally {
    isLoading.value = false
  }
}

// Start editing nickname
function startEdit() {
  isEditing.value = true
  editForm.value.nickname = profile.value?.nickname || ''
}

// Cancel editing
function cancelEdit() {
  isEditing.value = false
  editForm.value.nickname = profile.value?.nickname || ''
}

// Save profile changes
async function saveProfile() {
  if (!editForm.value.nickname.trim()) {
    toast.error(t('validation.required'))
    return
  }

  if (editForm.value.nickname.length > 20) {
    toast.error(t('validation.nicknameTooLong'))
    return
  }

  isSaving.value = true
  try {
    const { data } = await userApi.updateProfile({
      nickname: editForm.value.nickname.trim()
    })
    profile.value = data.data
    isEditing.value = false

    // Update user store
    userStore.updateUserInfo({
      nickname: data.data.nickname
    })

    toast.success(t('common.save') + ' ✓')
  } catch (error) {
    console.error('Failed to update profile:', error)
    toast.error(t('common.error'))
  } finally {
    isSaving.value = false
  }
}

// Handle logout
async function handleLogout() {
  try {
    await userStore.logoutAction()
    router.push('/login')
  } catch (error) {
    console.error('Logout failed:', error)
    // Still redirect even if API call fails
    router.push('/login')
  }
}

// Quick actions
function handleExportReport() {
  toast.info('导出数据报告功能即将上线')
}

function handleInviteFriends() {
  toast.info('邀请好友功能即将上线')
}

function handleHelpCenter() {
  toast.info('帮助中心即将上线')
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="profile-page">
    <!-- Page Header -->
    <div class="flex items-center justify-between mb-8">
      <div>
        <h2 class="text-2xl font-bold text-white">{{ t('profile.title') }}</h2>
        <p class="text-slate-400 mt-1">{{ t('profile.subtitle', '管理你的账户信息和偏好设置') }}</p>
      </div>
      <div class="flex items-center gap-4">
        <span class="text-sm text-slate-500">
          {{ t('profile.lastLogin', '最后登录') }}: {{ formatDate(profile?.lastLoginTime) }}
        </span>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="flex justify-center items-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-[var(--primary)]"></div>
    </div>

    <!-- Main Content - Two Column Layout -->
    <div v-else class="grid grid-cols-12 gap-6">
      <!-- Left Column - Profile Card & Quick Actions (4 cols) -->
      <div class="col-span-12 lg:col-span-4 xl:col-span-3 space-y-6">
        <!-- Profile Card -->
        <GlassCard class="p-6 text-center">
          <!-- Avatar -->
          <div class="relative inline-block mb-4">
            <div
              class="w-24 h-24 rounded-full bg-gradient-to-br from-orange-500 to-blue-600 flex items-center justify-center text-3xl font-bold text-white"
              data-testid="profile-avatar"
            >
              {{ avatarInitial }}
            </div>
            <div class="absolute -bottom-1 -right-1 w-8 h-8 bg-green-500 rounded-full border-4 border-slate-900 flex items-center justify-center">
              <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
            </div>
          </div>

          <!-- Name -->
          <div v-if="!isEditing">
            <h3 class="text-xl font-semibold text-white" data-testid="profile-name">
              {{ displayName }}
            </h3>
            <p class="text-slate-400 text-sm mt-1" data-testid="profile-phone">
              {{ profile?.phone }}
            </p>
          </div>
          <div v-else class="max-w-xs mx-auto">
            <input
              v-model="editForm.nickname"
              type="text"
              maxlength="20"
              class="w-full px-3 py-2 rounded-lg bg-slate-800 border border-slate-600 text-white text-center focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent"
              :placeholder="t('profile.nickname')"
              data-testid="profile-nickname-input"
            />
            <p class="text-xs text-slate-500 mt-1">
              {{ editForm.nickname.length }}/20
            </p>
          </div>

          <!-- Subscription Badge -->
          <div class="flex items-center justify-center gap-2 mt-4">
            <Badge
              :variant="subscriptionBadgeVariant"
              size="md"
              data-testid="profile-subscription-badge"
            >
              {{ subscriptionLabel }}
            </Badge>
          </div>

          <!-- Member Expiry -->
          <div class="mt-4 pt-4 border-t border-slate-700">
            <div class="flex items-center justify-between text-sm">
              <span class="text-slate-500">{{ t('profile.memberExpiry', '会员到期') }}</span>
              <span class="text-white font-medium">{{ formatDate(profile?.subscriptionExpireDate) }}</span>
            </div>
            <div class="mt-2 h-1.5 bg-slate-700 rounded-full overflow-hidden">
              <div class="h-full bg-gradient-to-r from-orange-500 to-blue-500 rounded-full" style="width: 75%"></div>
            </div>
            <div class="mt-1 text-xs text-slate-500">{{ t('profile.daysRemaining', '剩余 28 天') }}</div>
          </div>

          <!-- Edit/Save Buttons -->
          <div class="mt-4 flex gap-2 justify-center">
            <template v-if="!isEditing">
              <Button variant="primary" size="sm" @click="startEdit">
                {{ t('common.edit') }}
              </Button>
              <Button variant="secondary" size="sm" @click="$router.push('/subscription')">
                {{ t('profile.renew', '续费升级') }}
              </Button>
            </template>
            <template v-else>
              <Button variant="ghost" size="sm" :disabled="isSaving" @click="cancelEdit">
                {{ t('common.cancel') }}
              </Button>
              <Button variant="primary" size="sm" :loading="isSaving" @click="saveProfile">
                {{ t('common.save') }}
              </Button>
            </template>
          </div>
        </GlassCard>

        <!-- Quick Actions -->
        <GlassCard class="p-6">
          <h3 class="text-lg font-semibold text-white mb-4">{{ t('profile.quickActions', '快捷操作') }}</h3>
          <div class="space-y-3">
            <button
              class="w-full flex items-center gap-3 p-3 rounded-xl bg-slate-800/50 hover:bg-slate-700/50 transition-colors text-left"
              @click="handleExportReport"
            >
              <div class="w-10 h-10 rounded-lg bg-blue-500/20 flex items-center justify-center">
                <svg class="w-5 h-5 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </div>
              <div class="flex-1">
                <div class="font-medium text-white text-sm">{{ t('profile.exportReport', '导出数据报告') }}</div>
                <div class="text-xs text-slate-500">{{ t('profile.exportReportHint', '下载你的分析报告') }}</div>
              </div>
              <svg class="w-4 h-4 text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </button>

            <button
              class="w-full flex items-center gap-3 p-3 rounded-xl bg-slate-800/50 hover:bg-slate-700/50 transition-colors text-left"
              @click="handleInviteFriends"
            >
              <div class="w-10 h-10 rounded-lg bg-green-500/20 flex items-center justify-center">
                <svg class="w-5 h-5 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
              </div>
              <div class="flex-1">
                <div class="font-medium text-white text-sm">{{ t('profile.inviteFriends', '邀请好友') }}</div>
                <div class="text-xs text-slate-500">{{ t('profile.inviteFriendsHint', '获得免费使用时长') }}</div>
              </div>
              <svg class="w-4 h-4 text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </button>

            <button
              class="w-full flex items-center gap-3 p-3 rounded-xl bg-slate-800/50 hover:bg-slate-700/50 transition-colors text-left"
              @click="handleHelpCenter"
            >
              <div class="w-10 h-10 rounded-lg bg-purple-500/20 flex items-center justify-center">
                <svg class="w-5 h-5 text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div class="flex-1">
                <div class="font-medium text-white text-sm">{{ t('profile.helpCenter', '帮助中心') }}</div>
                <div class="text-xs text-slate-500">{{ t('profile.helpCenterHint', '查看使用教程和FAQ') }}</div>
              </div>
              <svg class="w-4 h-4 text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </button>
          </div>
        </GlassCard>
      </div>

      <!-- Right Column - Main Content (8 cols) -->
      <div class="col-span-12 lg:col-span-8 xl:col-span-9 space-y-6">
        <!-- Usage Statistics -->
        <GlassCard class="p-6" data-testid="profile-statistics">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-lg font-semibold text-white">{{ t('profile.statistics') }}</h3>
            <span class="text-sm text-slate-500">{{ new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long' }) }}</span>
          </div>

          <div v-if="usageStats" class="grid grid-cols-3 gap-6">
            <!-- Search Count -->
            <div class="stat-card">
              <div class="stat-value">{{ usageStats.searchCount }}</div>
              <div class="stat-label">{{ t('profile.totalSearches') }}</div>
              <div class="mt-3 h-1.5 bg-slate-700 rounded-full overflow-hidden">
                <div
                  class="h-full rounded-full transition-all duration-300"
                  :class="getProgressColor(usagePercentage.search)"
                  :style="{ width: `${usagePercentage.search}%` }"
                ></div>
              </div>
              <div class="mt-1 text-xs text-slate-500">
                {{ t('profile.limit', '限额') }}: {{ usageStats.searchLimit }}
              </div>
            </div>

            <!-- Export Count -->
            <div class="stat-card">
              <div class="stat-value">{{ usageStats.exportCount }}</div>
              <div class="stat-label">{{ t('profile.exports', '导出次数') }}</div>
              <div class="mt-3 h-1.5 bg-slate-700 rounded-full overflow-hidden">
                <div
                  class="h-full rounded-full transition-all duration-300"
                  :class="getProgressColor(usagePercentage.export)"
                  :style="{ width: `${usagePercentage.export}%` }"
                ></div>
              </div>
              <div class="mt-1 text-xs text-slate-500">
                {{ t('profile.limit', '限额') }}: {{ usageStats.exportLimit }}
              </div>
            </div>

            <!-- Favorites Count -->
            <div class="stat-card">
              <div class="stat-value">{{ usageStats.favoriteCount }}</div>
              <div class="stat-label">{{ t('profile.totalFavorites') }}</div>
              <div class="mt-3 h-1.5 bg-slate-700 rounded-full overflow-hidden">
                <div
                  class="h-full rounded-full transition-all duration-300"
                  :class="getProgressColor(usagePercentage.favorite)"
                  :style="{ width: `${usagePercentage.favorite}%` }"
                ></div>
              </div>
              <div class="mt-1 text-xs text-slate-500">
                {{ t('profile.limit', '限额') }}: {{ usageStats.favoriteLimit }}
              </div>
            </div>
          </div>

          <!-- Empty state for stats -->
          <div v-else class="text-center py-8 text-slate-500">
            {{ t('common.noData') }}
          </div>
        </GlassCard>

        <!-- Account Settings -->
        <GlassCard class="p-6">
          <h3 class="text-lg font-semibold text-white mb-6">{{ t('profile.accountSettings', '账户设置') }}</h3>

          <div class="space-y-4">
            <div class="flex items-center justify-between p-4 rounded-xl bg-slate-800/30">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-lg bg-slate-700 flex items-center justify-center">
                  <svg class="w-5 h-5 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129" />
                  </svg>
                </div>
                <div>
                  <div class="font-medium text-white">{{ t('profile.phone') }}</div>
                  <div class="text-sm text-slate-400">{{ profile?.phone || '-' }}</div>
                </div>
              </div>
              <Button variant="ghost" size="sm">{{ t('common.modify', '修改') }}</Button>
            </div>

            <div class="flex items-center justify-between p-4 rounded-xl bg-slate-800/30">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-lg bg-slate-700 flex items-center justify-center">
                  <svg class="w-5 h-5 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                  </svg>
                </div>
                <div>
                  <div class="font-medium text-white">{{ t('profile.email') }}</div>
                  <div class="text-sm text-slate-400">{{ profile?.email || '-' }}</div>
                </div>
              </div>
              <Button variant="ghost" size="sm">{{ t('common.modify', '修改') }}</Button>
            </div>

            <div class="flex items-center justify-between p-4 rounded-xl bg-slate-800/30">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-lg bg-slate-700 flex items-center justify-center">
                  <svg class="w-5 h-5 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
                <div>
                  <div class="font-medium text-white">{{ t('profile.memberSince') }}</div>
                  <div class="text-sm text-slate-400">{{ formatDate(profile?.createdAt) }}</div>
                </div>
              </div>
            </div>
          </div>
        </GlassCard>

        <!-- Logout Section -->
        <GlassCard class="p-6">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-lg font-medium text-white">{{ t('nav.logout') }}</h3>
              <p class="text-sm text-slate-500">{{ t('profile.logoutHint', '退出当前账户') }}</p>
            </div>
            <Button variant="ghost" @click="handleLogout" data-testid="profile-logout-button">
              <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
              {{ t('nav.logout') }}
            </Button>
          </div>
        </GlassCard>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  min-height: calc(100vh - 200px);
}

/* Stat card styles */
.stat-card {
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.8) 0%, rgba(15, 23, 42, 0.8) 100%);
  border: 1px solid rgba(51, 65, 85, 0.5);
  border-radius: 12px;
  padding: 20px;
  text-align: center;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 0.875rem;
  margin-top: 4px;
}
</style>
