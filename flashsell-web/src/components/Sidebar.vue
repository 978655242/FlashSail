<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

/**
 * Sidebar Component
 * 
 * Implements the main navigation sidebar with:
 * - Navigation items with icons (Dashboard, Search, Favorites, Analysis, Subscription, Profile)
 * - FlashSell logo and branding
 * - Collapse/expand functionality
 * - Active state highlighting based on current route
 * - User profile section with logout
 * - Mobile responsive behavior
 * 
 * Requirements: 3.1, 3.2, 3.4, 3.5
 */

interface Props {
  collapsed?: boolean
  mobileOpen?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  collapsed: false,
  mobileOpen: false
})

const emit = defineEmits<{
  (e: 'toggle'): void
  (e: 'mobile-close'): void
  (e: 'logout'): void
}>()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// Navigation item interface
interface NavItem {
  id: string
  label: string
  icon: string
  route: string
  routeName: string
  badge?: number
}

// Main navigation items - Requirements 3.1
const mainNavItems: NavItem[] = [
  { id: 'dashboard', label: 'Dashboard', icon: 'dashboard', route: '/', routeName: 'Home' },
  { id: 'search', label: 'AI Search', icon: 'search', route: '/search', routeName: 'Search' },
  { id: 'favorites', label: 'Favorites', icon: 'heart', route: '/favorites', routeName: 'Favorites' },
  { id: 'analysis', label: 'Market Analysis', icon: 'chart', route: '/market', routeName: 'Market' }
]

// Account navigation items
const accountNavItems: NavItem[] = [
  { id: 'subscription', label: 'Subscription', icon: 'credit-card', route: '/subscription', routeName: 'Subscription' },
  { id: 'profile', label: 'Profile', icon: 'user', route: '/profile', routeName: 'Profile' }
]

// Current route name for active state highlighting - Requirements 3.4
const currentRouteName = computed(() => route.name as string)

// Check if a nav item is active
function isActive(item: NavItem): boolean {
  return currentRouteName.value === item.routeName
}

// Navigate to a route
function navigateTo(item: NavItem) {
  router.push(item.route)
  // Close mobile sidebar after navigation
  if (props.mobileOpen) {
    emit('mobile-close')
  }
}

// Handle sidebar toggle - Requirements 3.2
function handleToggle() {
  emit('toggle')
}

// Handle logout
function handleLogout() {
  emit('logout')
  userStore.logout()
  router.push({ name: 'Login' })
}

// Get user initials for avatar
const userInitial = computed(() => {
  return userStore.userInfo?.nickname?.charAt(0)?.toUpperCase() || 'U'
})

// Get user display name
const userName = computed(() => {
  return userStore.userInfo?.nickname || 'User'
})

// Get user plan (subscription level)
const userPlan = computed(() => {
  const level = userStore.userInfo?.subscriptionLevel || 'FREE'
  // Convert subscription level to display name
  const planNames: Record<string, string> = {
    'FREE': 'Free',
    'BASIC': 'Basic',
    'PRO': 'Pro'
  }
  return planNames[level] || 'Free'
})
</script>

