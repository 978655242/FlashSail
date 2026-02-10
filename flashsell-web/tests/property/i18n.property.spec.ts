/**
 * Property-Based Tests for i18n System
 * 
 * These tests validate the correctness properties of the internationalization system
 * using fast-check for property-based testing.
 * 
 * **Validates: Requirements 13.4, 13.5**
 * - 13.4: Persist language preference in localStorage
 * - 13.5: Update all UI text immediately when language is changed
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import fc from 'fast-check'
import { useI18n, resetI18nState, LOCALE_STORAGE_KEY, DEFAULT_LOCALE, type Locale } from '@/composables/useI18n'
import { useI18nStore } from '@/stores/i18n'
import { zhMessages } from '@/locales/zh'
import { enMessages } from '@/locales/en'

// Create a proper localStorage mock that persists data
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

/**
 * Helper function to initialize the i18n store with translation messages.
 */
function initializeI18nWithMessages() {
  const store = useI18nStore()
  store.setMessages('zh', zhMessages)
  store.setMessages('en', enMessages)
  return store
}

/**
 * Helper function to get all translation keys from a messages object.
 * Returns flat keys using dot notation (e.g., 'nav.dashboard').
 */
function getAllTranslationKeys(messages: Record<string, unknown>, prefix = ''): string[] {
  const keys: string[] = []
  
  for (const [key, value] of Object.entries(messages)) {
    const fullKey = prefix ? `${prefix}.${key}` : key
    
    if (typeof value === 'string') {
      keys.push(fullKey)
    } else if (typeof value === 'object' && value !== null) {
      keys.push(...getAllTranslationKeys(value as Record<string, unknown>, fullKey))
    }
  }
  
  return keys
}

/**
 * Helper function to get a sample of translation keys for testing.
 * Returns a subset of keys to keep tests efficient.
 */
function getSampleTranslationKeys(): string[] {
  // Return a representative sample of translation keys from different sections
  return [
    'nav.dashboard',
    'nav.search',
    'nav.favorites',
    'dashboard.welcome',
    'dashboard.aiRecommendations',
    'search.title',
    'search.placeholder',
    'product.price',
    'product.addToFavorites',
    'favorites.title',
    'favorites.empty',
    'common.loading',
    'common.error',
    'common.retry',
    'common.cancel',
    'common.confirm',
    'language.zh',
    'language.en',
    'theme.dark',
    'theme.light'
  ]
}

