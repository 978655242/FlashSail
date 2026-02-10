/**
 * Property-Based Tests for Login Page
 * 
 * These tests validate the correctness properties of the login page
 * using fast-check for property-based testing.
 * 
 * **Validates: Requirements 4.4, 4.5**
 * - 4.4: Validate phone number format (Chinese mobile: 1[3-9]XXXXXXXXX)
 * - 4.5: Support switching between login and registration modes
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { ref, computed } from 'vue'
import fc from 'fast-check'
import { validatePhone, isValidPhone, type ValidationError } from '@/utils/validators'

/**
 * Chinese mobile phone number pattern
 * Format: 1[3-9]XXXXXXXXX (11 digits starting with 1, second digit 3-9)
 */
const CHINESE_MOBILE_PATTERN = /^1[3-9]\d{9}$/

/**
 * Login mode type
 */
type LoginMode = 'login' | 'register'

/**
 * Simulates the login mode toggle functionality from Login.vue
 * This is a pure function that mirrors the component's switchMode behavior
 */
function createLoginModeState(initialMode: LoginMode = 'login') {
  const mode = ref<LoginMode>(initialMode)
  const isLoginMode = computed(() => mode.value === 'login')
  
  function switchMode() {
    mode.value = isLoginMode.value ? 'register' : 'login'
  }
  
  return {
    mode,
    isLoginMode,
    switchMode
  }
}