<template>
  <aside
    :class="[
      'sidebar fixed left-0 top-0 h-screen flex flex-col z-50',
      'glass-panel transition-all duration-300',
      props.collapsed ? 'w-[var(--sidebar-collapsed-width)]' : 'w-[var(--sidebar-width)]',
      // Mobile styles
      'lg:translate-x-0',
      props.mobileOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
    ]"
  >
    <!-- Toggle Button (Desktop only) - Requirements 3.2 -->
    <div
      class="sidebar-toggle hidden lg:flex items-center justify-end p-3 cursor-pointer hover:bg-slate-800/30 transition-colors"
      @click="handleToggle"
      role="button"
      tabindex="0"
      aria-label="Toggle sidebar"
      @keydown.enter="handleToggle"
      @keydown.space.prevent="handleToggle"
    >
      <svg
        :class="['w-4 h-4 text-slate-400 transition-transform duration-300', props.collapsed ? 'rotate-180' : '']"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
      </svg>
    </div>

    <!-- Logo - Requirements 3.5 -->
    <div class="flex items-center gap-3 mb-8 px-4">
      <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-orange-500 to-blue-600 flex items-center justify-center flex-shrink-0">
        <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>
        </svg>
      </div>
      <div v-if="!props.collapsed" class="logo-text overflow-hidden">
        <h1 class="font-bold text-lg text-white whitespace-nowrap">FlashSell</h1>
        <p class="text-xs text-slate-500 whitespace-nowrap">AI智能选品</p>
      </div>
    </div>

    <!-- Navigation -->
    <nav class="flex-1 overflow-y-auto px-2">
      <!-- Main Navigation Section -->
      <div 
        v-if="!props.collapsed" 
        class="nav-section-label text-xs font-semibold text-slate-500 px-4 mb-3 uppercase tracking-wider"
      >
        Main
      </div>
      
      <!-- Main Nav Items - Requirements 3.1 -->
      <div
        v-for="item in mainNavItems"
        :key="item.id"
        :class="[
          'nav-item flex items-center gap-3 px-4 py-3 rounded-xl cursor-pointer transition-all mb-1',
          props.collapsed ? 'justify-center' : '',
          isActive(item)
            ? 'bg-gradient-to-r from-orange-500/15 to-orange-500/5 text-orange-400 border border-orange-500/20'
            : 'text-slate-400 hover:bg-slate-800/50 hover:text-slate-200 border border-transparent'
        ]"
        @click="navigateTo(item)"
        role="button"
        tabindex="0"
        :aria-current="isActive(item) ? 'page' : undefined"
        @keydown.enter="navigateTo(item)"
        @keydown.space.prevent="navigateTo(item)"
      >
        <!-- Icons -->
        <svg class="w-5 h-5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <!-- Dashboard Icon -->
          <template v-if="item.icon === 'dashboard'">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"/>
          </template>
          <!-- Search Icon -->
          <template v-else-if="item.icon === 'search'">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
          </template>
          <!-- Heart Icon -->
          <template v-else-if="item.icon === 'heart'">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
          </template>
          <!-- Chart Icon -->
          <template v-else-if="item.icon === 'chart'">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
          </template>
          <!-- Credit Card Icon -->
          <template v-else-if="item.icon === 'credit-card'">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"/>
          </template>
          <!-- User Icon -->
          <template v-else-if="item.icon === 'user'">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
          </template>
        </svg>
        
        <!-- Label -->
        <span 
          v-if="!props.collapsed" 
          class="nav-text text-sm font-medium whitespace-nowrap"
        >
          {{ item.label }}
        </span>
        
        <!-- Badge (if any) -->
        <span 
          v-if="!props.collapsed && item.badge" 
          class="ml-auto px-2 py-0.5 text-xs font-medium bg-orange-500/20 text-orange-400 rounded-full"
        >
          {{ item.badge }}
        </span>
      </div>

      <!-- Account Section -->
      <div 
        v-if="!props.collapsed" 
        class="nav-section-label text-xs font-semibold text-slate-500 px-4 mb-3 mt-6 uppercase tracking-wider"
      >
        Account
      </div>
      
      <!-- Account Nav Items -->
      <div
        v-for="item in accountNavItems"
        :key="item.id"
        :class="[
          'nav-item flex items-center gap-3 px-4 py-3 rounded-xl cursor-pointer transition-all mb-1',
          props.collapsed ? 'justify-center' : '',
          isActive(item)
            ? 'bg-gradient-to-r from-orange-500/15 to-orange-500/5 text-orange-400 border border-orange-500/20'
            : 'text-slate-400 hover:bg-slate-800/50 hover:text-slate-200 border border-transparent'
        ]"
        @click="navigateTo(item)"
        role="button"
        tabindex="0"
        :aria-current="isActive(item) ? 'page' : undefined"
        @keydown.enter="navigateTo(item)"
        @keydown.space.prevent="navigateTo(item)"
      >
        <!-- Icons -->
        <svg class="w-5 h-5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <!-- Credit Card Icon -->
          <template v-if="item.icon === 'credit-card'">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"/>
          </template>
          <!-- User Icon -->
          <template v-else-if="item.icon === 'user'">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
          </template>
        </svg>
        
        <!-- Label -->
        <span 
          v-if="!props.collapsed" 
          class="nav-text text-sm font-medium whitespace-nowrap"
        >
          {{ item.label }}
        </span>
      </div>
    </nav>

    <!-- User Profile Section (Bottom) - Requirements 3.6, 3.7 -->
    <div 
      class="user-section border-t border-slate-700/50 pt-4 mt-4 px-4 pb-4"
      :class="props.collapsed ? 'flex flex-col items-center gap-2' : 'flex flex-row items-center'"
    >
      <!-- User Avatar -->
      <div
        :class="[
          'rounded-full flex items-center justify-center flex-shrink-0',
          'bg-gradient-to-br from-orange-500 to-blue-600 text-white font-bold',
          props.collapsed ? 'w-8 h-8 text-sm' : 'w-10 h-10 text-lg'
        ]"
      >
        {{ userInitial }}
      </div>
      
      <!-- User Info (when expanded) -->
      <div v-if="!props.collapsed" class="flex-1 ml-3 overflow-hidden">
        <div class="font-medium text-sm text-white truncate">
          {{ userName }}
        </div>
        <div class="text-xs text-slate-500 truncate">
          {{ userPlan }} · Member
        </div>
      </div>
      
      <!-- Logout Button - Requirements 3.7 -->
      <button
        v-if="!props.collapsed"
        @click="handleLogout"
        class="text-slate-500 hover:text-red-400 transition-colors p-1"
        title="Logout"
        aria-label="Logout"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
        </svg>
      </button>
      
      <!-- Logout Button (collapsed state) -->
      <button
        v-else
        @click="handleLogout"
        class="text-slate-500 hover:text-red-400 transition-colors p-1 mt-2"
        title="Logout"
        aria-label="Logout"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
        </svg>
      </button>
    </div>
  </aside>
  
  <!-- Mobile Overlay -->
  <div
    v-if="props.mobileOpen"
    class="fixed inset-0 bg-black/50 z-40 lg:hidden"
    @click="emit('mobile-close')"
    aria-hidden="true"
  />
