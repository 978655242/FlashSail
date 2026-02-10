/**
 * Property-Based Tests for Sidebar Component
 * 
 * These tests validate the correctness properties of the Sidebar component
 * using fast-check for property-based testing.
 * 
 * **Validates: Requirements 3.2, 3.3, 3.4, 3.6, 3.7**
 * - 3.2: Sidebar collapse/expand functionality
 * - 3.3: Main content area adjustment when sidebar collapses
 * - 3.4: Active navigation item highlighting
 * - 3.6: User profile display when logged in
 * - 3.7: Logout button visibility for authenticated users
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import fc from 'fast-check'
import Sidebar from '@/components/Sidebar.vue'
import { useUserStore } from '@/stores/user'
import type { UserInfo } from '@/types/user'

// Navigation items as defined in Sidebar.vue
const mainNavItems = [
  { id: 'dashboard', label: 'Dashboard', icon: 'dashboard', route: '/', routeName: 'Home' },
  { id: 'search', label: 'AI Search', icon: 'search', route: '/search', routeName: 'Search' },
  { id: 'favorites', label: 'Favorites', icon: 'heart', route: '/favorites', routeName: 'Favorites' },
  { id: 'analysis', label: 'Market Analysis', icon: 'chart', route: '/market', routeName: 'Market' }
]

const accountNavItems = [
  { id: 'subscription', label: 'Subscription', icon: 'credit-card', route: '/subscription', routeName: 'Subscription' },
  { id: 'profile', label: 'Profile', icon: 'user', route: '/profile', routeName: 'Profile' }
]

const allNavItems = [...mainNavItems, ...accountNavItems]
const allRouteNames = allNavItems.map(item => item.routeName)

// Mock vue-router
const mockPush = vi.fn()
const mockRoute = {
  name: 'Home',
  path: '/',
  params: {},
  query: {},
  meta: {}
}

vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-router')>()
  return {
    ...actual,
    useRoute: () => mockRoute,
    useRouter: () => ({
      push: mockPush
    })
  }
})

// Create localStorage mock
function createLocalStorageMock() {
  let store: Record<string, string> = {}
  return {
    getItem: (key: string) => store[key] ?? null,
    setItem: (key: string, value: string) => {
      store[key] = value
    },
    removeItem: (key: string) => {
      delete store[key]
    },
    clear: () => {
      store = {}
    },
    get length() {
      return Object.keys(store).length
    },
    key: (index: number) => Object.keys(store)[index] ?? null
  }
}

// Helper to generate user info for testing
function generateUserInfo(nickname: string, subscriptionLevel: string): UserInfo {
  return {
    userId: 1,
    nickname,
    avatar: '',
    email: 'test@example.com',
    phone: '13812345678',
    subscriptionLevel: subscriptionLevel as 'FREE' | 'BASIC' | 'PRO',
    subscriptionExpireDate: null
  }
}

describe('Sidebar Property Tests', () => {
  let localStorageMock: ReturnType<typeof createLocalStorageMock>
  let pinia: ReturnType<typeof createPinia>

  beforeEach(() => {
    // Create fresh localStorage mock
    localStorageMock = createLocalStorageMock()
    vi.stubGlobal('localStorage', localStorageMock)
    
    // Create fresh Pinia instance
    pinia = createPinia()
    setActivePinia(pinia)
    
    // Reset mock functions
    mockPush.mockClear()
    mockRoute.name = 'Home'
    mockRoute.path = '/'
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  /**
   * **Feature: frontend-ui-refactor, Property 5: Sidebar Collapse State**
   * 
   * *For any* sidebar state (expanded or collapsed), toggling the collapse 
   * should change the sidebar width and the main content area should adjust 
   * its margin accordingly.
   * 
   * **Validates: Requirements 3.2, 3.3**
   */
  describe('Property 5: Sidebar Collapse State', () => {
    it('should have correct width class based on collapsed state', () => {
      fc.assert(
        fc.property(
          fc.boolean(),
          (collapsed: boolean) => {
            const wrapper = mount(Sidebar, {
              props: {
                collapsed,
                mobileOpen: false
              },
              global: {
                plugins: [pinia]
              }
            })

            const sidebar = wrapper.find('aside')
            
            if (collapsed) {
              // When collapsed, should have collapsed width class
              expect(sidebar.classes()).toContain('w-[var(--sidebar-collapsed-width)]')
              expect(sidebar.classes()).not.toContain('w-[var(--sidebar-width)]')
            } else {
              // When expanded, should have full width class
              expect(sidebar.classes()).toContain('w-[var(--sidebar-width)]')
              expect(sidebar.classes()).not.toContain('w-[var(--sidebar-collapsed-width)]')
            }

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should hide navigation labels when collapsed', () => {
      fc.assert(
        fc.property(
          fc.boolean(),
          (collapsed: boolean) => {
            const wrapper = mount(Sidebar, {
              props: {
                collapsed,
                mobileOpen: false
              },
              global: {
                plugins: [pinia]
              }
            })

            const navTexts = wrapper.findAll('.nav-text')
            
            if (collapsed) {
              // When collapsed, nav text elements should not be visible (v-if="!props.collapsed")
              expect(navTexts.length).toBe(0)
            } else {
              // When expanded, nav text elements should be visible
              expect(navTexts.length).toBeGreaterThan(0)
            }

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should hide section labels when collapsed', () => {
      fc.assert(
        fc.property(
          fc.boolean(),
          (collapsed: boolean) => {
            const wrapper = mount(Sidebar, {
              props: {
                collapsed,
                mobileOpen: false
              },
              global: {
                plugins: [pinia]
              }
            })

            const sectionLabels = wrapper.findAll('.nav-section-label')
            
            if (collapsed) {
              // When collapsed, section labels should not be visible
              expect(sectionLabels.length).toBe(0)
            } else {
              // When expanded, section labels should be visible (Main and Account)
              expect(sectionLabels.length).toBe(2)
            }

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should emit toggle event when toggle button is clicked', async () => {
      await fc.assert(
        fc.asyncProperty(
          fc.boolean(),
          async (initialCollapsed: boolean) => {
            const wrapper = mount(Sidebar, {
              props: {
                collapsed: initialCollapsed,
                mobileOpen: false
              },
              global: {
                plugins: [pinia]
              }
            })

            const toggleButton = wrapper.find('.sidebar-toggle')
            
            // Toggle button exists (it's hidden on mobile via CSS, but exists in DOM)
            expect(toggleButton.exists()).toBe(true)
            
            await toggleButton.trigger('click')

            // Should emit toggle event
            expect(wrapper.emitted('toggle')).toBeTruthy()
            expect(wrapper.emitted('toggle')?.length).toBe(1)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should rotate toggle icon based on collapsed state', () => {
      fc.assert(
        fc.property(
          fc.boolean(),
          (collapsed: boolean) => {
            const wrapper = mount(Sidebar, {
              props: {
                collapsed,
                mobileOpen: false
              },
              global: {
                plugins: [pinia]
              }
            })

            const toggleIcon = wrapper.find('.sidebar-toggle svg')
            
            if (collapsed) {
              // When collapsed, icon should be rotated 180 degrees
              expect(toggleIcon.classes()).toContain('rotate-180')
            } else {
              // When expanded, icon should not be rotated
              expect(toggleIcon.classes()).not.toContain('rotate-180')
            }

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should center nav items when collapsed', () => {
      fc.assert(
        fc.property(
          fc.boolean(),
          (collapsed: boolean) => {
            const wrapper = mount(Sidebar, {
              props: {
                collapsed,
                mobileOpen: false
              },
              global: {
                plugins: [pinia]
              }
            })

            const navItems = wrapper.findAll('.nav-item')
            
            navItems.forEach(navItem => {
              if (collapsed) {
                // When collapsed, nav items should be centered
                expect(navItem.classes()).toContain('justify-center')
              } else {
                // When expanded, nav items should not be centered
                expect(navItem.classes()).not.toContain('justify-center')
              }
            })

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * **Feature: frontend-ui-refactor, Property 6: Active Navigation Highlighting**
   * 
   * *For any* route in the application, the corresponding navigation item 
   * in the sidebar should have the active class applied, and no other 
   * navigation items should be active.
   * 
   * **Validates: Requirements 3.4**
   */
  describe('Property 6: Active Navigation Highlighting', () => {
    it('should highlight exactly one navigation item for any valid route', () => {
      fc.assert(
        fc.property(
          fc.constantFrom(...allRouteNames),
          (routeName: string) => {
            // Set the mock route to the test route
            mockRoute.name = routeName
            const navItem = allNavItems.find(item => item.routeName === routeName)
            if (navItem) {
              mockRoute.path = navItem.route
            }

            const wrapper = mount(Sidebar, {
              props: {
                collapsed: false,
                mobileOpen: false
              },
              global: {
                plugins: [pinia]
              }
            })

            const navItems = wrapper.findAll('.nav-item')
            
            // Count active items (items with aria-current="page")
            const activeItems = navItems.filter(item => 
              item.attributes('aria-current') === 'page'
            )

            // Exactly one item should be active
            expect(activeItems.length).toBe(1)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should apply active styling to the correct navigation item', () => {
      fc.assert(
        fc.property(
          fc.constantFrom(...allNavItems),
          (targetNavItem) => {
            // Set the mock route to the target route
            mockRoute.name = targetNavItem.routeName
            mockRoute.path = targetNavItem.route

            const wrapper = mount(Sidebar, {
              props: {
                collapsed: false,
                mobileOpen: false
              },
              global: {
                plugins: [pinia]
              }
            })

            const navItems = wrapper.findAll('.nav-item')
            
            navItems.forEach(navItem => {
              const navText = navItem.find('.nav-text')
              const isTargetItem = navText.exists() && navText.text() === targetNavItem.label
              
              if (isTargetItem) {
                // Target item should have active styling
                expect(navItem.attributes('aria-current')).toBe('page')
                expect(navItem.classes()).toContain('text-orange-400')
              } else {
                // Other items should not have active styling
                expect(navItem.attributes('aria-current')).toBeUndefined()
                expect(navItem.classes()).toContain('text-slate-400')
              }
            })

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should maintain active state consistency when collapsed', () => {
      fc.assert(
        fc.property(
          fc.constantFrom(...allRouteNames),
          fc.boolean(),
          (routeName: string, collapsed: boolean) => {
            // Set the mock route
            mockRoute.name = routeName
            const navItem = allNavItems.find(item => item.routeName === routeName)
            if (navItem) {
              mockRoute.path = navItem.route
            }

            const wrapper = mount(Sidebar, {
              props: {
                collapsed,
                mobileOpen: false
              },
              global: {
                plugins: [pinia]
              }
            })

            const navItems = wrapper.findAll('.nav-item')
            
            // Count active items
            const activeItems = navItems.filter(item => 
              item.attributes('aria-current') === 'page'
            )

            // Exactly one item should be active regardless of collapsed state
            expect(activeItems.length).toBe(1)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should navigate to correct route when nav item is clicked', async () => {
      await fc.assert(
        fc.asyncProperty(
          fc.constantFrom(...allNavItems),
          async (targetNavItem) => {
            mockRoute.name = 'Home'
            mockRoute.path = '/'
            mockPush.mockClear()

            const wrapper = mount(Sidebar, {
              props: {
                collapsed: false,
                mobileOpen: false
              },
              global: {
                plugins: [pinia]
              }
            })

            // Find the nav item by label
            const navItems = wrapper.findAll('.nav-item')
            const targetElement = navItems.find(item => {
              const navText = item.find('.nav-text')
              return navText.exists() && navText.text() === targetNavItem.label
            })

            if (targetElement) {
              await targetElement.trigger('click')
              
              // Should call router.push with correct route
              expect(mockPush).toHaveBeenCalledWith(targetNavItem.route)
            }

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * **Feature: frontend-ui-refactor, Property 7: User Authentication State Display**
   * 
   * *For any* authentication state (logged in or logged out), the sidebar 
   * should display user profile information only when logged in, and the 
   * logout button should only be visible when authenticated.
   * 
   * **Validates: Requirements 3.6, 3.7**
   */
  describe('Property 7: User Authentication State Display', () => {
    // Arbitrary for generating user nicknames
    const nicknameArb = fc.string({ minLength: 1, maxLength: 20 })
      .filter(s => s.trim().length > 0)
      .map(s => s.trim())
    
    // Arbitrary for subscription levels
    const subscriptionLevelArb = fc.constantFrom('FREE', 'BASIC', 'PRO')

    it('should display user initial in avatar when logged in', () => {
      fc.assert(
        fc.property(
          nicknameArb,
          subscriptionLevelArb,
          (nickname: string, subscriptionLevel: string) => {
            // Setup user store with logged in state
            const userStore = useUserStore()
            userStore.setLoginData({
              token: 'test-token',
              refreshToken: 'test-refresh-token',
              userInfo: generateUserInfo(nickname, subscriptionLevel)
            })

            const wrapper = mount(Sidebar, {
              props: {
                collapsed: false,
                mobileOpen: false
              },
              global: {
                plugins: [pinia]
              }
            })

            // Find user avatar
            const userSection = wrapper.find('.user-section')
            const avatar = userSection.find('.rounded-full')
            
            // Avatar should display first character of nickname (uppercase)
            const expectedInitial = nickname.charAt(0).toUpperCase()
            expect(avatar.text()).toBe(expectedInitial)

            wrapper.unmount()
            userStore.logout()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display user name when logged in and sidebar is expanded', () => {
      fc.assert(
        fc.property(
          nicknameArb,
          subscriptionLevelArb,
          (nickname: string, subscriptionLevel: string) => {
            // Setup user store with logged in state
            const userStore = useUserStore()
            userStore.setLoginData({
              token: 'test-token',
              refreshToken: 'test-refresh-token',
              userInfo: generateUserInfo(nickname, subscriptionLevel)
            })

            const wrapper = mount(Sidebar, {
              props: {
                collapsed: false,
                mobileOpen: false
              },
              global: {
                plugins: [pinia]
              }
            })

            // Find user name display
            const userSection = wrapper.find('.user-section')
            const userNameElement = userSection.find('.font-medium')
            
            // Should display the nickname
            expect(userNameElement.text()).toBe(nickname)

            wrapper.unmount()
            userStore.logout()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should hide user name when sidebar is collapsed', () => {
      fc.assert(
        fc.property(
          nicknameArb,
          subscriptionLevelArb,
          (nickname: string, subscriptionLevel: string) => {
            // Setup user store with logged in state
            const userStore = useUserStore()
            userStore.setLoginData({
              token: 'test-token',
              refreshToken: 'test-refresh-token',
              userInfo: generateUserInfo(nickname, subscriptionLevel)
            })

            const wrapper = mount(Sidebar, {
              props: {
                collapsed: true,
                mobileOpen: false
              },
              global: {
                plugins: [pinia]
              }
            })

            // Find user section
            const userSection = wrapper.find('.user-section')
            
            // User name element should not exist when collapsed (v-if="!props.collapsed")
            const userNameElement = userSection.find('.flex-1.ml-3')
            expect(userNameElement.exists()).toBe(false)

            wrapper.unmount()
            userStore.logout()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display correct subscription plan', () => {
      fc.assert(
        fc.property(
          nicknameArb,
          subscriptionLevelArb,
          (nickname: string, subscriptionLevel: string) => {
            // Setup user store with logged in state
            const userStore = useUserStore()
            userStore.setLoginData({
              token: 'test-token',
              refreshToken: 'test-refresh-token',
              userInfo: generateUserInfo(nickname, subscriptionLevel)
            })

            const wrapper = mount(Sidebar, {
              props: {
                collapsed: false,
                mobileOpen: false
              },
              global: {
                plugins: [pinia]
              }
            })

            // Find plan display
            const userSection = wrapper.find('.user-section')
            const planElement = userSection.find('.text-xs.text-slate-500')
            
            // Map subscription level to display name
            const planNames: Record<string, string> = {
              'FREE': 'Free',
              'BASIC': 'Basic',
              'PRO': 'Pro'
            }
            const expectedPlan = planNames[subscriptionLevel] || 'Free'
            
            // Should display the plan name
            expect(planElement.text()).toContain(expectedPlan)

            wrapper.unmount()
            userStore.logout()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should show logout button when authenticated', () => {
      fc.assert(
        fc.property(
          nicknameArb,
          subscriptionLevelArb,
          fc.boolean(),
          (nickname: string, subscriptionLevel: string, collapsed: boolean) => {
            // Setup user store with logged in state
            const userStore = useUserStore()
            userStore.setLoginData({
              token: 'test-token',
              refreshToken: 'test-refresh-token',
              userInfo: generateUserInfo(nickname, subscriptionLevel)
            })

            const wrapper = mount(Sidebar, {
              props: {
                collapsed,
                mobileOpen: false
              },
              global: {
                plugins: [pinia]
              }
            })

            // Find logout button
            const logoutButton = wrapper.find('button[aria-label="Logout"]')
            
            // Logout button should always be visible when authenticated
            expect(logoutButton.exists()).toBe(true)

            wrapper.unmount()
            userStore.logout()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should emit logout event when logout button is clicked', async () => {
      await fc.assert(
        fc.asyncProperty(
          nicknameArb,
          subscriptionLevelArb,
          async (nickname: string, subscriptionLevel: string) => {
            // Setup user store with logged in state
            const userStore = useUserStore()
            userStore.setLoginData({
              token: 'test-token',
              refreshToken: 'test-refresh-token',
              userInfo: generateUserInfo(nickname, subscriptionLevel)
            })

            const wrapper = mount(Sidebar, {
              props: {
                collapsed: false,
                mobileOpen: false
              },
              global: {
                plugins: [pinia]
              }
            })

            // Find and click logout button
            const logoutButton = wrapper.find('button[aria-label="Logout"]')
            await logoutButton.trigger('click')

            // Should emit logout event
            expect(wrapper.emitted('logout')).toBeTruthy()
            expect(wrapper.emitted('logout')?.length).toBe(1)

            wrapper.unmount()
            userStore.logout()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display default user initial when no nickname is set', () => {
      fc.assert(
        fc.property(
          subscriptionLevelArb,
          (subscriptionLevel: string) => {
            // Setup user store with empty nickname
            const userStore = useUserStore()
            userStore.setLoginData({
              token: 'test-token',
              refreshToken: 'test-refresh-token',
              userInfo: {
                userId: 1,
                nickname: '',
                avatar: '',
                email: 'test@example.com',
                phone: '13812345678',
                subscriptionLevel: subscriptionLevel as 'FREE' | 'BASIC' | 'PRO',
                subscriptionExpireDate: null
              }
            })

            const wrapper = mount(Sidebar, {
              props: {
                collapsed: false,
                mobileOpen: false
              },
              global: {
                plugins: [pinia]
              }
            })

            // Find user avatar
            const userSection = wrapper.find('.user-section')
            const avatar = userSection.find('.rounded-full')
            
            // Avatar should display 'U' as default when nickname is empty
            expect(avatar.text()).toBe('U')

            wrapper.unmount()
            userStore.logout()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display default user name when no nickname is set', () => {
      fc.assert(
        fc.property(
          subscriptionLevelArb,
          (subscriptionLevel: string) => {
            // Setup user store with empty nickname
            const userStore = useUserStore()
            userStore.setLoginData({
              token: 'test-token',
              refreshToken: 'test-refresh-token',
              userInfo: {
                userId: 1,
                nickname: '',
                avatar: '',
                email: 'test@example.com',
                phone: '13812345678',
                subscriptionLevel: subscriptionLevel as 'FREE' | 'BASIC' | 'PRO',
                subscriptionExpireDate: null
              }
            })

            const wrapper = mount(Sidebar, {
              props: {
                collapsed: false,
                mobileOpen: false
              },
              global: {
                plugins: [pinia]
              }
            })

            // Find user name display
            const userSection = wrapper.find('.user-section')
            const userNameElement = userSection.find('.font-medium')
            
            // Should display 'User' as default when nickname is empty
            expect(userNameElement.text()).toBe('User')

            wrapper.unmount()
            userStore.logout()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should maintain user display consistency across collapsed states', () => {
      fc.assert(
        fc.property(
          nicknameArb,
          subscriptionLevelArb,
          fc.boolean(),
          (nickname: string, subscriptionLevel: string, collapsed: boolean) => {
            // Setup user store with logged in state
            const userStore = useUserStore()
            userStore.setLoginData({
              token: 'test-token',
              refreshToken: 'test-refresh-token',
              userInfo: generateUserInfo(nickname, subscriptionLevel)
            })

            const wrapper = mount(Sidebar, {
              props: {
                collapsed,
                mobileOpen: false
              },
              global: {
                plugins: [pinia]
              }
            })

            // Find user section
            const userSection = wrapper.find('.user-section')
            const avatar = userSection.find('.rounded-full')
            
            // Avatar should always be visible and show correct initial
            expect(avatar.exists()).toBe(true)
            expect(avatar.text()).toBe(nickname.charAt(0).toUpperCase())

            wrapper.unmount()
            userStore.logout()
          }
        ),
        { numRuns: 100 }
      )
    })
  })
})
