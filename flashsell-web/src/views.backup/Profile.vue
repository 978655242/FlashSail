<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import * as userApi from '@/api/user'
import type { UserProfile, UserUsageStats } from '@/api/user'
import PageHeader from '@/components/PageHeader.vue'
import { useToast } from '@/composables/useToast'

const userStore = useUserStore()
const toast = useToast()

// 状态
const profile = ref<UserProfile | null>(null)
const usageStats = ref<UserUsageStats | null>(null)
const inviteInfo = ref<{ inviteCode: string; inviteUrl: string } | null>(null)
const isLoading = ref(true)
const isEditing = ref(false)
const activeTab = ref<'profile' | 'settings' | 'security'>('profile')

// 编辑表单
const editForm = ref({
  nickname: '',
  email: ''
})

// 保存中状态
const isSaving = ref(false)

// 计算属性
const usagePercentage = computed(() => {
  if (!usageStats.value) return {
    search: 0,
    export: 0,
    favorite: 0,
    board: 0
  }
  return {
    search: usageStats.value.searchCount / usageStats.value.searchLimit * 100,
    export: usageStats.value.exportCount / usageStats.value.exportLimit * 100,
    favorite: usageStats.value.favoriteCount / usageStats.value.favoriteLimit * 100,
    board: usageStats.value.boardCount / usageStats.value.boardLimit * 100
  }
})

// 获取使用进度条颜色
const getProgressColor = (percentage: number) => {
  if (percentage >= 90) return 'bg-red-500'
  if (percentage >= 70) return 'bg-yellow-500'
  return 'bg-green-500'
}

// 加载用户数据
async function loadData() {
  isLoading.value = true
  try {
    const [profileRes, statsRes, inviteRes] = await Promise.all([
      userApi.getProfile(),
      userApi.getUsage(),
      userApi.getInviteCode()
    ])

    profile.value = profileRes.data.data
    usageStats.value = statsRes.data.data
    inviteInfo.value = inviteRes.data.data

    // 初始化编辑表单
    editForm.value = {
      nickname: profile.value.nickname || '',
      email: profile.value.email || ''
    }
  } catch (error) {
    console.error('加载用户数据失败:', error)
  } finally {
    isLoading.value = false
  }
}

// 开始编辑
function startEdit() {
  isEditing.value = true
  editForm.value = {
    nickname: profile.value?.nickname || '',
    email: profile.value?.email || ''
  }
}

// 取消编辑
function cancelEdit() {
  isEditing.value = false
  editForm.value = {
    nickname: profile.value?.nickname || '',
    email: profile.value?.email || ''
  }
}

// 保存资料
async function saveProfile() {
  isSaving.value = true
  try {
    const { data } = await userApi.updateProfile(editForm.value)
    profile.value = data.data
    isEditing.value = false

    // 更新用户store中的信息
    userStore.updateUserInfo({
      nickname: data.data.nickname,
      email: data.data.email
    })

    toast.success('资料更新成功')
  } catch (error) {
    console.error('更新资料失败:', error)
    toast.error('更新失败，请稍后重试')
  } finally {
    isSaving.value = false
  }
}

// 复制邀请链接
function copyInviteLink() {
  if (inviteInfo.value?.inviteUrl) {
    navigator.clipboard.writeText(inviteInfo.value.inviteUrl)
    toast.success('邀请链接已复制到剪贴板')
  }
}

