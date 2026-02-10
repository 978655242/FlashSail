<script setup lang="ts">
/**
 * TopNav Component
 * 
 * Provides a top navigation header with:
 * - Language toggle button (Chinese/English)
 * - Theme toggle button (dark/light)
 * - Current language badge display
 * 
 * Requirements: 5.8, 13.3
 * - 5.8: Dashboard shall support language toggle
 * - 13.3: Frontend shall provide a language toggle button in the header
 */

import { ref, computed } from 'vue'
import { useTheme } from '@/composables/useTheme'
import { useI18n } from '@/composables/useI18n'
import type { UserInfo } from '@/types/user'

interface Props {
  user?: UserInfo | null
}

defineProps<Props>()
defineEmits<{
  toggleSidebar: []
  logout: []
}>()

// Theme composable
const { isDark, toggleTheme } = useTheme()

// i18n composable - using the store for language management
const { locale, t, toggleLocale } = useI18n()

// Computed properties for display
const languageLabel = computed(() => locale.value === 'zh' ? '中文' : 'EN')
const languageBadgeText = computed(() => locale.value === 'zh' ? '中' : 'EN')

const showUserMenu = ref(false)

function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value
}

function closeUserMenu() {
  showUserMenu.value = false
}
</script>

<template>
  <header class="top-nav glass-panel">
    <!-- Left: Mobile menu button -->
    <button
      class="mobile-menu-btn lg:hidden"
      @click="$emit('toggleSidebar')"
      aria-label="Toggle sidebar"
    >
      <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
      </svg>
    </button>

    <!-- Center: Spacer -->
    <div class="flex-1"></div>

    <!-- Right: Controls -->
    <div class="flex items-center gap-2">
      <!-- Language Badge -->
      <span 
        class="language-badge"
        :title="locale === 'zh' ? 'Current: Chinese' : 'Current: English'"
      >
        {{ languageBadgeText }}
      </span>

      <!-- Language Toggle Button -->
      <button
        class="toggle-btn"
        @click="toggleLocale"
        :title="locale === 'zh' ? 'Switch to English' : '切换到中文'"
        aria-label="Toggle language"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129" />
        </svg>
        <span class="hidden sm:inline ml-1.5 text-sm">{{ languageLabel }}</span>
      </button>

      <!-- Theme Toggle Button -->
      <button
        class="toggle-btn"
        @click="toggleTheme"
        :title="isDark ? 'Switch to light mode' : 'Switch to dark mode'"
        aria-label="Toggle theme"
      >
        <!-- Sun icon (shown in dark mode) -->
        <svg v-if="isDark" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
        </svg>
        <!-- Moon icon (shown in light mode) -->
        <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
        </svg>
      </button>

      <!-- User Menu (if user is provided) -->
      <div v-if="user" class="relative ml-2">
        <button
          class="user-menu-btn"
          @click="toggleUserMenu"
          @blur="closeUserMenu"
        >
          <div class="user-avatar">
            {{ user?.nickname?.charAt(0) || 'U' }}
          </div>
          <span class="hidden md:block text-sm font-medium text-[var(--text-primary)]">
            {{ user?.nickname || '用户' }}
          </span>
          <svg class="w-4 h-4 text-[var(--text-muted)]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
          </svg>
        </button>

        <!-- Dropdown Menu -->
        <div
          v-if="showUserMenu"
          class="user-dropdown glass-panel"
        >
          <div class="dropdown-header">
            <p class="text-sm font-medium text-[var(--text-primary)]">{{ user?.nickname }}</p>
            <p class="text-xs text-[var(--text-muted)]">{{ user?.email || user?.phone }}</p>
          </div>
          <a href="/profile" class="dropdown-item">
            {{ t('nav.profile') }}
          </a>
          <a href="/subscription" class="dropdown-item">
            {{ t('nav.subscription') }}
          </a>
          <hr class="dropdown-divider" />
          <button
            class="dropdown-item text-[var(--danger)]"
            @mousedown.prevent="$emit('logout')"
          >
            {{ t('nav.logout') }}
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
.top-nav {
  display: flex;
  align-items: center;
  height: var(--topnav-height, 64px);
  padding: 0 var(--content-padding, 24px);
  border-bottom: 1px solid var(--border-subtle);
  background: var(--glass-bg-dark);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
}

.mobile-menu-btn {
  padding: 0.5rem;
  color: var(--text-secondary);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.mobile-menu-btn:hover {
  color: var(--text-primary);
  background: var(--bg-card-hover);
}

/* Language Badge */
.language-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  padding: 0 8px;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--primary);
  background: rgba(249, 115, 22, 0.15);
  border: 1px solid rgba(249, 115, 22, 0.3);
  border-radius: var(--radius-full);
  transition: all var(--transition-fast);
}

/* Toggle Buttons */
.toggle-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.5rem 0.75rem;
  color: var(--text-secondary);
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  cursor: pointer;
}

.toggle-btn:hover {
  color: var(--text-primary);
  background: var(--bg-card-hover);
  border-color: var(--border-hover);
}

.toggle-btn:active {
  transform: scale(0.95);
}

/* User Menu Button */
.user-menu-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.375rem;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  cursor: pointer;
  background: transparent;
  border: none;
}

.user-menu-btn:hover {
  background: var(--bg-card-hover);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

/* User Dropdown */
.user-dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  width: 200px;
  padding: 0.5rem 0;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
  background: var(--glass-bg-dark);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  box-shadow: var(--shadow-lg);
  z-index: var(--z-dropdown);
}

.dropdown-header {
  padding: 0.75rem 1rem;
  border-bottom: 1px solid var(--border-subtle);
}

.dropdown-item {
  display: block;
  width: 100%;
  padding: 0.625rem 1rem;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  text-align: left;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.dropdown-item:hover {
  color: var(--text-primary);
  background: var(--bg-card-hover);
}

.dropdown-divider {
  margin: 0.5rem 0;
  border: none;
  border-top: 1px solid var(--border-subtle);
}

/* Responsive adjustments */
@media (max-width: 640px) {
  .top-nav {
    padding: 0 1rem;
  }
  
  .toggle-btn {
    padding: 0.5rem;
  }
}
</style>
