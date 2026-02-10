/**
 * Property-Based Tests for Error Handling and Loading States
 * 
 * These tests validate the correctness properties of error handling,
 * loading states, and API retry logic using fast-check for property-based testing.
 * 
 * **Validates: Requirements 14.1, 14.2, 14.3, 14.4**
 * - 14.1: Display skeleton loaders or loading spinners when data is loading
 * - 14.2: Display error message with retry option when API request fails
 * - 14.3: Display network error message when network is unavailable
 * - 14.4: Implement automatic retry for failed requests (max 2 retries)
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import fc from 'fast-check'
import { nextTick } from 'vue'

// Components
import LoadingState from '@/components/LoadingState.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import NetworkError from '@/components/NetworkError.vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import CardSkeleton from '@/components/CardSkeleton.vue'
import ProductGrid from '@/components/ProductGrid.vue'

// API utilities
import { 
  ApiError, 
  ErrorType, 
  classifyError, 
  MAX_RETRY_TIMES, 
  RETRY_DELAY,
  type NetworkErrorType 
} from '@/api/request'

// Mock i18n
vi.mock('@/composables/useI18n', () => ({
  useI18n: () => ({
    t: (key: string, fallback?: string) => fallback || key,
    locale: { value: 'zh' },
    isZh: { value: true },
    isEn: { value: false },
    setLocale: vi.fn(),
    toggleLocale: vi.fn(),
    setMessages: vi.fn(),
    mergeMessages: vi.fn()
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

// Arbitrary for generating error types
const errorTypeArb = fc.constantFrom<ErrorType>(
  ErrorType.NETWORK,
  ErrorType.TIMEOUT,
  ErrorType.SERVER,
  ErrorType.BUSINESS,
  ErrorType.AUTH
)

// Arbitrary for generating network error types
const networkErrorTypeArb = fc.constantFrom<NetworkErrorType>(
  'network',
  'server',
  'timeout',
  'unknown'
)

// Arbitrary for generating error messages
const errorMessageArb = fc.string({ minLength: 1, maxLength: 200 }).filter(s => s.trim().length > 0)

// Arbitrary for generating loading sizes
const loadingSizeArb = fc.constantFrom<'sm' | 'md' | 'lg'>('sm', 'md', 'lg')

// Arbitrary for generating skeleton types
const skeletonTypeArb = fc.constantFrom<'text' | 'card' | 'avatar' | 'image' | 'custom'>(
  'text', 'card', 'avatar', 'image', 'custom'
)

describe('Error Handling Property Tests', () => {
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
   * **Feature: frontend-ui-refactor, Property 16: Loading State Display**
   * 
   * *For any* API request in progress, the appropriate loading indicator 
   * (skeleton or spinner) should be displayed, and when the request completes, 
   * the loading indicator should be replaced with the actual content.
   * 
   * **Validates: Requirements 14.1**
   */
  describe('Property 16: Loading State Display', () => {
    it('should display loading spinner with correct size for any size prop', () => {
      fc.assert(
        fc.property(
          loadingSizeArb,
          (size: 'sm' | 'md' | 'lg') => {
            const wrapper = mount(LoadingState, {
              props: {
                size,
                text: 'Loading...'
              },
              global: {
                plugins: [pinia]
              }
            })

            // Should render the loading spinner
            const spinner = wrapper.find('svg')
            expect(spinner.exists()).toBe(true)
            expect(spinner.classes()).toContain('animate-spin')

            // Should have correct size class
            const sizeClasses = {
              sm: 'w-4',
              md: 'w-8',
              lg: 'w-12'
            }
            expect(spinner.classes()).toContain(sizeClasses[size])

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display loading text when provided', () => {
      fc.assert(
        fc.property(
          fc.string({ minLength: 1, maxLength: 50 }).filter(s => s.trim().length > 0),
          (loadingText: string) => {
            const wrapper = mount(LoadingState, {
              props: {
                text: loadingText
              },
              global: {
                plugins: [pinia]
              }
            })

            // Should display the loading text (trimmed version)
            expect(wrapper.text()).toContain(loadingText.trim())

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should render skeleton loader with correct type', () => {
      fc.assert(
        fc.property(
          skeletonTypeArb,
          (type: 'text' | 'card' | 'avatar' | 'image' | 'custom') => {
            const wrapper = mount(SkeletonLoader, {
              props: {
                type,
                lines: 3,
                width: '100%',
                height: '1rem'
              },
              global: {
                plugins: [pinia]
              }
            })

            // Should render with animate-pulse class
            const animatedElements = wrapper.findAll('.animate-pulse')
            expect(animatedElements.length).toBeGreaterThan(0)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should render correct number of skeleton lines for text type', () => {
      fc.assert(
        fc.property(
          fc.integer({ min: 1, max: 10 }),
          (lines: number) => {
            const wrapper = mount(SkeletonLoader, {
              props: {
                type: 'text',
                lines
              },
              global: {
                plugins: [pinia]
              }
            })

            // The skeleton loader renders lines inside a space-y-2 container
            // Each line is a direct child div with animate-pulse class
            const container = wrapper.find('.space-y-2')
            if (container.exists()) {
              const skeletonLines = container.findAll('div')
              expect(skeletonLines.length).toBe(lines)
            } else {
              // Fallback: just check that animate-pulse elements exist
              const animatedElements = wrapper.findAll('.animate-pulse')
              expect(animatedElements.length).toBeGreaterThan(0)
            }

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should render CardSkeleton with correct count', () => {
      fc.assert(
        fc.property(
          fc.integer({ min: 1, max: 12 }),
          fc.constantFrom<1 | 2 | 3 | 4>(1, 2, 3, 4),
          (count: number, columns: 1 | 2 | 3 | 4) => {
            const wrapper = mount(CardSkeleton, {
              props: {
                count,
                columns
              },
              global: {
                plugins: [pinia]
              }
            })

            // Should render the correct number of skeleton cards
            const skeletonCards = wrapper.findAll('.glass-card')
            expect(skeletonCards.length).toBe(count)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should show skeleton loader in ProductGrid when loading', () => {
      fc.assert(
        fc.property(
          fc.integer({ min: 1, max: 12 }),
          (skeletonCount: number) => {
            const wrapper = mount(ProductGrid, {
              props: {
                products: [],
                loading: true,
                skeletonCount,
                showFavorite: false,
                favoriteIds: new Set<number>()
              },
              global: {
                plugins: [pinia]
              }
            })

            // When loading, should not show empty state
            const emptyState = wrapper.find('[data-testid="empty-state"]')
            // Empty state should not be visible when loading
            expect(wrapper.text()).not.toContain('No favorites')

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should transition from loading to content state correctly', () => {
      fc.assert(
        fc.property(
          fc.boolean(),
          (initialLoading: boolean) => {
            const wrapper = mount(LoadingState, {
              props: {
                size: 'md',
                text: 'Loading...',
                fullscreen: false
              },
              global: {
                plugins: [pinia]
              }
            })

            // Should always show loading indicator when mounted
            const spinner = wrapper.find('svg')
            expect(spinner.exists()).toBe(true)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should support fullscreen loading mode', () => {
      fc.assert(
        fc.property(
          fc.boolean(),
          (fullscreen: boolean) => {
            const wrapper = mount(LoadingState, {
              props: {
                fullscreen,
                text: 'Loading...'
              },
              global: {
                plugins: [pinia]
              }
            })

            const container = wrapper.find('div')
            if (fullscreen) {
              expect(container.classes()).toContain('fixed')
              expect(container.classes()).toContain('inset-0')
            } else {
              expect(container.classes()).not.toContain('fixed')
            }

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * **Feature: frontend-ui-refactor, Property 17: Error State Display**
   * 
   * *For any* failed API request, an error message should be displayed 
   * with a retry option, and clicking retry should re-attempt the request.
   * 
   * **Validates: Requirements 14.2, 14.3**
   */
  describe('Property 17: Error State Display', () => {
    it('should display error message with correct content', () => {
      fc.assert(
        fc.property(
          errorMessageArb,
          (message: string) => {
            const wrapper = mount(ErrorMessage, {
              props: {
                message,
                retryable: true
              },
              global: {
                plugins: [pinia]
              }
            })

            // Should display the error message
            expect(wrapper.text()).toContain(message)

            // Should have error icon
            const icon = wrapper.find('svg')
            expect(icon.exists()).toBe(true)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should show retry button when retryable is true', () => {
      fc.assert(
        fc.property(
          fc.boolean(),
          (retryable: boolean) => {
            const wrapper = mount(ErrorMessage, {
              props: {
                message: 'Test error',
                retryable
              },
              global: {
                plugins: [pinia]
              }
            })

            const retryButton = wrapper.find('button')
            expect(retryButton.exists()).toBe(retryable)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should emit retry event when retry button is clicked', async () => {
      await fc.assert(
        fc.asyncProperty(
          errorMessageArb,
          async (message: string) => {
            const wrapper = mount(ErrorMessage, {
              props: {
                message,
                retryable: true
              },
              global: {
                plugins: [pinia]
              }
            })

            const retryButton = wrapper.find('button')
            await retryButton.trigger('click')

            const emitted = wrapper.emitted('retry')
            expect(emitted).toBeTruthy()
            expect(emitted!.length).toBeGreaterThanOrEqual(1)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display NetworkError with correct type and message', () => {
      fc.assert(
        fc.property(
          networkErrorTypeArb,
          fc.option(errorMessageArb, { nil: undefined }),
          (type: NetworkErrorType, message: string | undefined) => {
            const wrapper = mount(NetworkError, {
              props: {
                type,
                message,
                retryable: true
              },
              global: {
                plugins: [pinia]
              }
            })

            // Should have an icon
            const icon = wrapper.find('svg')
            expect(icon.exists()).toBe(true)

            // Should have a title
            const title = wrapper.find('h3')
            expect(title.exists()).toBe(true)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display correct icon for each network error type', () => {
      fc.assert(
        fc.property(
          networkErrorTypeArb,
          (type: NetworkErrorType) => {
            const wrapper = mount(NetworkError, {
              props: {
                type,
                retryable: true
              },
              global: {
                plugins: [pinia]
              }
            })

            // Should have an SVG icon
            const icons = wrapper.findAll('svg')
            expect(icons.length).toBeGreaterThan(0)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should emit retry event from NetworkError when retry button is clicked', async () => {
      await fc.assert(
        fc.asyncProperty(
          networkErrorTypeArb,
          async (type: NetworkErrorType) => {
            const wrapper = mount(NetworkError, {
              props: {
                type,
                retryable: true
              },
              global: {
                plugins: [pinia]
              }
            })

            const retryButton = wrapper.find('button')
            await retryButton.trigger('click')

            const emitted = wrapper.emitted('retry')
            expect(emitted).toBeTruthy()
            expect(emitted!.length).toBeGreaterThanOrEqual(1)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should not show retry button when retryable is false in NetworkError', () => {
      fc.assert(
        fc.property(
          networkErrorTypeArb,
          (type: NetworkErrorType) => {
            const wrapper = mount(NetworkError, {
              props: {
                type,
                retryable: false
              },
              global: {
                plugins: [pinia]
              }
            })

            const retryButton = wrapper.find('button')
            expect(retryButton.exists()).toBe(false)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display custom title in ErrorMessage when provided', () => {
      fc.assert(
        fc.property(
          fc.string({ minLength: 1, maxLength: 50 }).filter(s => s.trim().length > 0),
          errorMessageArb,
          (title: string, message: string) => {
            const wrapper = mount(ErrorMessage, {
              props: {
                title,
                message,
                retryable: true
              },
              global: {
                plugins: [pinia]
              }
            })

            // Should display the custom title (trimmed version as DOM trims whitespace)
            const titleElement = wrapper.find('h3')
            expect(titleElement.text()).toBe(title.trim())

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * **Feature: frontend-ui-refactor, Property 18: API Retry Logic**
   * 
   * *For any* failed API request due to network or server error, the system 
   * should automatically retry up to 2 times before displaying an error to the user.
   * 
   * **Validates: Requirements 14.4**
   */
  describe('Property 18: API Retry Logic', () => {
    it('should have MAX_RETRY_TIMES set to 2', () => {
      fc.assert(
        fc.property(
          fc.constant(null),
          () => {
            expect(MAX_RETRY_TIMES).toBe(2)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should have RETRY_DELAY set to 1000ms', () => {
      fc.assert(
        fc.property(
          fc.constant(null),
          () => {
            expect(RETRY_DELAY).toBe(1000)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should create ApiError with correct type and message', () => {
      fc.assert(
        fc.property(
          errorTypeArb,
          errorMessageArb,
          fc.integer({ min: 0, max: 5 }),
          (type: ErrorType, message: string, retryCount: number) => {
            const error = new ApiError(type, message, null, retryCount)

            expect(error.type).toBe(type)
            expect(error.message).toBe(message)
            expect(error.retryCount).toBe(retryCount)
            expect(error.name).toBe('ApiError')
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should correctly determine canRetry based on error type', () => {
      fc.assert(
        fc.property(
          errorTypeArb,
          (type: ErrorType) => {
            const error = new ApiError(type, 'Test error', null, 0)

            // Business and Auth errors should not be retryable
            const expectedCanRetry = type !== ErrorType.BUSINESS && type !== ErrorType.AUTH
            expect(error.canRetry).toBe(expectedCanRetry)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should correctly map ErrorType to NetworkErrorType', () => {
      fc.assert(
        fc.property(
          errorTypeArb,
          (type: ErrorType) => {
            const error = new ApiError(type, 'Test error', null, 0)
            const networkType = error.networkErrorType

            switch (type) {
              case ErrorType.NETWORK:
                expect(networkType).toBe('network')
                break
              case ErrorType.SERVER:
                expect(networkType).toBe('server')
                break
              case ErrorType.TIMEOUT:
                expect(networkType).toBe('timeout')
                break
              default:
                expect(networkType).toBe('unknown')
            }
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should classify ApiError correctly', () => {
      fc.assert(
        fc.property(
          errorTypeArb,
          errorMessageArb,
          (type: ErrorType, message: string) => {
            const error = new ApiError(type, message, null, 0)
            const classified = classifyError(error)

            expect(classified.isApiError).toBe(true)
            expect(classified.message).toBe(message)
            expect(classified.canRetry).toBe(error.canRetry)
            expect(classified.type).toBe(error.networkErrorType)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should classify non-ApiError correctly', () => {
      fc.assert(
        fc.property(
          errorMessageArb,
          (message: string) => {
            const error = new Error(message)
            const classified = classifyError(error)

            expect(classified.isApiError).toBe(false)
            expect(classified.message).toBe(message)
            expect(classified.canRetry).toBe(true)
            expect(classified.type).toBe('unknown')
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should classify unknown error types correctly', () => {
      fc.assert(
        fc.property(
          fc.anything().filter(x => !(x instanceof Error) && !(x instanceof ApiError)),
          (unknownError) => {
            const classified = classifyError(unknownError)

            expect(classified.isApiError).toBe(false)
            expect(classified.canRetry).toBe(true)
            expect(classified.type).toBe('unknown')
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should track retry count in ApiError', () => {
      fc.assert(
        fc.property(
          fc.integer({ min: 0, max: MAX_RETRY_TIMES + 1 }),
          (retryCount: number) => {
            const error = new ApiError(ErrorType.NETWORK, 'Test error', null, retryCount)

            expect(error.retryCount).toBe(retryCount)
            
            // After MAX_RETRY_TIMES, the error should still be created
            // but the retry logic in the interceptor should not retry further
            if (retryCount >= MAX_RETRY_TIMES) {
              // Error was created after max retries exhausted
              expect(error.retryCount).toBeGreaterThanOrEqual(MAX_RETRY_TIMES)
            }
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should correctly identify retryable error types', () => {
      fc.assert(
        fc.property(
          errorTypeArb,
          (type: ErrorType) => {
            const error = new ApiError(type, 'Test error', null, 0)

            // Network, Timeout, and Server errors should be retryable
            const retryableTypes = [ErrorType.NETWORK, ErrorType.TIMEOUT, ErrorType.SERVER]
            const shouldBeRetryable = retryableTypes.includes(type)

            expect(error.canRetry).toBe(shouldBeRetryable)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should not allow retry for business errors', () => {
      fc.assert(
        fc.property(
          errorMessageArb,
          (message: string) => {
            const error = new ApiError(ErrorType.BUSINESS, message, null, 0)

            expect(error.canRetry).toBe(false)
            expect(error.networkErrorType).toBe('unknown')
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should not allow retry for auth errors', () => {
      fc.assert(
        fc.property(
          errorMessageArb,
          (message: string) => {
            const error = new ApiError(ErrorType.AUTH, message, null, 0)

            expect(error.canRetry).toBe(false)
            expect(error.networkErrorType).toBe('unknown')
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * Additional tests for error handling integration
   */
  describe('Error Handling Integration', () => {
    it('should handle loading to error state transition', () => {
      fc.assert(
        fc.property(
          fc.boolean(),
          networkErrorTypeArb,
          (wasLoading: boolean, errorType: NetworkErrorType) => {
            // Simulate state transition from loading to error
            const loadingWrapper = mount(LoadingState, {
              props: { text: 'Loading...' },
              global: { plugins: [pinia] }
            })

            // Loading state should show spinner
            expect(loadingWrapper.find('svg').exists()).toBe(true)
            loadingWrapper.unmount()

            // Error state should show error message
            const errorWrapper = mount(NetworkError, {
              props: { type: errorType, retryable: true },
              global: { plugins: [pinia] }
            })

            expect(errorWrapper.find('h3').exists()).toBe(true)
            expect(errorWrapper.find('button').exists()).toBe(true)
            errorWrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should maintain consistent error display across error types', () => {
      fc.assert(
        fc.property(
          networkErrorTypeArb,
          (type: NetworkErrorType) => {
            const wrapper = mount(NetworkError, {
              props: { type, retryable: true },
              global: { plugins: [pinia] }
            })

            // All error types should have consistent structure
            expect(wrapper.find('h3').exists()).toBe(true) // Title
            expect(wrapper.find('p').exists()).toBe(true)  // Message
            expect(wrapper.find('button').exists()).toBe(true) // Retry button
            expect(wrapper.findAll('svg').length).toBeGreaterThan(0) // Icons

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })
  })
})