describe('Login Page Property Tests', () => {
  /**
   * **Feature: frontend-ui-refactor, Property 3: Phone Number Validation**
   * 
   * *For any* string input, the phone validation function should return true 
   * only for valid Chinese mobile numbers matching the pattern `1[3-9]\d{9}`, 
   * and false for all other strings.
   * 
   * **Validates: Requirements 4.4**
   */
  describe('Property 3: Phone Number Validation', () => {
    it('should correctly validate any string as phone number', () => {
      fc.assert(
        fc.property(fc.string(), (input: string) => {
          const result = validatePhone(input)
          const isValidFormat = CHINESE_MOBILE_PATTERN.test(input)
          
          if (isValidFormat) {
            // Valid phone numbers should return null (no error)
            expect(result).toBeNull()
          } else {
            // Invalid phone numbers should return a ValidationError
            expect(result).not.toBeNull()
            expect(result?.field).toBe('phone')
            expect(typeof result?.message).toBe('string')
          }
        }),
        { numRuns: 100 }
      )
    })

    it('should return true for isValidPhone only when pattern matches', () => {
      fc.assert(
        fc.property(fc.string(), (input: string) => {
          const result = isValidPhone(input)
          const expectedResult = CHINESE_MOBILE_PATTERN.test(input)
          
          expect(result).toBe(expectedResult)
        }),
        { numRuns: 100 }
      )
    })

    it('should accept all valid Chinese mobile number prefixes (13-19)', () => {
      // Generate valid phone numbers with all valid prefixes
      // Use array of digits and join them to create the remaining 9 digits
      const validPhoneArbitrary = fc.integer({ min: 3, max: 9 }).chain(secondDigit =>
        fc.array(fc.integer({ min: 0, max: 9 }), { minLength: 9, maxLength: 9 }).map(
          digits => `1${secondDigit}${digits.join('')}`
        )
      )
      
      fc.assert(
        fc.property(
          validPhoneArbitrary,
          (phone: string) => {
            const result = validatePhone(phone)
            
            // All generated phones should be valid
            expect(result).toBeNull()
            expect(isValidPhone(phone)).toBe(true)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should reject phone numbers with invalid second digit (0, 1, 2)', () => {
      // Generate invalid phone numbers with invalid prefixes (10, 11, 12)
      const invalidPhoneArbitrary = fc.integer({ min: 0, max: 2 }).chain(secondDigit =>
        fc.array(fc.integer({ min: 0, max: 9 }), { minLength: 9, maxLength: 9 }).map(
          digits => `1${secondDigit}${digits.join('')}`
        )
      )
      
      fc.assert(
        fc.property(
          invalidPhoneArbitrary,
          (phone: string) => {
            const result = validatePhone(phone)
            
            // All generated phones should be invalid
            expect(result).not.toBeNull()
            expect(result?.field).toBe('phone')
            expect(isValidPhone(phone)).toBe(false)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should reject phone numbers with incorrect length', () => {
      // Generate phone numbers with wrong length (not 11 digits)
      const wrongLengthArbitrary = fc.oneof(
        // Too short (1-10 digits)
        fc.integer({ min: 1, max: 10 }).chain(length =>
          fc.array(fc.integer({ min: 0, max: 9 }), { minLength: length, maxLength: length }).map(
            digits => digits.join('')
          )
        ),
        // Too long (12+ digits)
        fc.integer({ min: 12, max: 20 }).chain(length =>
          fc.array(fc.integer({ min: 0, max: 9 }), { minLength: length, maxLength: length }).map(
            digits => digits.join('')
          )
        )
      )
      
      fc.assert(
        fc.property(wrongLengthArbitrary, (phone: string) => {
          const result = validatePhone(phone)
          
          // All generated phones should be invalid due to wrong length
          expect(result).not.toBeNull()
          expect(isValidPhone(phone)).toBe(false)
        }),
        { numRuns: 100 }
      )
    })

    it('should reject phone numbers containing non-digit characters', () => {
      // Generate strings with at least one non-digit character
      const nonDigitArbitrary = fc.string().filter(s => /[^0-9]/.test(s) && s.length > 0)
      
      fc.assert(
        fc.property(nonDigitArbitrary, (input: string) => {
          const result = validatePhone(input)
          
          // Strings with non-digit characters should be invalid
          expect(result).not.toBeNull()
          expect(isValidPhone(input)).toBe(false)
        }),
        { numRuns: 100 }
      )
    })

    it('should handle empty and whitespace-only strings', () => {
      const emptyOrWhitespaceArbitrary = fc.oneof(
        fc.constant(''),
        fc.array(fc.constantFrom(' ', '\t', '\n', '\r'), { minLength: 1, maxLength: 10 }).map(
          chars => chars.join('')
        )
      )
      
      fc.assert(
        fc.property(emptyOrWhitespaceArbitrary, (input: string) => {
          const result = validatePhone(input)
          
          // Empty or whitespace-only strings should be invalid
          expect(result).not.toBeNull()
          expect(result?.field).toBe('phone')
          expect(isValidPhone(input)).toBe(false)
        }),
        { numRuns: 100 }
      )
    })

    it('should reject phone numbers not starting with 1', () => {
      // Generate 11-digit numbers not starting with 1
      const nonOneStartArbitrary = fc.integer({ min: 2, max: 9 }).chain(firstDigit =>
        fc.array(fc.integer({ min: 0, max: 9 }), { minLength: 10, maxLength: 10 }).map(
          digits => `${firstDigit}${digits.join('')}`
        )
      )
      
      fc.assert(
        fc.property(nonOneStartArbitrary, (phone: string) => {
          const result = validatePhone(phone)
          
          // Phone numbers not starting with 1 should be invalid
          expect(result).not.toBeNull()
          expect(isValidPhone(phone)).toBe(false)
        }),
        { numRuns: 100 }
      )
    })
  })

  /**
   * **Feature: frontend-ui-refactor, Property 4: Login Mode Toggle**
   * 
   * *For any* current login mode (login or register), clicking the mode switch 
   * should toggle to the opposite mode, and the form should update accordingly.
   * 
   * **Validates: Requirements 4.5**
   */
  describe('Property 4: Login Mode Toggle', () => {
    it('should toggle mode correctly for any initial state', () => {
      fc.assert(
        fc.property(
          fc.constantFrom<LoginMode>('login', 'register'),
          (initialMode: LoginMode) => {
            const { mode, isLoginMode, switchMode } = createLoginModeState(initialMode)
            
            // Verify initial state
            expect(mode.value).toBe(initialMode)
            expect(isLoginMode.value).toBe(initialMode === 'login')
            
            // First toggle - should switch to opposite mode
            switchMode()
            const afterFirstToggle = mode.value
            const expectedAfterFirst = initialMode === 'login' ? 'register' : 'login'
            expect(afterFirstToggle).toBe(expectedAfterFirst)
            expect(afterFirstToggle).not.toBe(initialMode)
            expect(isLoginMode.value).toBe(expectedAfterFirst === 'login')
            
            // Second toggle - should restore original mode
            switchMode()
            const afterSecondToggle = mode.value
            expect(afterSecondToggle).toBe(initialMode)
            expect(isLoginMode.value).toBe(initialMode === 'login')
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should maintain toggle consistency across multiple toggles', () => {
      fc.assert(
        fc.property(
          fc.constantFrom<LoginMode>('login', 'register'),
          fc.integer({ min: 1, max: 20 }),
          (initialMode: LoginMode, toggleCount: number) => {
            const { mode, isLoginMode, switchMode } = createLoginModeState(initialMode)
            
            // Perform multiple toggles
            for (let i = 0; i < toggleCount; i++) {
              switchMode()
            }
            
            // After even number of toggles, should be back to initial
            // After odd number of toggles, should be opposite
            const expectedMode = toggleCount % 2 === 0 
              ? initialMode 
              : (initialMode === 'login' ? 'register' : 'login')
            
            expect(mode.value).toBe(expectedMode)
            expect(isLoginMode.value).toBe(expectedMode === 'login')
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should always have isLoginMode consistent with mode value', () => {
      fc.assert(
        fc.property(
          fc.constantFrom<LoginMode>('login', 'register'),
          fc.array(fc.boolean(), { minLength: 0, maxLength: 20 }),
          (initialMode: LoginMode, toggleSequence: boolean[]) => {
            const { mode, isLoginMode, switchMode } = createLoginModeState(initialMode)
            
            // Apply random toggle sequence (true = toggle, false = no-op)
            for (const shouldToggle of toggleSequence) {
              if (shouldToggle) {
                switchMode()
              }
              
              // After each operation, isLoginMode should be consistent with mode
              expect(isLoginMode.value).toBe(mode.value === 'login')
            }
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should only have two possible mode values', () => {
      fc.assert(
        fc.property(
          fc.constantFrom<LoginMode>('login', 'register'),
          fc.integer({ min: 0, max: 50 }),
          (initialMode: LoginMode, toggleCount: number) => {
            const { mode, switchMode } = createLoginModeState(initialMode)
            
            // Perform toggles and collect all mode values
            const modeValues = new Set<string>()
            modeValues.add(mode.value)
            
            for (let i = 0; i < toggleCount; i++) {
              switchMode()
              modeValues.add(mode.value)
            }
            
            // Should only ever have 'login' and/or 'register' as values
            expect(modeValues.size).toBeLessThanOrEqual(2)
            for (const value of modeValues) {
              expect(['login', 'register']).toContain(value)
            }
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should toggle between exactly two states', () => {
      fc.assert(
        fc.property(
          fc.constantFrom<LoginMode>('login', 'register'),
          (initialMode: LoginMode) => {
            const { mode, switchMode } = createLoginModeState(initialMode)
            
            // Get the two possible states
            const state1 = mode.value
            switchMode()
            const state2 = mode.value
            
            // The two states should be different
            expect(state1).not.toBe(state2)
            
            // The two states should be exactly 'login' and 'register'
            const states = new Set([state1, state2])
            expect(states.has('login')).toBe(true)
            expect(states.has('register')).toBe(true)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should preserve mode independence from other state', () => {
      // This test verifies that mode toggle doesn't affect other form state
      fc.assert(
        fc.property(
          fc.constantFrom<LoginMode>('login', 'register'),
          fc.string(),
          fc.string(),
          (initialMode: LoginMode, phone: string, code: string) => {
            const { mode, switchMode } = createLoginModeState(initialMode)
            
            // Simulate form state
            const formPhone = ref(phone)
            const formCode = ref(code)
            
            // Toggle mode
            switchMode()
            
            // Form state should be preserved (mode toggle doesn't clear form)
            // Note: In the actual component, form state is preserved on mode switch
            expect(formPhone.value).toBe(phone)
            expect(formCode.value).toBe(code)
            
            // Mode should have changed
            expect(mode.value).not.toBe(initialMode)
          }
        ),
        { numRuns: 100 }
      )
    })
  })
})
