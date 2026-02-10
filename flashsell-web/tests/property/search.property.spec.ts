/**
 * Property-Based Tests for Search Page
 * 
 * These tests validate the correctness properties of the Search page
 * using fast-check for property-based testing.
 * 
 * **Validates: Requirements 6.4, 6.5, 6.6**
 * - 6.4: User query displayed as message bubble
 * - 6.5: AI thinking indicator with progress steps
 * - 6.6: Results displayed in grid layout
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount, VueWrapper, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import fc from 'fast-check'
import ChatMessage from '@/components/ChatMessage.vue'
import SearchInput from '@/components/SearchInput.vue'
import ThinkingIndicator from '@/components/ThinkingIndicator.vue'
import type { ThinkingStep } from '@/components/ThinkingIndicator.vue'

// Mock vue-router
const mockPush = vi.fn()
const mockRoute = {
  name: 'Search',
  path: '/search',
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

describe('Search Property Tests', () => {
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
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  /**
   * **Feature: frontend-ui-refactor, Property 8: Search Query Display**
   * 
   * *For any* non-empty search query submitted by the user, the query 
   * should appear as a user message bubble in the chat interface.
   * 
   * **Validates: Requirements 6.4**
   */
  describe('Property 8: Search Query Display', () => {
    // Arbitrary for generating non-empty search queries
    const searchQueryArb = fc.string({ minLength: 1, maxLength: 200 })
      .filter(s => s.trim().length > 0)
      .map(s => s.trim())

    it('should display user message with correct type for any non-empty query', () => {
      fc.assert(
        fc.property(
          searchQueryArb,
          (query: string) => {
            const wrapper = mount(ChatMessage, {
              props: {
                type: 'user',
                content: query,
                timestamp: new Date()
              },
              global: {
                plugins: [pinia]
              }
            })

            // User message should be right-aligned (flex-row-reverse)
            const messageContainer = wrapper.find('div')
            expect(messageContainer.classes()).toContain('flex-row-reverse')

            // Message content should match the query
            const messageText = wrapper.find('p')
            expect(messageText.text()).toBe(query)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display AI message with correct type for any response', () => {
      fc.assert(
        fc.property(
          searchQueryArb,
          (content: string) => {
            const wrapper = mount(ChatMessage, {
              props: {
                type: 'ai',
                content: content,
                timestamp: new Date()
              },
              global: {
                plugins: [pinia]
              }
            })

            // AI message should be left-aligned (flex-row)
            const messageContainer = wrapper.find('div')
            expect(messageContainer.classes()).toContain('flex-row')
            expect(messageContainer.classes()).not.toContain('flex-row-reverse')

            // Message content should match
            const messageText = wrapper.find('p')
            expect(messageText.text()).toBe(content)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should have different styling for user vs AI messages', () => {
      fc.assert(
        fc.property(
          searchQueryArb,
          fc.constantFrom('user', 'ai') as fc.Arbitrary<'user' | 'ai'>,
          (content: string, type: 'user' | 'ai') => {
            const wrapper = mount(ChatMessage, {
              props: {
                type,
                content,
                timestamp: new Date()
              },
              global: {
                plugins: [pinia]
              }
            })

            // Find the message bubble
            const messageBubble = wrapper.find('.rounded-2xl')
            
            if (type === 'user') {
              // User messages should have orange background
              expect(messageBubble.classes()).toContain('bg-orange-500')
              expect(messageBubble.classes()).toContain('text-white')
            } else {
              // AI messages should have glass-card styling
              expect(messageBubble.classes()).toContain('glass-card')
            }

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display timestamp when provided', () => {
      fc.assert(
        fc.property(
          searchQueryArb,
          fc.date({ min: new Date('2020-01-01'), max: new Date('2030-12-31') }),
          (content: string, timestamp: Date) => {
            const wrapper = mount(ChatMessage, {
              props: {
                type: 'user',
                content,
                timestamp
              },
              global: {
                plugins: [pinia]
              }
            })

            // Timestamp should be displayed
            const timestampElement = wrapper.find('.text-xs.text-slate-500')
            expect(timestampElement.exists()).toBe(true)
            
            // Format should be HH:MM
            const hours = timestamp.getHours().toString().padStart(2, '0')
            const minutes = timestamp.getMinutes().toString().padStart(2, '0')
            expect(timestampElement.text()).toBe(`${hours}:${minutes}`)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should not display timestamp when not provided', () => {
      fc.assert(
        fc.property(
          searchQueryArb,
          (content: string) => {
            const wrapper = mount(ChatMessage, {
              props: {
                type: 'user',
                content
                // No timestamp provided
              },
              global: {
                plugins: [pinia]
              }
            })

            // Timestamp element should not exist or be empty
            const timestampElement = wrapper.find('.text-xs.text-slate-500')
            if (timestampElement.exists()) {
              expect(timestampElement.text()).toBe('')
            }

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should preserve whitespace in message content', () => {
      fc.assert(
        fc.property(
          fc.array(fc.constantFrom(' ', '\n', '\t', 'a', 'b', '1'), { minLength: 1, maxLength: 50 })
            .map(arr => arr.join(''))
            .filter(s => s.trim().length > 0),
          (content: string) => {
            const wrapper = mount(ChatMessage, {
              props: {
                type: 'user',
                content,
                timestamp: new Date()
              },
              global: {
                plugins: [pinia]
              }
            })

            // Message should have whitespace-pre-wrap class
            const messageText = wrapper.find('p')
            expect(messageText.classes()).toContain('whitespace-pre-wrap')

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * **Feature: frontend-ui-refactor, Property 9: Search Loading State**
   * 
   * *For any* search request in progress, the AI thinking indicator 
   * should be visible, and when the request completes, the indicator 
   * should be hidden and results should be displayed.
   * 
   * **Validates: Requirements 6.5, 6.6**
   */
  describe('Property 9: Search Loading State', () => {
    // Arbitrary for generating thinking steps
    const thinkingStepStatusArb = fc.constantFrom('pending', 'active', 'completed') as fc.Arbitrary<'pending' | 'active' | 'completed'>
    
    const thinkingStepArb = fc.record({
      id: fc.string({ minLength: 1, maxLength: 20 }).filter(s => s.trim().length > 0),
      label: fc.string({ minLength: 1, maxLength: 50 }).filter(s => s.trim().length > 0),
      status: thinkingStepStatusArb
    }) as fc.Arbitrary<ThinkingStep>

    const thinkingStepsArb = fc.array(thinkingStepArb, { minLength: 1, maxLength: 6 })

    it('should display thinking indicator when show is true', () => {
      fc.assert(
        fc.property(
          thinkingStepsArb,
          (steps: ThinkingStep[]) => {
            const wrapper = mount(ThinkingIndicator, {
              props: {
                steps,
                show: true
              },
              global: {
                plugins: [pinia]
              }
            })

            // Indicator should be visible
            expect(wrapper.find('.glass-card').exists()).toBe(true)
            
            // All steps should be rendered
            const stepElements = wrapper.findAll('.flex.items-center.gap-2')
            expect(stepElements.length).toBe(steps.length)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should hide thinking indicator when show is false', () => {
      fc.assert(
        fc.property(
          thinkingStepsArb,
          (steps: ThinkingStep[]) => {
            const wrapper = mount(ThinkingIndicator, {
              props: {
                steps,
                show: false
              },
              global: {
                plugins: [pinia]
              }
            })

            // Indicator should not be visible
            expect(wrapper.find('.glass-card').exists()).toBe(false)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display correct icon for each step status', () => {
      fc.assert(
        fc.property(
          thinkingStepStatusArb,
          (status: 'pending' | 'active' | 'completed') => {
            const steps: ThinkingStep[] = [
              { id: 'test', label: 'Test Step', status }
            ]

            const wrapper = mount(ThinkingIndicator, {
              props: {
                steps,
                show: true
              },
              global: {
                plugins: [pinia]
              }
            })

            const stepElement = wrapper.find('.flex.items-center.gap-2')
            
            if (status === 'completed') {
              // Should show check icon
              const checkIcon = stepElement.find('svg path[d="M5 13l4 4L19 7"]')
              expect(checkIcon.exists()).toBe(true)
            } else if (status === 'active') {
              // Should show spinner
              const spinner = stepElement.find('svg.animate-spin')
              expect(spinner.exists()).toBe(true)
            } else {
              // Should show pending circle
              const circle = stepElement.find('.rounded-full.bg-current')
              expect(circle.exists()).toBe(true)
            }

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should apply correct color classes based on step status', () => {
      fc.assert(
        fc.property(
          thinkingStepStatusArb,
          (status: 'pending' | 'active' | 'completed') => {
            const steps: ThinkingStep[] = [
              { id: 'test', label: 'Test Step', status }
            ]

            const wrapper = mount(ThinkingIndicator, {
              props: {
                steps,
                show: true
              },
              global: {
                plugins: [pinia]
              }
            })

            const stepElement = wrapper.find('.flex.items-center.gap-2')
            
            if (status === 'completed') {
              expect(stepElement.classes()).toContain('text-green-400')
            } else if (status === 'active') {
              expect(stepElement.classes()).toContain('text-orange-400')
            } else {
              expect(stepElement.classes()).toContain('text-slate-500')
            }

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display step labels correctly', () => {
      fc.assert(
        fc.property(
          thinkingStepsArb,
          (steps: ThinkingStep[]) => {
            const wrapper = mount(ThinkingIndicator, {
              props: {
                steps,
                show: true
              },
              global: {
                plugins: [pinia]
              }
            })

            const stepElements = wrapper.findAll('.flex.items-center.gap-2 span')
            
            steps.forEach((step, index) => {
              // Compare trimmed values since DOM may trim whitespace
              expect(stepElements[index].text().trim()).toBe(step.label.trim())
            })

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should apply font-medium to active step label', () => {
      fc.assert(
        fc.property(
          fc.integer({ min: 0, max: 3 }),
          (activeIndex: number) => {
            const steps: ThinkingStep[] = [
              { id: 'step1', label: 'Step 1', status: 'pending' },
              { id: 'step2', label: 'Step 2', status: 'pending' },
              { id: 'step3', label: 'Step 3', status: 'pending' },
              { id: 'step4', label: 'Step 4', status: 'pending' }
            ]
            
            // Set one step as active
            steps[activeIndex].status = 'active'
            // Set previous steps as completed
            for (let i = 0; i < activeIndex; i++) {
              steps[i].status = 'completed'
            }

            const wrapper = mount(ThinkingIndicator, {
              props: {
                steps,
                show: true
              },
              global: {
                plugins: [pinia]
              }
            })

            const stepLabels = wrapper.findAll('.flex.items-center.gap-2 span')
            
            stepLabels.forEach((label, index) => {
              if (index === activeIndex) {
                expect(label.classes()).toContain('font-medium')
              } else {
                expect(label.classes()).not.toContain('font-medium')
              }
            })

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display AI avatar in thinking indicator', () => {
      fc.assert(
        fc.property(
          thinkingStepsArb,
          (steps: ThinkingStep[]) => {
            const wrapper = mount(ThinkingIndicator, {
              props: {
                steps,
                show: true
              },
              global: {
                plugins: [pinia]
              }
            })

            // Should have AI avatar with gradient background
            const avatar = wrapper.find('.rounded-full.bg-gradient-to-br')
            expect(avatar.exists()).toBe(true)
            expect(avatar.classes()).toContain('from-blue-500')
            expect(avatar.classes()).toContain('to-purple-600')

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * Additional tests for SearchInput component
   */
  describe('SearchInput Component', () => {
    // Arbitrary for generating search input values
    const inputValueArb = fc.string({ minLength: 0, maxLength: 500 })

    it('should enable send button only for non-empty trimmed input', () => {
      fc.assert(
        fc.property(
          inputValueArb,
          (value: string) => {
            const wrapper = mount(SearchInput, {
              props: {
                modelValue: value,
                disabled: false
              },
              global: {
                plugins: [pinia]
              }
            })

            const sendButton = wrapper.find('button[type="button"]:last-child')
            const canSend = value.trim().length > 0

            if (canSend) {
              expect(sendButton.classes()).toContain('bg-orange-500')
              expect(sendButton.attributes('disabled')).toBeUndefined()
            } else {
              expect(sendButton.classes()).toContain('bg-slate-700')
              expect(sendButton.attributes('disabled')).toBeDefined()
            }

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should disable send button when disabled prop is true', () => {
      fc.assert(
        fc.property(
          fc.string({ minLength: 1, maxLength: 100 }).filter(s => s.trim().length > 0),
          (value: string) => {
            const wrapper = mount(SearchInput, {
              props: {
                modelValue: value,
                disabled: true
              },
              global: {
                plugins: [pinia]
              }
            })

            const sendButton = wrapper.find('button[type="button"]:last-child')
            expect(sendButton.attributes('disabled')).toBeDefined()

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display character count when input has content', () => {
      fc.assert(
        fc.property(
          fc.string({ minLength: 1, maxLength: 500 }),
          fc.integer({ min: 100, max: 1000 }),
          (value: string, maxLength: number) => {
            const wrapper = mount(SearchInput, {
              props: {
                modelValue: value,
                maxLength
              },
              global: {
                plugins: [pinia]
              }
            })

            if (value.length > 0) {
              const charCount = wrapper.find('.text-xs')
              expect(charCount.exists()).toBe(true)
              expect(charCount.text()).toBe(`${value.length}/${maxLength}`)
            }

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should apply red color to character count when at max length', () => {
      fc.assert(
        fc.property(
          fc.integer({ min: 10, max: 100 }),
          (maxLength: number) => {
            // Create a string exactly at max length
            const value = 'a'.repeat(maxLength)

            const wrapper = mount(SearchInput, {
              props: {
                modelValue: value,
                maxLength
              },
              global: {
                plugins: [pinia]
              }
            })

            const charCount = wrapper.find('.text-xs')
            expect(charCount.classes()).toContain('text-red-400')

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should emit submit event with trimmed value on button click', async () => {
      await fc.assert(
        fc.asyncProperty(
          fc.string({ minLength: 1, maxLength: 100 })
            .filter(s => s.trim().length > 0)
            .map(s => `  ${s}  `), // Add whitespace
          async (value: string) => {
            const wrapper = mount(SearchInput, {
              props: {
                modelValue: value,
                disabled: false
              },
              global: {
                plugins: [pinia]
              }
            })

            const sendButton = wrapper.find('button[type="button"]:last-child')
            await sendButton.trigger('click')

            const emitted = wrapper.emitted('submit')
            expect(emitted).toBeTruthy()
            expect(emitted![0][0]).toBe(value.trim())

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should not emit submit for empty or whitespace-only input', async () => {
      await fc.assert(
        fc.asyncProperty(
          fc.constantFrom('', '   ', '\t', '\n', '  \n  '),
          async (value: string) => {
            const wrapper = mount(SearchInput, {
              props: {
                modelValue: value,
                disabled: false
              },
              global: {
                plugins: [pinia]
              }
            })

            const sendButton = wrapper.find('button[type="button"]:last-child')
            await sendButton.trigger('click')

            const emitted = wrapper.emitted('submit')
            expect(emitted).toBeFalsy()

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * ChatMessage typing indicator tests
   */
  describe('ChatMessage Typing Indicator', () => {
    it('should display typing indicator when isTyping is true', () => {
      fc.assert(
        fc.property(
          fc.boolean(),
          (isTyping: boolean) => {
            const wrapper = mount(ChatMessage, {
              props: {
                type: 'ai',
                content: 'Test message',
                isTyping
              },
              global: {
                plugins: [pinia]
              }
            })

            const typingDots = wrapper.findAll('.typing-dot')
            
            if (isTyping) {
              expect(typingDots.length).toBe(3)
              // Message text should not be visible
              expect(wrapper.find('p').exists()).toBe(false)
            } else {
              expect(typingDots.length).toBe(0)
              // Message text should be visible
              expect(wrapper.find('p').exists()).toBe(true)
            }

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })
  })
})