</template>

<style scoped>
/* Sidebar base styles using CSS variables */
.sidebar {
  padding: 20px 12px;
}

/* Ensure smooth width transitions */
.sidebar {
  will-change: width, transform;
}

/* Nav item hover effect */
.nav-item {
  transition: 
    background-color var(--transition-normal),
    color var(--transition-normal),
    border-color var(--transition-normal);
}

/* User section minimum height */
.user-section {
  min-height: 72px;
}

/* Hide scrollbar but keep functionality */
nav::-webkit-scrollbar {
  width: 4px;
}

nav::-webkit-scrollbar-track {
  background: transparent;
}

nav::-webkit-scrollbar-thumb {
  background: rgba(100, 116, 139, 0.3);
  border-radius: 2px;
}

nav::-webkit-scrollbar-thumb:hover {
  background: rgba(100, 116, 139, 0.5);
}

/* Light mode adjustments */
:global(html.light) .sidebar {
  background: rgba(255, 255, 255, 0.95);
  border-right-color: var(--glass-border-light);
}

:global(html.light) .nav-item {
  color: var(--text-secondary);
}

:global(html.light) .nav-item:hover {
  background: rgba(59, 130, 246, 0.08);
  color: var(--text-primary);
}

:global(html.light) .nav-item.active,
:global(html.light) .nav-item[aria-current="page"] {
  background: linear-gradient(135deg, rgba(249, 115, 22, 0.1) 0%, rgba(249, 115, 22, 0.03) 100%);
  color: var(--primary);
  border-color: rgba(249, 115, 22, 0.15);
}

:global(html.light) .nav-section-label {
  color: var(--text-muted);
}

:global(html.light) .logo-text h1 {
  color: var(--text-primary);
}

:global(html.light) .user-section {
  border-top-color: var(--glass-border-light);
}

:global(html.light) .user-section .font-medium {
  color: var(--text-primary);
}
</style>
