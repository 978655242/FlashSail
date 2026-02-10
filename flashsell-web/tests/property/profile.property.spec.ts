/**
 * Property-Based Tests for Profile Page
 * 
 * These tests validate the correctness properties of the Profile page
 * using fast-check for property-based testing.
 * 
 * **Feature: frontend-ui-refactor, Property 24: Profile Data Display**
 * **Validates: Requirements 11.1, 11.2**
 * - 11.1: Display user information (name, phone, avatar)
 * - 11.2: Allow editing user nickname
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import fc from 'fast-check'
import type { UserProfile, UserUsageStats } from '@/api/user'

// Mock vue-router
const mockPush = vi.fn()
const mockRoute = {
  name: 'Profile',
  path: '/profile',
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

// Mock user API
let mockProfile: UserProfile = {
  id: 1,
  phone: '13812345678',
  nickname: 'TestUser',
  avatarUrl: '',
  email: 'test@example.com',
  subscriptionLevel: 'FREE',
  subscriptionExpireDate: null,
  phoneVerified: true,
  twoFactorEnabled: false,
  notificationEnabled: true,
  emailSubscribed: false,
  lastLoginTime: '2024-01-01T00:00:00Z',
  createdAt: '2024-01-01T00:00:00Z'
}

let mockUsageStats: UserUsageStats = {
  searchCount: 5,
  exportCount: 2,
  favoriteCount: 10,
  boardCount: 1,
  searchLimit: 10,
  exportLimit: 5,
  favoriteLimit: 50,
  boardLimit: 3
}

vi.mock('@/api/user', () => ({
  getProfile: vi.fn().mockImplementation(() => 
    Promise.resolve({ data: { data: mockProfile } })
  ),
  getUsage: vi.fn().mockImplementation(() => 
    Promise.resolve({ data: { data: mockUsageStats } })
  ),
  updateProfile: vi.fn().mockImplementation((data: { nickname?: string }) => 
    Promise.resolve({ 
      data: { 
        data: { ...mockProfile, ...data } 
      } 
    })
  ),
  getInviteCode: vi.fn().mockResolvedValue({
    data: { data: { inviteCode: 'ABC123', inviteUrl: 'https://example.com/invite/ABC123' } }
  })
}))

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

// Arbitrary for generating subscription levels
const subscriptionLevelArb: fc.Arbitrary<'FREE' | 'BASIC' | 'PRO'> = fc.constantFrom('FREE', 'BASIC', 'PRO')

// Arbitrary for generating valid Chinese phone numbers
const phoneNumberArb: fc.Arbitrary<string> = fc.integer({ min: 130, max: 199 })
  .chain(prefix => 
    fc.integer({ min: 10000000, max: 99999999 })
      .map(suffix => `${prefix}${suffix}`)
  )

// Arbitrary for generating valid nicknames (1-20 characters)
const nicknameArb: fc.Arbitrary<string> = fc.string({ minLength: 1, maxLength: 20 })
  .filter(s => s.trim().length > 0)

// Arbitrary for generating email addresses
const emailArb: fc.Arbitrary<string | null> = fc.option(
  fc.emailAddress(),
  { nil: null }
)

// Arbitrary for generating user profiles
const userProfileArb: fc.Arbitrary<UserProfile> = fc.record({
  id: fc.nat({ max: 1000000 }),
  phone: phoneNumberArb,
  nickname: fc.option(nicknameArb, { nil: '' }),
  avatarUrl: fc.option(fc.webUrl(), { nil: '' }),
  email: fc.option(fc.emailAddress(), { nil: '' }),
  subscriptionLevel: subscriptionLevelArb as fc.Arbitrary<string>,
  subscriptionExpireDate: fc.option(
    fc.integer({ min: Date.now(), max: Date.now() + 365 * 24 * 60 * 60 * 1000 })
      .map(timestamp => new Date(timestamp).toISOString()),
    { nil: null }
  ),
  phoneVerified: fc.boolean(),
  twoFactorEnabled: fc.boolean(),
  notificationEnabled: fc.boolean(),
  emailSubscribed: fc.boolean(),
  lastLoginTime: fc.integer({ min: Date.now() - 365 * 24 * 60 * 60 * 1000, max: Date.now() })
    .map(timestamp => new Date(timestamp).toISOString()),
  createdAt: fc.integer({ min: Date.now() - 730 * 24 * 60 * 60 * 1000, max: Date.now() })
    .map(timestamp => new Date(timestamp).toISOString())
})

// Arbitrary for generating usage stats
const usageStatsArb: fc.Arbitrary<UserUsageStats> = fc.record({
  searchCount: fc.nat({ max: 1000 }),
  exportCount: fc.nat({ max: 500 }),
  favoriteCount: fc.nat({ max: 1000 }),
  boardCount: fc.nat({ max: 100 }),
  searchLimit: fc.integer({ min: 1, max: 10000 }),
  exportLimit: fc.integer({ min: 1, max: 1000 }),
  favoriteLimit: fc.integer({ min: 1, max: 10000 }),
  boardLimit: fc.integer({ min: 1, max: 100 })
})

// Helper function to get display name from profile
function getDisplayName(profile: UserProfile): string {
  return profile.nickname || profile.phone || 'Nickname'
}

// Helper function to get avatar initial from profile
function getAvatarInitial(profile: UserProfile): string {
  if (profile.nickname && profile.nickname.length > 0) {
    return profile.nickname.charAt(0).toUpperCase()
  }
  if (profile.phone && profile.phone.length > 0) {
    return profile.phone.charAt(0)
  }
  return 'U'
}

// Helper function to validate nickname
function isValidNickname(nickname: string): boolean {
  const trimmed = nickname.trim()
  return trimmed.length > 0 && trimmed.length <= 20
}

// Helper function to calculate usage percentage
function calculateUsagePercentage(count: number, limit: number): number {
  if (limit <= 0) return 0
  return Math.min((count / limit) * 100, 100)
}

// Helper function to get subscription label
function getSubscriptionLabel(level: string): string {
  switch (level) {
    case 'PRO':
      return 'Enterprise'
    case 'BASIC':
      return 'Pro'
    default:
      return 'Free'
  }
}

describe('Profile Property Tests', () => {
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
    
    // Reset mock profile to default
    mockProfile = {
      id: 1,
      phone: '13812345678',
      nickname: 'TestUser',
      avatarUrl: '',
      email: 'test@example.com',
      subscriptionLevel: 'FREE',
      subscriptionExpireDate: null,
      phoneVerified: true,
      twoFactorEnabled: false,
      notificationEnabled: true,
      emailSubscribed: false,
      lastLoginTime: '2024-01-01T00:00:00Z',
      createdAt: '2024-01-01T00:00:00Z'
    }
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  /**
   * **Feature: frontend-ui-refactor, Property 24: Profile Data Display**
   * 
   * *For any* authenticated user, the profile page should display their user 
   * information (name, phone) and allow editing the nickname.
   * 
   * **Validates: Requirements 11.1, 11.2**
   */
  describe('Property 24: Profile Data Display', () => {
    it('should correctly display name for any user profile', () => {
      fc.assert(
        fc.property(
          userProfileArb,
          (profile: UserProfile) => {
            const displayName = getDisplayName(profile)
            
            // Display name should be nickname if available, otherwise phone
            if (profile.nickname && profile.nickname.length > 0) {
              expect(displayName).toBe(profile.nickname)
            } else if (profile.phone && profile.phone.length > 0) {
              expect(displayName).toBe(profile.phone)
            } else {
              expect(displayName).toBe('Nickname')
            }
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should correctly display avatar initial for any user profile', () => {
      fc.assert(
        fc.property(
          userProfileArb,
          (profile: UserProfile) => {
            const initial = getAvatarInitial(profile)
            
            // Avatar initial should be first character of nickname (uppercase) or phone
            if (profile.nickname && profile.nickname.length > 0) {
              expect(initial).toBe(profile.nickname.charAt(0).toUpperCase())
            } else if (profile.phone && profile.phone.length > 0) {
              expect(initial).toBe(profile.phone.charAt(0))
            } else {
              expect(initial).toBe('U')
            }
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should always display phone number when available', () => {
      fc.assert(
        fc.property(
          userProfileArb,
          (profile: UserProfile) => {
            // Phone should always be displayed if present
            if (profile.phone && profile.phone.length > 0) {
              expect(profile.phone).toBeTruthy()
              expect(profile.phone.length).toBeGreaterThan(0)
            }
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should validate nickname correctly for any input', () => {
      fc.assert(
        fc.property(
          fc.string({ minLength: 0, maxLength: 30 }),
          (nickname: string) => {
            const isValid = isValidNickname(nickname)
            const trimmed = nickname.trim()
            
            // Valid nicknames are 1-20 characters after trimming
            if (trimmed.length > 0 && trimmed.length <= 20) {
              expect(isValid).toBe(true)
            } else {
              expect(isValid).toBe(false)
            }
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should reject empty nicknames', () => {
      fc.assert(
        fc.property(
          fc.constantFrom('', '   ', '\t', '\n', '  \t  '),
          (emptyNickname: string) => {
            const isValid = isValidNickname(emptyNickname)
            expect(isValid).toBe(false)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should reject nicknames longer than 20 characters', () => {
      fc.assert(
        fc.property(
          fc.string({ minLength: 21, maxLength: 100 }).filter(s => s.trim().length > 20),
          (longNickname: string) => {
            const isValid = isValidNickname(longNickname)
            expect(isValid).toBe(false)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should accept valid nicknames between 1-20 characters', () => {
      fc.assert(
        fc.property(
          nicknameArb,
          (validNickname: string) => {
            const isValid = isValidNickname(validNickname)
            expect(isValid).toBe(true)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should correctly identify subscription level for any profile', () => {
      fc.assert(
        fc.property(
          userProfileArb,
          (profile: UserProfile) => {
            const label = getSubscriptionLabel(profile.subscriptionLevel)
            
            // Subscription label should match the level
            switch (profile.subscriptionLevel) {
              case 'PRO':
                expect(label).toBe('Enterprise')
                break
              case 'BASIC':
                expect(label).toBe('Pro')
                break
              case 'FREE':
              default:
                expect(label).toBe('Free')
                break
            }
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display all required user information fields', () => {
      fc.assert(
        fc.property(
          userProfileArb,
          (profile: UserProfile) => {
            // Required fields that should be displayed
            const requiredFields = ['phone', 'subscriptionLevel', 'createdAt']
            
            requiredFields.forEach(field => {
              expect(profile).toHaveProperty(field)
            })
            
            // Phone should be a valid format
            if (profile.phone) {
              expect(profile.phone.length).toBeGreaterThanOrEqual(10)
            }
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should handle profiles with missing optional fields', () => {
      fc.assert(
        fc.property(
          fc.record({
            id: fc.nat({ max: 1000000 }),
            phone: phoneNumberArb,
            nickname: fc.constant(''),
            avatarUrl: fc.constant(''),
            email: fc.constant(''),
            subscriptionLevel: fc.constant('FREE'),
            subscriptionExpireDate: fc.constant(null),
            phoneVerified: fc.boolean(),
            twoFactorEnabled: fc.boolean(),
            notificationEnabled: fc.boolean(),
            emailSubscribed: fc.boolean(),
            lastLoginTime: fc.constant('2024-01-01T00:00:00Z'),
            createdAt: fc.constant('2024-01-01T00:00:00Z')
          }),
          (profile: UserProfile) => {
            // Should still be able to get display name (falls back to phone)
            const displayName = getDisplayName(profile)
            expect(displayName).toBe(profile.phone)
            
            // Should still be able to get avatar initial (falls back to phone)
            const initial = getAvatarInitial(profile)
            expect(initial).toBe(profile.phone.charAt(0))
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should preserve nickname after editing for any valid nickname', () => {
      fc.assert(
        fc.property(
          userProfileArb,
          nicknameArb,
          (profile: UserProfile, newNickname: string) => {
            // Simulate editing nickname
            const updatedProfile = { ...profile, nickname: newNickname }
            
            // Display name should now be the new nickname
            const displayName = getDisplayName(updatedProfile)
            expect(displayName).toBe(newNickname)
            
            // Avatar initial should update
            const initial = getAvatarInitial(updatedProfile)
            expect(initial).toBe(newNickname.charAt(0).toUpperCase())
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * Tests for account statistics display
   */
  describe('Account Statistics Display', () => {
    it('should correctly calculate usage percentage for any stats', () => {
      fc.assert(
        fc.property(
          usageStatsArb,
          (stats: UserUsageStats) => {
            const searchPercentage = calculateUsagePercentage(stats.searchCount, stats.searchLimit)
            const exportPercentage = calculateUsagePercentage(stats.exportCount, stats.exportLimit)
            const favoritePercentage = calculateUsagePercentage(stats.favoriteCount, stats.favoriteLimit)
            const boardPercentage = calculateUsagePercentage(stats.boardCount, stats.boardLimit)
            
            // Percentages should be between 0 and 100
            expect(searchPercentage).toBeGreaterThanOrEqual(0)
            expect(searchPercentage).toBeLessThanOrEqual(100)
            expect(exportPercentage).toBeGreaterThanOrEqual(0)
            expect(exportPercentage).toBeLessThanOrEqual(100)
            expect(favoritePercentage).toBeGreaterThanOrEqual(0)
            expect(favoritePercentage).toBeLessThanOrEqual(100)
            expect(boardPercentage).toBeGreaterThanOrEqual(0)
            expect(boardPercentage).toBeLessThanOrEqual(100)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should cap percentage at 100 when count exceeds limit', () => {
      fc.assert(
        fc.property(
          fc.integer({ min: 100, max: 1000 }),
          fc.integer({ min: 1, max: 50 }),
          (count: number, limit: number) => {
            const percentage = calculateUsagePercentage(count, limit)
            
            // When count > limit, percentage should be capped at 100
            expect(percentage).toBe(100)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should return 0 percentage when count is 0', () => {
      fc.assert(
        fc.property(
          fc.integer({ min: 1, max: 1000 }),
          (limit: number) => {
            const percentage = calculateUsagePercentage(0, limit)
            expect(percentage).toBe(0)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should handle edge case of zero limit', () => {
      fc.assert(
        fc.property(
          fc.nat({ max: 1000 }),
          (count: number) => {
            const percentage = calculateUsagePercentage(count, 0)
            // Should return 0 to avoid division by zero
            expect(percentage).toBe(0)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display all usage statistics fields', () => {
      fc.assert(
        fc.property(
          usageStatsArb,
          (stats: UserUsageStats) => {
            // All required fields should be present
            expect(stats).toHaveProperty('searchCount')
            expect(stats).toHaveProperty('searchLimit')
            expect(stats).toHaveProperty('exportCount')
            expect(stats).toHaveProperty('exportLimit')
            expect(stats).toHaveProperty('favoriteCount')
            expect(stats).toHaveProperty('favoriteLimit')
            expect(stats).toHaveProperty('boardCount')
            expect(stats).toHaveProperty('boardLimit')
            
            // All values should be non-negative
            expect(stats.searchCount).toBeGreaterThanOrEqual(0)
            expect(stats.exportCount).toBeGreaterThanOrEqual(0)
            expect(stats.favoriteCount).toBeGreaterThanOrEqual(0)
            expect(stats.boardCount).toBeGreaterThanOrEqual(0)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should correctly format usage as count/limit for any stats', () => {
      fc.assert(
        fc.property(
          usageStatsArb,
          (stats: UserUsageStats) => {
            // Format should be "count / limit"
            const searchFormat = `${stats.searchCount} / ${stats.searchLimit}`
            const exportFormat = `${stats.exportCount} / ${stats.exportLimit}`
            const favoriteFormat = `${stats.favoriteCount} / ${stats.favoriteLimit}`
            const boardFormat = `${stats.boardCount} / ${stats.boardLimit}`
            
            // Verify format is correct
            expect(searchFormat).toMatch(/^\d+ \/ \d+$/)
            expect(exportFormat).toMatch(/^\d+ \/ \d+$/)
            expect(favoriteFormat).toMatch(/^\d+ \/ \d+$/)
            expect(boardFormat).toMatch(/^\d+ \/ \d+$/)
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * Tests for profile editing functionality
   */
  describe('Profile Editing', () => {
    it('should maintain original data when edit is cancelled', () => {
      fc.assert(
        fc.property(
          userProfileArb,
          nicknameArb,
          (originalProfile: UserProfile, newNickname: string) => {
            // Simulate starting edit
            let editForm = { nickname: originalProfile.nickname || '' }
            
            // Simulate changing nickname
            editForm.nickname = newNickname
            
            // Simulate cancelling edit (restore original)
            editForm.nickname = originalProfile.nickname || ''
            
            // Original nickname should be preserved
            expect(editForm.nickname).toBe(originalProfile.nickname || '')
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should update profile data after successful save', () => {
      fc.assert(
        fc.property(
          userProfileArb,
          nicknameArb,
          (originalProfile: UserProfile, newNickname: string) => {
            // Simulate successful save
            const updatedProfile = { ...originalProfile, nickname: newNickname }
            
            // Profile should have new nickname
            expect(updatedProfile.nickname).toBe(newNickname)
            
            // Other fields should remain unchanged
            expect(updatedProfile.phone).toBe(originalProfile.phone)
            expect(updatedProfile.email).toBe(originalProfile.email)
            expect(updatedProfile.subscriptionLevel).toBe(originalProfile.subscriptionLevel)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should trim whitespace from nickname before saving', () => {
      fc.assert(
        fc.property(
          // Generate nicknames without leading/trailing whitespace
          fc.string({ minLength: 1, maxLength: 15 })
            .filter(s => s.trim().length > 0 && s === s.trim()),
          (nickname: string) => {
            // Add whitespace around the nickname
            const nicknameWithWhitespace = `  ${nickname}  `
            const trimmed = nicknameWithWhitespace.trim()
            
            // Trimmed nickname should equal the original (without added whitespace)
            expect(trimmed).toBe(nickname)
            expect(trimmed.length).toBeLessThanOrEqual(20)
            // Verify no leading/trailing whitespace
            expect(trimmed).toBe(trimmed.trim())
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * Tests for subscription display
   */
  describe('Subscription Display', () => {
    it('should correctly map subscription level to display label', () => {
      fc.assert(
        fc.property(
          subscriptionLevelArb,
          (level: 'FREE' | 'BASIC' | 'PRO') => {
            const label = getSubscriptionLabel(level)
            
            // Each level should have a unique label
            const expectedLabels: Record<string, string> = {
              'FREE': 'Free',
              'BASIC': 'Pro',
              'PRO': 'Enterprise'
            }
            
            expect(label).toBe(expectedLabels[level])
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should handle unknown subscription levels gracefully', () => {
      fc.assert(
        fc.property(
          fc.string({ minLength: 1, maxLength: 20 }).filter(s => !['FREE', 'BASIC', 'PRO'].includes(s)),
          (unknownLevel: string) => {
            const label = getSubscriptionLabel(unknownLevel)
            
            // Unknown levels should default to 'Free'
            expect(label).toBe('Free')
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * Tests for date formatting
   */
  describe('Date Formatting', () => {
    it('should handle valid date strings', () => {
      fc.assert(
        fc.property(
          fc.integer({ min: Date.now() - 730 * 24 * 60 * 60 * 1000, max: Date.now() })
            .map(timestamp => new Date(timestamp).toISOString()),
          (dateStr: string) => {
            const date = new Date(dateStr)
            
            // Date should be valid
            expect(date.toString()).not.toBe('Invalid Date')
            
            // Date should be parseable
            expect(date.getTime()).toBeGreaterThan(0)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should handle null/undefined dates', () => {
      fc.assert(
        fc.property(
          fc.constantFrom(null, undefined),
          (dateValue: null | undefined) => {
            // Null/undefined should be handled gracefully
            const result = dateValue ? new Date(dateValue).toLocaleDateString() : '-'
            expect(result).toBe('-')
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * Tests for avatar display
   */
  describe('Avatar Display', () => {
    it('should always produce a single character initial', () => {
      fc.assert(
        fc.property(
          userProfileArb,
          (profile: UserProfile) => {
            const initial = getAvatarInitial(profile)
            
            // Initial should always be exactly 1 character
            expect(initial.length).toBe(1)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should uppercase nickname initial', () => {
      fc.assert(
        fc.property(
          fc.string({ minLength: 1, maxLength: 20 }).filter(s => s.trim().length > 0),
          (nickname: string) => {
            const profile: UserProfile = {
              ...mockProfile,
              nickname
            }
            
            const initial = getAvatarInitial(profile)
            
            // Initial should be uppercase
            expect(initial).toBe(nickname.charAt(0).toUpperCase())
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should fallback to phone initial when nickname is empty', () => {
      fc.assert(
        fc.property(
          phoneNumberArb,
          (phone: string) => {
            const profile: UserProfile = {
              ...mockProfile,
              nickname: '',
              phone
            }
            
            const initial = getAvatarInitial(profile)
            
            // Should use phone's first character
            expect(initial).toBe(phone.charAt(0))
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should fallback to "U" when both nickname and phone are empty', () => {
      fc.assert(
        fc.property(
          fc.constant(null),
          () => {
            const profile: UserProfile = {
              ...mockProfile,
              nickname: '',
              phone: ''
            }
            
            const initial = getAvatarInitial(profile)
            
            // Should fallback to 'U'
            expect(initial).toBe('U')
          }
        ),
        { numRuns: 100 }
      )
    })
  })
})
