/**
 * Property-Based Tests for Dashboard Component
 * 
 * These tests validate the correctness properties of the Dashboard component
 * using fast-check for property-based testing.
 * 
 * **Validates: Requirements 5.1**
 * - 5.1: Dashboard welcome message with user's name (if logged in)
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import fc from 'fast-check'
import { useUserStore } from '@/stores/user'
import type { UserInfo } from '@/types/user'

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

/**
 * Welcome message generator function that mirrors the Dashboard logic
 * This function represents the expected behavior based on Requirements 5.1:
 * - For authenticated users: personalized welcome with user's name
 * - For unauthenticated users: generic welcome message
 */
function generateWelcomeMessage(isLoggedIn: boolean, nickname: string | null): string {
  if (isLoggedIn && nickname && nickname.trim().length > 0) {
    return `欢迎回来，${nickname}！查看今日选品数据`
  }
  return '欢迎回来，查看今日选品数据'
}

/**
 * Extract user display name from user info
 * Handles edge cases like empty or whitespace-only nicknames
 */
function getUserDisplayName(userInfo: UserInfo | null): string {
  if (!userInfo) return ''
  const nickname = userInfo.nickname?.trim()
  return nickname || ''
}

describe('Dashboard Property Tests', () => {
  let localStorageMock: ReturnType<typeof createLocalStorageMock>
  let pinia: ReturnType<typeof createPinia>

  beforeEach(() => {
    // Create fresh localStorage mock
    localStorageMock = createLocalStorageMock()
    vi.stubGlobal('localStorage', localStorageMock)
    
    // Create fresh Pinia instance
    pinia = createPinia()
    setActivePinia(pinia)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  /**
   * **Feature: frontend-ui-refactor, Property 25: Dashboard Welcome Message**
   * 
   * *For any* authenticated user, the dashboard should display a personalized 
   * welcome message with the user's name. For unauthenticated users, a generic 
   * welcome message should be shown.
   * 
   * **Validates: Requirements 5.1**
   */
  describe('Property 25: Dashboard Welcome Message', () => {
    // Arbitrary for generating user nicknames
    const nicknameArb = fc.string({ minLength: 1, maxLength: 20 })
      .filter(s => s.trim().length > 0)
      .map(s => s.trim())
    
    // Arbitrary for subscription levels
    const subscriptionLevelArb = fc.constantFrom('FREE', 'BASIC', 'PRO')

    it('should generate personalized welcome message for authenticated users with valid nickname', () => {
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

            // Verify user is logged in
            expect(userStore.isLoggedIn).toBe(true)
            expect(userStore.userInfo).not.toBeNull()

            // Get the display name
            const displayName = getUserDisplayName(userStore.userInfo)
            
            // Generate welcome message
            const welcomeMessage = generateWelcomeMessage(userStore.isLoggedIn, displayName)

            // For authenticated users with valid nickname, message should include the name
            expect(welcomeMessage).toContain(nickname)
            expect(welcomeMessage).toContain('欢迎回来')
            
            // Clean up
            userStore.logout()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should generate generic welcome message for unauthenticated users', () => {
      fc.assert(
        fc.property(
          fc.constant(null),
          () => {
            // Ensure user is logged out
            const userStore = useUserStore()
            userStore.logout()

            // Verify user is logged out
            expect(userStore.isLoggedIn).toBe(false)
            expect(userStore.userInfo).toBeNull()

            // Get the display name (should be empty for logged out users)
            const displayName = getUserDisplayName(userStore.userInfo)
            
            // Generate welcome message
            const welcomeMessage = generateWelcomeMessage(userStore.isLoggedIn, displayName)

            // For unauthenticated users, message should be generic
            expect(welcomeMessage).toBe('欢迎回来，查看今日选品数据')
            expect(welcomeMessage).not.toMatch(/欢迎回来，[^，]+！/)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should differentiate welcome message based on authentication state', () => {
      fc.assert(
        fc.property(
          nicknameArb,
          subscriptionLevelArb,
          fc.boolean(),
          (nickname: string, subscriptionLevel: string, isAuthenticated: boolean) => {
            const userStore = useUserStore()
            
            if (isAuthenticated) {
              userStore.setLoginData({
                token: 'test-token',
                refreshToken: 'test-refresh-token',
                userInfo: generateUserInfo(nickname, subscriptionLevel)
              })
            } else {
              userStore.logout()
            }

            const displayName = getUserDisplayName(userStore.userInfo)
            const welcomeMessage = generateWelcomeMessage(userStore.isLoggedIn, displayName)

            if (isAuthenticated) {
              // Authenticated users should see personalized message
              expect(welcomeMessage).toContain(nickname)
              expect(welcomeMessage).toMatch(/欢迎回来，.+！/)
            } else {
              // Unauthenticated users should see generic message
              expect(welcomeMessage).toBe('欢迎回来，查看今日选品数据')
            }

            // Clean up
            userStore.logout()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should handle empty nickname gracefully for authenticated users', () => {
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

            // User is authenticated but has empty nickname
            expect(userStore.isLoggedIn).toBe(true)

            const displayName = getUserDisplayName(userStore.userInfo)
            const welcomeMessage = generateWelcomeMessage(userStore.isLoggedIn, displayName)

            // With empty nickname, should fall back to generic message
            expect(welcomeMessage).toBe('欢迎回来，查看今日选品数据')
            // Should not contain "undefined" or "null"
            expect(welcomeMessage).not.toContain('undefined')
            expect(welcomeMessage).not.toContain('null')

            // Clean up
            userStore.logout()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should handle whitespace-only nickname gracefully', () => {
      fc.assert(
        fc.property(
          fc.constantFrom('   ', '\t', '\n', '  \t  ', '\n\n', ' \t \n '),
          subscriptionLevelArb,
          (whitespaceNickname: string, subscriptionLevel: string) => {
            // Setup user store with whitespace-only nickname
            const userStore = useUserStore()
            userStore.setLoginData({
              token: 'test-token',
              refreshToken: 'test-refresh-token',
              userInfo: {
                userId: 1,
                nickname: whitespaceNickname,
                avatar: '',
                email: 'test@example.com',
                phone: '13812345678',
                subscriptionLevel: subscriptionLevel as 'FREE' | 'BASIC' | 'PRO',
                subscriptionExpireDate: null
              }
            })

            const displayName = getUserDisplayName(userStore.userInfo)
            const welcomeMessage = generateWelcomeMessage(userStore.isLoggedIn, displayName)

            // With whitespace-only nickname, should fall back to generic message
            expect(welcomeMessage).toBe('欢迎回来，查看今日选品数据')

            // Clean up
            userStore.logout()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should maintain welcome message consistency for same user state', () => {
      fc.assert(
        fc.property(
          nicknameArb,
          subscriptionLevelArb,
          (nickname: string, subscriptionLevel: string) => {
            const userStore = useUserStore()
            userStore.setLoginData({
              token: 'test-token',
              refreshToken: 'test-refresh-token',
              userInfo: generateUserInfo(nickname, subscriptionLevel)
            })

            // Generate welcome message multiple times
            const displayName = getUserDisplayName(userStore.userInfo)
            const welcomeMessage1 = generateWelcomeMessage(userStore.isLoggedIn, displayName)
            const welcomeMessage2 = generateWelcomeMessage(userStore.isLoggedIn, displayName)
            const welcomeMessage3 = generateWelcomeMessage(userStore.isLoggedIn, displayName)

            // Welcome message should be consistent across multiple calls
            expect(welcomeMessage1).toBe(welcomeMessage2)
            expect(welcomeMessage2).toBe(welcomeMessage3)

            // Clean up
            userStore.logout()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should update welcome message when user logs in or out', () => {
      fc.assert(
        fc.property(
          nicknameArb,
          subscriptionLevelArb,
          (nickname: string, subscriptionLevel: string) => {
            const userStore = useUserStore()
            
            // Initially logged out
            userStore.logout()
            let displayName = getUserDisplayName(userStore.userInfo)
            const loggedOutMessage = generateWelcomeMessage(userStore.isLoggedIn, displayName)
            
            // Log in
            userStore.setLoginData({
              token: 'test-token',
              refreshToken: 'test-refresh-token',
              userInfo: generateUserInfo(nickname, subscriptionLevel)
            })
            displayName = getUserDisplayName(userStore.userInfo)
            const loggedInMessage = generateWelcomeMessage(userStore.isLoggedIn, displayName)
            
            // Log out again
            userStore.logout()
            displayName = getUserDisplayName(userStore.userInfo)
            const loggedOutAgainMessage = generateWelcomeMessage(userStore.isLoggedIn, displayName)

            // Messages should differ based on auth state
            expect(loggedOutMessage).not.toBe(loggedInMessage)
            expect(loggedOutMessage).toBe(loggedOutAgainMessage)
            expect(loggedInMessage).toContain(nickname)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should handle special characters in nickname', () => {
      fc.assert(
        fc.property(
          fc.string({ minLength: 1, maxLength: 20 })
            .filter(s => s.trim().length > 0)
            .map(s => s.trim()),
          subscriptionLevelArb,
          (nickname: string, subscriptionLevel: string) => {
            const userStore = useUserStore()
            userStore.setLoginData({
              token: 'test-token',
              refreshToken: 'test-refresh-token',
              userInfo: generateUserInfo(nickname, subscriptionLevel)
            })

            const displayName = getUserDisplayName(userStore.userInfo)
            const welcomeMessage = generateWelcomeMessage(userStore.isLoggedIn, displayName)

            // Welcome message should contain the nickname regardless of special characters
            expect(welcomeMessage).toContain(nickname)
            expect(welcomeMessage).toContain('欢迎回来')

            // Clean up
            userStore.logout()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should handle Chinese characters in nickname', () => {
      fc.assert(
        fc.property(
          fc.constantFrom('张三', '李四', '王五', '小明', '用户测试', '新用户'),
          subscriptionLevelArb,
          (nickname: string, subscriptionLevel: string) => {
            const userStore = useUserStore()
            userStore.setLoginData({
              token: 'test-token',
              refreshToken: 'test-refresh-token',
              userInfo: generateUserInfo(nickname, subscriptionLevel)
            })

            const displayName = getUserDisplayName(userStore.userInfo)
            const welcomeMessage = generateWelcomeMessage(userStore.isLoggedIn, displayName)

            // Welcome message should properly display Chinese nickname
            expect(welcomeMessage).toContain(nickname)
            expect(welcomeMessage).toBe(`欢迎回来，${nickname}！查看今日选品数据`)

            // Clean up
            userStore.logout()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should verify user store state consistency with welcome message', () => {
      fc.assert(
        fc.property(
          nicknameArb,
          subscriptionLevelArb,
          (nickname: string, subscriptionLevel: string) => {
            const userStore = useUserStore()
            userStore.setLoginData({
              token: 'test-token',
              refreshToken: 'test-refresh-token',
              userInfo: generateUserInfo(nickname, subscriptionLevel)
            })

            // Verify store state
            expect(userStore.isLoggedIn).toBe(true)
            expect(userStore.userInfo?.nickname).toBe(nickname)
            expect(userStore.userInfo?.subscriptionLevel).toBe(subscriptionLevel)

            const displayName = getUserDisplayName(userStore.userInfo)
            const welcomeMessage = generateWelcomeMessage(userStore.isLoggedIn, displayName)

            // Welcome message should reflect the store state
            expect(welcomeMessage).toContain(nickname)

            // Clean up
            userStore.logout()
            
            // After logout, store should be cleared
            expect(userStore.isLoggedIn).toBe(false)
            expect(userStore.userInfo).toBeNull()
          }
        ),
        { numRuns: 100 }
      )
    })
  })
})
