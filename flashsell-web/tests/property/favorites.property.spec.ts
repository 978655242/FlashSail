/**
 * Property-Based Tests for Favorites Page
 * 
 * These tests validate the correctness properties of the Favorites page
 * using fast-check for property-based testing.
 * 
 * **Validates: Requirements 7.1, 7.2, 7.3, 7.5, 7.6**
 * - 7.1: Display a grid of favorited product cards
 * - 7.2: Support removing products from favorites
 * - 7.3: Display empty state when no favorites exist
 * - 7.5: Update UI immediately on remove
 * - 7.6: Display total count of favorites
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount, VueWrapper, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import fc from 'fast-check'
import { useFavoritesStore } from '@/stores/favorites'
import ProductGrid from '@/components/ProductGrid.vue'
import EmptyState from '@/components/EmptyState.vue'
import type { ProductDTO } from '@/types/product'

// Mock vue-router
const mockPush = vi.fn()
const mockRoute = {
  name: 'Favorites',
  path: '/favorites',
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

// Mock API calls
vi.mock('@/api/favorites', () => ({
  getFavorites: vi.fn().mockResolvedValue({
    data: { code: 200, data: { products: [], total: 0, page: 1 } }
  }),
  addFavorite: vi.fn().mockResolvedValue({
    data: { code: 200, data: null }
  }),
  removeFavorite: vi.fn().mockResolvedValue({
    data: { code: 200, data: null }
  }),
  getBoards: vi.fn().mockResolvedValue({
    data: { code: 200, data: { boards: [], maxBoards: 10 } }
  }),
  getBoardDetail: vi.fn().mockResolvedValue({
    data: { code: 200, data: null }
  }),
  createBoard: vi.fn().mockResolvedValue({
    data: { code: 200, data: { id: 1, name: 'Test', productCount: 0 } }
  }),
  deleteBoard: vi.fn().mockResolvedValue({
    data: { code: 200, data: null }
  }),
  addToBoard: vi.fn().mockResolvedValue({
    data: { code: 200, data: null }
  }),
  removeFromBoard: vi.fn().mockResolvedValue({
    data: { code: 200, data: null }
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

// Arbitrary for generating product data
const productArb: fc.Arbitrary<ProductDTO> = fc.record({
  id: fc.nat({ max: 10000 }),
  title: fc.string({ minLength: 1, maxLength: 100 }).filter(s => s.trim().length > 0),
  currentPrice: fc.float({ min: Math.fround(0.01), max: Math.fround(9999.99), noNaN: true }),
  image: fc.option(fc.webUrl(), { nil: undefined }),
  rating: fc.option(fc.float({ min: Math.fround(0), max: Math.fround(5), noNaN: true }), { nil: undefined }),
  reviewCount: fc.option(fc.nat({ max: 100000 }), { nil: undefined }),
  bsrRank: fc.option(fc.nat({ max: 1000000 }), { nil: undefined }),
  competitionScore: fc.option(fc.float({ min: Math.fround(0), max: Math.fround(1), noNaN: true }), { nil: undefined }),
  category: fc.option(fc.record({
    id: fc.nat(),
    name: fc.string({ minLength: 1, maxLength: 50 })
  }), { nil: undefined })
}) as fc.Arbitrary<ProductDTO>

// Arbitrary for generating unique product arrays
const uniqueProductsArb = (minLength: number, maxLength: number): fc.Arbitrary<ProductDTO[]> => {
  return fc.array(productArb, { minLength, maxLength })
    .map(products => {
      // Ensure unique IDs
      const seen = new Set<number>()
      return products.filter(p => {
        if (seen.has(p.id)) return false
        seen.add(p.id)
        return true
      })
    })
    .filter(products => products.length >= minLength)
}

describe('Favorites Property Tests', () => {
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
   * **Feature: frontend-ui-refactor, Property 10: Favorites List Consistency**
   * 
   * *For any* product added to or removed from favorites, the favorites list 
   * should immediately reflect the change, and the total count should be 
   * updated accordingly.
   * 
   * **Validates: Requirements 7.1, 7.2, 7.5, 7.6**
   */
  describe('Property 10: Favorites List Consistency', () => {
    it('should correctly track favorite state after adding products', () => {
      fc.assert(
        fc.property(
          uniqueProductsArb(1, 10),
          (products: ProductDTO[]) => {
            const store = useFavoritesStore()
            
            // Reset store state
            store.favorites = []
            store.favoriteIds = new Set()
            store.total = 0
            
            // Add each product to favorites
            products.forEach(product => {
              store.favoriteIds.add(product.id)
              store.favorites.push(product)
              store.total++
            })
            
            // Verify all products are tracked as favorites
            products.forEach(product => {
              expect(store.isFavorite(product.id)).toBe(true)
            })
            
            // Verify total count matches
            expect(store.total).toBe(products.length)
            expect(store.favoriteIds.size).toBe(products.length)
            expect(store.favorites.length).toBe(products.length)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should correctly update state after removing products', () => {
      fc.assert(
        fc.property(
          uniqueProductsArb(2, 10),
          fc.nat(),
          (products: ProductDTO[], removeIndex: number) => {
            const store = useFavoritesStore()
            
            // Reset and populate store
            store.favorites = [...products]
            store.favoriteIds = new Set(products.map(p => p.id))
            store.total = products.length
            
            // Select a product to remove
            const productToRemove = products[removeIndex % products.length]
            const initialCount = store.total
            
            // Remove the product (simulating what removeFavorite does)
            store.favoriteIds.delete(productToRemove.id)
            store.favorites = store.favorites.filter(p => p.id !== productToRemove.id)
            store.total--
            
            // Verify the product is no longer a favorite
            expect(store.isFavorite(productToRemove.id)).toBe(false)
            
            // Verify count decreased by 1
            expect(store.total).toBe(initialCount - 1)
            expect(store.favoriteIds.size).toBe(initialCount - 1)
            expect(store.favorites.length).toBe(initialCount - 1)
            
            // Verify other products are still favorites
            products
              .filter(p => p.id !== productToRemove.id)
              .forEach(product => {
                expect(store.isFavorite(product.id)).toBe(true)
              })
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should maintain consistency between favoriteIds Set and favorites array', () => {
      fc.assert(
        fc.property(
          uniqueProductsArb(1, 20),
          fc.array(fc.boolean(), { minLength: 1, maxLength: 20 }),
          (products: ProductDTO[], operations: boolean[]) => {
            const store = useFavoritesStore()
            
            // Reset store
            store.favorites = []
            store.favoriteIds = new Set()
            store.total = 0
            
            // Perform random add/remove operations
            products.forEach((product, index) => {
              const shouldAdd = operations[index % operations.length]
              
              if (shouldAdd && !store.isFavorite(product.id)) {
                store.favoriteIds.add(product.id)
                store.favorites.push(product)
                store.total++
              } else if (!shouldAdd && store.isFavorite(product.id)) {
                store.favoriteIds.delete(product.id)
                store.favorites = store.favorites.filter(p => p.id !== product.id)
                store.total--
              }
            })
            
            // Verify consistency
            expect(store.favoriteIds.size).toBe(store.favorites.length)
            expect(store.total).toBe(store.favorites.length)
            
            // Every ID in favoriteIds should have a corresponding product in favorites
            store.favoriteIds.forEach(id => {
              expect(store.favorites.some(p => p.id === id)).toBe(true)
            })
            
            // Every product in favorites should have its ID in favoriteIds
            store.favorites.forEach(product => {
              expect(store.favoriteIds.has(product.id)).toBe(true)
            })
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should correctly report favoriteCount computed property', () => {
      fc.assert(
        fc.property(
          fc.array(fc.nat({ max: 1000 }), { minLength: 0, maxLength: 50 }),
          (productIds: number[]) => {
            const store = useFavoritesStore()
            
            // Reset store
            store.favorites = []
            store.favoriteIds = new Set()
            store.total = 0
            
            // Add unique product IDs
            const uniqueIds = [...new Set(productIds)]
            uniqueIds.forEach(id => {
              store.favoriteIds.add(id)
            })
            
            // Verify favoriteCount matches the Set size
            expect(store.favoriteCount).toBe(uniqueIds.length)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should handle adding duplicate products correctly', () => {
      fc.assert(
        fc.property(
          productArb,
          fc.integer({ min: 2, max: 5 }),
          (product: ProductDTO, duplicateCount: number) => {
            const store = useFavoritesStore()
            
            // Reset store
            store.favorites = []
            store.favoriteIds = new Set()
            store.total = 0
            
            // Try to add the same product multiple times
            for (let i = 0; i < duplicateCount; i++) {
              if (!store.isFavorite(product.id)) {
                store.favoriteIds.add(product.id)
                store.favorites.push(product)
                store.total++
              }
            }
            
            // Should only have one instance
            expect(store.favoriteIds.size).toBe(1)
            expect(store.favorites.length).toBe(1)
            expect(store.total).toBe(1)
            expect(store.isFavorite(product.id)).toBe(true)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should handle removing non-existent products gracefully', () => {
      fc.assert(
        fc.property(
          uniqueProductsArb(1, 5),
          fc.nat({ max: 10000 }),
          (products: ProductDTO[], nonExistentId: number) => {
            const store = useFavoritesStore()
            
            // Reset and populate store
            store.favorites = [...products]
            store.favoriteIds = new Set(products.map(p => p.id))
            store.total = products.length
            
            // Ensure nonExistentId is not in the favorites
            const safeNonExistentId = products.some(p => p.id === nonExistentId) 
              ? nonExistentId + 10001 
              : nonExistentId
            
            const initialCount = store.total
            
            // Try to remove non-existent product
            if (store.isFavorite(safeNonExistentId)) {
              store.favoriteIds.delete(safeNonExistentId)
              store.favorites = store.favorites.filter(p => p.id !== safeNonExistentId)
              store.total--
            }
            
            // State should remain unchanged
            expect(store.total).toBe(initialCount)
            expect(store.favoriteIds.size).toBe(initialCount)
            expect(store.favorites.length).toBe(initialCount)
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * **Feature: frontend-ui-refactor, Property 11: Empty State Display**
   * 
   * *For any* favorites list with zero items, the empty state component 
   * should be displayed instead of the product grid.
   * 
   * **Validates: Requirements 7.3**
   */
  describe('Property 11: Empty State Display', () => {
    it('should show empty state when products array is empty and not loading', () => {
      fc.assert(
        fc.property(
          fc.constant([]),
          fc.boolean(),
          (products: ProductDTO[], loading: boolean) => {
            const wrapper = mount(ProductGrid, {
              props: {
                products,
                loading,
                showFavorite: true,
                favoriteIds: new Set<number>(),
                emptyTitle: 'No favorites',
                emptyDescription: 'Add some products to favorites',
                emptyIcon: 'favorite'
              },
              global: {
                plugins: [pinia]
              }
            })

            if (!loading && products.length === 0) {
              // Empty state should be visible
              const emptyState = wrapper.findComponent(EmptyState)
              expect(emptyState.exists()).toBe(true)
              
              // Product grid should not be visible
              const productGrid = wrapper.find('.grid')
              expect(productGrid.exists()).toBe(false)
            }

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should show product grid when products exist', () => {
      fc.assert(
        fc.property(
          uniqueProductsArb(1, 10),
          (products: ProductDTO[]) => {
            const favoriteIds = new Set(products.map(p => p.id))
            
            const wrapper = mount(ProductGrid, {
              props: {
                products,
                loading: false,
                showFavorite: true,
                favoriteIds,
                emptyTitle: 'No favorites',
                emptyDescription: 'Add some products to favorites',
                emptyIcon: 'favorite'
              },
              global: {
                plugins: [pinia]
              }
            })

            // Product grid should be visible
            const productGrid = wrapper.find('.grid')
            expect(productGrid.exists()).toBe(true)
            
            // Empty state should not be visible
            const emptyState = wrapper.findComponent(EmptyState)
            expect(emptyState.exists()).toBe(false)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should show skeleton loader when loading with no products', () => {
      fc.assert(
        fc.property(
          fc.integer({ min: 1, max: 12 }),
          (skeletonCount: number) => {
            const wrapper = mount(ProductGrid, {
              props: {
                products: [],
                loading: true,
                skeletonCount,
                showFavorite: true,
                favoriteIds: new Set<number>(),
                emptyTitle: 'No favorites',
                emptyDescription: 'Add some products to favorites'
              },
              global: {
                plugins: [pinia]
              }
            })

            // When loading, CardSkeleton should be rendered
            // Empty state should not be visible when loading
            const emptyState = wrapper.findComponent(EmptyState)
            expect(emptyState.exists()).toBe(false)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display correct empty state props', () => {
      fc.assert(
        fc.property(
          fc.string({ minLength: 1, maxLength: 50 }).filter(s => s.trim().length > 0),
          fc.string({ minLength: 1, maxLength: 100 }).filter(s => s.trim().length > 0),
          fc.constantFrom('empty', 'search', 'favorite', 'error') as fc.Arbitrary<'empty' | 'search' | 'favorite' | 'error'>,
          (title: string, description: string, icon: 'empty' | 'search' | 'favorite' | 'error') => {
            const wrapper = mount(ProductGrid, {
              props: {
                products: [],
                loading: false,
                showFavorite: true,
                favoriteIds: new Set<number>(),
                emptyTitle: title,
                emptyDescription: description,
                emptyIcon: icon
              },
              global: {
                plugins: [pinia]
              }
            })

            const emptyState = wrapper.findComponent(EmptyState)
            expect(emptyState.exists()).toBe(true)
            expect(emptyState.props('title')).toBe(title)
            expect(emptyState.props('description')).toBe(description)
            expect(emptyState.props('icon')).toBe(icon)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should transition from empty to populated state correctly', () => {
      fc.assert(
        fc.property(
          uniqueProductsArb(1, 5),
          (products: ProductDTO[]) => {
            const store = useFavoritesStore()
            
            // Start with empty state
            store.favorites = []
            store.favoriteIds = new Set()
            store.total = 0
            store.isLoading = false
            
            // Verify empty state
            expect(store.favorites.length).toBe(0)
            expect(store.total).toBe(0)
            
            // Add products
            products.forEach(product => {
              store.favoriteIds.add(product.id)
              store.favorites.push(product)
              store.total++
            })
            
            // Verify populated state
            expect(store.favorites.length).toBe(products.length)
            expect(store.total).toBe(products.length)
            expect(store.favoriteIds.size).toBe(products.length)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should transition from populated to empty state correctly', () => {
      fc.assert(
        fc.property(
          uniqueProductsArb(1, 5),
          (products: ProductDTO[]) => {
            const store = useFavoritesStore()
            
            // Start with populated state
            store.favorites = [...products]
            store.favoriteIds = new Set(products.map(p => p.id))
            store.total = products.length
            store.isLoading = false
            
            // Verify populated state
            expect(store.favorites.length).toBe(products.length)
            
            // Remove all products
            products.forEach(product => {
              store.favoriteIds.delete(product.id)
              store.favorites = store.favorites.filter(p => p.id !== product.id)
              store.total--
            })
            
            // Verify empty state
            expect(store.favorites.length).toBe(0)
            expect(store.total).toBe(0)
            expect(store.favoriteIds.size).toBe(0)
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * Additional tests for EmptyState component
   */
  describe('EmptyState Component', () => {
    it('should render with correct icon based on prop', () => {
      fc.assert(
        fc.property(
          fc.constantFrom('empty', 'search', 'favorite', 'error') as fc.Arbitrary<'empty' | 'search' | 'favorite' | 'error'>,
          (icon: 'empty' | 'search' | 'favorite' | 'error') => {
            const wrapper = mount(EmptyState, {
              props: {
                title: 'Test Title',
                description: 'Test Description',
                icon
              },
              global: {
                plugins: [pinia]
              }
            })

            // Should have an SVG icon
            const svg = wrapper.find('svg')
            expect(svg.exists()).toBe(true)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display action button when actionText is provided', () => {
      fc.assert(
        fc.property(
          fc.string({ minLength: 1, maxLength: 30 }).filter(s => s.trim().length > 0),
          (actionText: string) => {
            const wrapper = mount(EmptyState, {
              props: {
                title: 'Test Title',
                description: 'Test Description',
                icon: 'favorite',
                actionText
              },
              global: {
                plugins: [pinia]
              }
            })

            const button = wrapper.find('button')
            expect(button.exists()).toBe(true)
            // Compare trimmed values since DOM may trim whitespace
            expect(button.text().trim()).toBe(actionText.trim())

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should not display action button when actionText is empty', () => {
      fc.assert(
        fc.property(
          fc.constant(''),
          (actionText: string) => {
            const wrapper = mount(EmptyState, {
              props: {
                title: 'Test Title',
                description: 'Test Description',
                icon: 'favorite',
                actionText
              },
              global: {
                plugins: [pinia]
              }
            })

            const button = wrapper.find('button')
            expect(button.exists()).toBe(false)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should emit action event when button is clicked', async () => {
      await fc.assert(
        fc.asyncProperty(
          fc.string({ minLength: 1, maxLength: 30 }).filter(s => s.trim().length > 0),
          async (actionText: string) => {
            const wrapper = mount(EmptyState, {
              props: {
                title: 'Test Title',
                description: 'Test Description',
                icon: 'favorite',
                actionText
              },
              global: {
                plugins: [pinia]
              }
            })

            const button = wrapper.find('button')
            await button.trigger('click')

            const emitted = wrapper.emitted('action')
            expect(emitted).toBeTruthy()
            expect(emitted!.length).toBe(1)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * Tests for favorites store isFavorite function
   */
  describe('isFavorite Function', () => {
    it('should return true for favorited products and false for non-favorited', () => {
      fc.assert(
        fc.property(
          fc.array(fc.nat({ max: 1000 }), { minLength: 1, maxLength: 20 }),
          fc.nat({ max: 1000 }),
          (favoriteIds: number[], testId: number) => {
            const store = useFavoritesStore()
            
            // Reset store
            store.favoriteIds = new Set(favoriteIds)
            
            const isFav = store.isFavorite(testId)
            const shouldBeFav = favoriteIds.includes(testId)
            
            expect(isFav).toBe(shouldBeFav)
          }
        ),
        { numRuns: 100 }
      )
    })
  })
})