describe('i18n System Property Tests', () => {
  let localStorageMock: ReturnType<typeof createLocalStorageMock>

  beforeEach(() => {
    // Create a fresh Pinia instance for each test
    setActivePinia(createPinia())
    
    // Create fresh localStorage mock
    localStorageMock = createLocalStorageMock()
    
    // Setup global mocks
    vi.stubGlobal('localStorage', localStorageMock)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  /**
   * **Feature: frontend-ui-refactor, Property 14: Language Persistence**
   * 
   * *For any* language selection (zh or en), storing the preference in 
   * localStorage and reloading should restore the same language.
   * 
   * **Validates: Requirements 13.4**
   */
  describe('Property 14: Language Persistence', () => {
    it('should persist and restore language preference after setLocale', () => {
      fc.assert(
        fc.property(
          fc.constantFrom<Locale>('zh', 'en'),
          (targetLocale: Locale) => {
            // Create fresh Pinia for each iteration
            setActivePinia(createPinia())
            localStorageMock.clear()
            
            // Initialize i18n with messages
            initializeI18nWithMessages()
            
            // Get the composable
            const { locale, setLocale } = useI18n()
            
            // Set the locale
            setLocale(targetLocale)
            
            // Verify the locale was set
            expect(locale.value).toBe(targetLocale)
            
            // Verify the locale was persisted to localStorage
            const storedLocale = localStorageMock.getItem(LOCALE_STORAGE_KEY)
            expect(storedLocale).toBe(targetLocale)
            
            // Simulate "reload" by creating a new Pinia instance
            setActivePinia(createPinia())
            
            // Re-initialize i18n with messages
            initializeI18nWithMessages()
            
            // Get the composable again (simulating app reload)
            const { locale: restoredLocale } = useI18n()
            
            // Verify the locale was restored from localStorage
            expect(restoredLocale.value).toBe(targetLocale)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should persist and restore language preference after toggleLocale', () => {
      fc.assert(
        fc.property(
          fc.constantFrom<Locale>('zh', 'en'),
          fc.integer({ min: 1, max: 10 }),
          (initialLocale: Locale, toggleCount: number) => {
            // Create fresh Pinia for each iteration
            setActivePinia(createPinia())
            localStorageMock.clear()
            
            // Pre-set locale in localStorage
            localStorageMock.setItem(LOCALE_STORAGE_KEY, initialLocale)
            
            // Initialize i18n with messages
            initializeI18nWithMessages()
            
            // Get the composable
            const { locale, toggleLocale } = useI18n()
            
            // Verify initial locale was restored
            expect(locale.value).toBe(initialLocale)
            
            // Perform toggles
            for (let i = 0; i < toggleCount; i++) {
              toggleLocale()
            }
            
            // Calculate expected final locale
            const expectedLocale: Locale = toggleCount % 2 === 0 
              ? initialLocale 
              : (initialLocale === 'zh' ? 'en' : 'zh')
            
            // Verify the final locale
            expect(locale.value).toBe(expectedLocale)
            
            // Verify persistence
            const storedLocale = localStorageMock.getItem(LOCALE_STORAGE_KEY)
            expect(storedLocale).toBe(expectedLocale)
            
            // Simulate "reload"
            setActivePinia(createPinia())
            initializeI18nWithMessages()
            
            // Verify restoration
            const { locale: restoredLocale } = useI18n()
            expect(restoredLocale.value).toBe(expectedLocale)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should restore locale from localStorage on initialization', () => {
      fc.assert(
        fc.property(
          fc.constantFrom<Locale>('zh', 'en'),
          (storedLocale: Locale) => {
            // Create fresh Pinia for each iteration
            setActivePinia(createPinia())
            localStorageMock.clear()
            
            // Pre-set locale in localStorage (simulating previous session)
            localStorageMock.setItem(LOCALE_STORAGE_KEY, storedLocale)
            
            // Initialize i18n with messages
            initializeI18nWithMessages()
            
            // Get the composable
            const { locale } = useI18n()
            
            // Verify locale was restored from localStorage
            expect(locale.value).toBe(storedLocale)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should default to Chinese (zh) when no preference is stored', () => {
      fc.assert(
        fc.property(
          fc.constant(null), // No stored preference
          () => {
            // Create fresh Pinia for each iteration
            setActivePinia(createPinia())
            localStorageMock.clear()
            
            // Initialize i18n with messages (no localStorage value)
            initializeI18nWithMessages()
            
            // Get the composable
            const { locale } = useI18n()
            
            // Should default to Chinese (zh) as per requirement 13.1
            expect(locale.value).toBe(DEFAULT_LOCALE)
            expect(locale.value).toBe('zh')
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should persist locale correctly through a sequence of operations', () => {
      fc.assert(
        fc.property(
          fc.constantFrom<Locale>('zh', 'en'),
          fc.array(
            fc.oneof(
              fc.constant('toggle' as const),
              fc.constantFrom<Locale>('zh', 'en').map(l => ({ setTo: l }))
            ),
            { minLength: 1, maxLength: 10 }
          ),
          (initialLocale: Locale, operations) => {
            // Create fresh Pinia for each iteration
            setActivePinia(createPinia())
            localStorageMock.clear()
            
            // Pre-set locale in localStorage
            localStorageMock.setItem(LOCALE_STORAGE_KEY, initialLocale)
            
            // Initialize i18n with messages
            initializeI18nWithMessages()
            
            // Get the composable
            const { locale, toggleLocale, setLocale } = useI18n()
            
            // Apply operations
            for (const op of operations) {
              if (op === 'toggle') {
                toggleLocale()
              } else {
                setLocale(op.setTo)
              }
            }
            
            const finalLocale = locale.value
            
            // Verify persistence
            const storedLocale = localStorageMock.getItem(LOCALE_STORAGE_KEY)
            expect(storedLocale).toBe(finalLocale)
            
            // Simulate "reload"
            setActivePinia(createPinia())
            initializeI18nWithMessages()
            
            // Verify restoration
            const { locale: restoredLocale } = useI18n()
            expect(restoredLocale.value).toBe(finalLocale)
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * **Feature: frontend-ui-refactor, Property 15: Language Switch Reactivity**
   * 
   * *For any* language change, all translatable text in the current view 
   * should update immediately to reflect the new language.
   * 
   * **Validates: Requirements 13.5**
   */
  describe('Property 15: Language Switch Reactivity', () => {
    it('should return correct translations for any locale', () => {
      fc.assert(
        fc.property(
          fc.constantFrom<Locale>('zh', 'en'),
          fc.constantFrom(...getSampleTranslationKeys()),
          (targetLocale: Locale, translationKey: string) => {
            // Create fresh Pinia for each iteration
            setActivePinia(createPinia())
            localStorageMock.clear()
            
            // Initialize i18n with messages
            initializeI18nWithMessages()
            
            // Get the composable
            const { t, setLocale } = useI18n()
            
            // Set the locale
            setLocale(targetLocale)
            
            // Get the translation
            const translation = t(translationKey)
            
            // Get the expected translation from the messages
            const messages = targetLocale === 'zh' ? zhMessages : enMessages
            const keys = translationKey.split('.')
            let expected: unknown = messages
            
            for (const key of keys) {
              if (expected && typeof expected === 'object' && key in expected) {
                expected = (expected as Record<string, unknown>)[key]
              } else {
                expected = translationKey // Fallback to key if not found
                break
              }
            }
            
            // Verify the translation matches the expected value
            if (typeof expected === 'string') {
              expect(translation).toBe(expected)
            } else {
              // If the key doesn't exist, it should return the key itself
              expect(translation).toBe(translationKey)
            }
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should update translations immediately when locale changes', () => {
      fc.assert(
        fc.property(
          fc.constantFrom<Locale>('zh', 'en'),
          fc.constantFrom(...getSampleTranslationKeys()),
          (initialLocale: Locale, translationKey: string) => {
            // Create fresh Pinia for each iteration
            setActivePinia(createPinia())
            localStorageMock.clear()
            
            // Pre-set locale in localStorage
            localStorageMock.setItem(LOCALE_STORAGE_KEY, initialLocale)
            
            // Initialize i18n with messages
            initializeI18nWithMessages()
            
            // Get the composable
            const { t, toggleLocale, locale } = useI18n()
            
            // Get translation in initial locale
            const initialTranslation = t(translationKey)
            
            // Toggle locale
            toggleLocale()
            
            // Get translation in new locale
            const newTranslation = t(translationKey)
            
            // Determine expected translations
            const zhMessages_ = zhMessages
            const enMessages_ = enMessages
            
            // Get expected values from both locales
            const getExpectedTranslation = (messages: typeof zhMessages, key: string): string => {
              const keys = key.split('.')
              let result: unknown = messages
              
              for (const k of keys) {
                if (result && typeof result === 'object' && k in result) {
                  result = (result as Record<string, unknown>)[k]
                } else {
                  return key // Fallback to key
                }
              }
              
              return typeof result === 'string' ? result : key
            }
            
            const expectedInitial = getExpectedTranslation(
              initialLocale === 'zh' ? zhMessages_ : enMessages_,
              translationKey
            )
            const expectedNew = getExpectedTranslation(
              locale.value === 'zh' ? zhMessages_ : enMessages_,
              translationKey
            )
            
            // Verify translations match expected values
            expect(initialTranslation).toBe(expectedInitial)
            expect(newTranslation).toBe(expectedNew)
            
            // If translations exist in both locales and are different,
            // verify they actually changed
            if (expectedInitial !== translationKey && expectedNew !== translationKey) {
              // The translations should be different for different locales
              // (unless they happen to be the same, like 'eBay')
              const zhValue = getExpectedTranslation(zhMessages_, translationKey)
              const enValue = getExpectedTranslation(enMessages_, translationKey)
              
              if (zhValue !== enValue) {
                expect(initialTranslation).not.toBe(newTranslation)
              }
            }
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should return different translations for zh and en locales', () => {
      fc.assert(
        fc.property(
          // Use keys that are known to have different translations
          fc.constantFrom(
            'nav.dashboard',
            'nav.search',
            'dashboard.welcome',
            'search.title',
            'common.loading',
            'common.error',
            'common.retry'
          ),
          (translationKey: string) => {
            // Create fresh Pinia for each iteration
            setActivePinia(createPinia())
            localStorageMock.clear()
            
            // Initialize i18n with messages
            initializeI18nWithMessages()
            
            // Get the composable
            const { t, setLocale } = useI18n()
            
            // Get translation in Chinese
            setLocale('zh')
            const zhTranslation = t(translationKey)
            
            // Get translation in English
            setLocale('en')
            const enTranslation = t(translationKey)
            
            // Both translations should exist (not be the key itself)
            expect(zhTranslation).not.toBe(translationKey)
            expect(enTranslation).not.toBe(translationKey)
            
            // Translations should be different for different locales
            expect(zhTranslation).not.toBe(enTranslation)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should update isZh and isEn computed properties when locale changes', () => {
      fc.assert(
        fc.property(
          fc.constantFrom<Locale>('zh', 'en'),
          fc.integer({ min: 1, max: 10 }),
          (initialLocale: Locale, toggleCount: number) => {
            // Create fresh Pinia for each iteration
            setActivePinia(createPinia())
            localStorageMock.clear()
            
            // Pre-set locale in localStorage
            localStorageMock.setItem(LOCALE_STORAGE_KEY, initialLocale)
            
            // Initialize i18n with messages
            initializeI18nWithMessages()
            
            // Get the composable
            const { locale, isZh, isEn, toggleLocale } = useI18n()
            
            // Verify initial computed properties
            expect(isZh.value).toBe(initialLocale === 'zh')
            expect(isEn.value).toBe(initialLocale === 'en')
            
            // Perform toggles and verify computed properties update
            for (let i = 0; i < toggleCount; i++) {
              toggleLocale()
              
              // Computed properties should always reflect current locale
              expect(isZh.value).toBe(locale.value === 'zh')
              expect(isEn.value).toBe(locale.value === 'en')
              
              // isZh and isEn should be mutually exclusive
              expect(isZh.value).not.toBe(isEn.value)
            }
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should return fallback value when translation key does not exist', () => {
      fc.assert(
        fc.property(
          fc.constantFrom<Locale>('zh', 'en'),
          fc.string({ minLength: 5, maxLength: 20 }).filter(s => !s.includes('.') && /^[a-z]+$/i.test(s)),
          fc.string({ minLength: 1, maxLength: 30 }),
          (targetLocale: Locale, nonExistentKey: string, fallbackValue: string) => {
            // Create fresh Pinia for each iteration
            setActivePinia(createPinia())
            localStorageMock.clear()
            
            // Initialize i18n with messages
            initializeI18nWithMessages()
            
            // Get the composable
            const { t, setLocale } = useI18n()
            
            // Set the locale
            setLocale(targetLocale)
            
            // Create a key that definitely doesn't exist
            const definitelyNonExistentKey = `nonexistent_${nonExistentKey}_key`
            
            // Without fallback, should return the key itself
            const withoutFallback = t(definitelyNonExistentKey)
            expect(withoutFallback).toBe(definitelyNonExistentKey)
            
            // With fallback, should return the fallback value
            const withFallback = t(definitelyNonExistentKey, fallbackValue)
            expect(withFallback).toBe(fallbackValue)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should handle nested translation keys correctly', () => {
      fc.assert(
        fc.property(
          fc.constantFrom<Locale>('zh', 'en'),
          fc.constantFrom(
            'nav.dashboard',
            'nav.search',
            'search.suggestions.hotProducts',
            'search.thinking.analyzing',
            'product.recommendation.buy',
            'product.metrics.priceCompetitiveness',
            'subscription.plans.free.name',
            'analysis.timeRange.week'
          ),
          (targetLocale: Locale, nestedKey: string) => {
            // Create fresh Pinia for each iteration
            setActivePinia(createPinia())
            localStorageMock.clear()
            
            // Initialize i18n with messages
            initializeI18nWithMessages()
            
            // Get the composable
            const { t, setLocale } = useI18n()
            
            // Set the locale
            setLocale(targetLocale)
            
            // Get the translation
            const translation = t(nestedKey)
            
            // Translation should exist (not be the key itself)
            expect(translation).not.toBe(nestedKey)
            
            // Translation should be a non-empty string
            expect(typeof translation).toBe('string')
            expect(translation.length).toBeGreaterThan(0)
          }
        ),
        { numRuns: 100 }
      )
    })
  })
})
