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
import PageHeader from '@/components/PageHeader.vue'
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

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="profile-page">
    <!-- Page Header -->
    <PageHeader :title="t('profile.title')" />

    <!-- Loading State -->
    <div v-if="isLoading" class="flex justify-center items-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-[var(--primary)]"></div>
    </div>

    <!-- Main Content -->
    <div v-else class="space-y-6">
      <!-- User Info Card -->
      <GlassCard class="p-6">
        <div class="flex items-start justify-between mb-6">
          <h2 class="text-xl font-semibold text-[var(--text-primary)]">
            {{ t('profile.avatar') }}
          </h2>
          <Button
            v-if="!isEditing"
            variant="primary"
            size="sm"
            @click="startEdit"
          >
            {{ t('common.edit') }}
          </Button>
          <div v-else class="flex gap-2">
            <Button
              variant="ghost"
              size="sm"
              :disabled="isSaving"
              @click="cancelEdit"
            >
              {{ t('common.cancel') }}
            </Button>
            <Button
              variant="primary"
              size="sm"
              :loading="isSaving"
              @click="saveProfile"
            >
              {{ t('common.save') }}
            </Button>
          </div>
        </div>

        <!-- Avatar and Basic Info -->
        <div class="flex items-center gap-6 mb-6">
          <!-- Avatar -->
          <div
            class="w-20 h-20 rounded-full bg-gradient-to-br from-[var(--primary)] to-pink-600 flex items-center justify-center text-white text-2xl font-bold shadow-lg"
            data-testid="profile-avatar"
          >
            {{ avatarInitial }}
          </div>

          <!-- Name and Phone -->
          <div class="flex-1">
            <div v-if="!isEditing">
              <h3
                class="text-xl font-semibold text-[var(--text-primary)] mb-1"
                data-testid="profile-name"
              >
                {{ displayName }}
              </h3>
              <p
                class="text-[var(--text-secondary)]"
                data-testid="profile-phone"
              >
                {{ profile?.phone }}
              </p>
            </div>
            <div v-else class="max-w-xs">
              <label class="block text-sm font-medium text-[var(--text-secondary)] mb-1">
                {{ t('profile.nickname') }}
              </label>
              <input
                v-model="editForm.nickname"
                type="text"
                maxlength="20"
                class="w-full px-3 py-2 rounded-lg bg-[var(--bg-card)] border border-[var(--border)] text-[var(--text-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--primary)] focus:border-transparent"
                :placeholder="t('profile.nickname')"
                data-testid="profile-nickname-input"
              />
              <p class="text-xs text-[var(--text-muted)] mt-1">
                {{ editForm.nickname.length }}/20
              </p>
            </div>
          </div>

          <!-- Subscription Badge -->
          <Badge
            :variant="subscriptionBadgeVariant"
            size="md"
            data-testid="profile-subscription-badge"
          >
            {{ subscriptionLabel }}
          </Badge>
        </div>

        <!-- User Details Grid -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 border-t border-[var(--border)]">
          <div>
            <p class="text-sm text-[var(--text-muted)]">{{ t('profile.phone') }}</p>
            <p class="text-[var(--text-primary)]">{{ profile?.phone || '-' }}</p>
          </div>
          <div>
            <p class="text-sm text-[var(--text-muted)]">{{ t('profile.email') }}</p>
            <p class="text-[var(--text-primary)]">{{ profile?.email || '-' }}</p>
          </div>
          <div>
            <p class="text-sm text-[var(--text-muted)]">{{ t('profile.memberSince') }}</p>
            <p class="text-[var(--text-primary)]">{{ formatDate(profile?.createdAt) }}</p>
          </div>
          <div>
            <p class="text-sm text-[var(--text-muted)]">{{ t('subscription.currentPlan') }}</p>
            <p class="text-[var(--text-primary)]">{{ subscriptionLabel }}</p>
          </div>
        </div>
      </GlassCard>

      <!-- Account Statistics Card -->
      <GlassCard class="p-6" data-testid="profile-statistics">
        <h2 class="text-xl font-semibold text-[var(--text-primary)] mb-6">
          {{ t('profile.statistics') }}
        </h2>

        <div v-if="usageStats" class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- Search Count -->
          <div>
            <div class="flex justify-between items-center mb-2">
              <span class="text-sm text-[var(--text-secondary)]">
                {{ t('profile.totalSearches') }}
              </span>
              <span class="text-sm font-medium text-[var(--text-primary)]">
                {{ usageStats.searchCount }} / {{ usageStats.searchLimit }}
              </span>
            </div>
            <div class="w-full bg-[var(--bg-card-hover)] rounded-full h-2">
              <div
                class="h-2 rounded-full transition-all duration-300"
                :class="getProgressColor(usagePercentage.search)"
                :style="{ width: `${usagePercentage.search}%` }"
              ></div>
            </div>
          </div>

          <!-- Export Count -->
          <div>
            <div class="flex justify-between items-center mb-2">
              <span class="text-sm text-[var(--text-secondary)]">
                {{ t('profile.exports', '导出次数') }}
              </span>
              <span class="text-sm font-medium text-[var(--text-primary)]">
                {{ usageStats.exportCount }} / {{ usageStats.exportLimit }}
              </span>
            </div>
            <div class="w-full bg-[var(--bg-card-hover)] rounded-full h-2">
              <div
                class="h-2 rounded-full transition-all duration-300"
                :class="getProgressColor(usagePercentage.export)"
                :style="{ width: `${usagePercentage.export}%` }"
              ></div>
            </div>
          </div>

          <!-- Favorites Count -->
          <div>
            <div class="flex justify-between items-center mb-2">
              <span class="text-sm text-[var(--text-secondary)]">
                {{ t('profile.totalFavorites') }}
              </span>
              <span class="text-sm font-medium text-[var(--text-primary)]">
                {{ usageStats.favoriteCount }} / {{ usageStats.favoriteLimit }}
              </span>
            </div>
            <div class="w-full bg-[var(--bg-card-hover)] rounded-full h-2">
              <div
                class="h-2 rounded-full transition-all duration-300"
                :class="getProgressColor(usagePercentage.favorite)"
                :style="{ width: `${usagePercentage.favorite}%` }"
              ></div>
            </div>
          </div>

          <!-- Board Count -->
          <div>
            <div class="flex justify-between items-center mb-2">
              <span class="text-sm text-[var(--text-secondary)]">
                {{ t('profile.boards', '看板数') }}
              </span>
              <span class="text-sm font-medium text-[var(--text-primary)]">
                {{ usageStats.boardCount }} / {{ usageStats.boardLimit }}
              </span>
            </div>
            <div class="w-full bg-[var(--bg-card-hover)] rounded-full h-2">
              <div
                class="h-2 rounded-full transition-all duration-300"
                :class="getProgressColor(usagePercentage.board)"
                :style="{ width: `${usagePercentage.board}%` }"
              ></div>
            </div>
          </div>
        </div>

        <!-- Empty state for stats -->
        <div v-else class="text-center py-8 text-[var(--text-muted)]">
          {{ t('common.noData') }}
        </div>
      </GlassCard>

      <!-- Logout Section -->
      <GlassCard class="p-6">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-lg font-medium text-[var(--text-primary)]">
              {{ t('nav.logout') }}
            </h3>
            <p class="text-sm text-[var(--text-muted)]">
              {{ t('profile.logoutHint', '退出当前账户') }}
            </p>
          </div>
          <Button
            variant="ghost"
            @click="handleLogout"
            data-testid="profile-logout-button"
          >
            <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
            </svg>
            {{ t('nav.logout') }}
          </Button>
        </div>
      </GlassCard>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  min-height: calc(100vh - 200px);
}
</style>
