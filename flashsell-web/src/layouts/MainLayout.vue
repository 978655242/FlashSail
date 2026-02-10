<script setup lang="ts">
import { ref, computed } from 'vue'
import { RouterView } from 'vue-router'
import Sidebar from '@/components/Sidebar.vue'
import TopNav from '@/components/TopNav.vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const sidebarCollapsed = ref(false)
const sidebarMobileOpen = ref(false)
const userStore = useUserStore()
const router = useRouter()

// Get current user from store
const currentUser = computed(() => userStore.userInfo)

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

function openMobileSidebar() {
  sidebarMobileOpen.value = true
}

function closeMobileSidebar() {
  sidebarMobileOpen.value = false
}

function handleLogout() {
  userStore.logout()
  router.push({ name: 'Login' })
}
</script>

<template>
  <div class="min-h-screen bg-[var(--bg-dark)]">
    <!-- Aurora Background -->
    <div class="aurora-bg"></div>

    <!-- Sidebar -->
    <Sidebar 
      :collapsed="sidebarCollapsed" 
      :mobile-open="sidebarMobileOpen"
      @toggle="toggleSidebar" 
      @mobile-close="closeMobileSidebar"
      @logout="handleLogout"
    />

    <!-- Main Content Area -->
    <div
      :class="[
        'main-content-wrapper min-h-screen transition-all duration-300',
        sidebarCollapsed ? 'lg:ml-[var(--sidebar-collapsed-width)]' : 'lg:ml-[var(--sidebar-width)]'
      ]"
    >
      <!-- TopNav Header -->
      <TopNav 
        :user="currentUser"
        @toggle-sidebar="openMobileSidebar"
        @logout="handleLogout"
      />

      <!-- Page Content -->
      <main class="main-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
/* Main content wrapper */
.main-content-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* Main content area */
.main-content {
  flex: 1;
  padding: var(--content-padding, 24px);
}

/* Mobile responsive - no margin on mobile */
@media (max-width: 1023px) {
  .main-content-wrapper {
    margin-left: 0 !important;
  }
}
</style>

<style scoped>
/* Mobile responsive - no margin on mobile */
@media (max-width: 1023px) {
  main {
    margin-left: 0 !important;
  }
}
</style>