// 格式化日期
function formatDate(dateStr: string | null | undefined) {
  if (!dateStr) return '永久有效'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <!-- 页面标题 -->
    <PageHeader
      title="个人中心"
      description="管理您的账户信息和偏好设置"
    />

    <!-- 加载状态 -->
    <div v-if="isLoading" class="flex justify-center items-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-500"></div>
    </div>

    <!-- 主内容 -->
    <div v-else class="space-y-6">
      <!-- 标签页导航 -->
      <div class="border-b border-slate-700">
        <nav class="-mb-px flex space-x-8">
          <button
            @click="activeTab = 'profile'"
            :class="[
              activeTab === 'profile'
                ? 'border-orange-500 text-orange-400'
                : 'border-transparent text-slate-400 hover:text-slate-200 hover:border-slate-600',
              'whitespace-nowrap border-b-2 py-4 px-1 text-sm font-medium transition-colors'
            ]"
          >
            个人资料
          </button>
          <button
            @click="activeTab = 'settings'"
            :class="[
              activeTab === 'settings'
                ? 'border-orange-500 text-orange-400'
                : 'border-transparent text-slate-400 hover:text-slate-200 hover:border-slate-600',
              'whitespace-nowrap border-b-2 py-4 px-1 text-sm font-medium transition-colors'
            ]"
          >
            账户设置
          </button>
          <button
            @click="activeTab = 'security'"
            :class="[
              activeTab === 'security'
                ? 'border-orange-500 text-orange-400'
                : 'border-transparent text-slate-400 hover:text-slate-200 hover:border-slate-600',
              'whitespace-nowrap border-b-2 py-4 px-1 text-sm font-medium transition-colors'
            ]"
          >
            安全设置
          </button>
        </nav>
      </div>

      <!-- 个人资料标签页 -->
      <div v-show="activeTab === 'profile'" class="space-y-6">
        <!-- 基本信息卡片 -->
        <div class="glass-card p-6">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-xl font-semibold text-white">基本信息</h2>
            <button
              v-if="!isEditing"
              @click="startEdit"
              class="px-4 py-2 btn-gradient-primary text-sm font-medium"
            >
              编辑资料
            </button>
            <div v-else class="flex space-x-2">
              <button
                @click="cancelEdit"
                :disabled="isSaving"
                class="px-4 py-2 bg-slate-700 hover:bg-slate-600 text-slate-200 rounded-lg text-sm font-medium transition-colors disabled:opacity-50"
              >
                取消
              </button>
              <button
                @click="saveProfile"
                :disabled="isSaving"
                class="px-4 py-2 btn-gradient-primary text-sm font-medium disabled:opacity-50"
              >
                {{ isSaving ? '保存中...' : '保存' }}
              </button>
            </div>
          </div>

          <!-- 头像和用户名 -->
          <div class="flex items-center space-x-4 mb-6">
            <div class="w-20 h-20 bg-gradient-to-br from-orange-500 to-pink-600 rounded-full flex items-center justify-center text-white text-2xl font-bold">
              {{ profile?.nickname?.charAt(0) || profile?.phone?.charAt(0) || 'U' }}
            </div>
            <div>
              <p class="text-lg font-medium text-white">
                {{ profile?.nickname || '未设置昵称' }}
              </p>
              <p class="text-sm text-slate-400">
                {{ profile?.phone }}
              </p>
            </div>
          </div>

          <!-- 编辑表单 -->
          <div v-if="isEditing" class="space-y-4 max-w-md">
            <div>
              <label class="block text-sm font-medium text-slate-300 mb-1">昵称</label>
              <input
                v-model="editForm.nickname"
                type="text"
                class="w-full px-3 py-2 border border-slate-600 rounded-lg focus:ring-2 focus:ring-orange-500 bg-slate-800 text-white"
                placeholder="请输入昵称"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-300 mb-1">邮箱</label>
              <input
                v-model="editForm.email"
                type="email"
                class="w-full px-3 py-2 border border-slate-600 rounded-lg focus:ring-2 focus:ring-orange-500 bg-slate-800 text-white"
                placeholder="请输入邮箱"
              />
            </div>
          </div>

          <!-- 只读信息 -->
          <div v-else class="space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <p class="text-sm text-slate-400">手机号</p>
                <p class="text-white">{{ profile?.phone }}</p>
              </div>
              <div>
                <p class="text-sm text-slate-400">邮箱</p>
                <p class="text-white">{{ profile?.email || '未设置' }}</p>
              </div>
              <div>
                <p class="text-sm text-slate-400">订阅等级</p>
                <p class="inline-block px-2 py-1 bg-orange-500/20 text-orange-400 rounded text-sm">
                  {{ profile?.subscriptionLevel === 'FREE' ? '免费版' : profile?.subscriptionLevel === 'BASIC' ? '基础版' : '专业版' }}
                </p>
              </div>
              <div>
                <p class="text-sm text-slate-400">订阅到期</p>
                <p class="text-white">{{ formatDate(profile?.subscriptionExpireDate) }}</p>
              </div>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <p class="text-sm text-slate-400">注册时间</p>
                <p class="text-white">{{ formatDate(profile?.createdAt) }}</p>
              </div>
              <div>
                <p class="text-sm text-slate-400">最后登录</p>
                <p class="text-white">{{ formatDate(profile?.lastLoginTime) }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 使用统计卡片 -->
        <div class="glass-card p-6">
          <h2 class="text-xl font-semibold text-white mb-6">使用统计</h2>

          <div v-if="usageStats" class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- 搜索次数 -->
            <div>
              <div class="flex justify-between items-center mb-2">
                <span class="text-sm text-slate-400">搜索次数</span>
                <span class="text-sm font-medium text-white">
                  {{ usageStats.searchCount }} / {{ usageStats.searchLimit }}
                </span>
              </div>
              <div class="w-full bg-slate-700 rounded-full h-2">
                <div
                  class="h-2 rounded-full transition-all"
                  :class="getProgressColor(usagePercentage.search)"
                  :style="{ width: `${usagePercentage.search}%` }"
                ></div>
              </div>
            </div>

            <!-- 导出次数 -->
            <div>
              <div class="flex justify-between items-center mb-2">
                <span class="text-sm text-slate-400">导出次数</span>
                <span class="text-sm font-medium text-white">
                  {{ usageStats.exportCount }} / {{ usageStats.exportLimit }}
                </span>
              </div>
              <div class="w-full bg-slate-700 rounded-full h-2">
                <div
                  class="h-2 rounded-full transition-all"
                  :class="getProgressColor(usagePercentage.export)"
                  :style="{ width: `${usagePercentage.export}%` }"
                ></div>
              </div>
            </div>

            <!-- 收藏数 -->
            <div>
              <div class="flex justify-between items-center mb-2">
                <span class="text-sm text-slate-400">收藏数</span>
                <span class="text-sm font-medium text-white">
                  {{ usageStats.favoriteCount }} / {{ usageStats.favoriteLimit }}
                </span>
              </div>
              <div class="w-full bg-slate-700 rounded-full h-2">
                <div
                  class="h-2 rounded-full transition-all"
                  :class="getProgressColor(usagePercentage.favorite)"
                  :style="{ width: `${usagePercentage.favorite}%` }"
                ></div>
              </div>
            </div>

            <!-- 看板数 -->
            <div>
              <div class="flex justify-between items-center mb-2">
                <span class="text-sm text-slate-400">看板数</span>
                <span class="text-sm font-medium text-white">
                  {{ usageStats.boardCount }} / {{ usageStats.boardLimit }}
                </span>
              </div>
              <div class="w-full bg-slate-700 rounded-full h-2">
                <div
                  class="h-2 rounded-full transition-all"
                  :class="getProgressColor(usagePercentage.board)"
                  :style="{ width: `${usagePercentage.board}%` }"
                ></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 邀请好友卡片 -->
        <div class="glass-card p-6">
          <h2 class="text-xl font-semibold text-white mb-4">邀请好友</h2>
          <p class="text-slate-400 mb-4">
            邀请好友注册，双方均可获得延长订阅时长奖励
          </p>

          <div v-if="inviteInfo" class="bg-slate-800 rounded-lg p-4">
            <p class="text-sm text-slate-400 mb-2">您的邀请码</p>
            <div class="flex items-center justify-between">
              <code class="text-lg font-mono text-orange-400">{{ inviteInfo.inviteCode }}</code>
              <button
                @click="copyInviteLink"
                class="px-4 py-2 btn-gradient-primary text-sm font-medium"
              >
                复制链接
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 账户设置标签页 -->
      <div v-show="activeTab === 'settings'" class="space-y-6">
        <div class="glass-card p-6">
          <h2 class="text-xl font-semibold text-white mb-6">通知设置</h2>

          <div class="space-y-4">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-white font-medium">启用消息通知</p>
                <p class="text-sm text-slate-400">接收重要更新和活动通知</p>
              </div>
              <button
                @click="profile!.notificationEnabled = !profile!.notificationEnabled"
                :class="[
                  'relative inline-flex h-6 w-11 transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-orange-500',
                  profile?.notificationEnabled ? 'bg-orange-500' : 'bg-slate-600'
                ]"
                class="rounded-full"
              >
                <span
                  :class="[
                    'inline-block w-5 h-5 transform bg-white rounded-full shadow transition-transform duration-200 ease-in-out',
                    profile?.notificationEnabled ? 'translate-x-6' : 'translate-x-1'
                  ]"
                ></span>
              </button>
            </div>

            <div class="flex items-center justify-between">
              <div>
                <p class="text-white font-medium">订阅邮件</p>
                <p class="text-sm text-slate-400">接收产品推荐和行业资讯</p>
              </div>
              <button
                @click="profile!.emailSubscribed = !profile!.emailSubscribed"
                :class="[
                  'relative inline-flex h-6 w-11 transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-orange-500',
                  profile?.emailSubscribed ? 'bg-orange-500' : 'bg-slate-600'
                ]"
                class="rounded-full"
              >
                <span
                  :class="[
                    'inline-block w-5 h-5 transform bg-white rounded-full shadow transition-transform duration-200 ease-in-out',
                    profile?.emailSubscribed ? 'translate-x-6' : 'translate-x-1'
                  ]"
                ></span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 安全设置标签页 -->
      <div v-show="activeTab === 'security'" class="space-y-6">
        <div class="glass-card p-6">
          <h2 class="text-xl font-semibold text-white mb-6">安全设置</h2>

          <div class="space-y-4">
            <!-- 手机号验证 -->
            <div class="flex items-center justify-between py-3 border-b border-slate-700">
              <div>
                <p class="text-white font-medium">手机号验证</p>
                <p class="text-sm text-slate-400">
                  {{ profile?.phoneVerified ? '已验证' : '未验证' }}
                </p>
              </div>
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                    :class="profile?.phoneVerified ? 'bg-green-500/20 text-green-400' : 'bg-yellow-500/20 text-yellow-400'">
                {{ profile?.phoneVerified ? '已验证' : '未验证' }}
              </span>
            </div>

            <!-- 两步验证 -->
            <div class="flex items-center justify-between py-3">
              <div>
                <p class="text-white font-medium">两步验证</p>
                <p class="text-sm text-slate-400">
                  {{ profile?.twoFactorEnabled ? '已启用' : '未启用' }}
                </p>
              </div>
              <button
                class="px-4 py-2 bg-slate-700 hover:bg-slate-600 text-slate-200 rounded-lg text-sm font-medium transition-colors"
              >
                {{ profile?.twoFactorEnabled ? '禁用' : '启用' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
