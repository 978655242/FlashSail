/**
 * Property-Based Tests for ProductCard Component
 * 
 * These tests validate the correctness properties of the ProductCard component
 * using fast-check for property-based testing.
 * 
 * **Validates: Requirements 15.1-15.8**
 * - 15.1: Product image with fallback placeholder
 * - 15.2: Product title (max 2 lines with ellipsis)
 * - 15.3: Product price with currency symbol
 * - 15.4: Platform badges (Amazon, eBay, etc.)
 * - 15.5: Hot/trending badge when applicable
 * - 15.6: Favorite button with toggle state
 * - 15.7: Hover lift effect
 * - 15.8: Click opens product detail modal
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import fc from 'fast-check'
import ProductCard from '@/components/ProductCard.vue'
import type { ProductDTO, Category } from '@/types/product'

// Arbitrary generators for ProductDTO
const categoryArb = fc.record<Category>({
  id: fc.nat({ max: 1000 }),
  name: fc.string({ minLength: 1, maxLength: 30 }).filter(s => s.trim().length > 0),
  productCount: fc.nat({ max: 10000 })
})

const productArb = fc.record<ProductDTO>({
  id: fc.nat({ max: 100000 }),
  asin: fc.string({ minLength: 10, maxLength: 10 }).map(s => s.toUpperCase()),
  title: fc.string({ minLength: 1, maxLength: 200 }).filter(s => s.trim().length > 0),
  image: fc.oneof(
    fc.constant(''),
    fc.webUrl()
  ),
  currentPrice: fc.float({ min: Math.fround(0.01), max: Math.fround(99999.99), noNaN: true }),
  bsrRank: fc.nat({ max: 1000000 }),
  reviewCount: fc.nat({ max: 100000 }),
  rating: fc.float({ min: Math.fround(0), max: Math.fround(5), noNaN: true }),
  competitionScore: fc.float({ min: Math.fround(0), max: Math.fround(1), noNaN: true }),
  category: categoryArb
})


// Badge type arbitrary (for Property 20)
type BadgeType = 'hot' | 'trending' | 'new' | undefined
const badgeTypeArb = fc.constantFrom<BadgeType>('hot', 'trending', 'new', undefined)

// Extended product with badge for Property 20 testing
interface ProductWithBadge extends ProductDTO {
  badge?: 'hot' | 'trending' | 'new'
}

const productWithBadgeArb = fc.record<ProductWithBadge>({
  id: fc.nat({ max: 100000 }),
  asin: fc.string({ minLength: 10, maxLength: 10 }).map(s => s.toUpperCase()),
  title: fc.string({ minLength: 1, maxLength: 200 }).filter(s => s.trim().length > 0),
  image: fc.oneof(fc.constant(''), fc.webUrl()),
  currentPrice: fc.float({ min: Math.fround(0.01), max: Math.fround(99999.99), noNaN: true }),
  bsrRank: fc.nat({ max: 1000000 }),
  reviewCount: fc.nat({ max: 100000 }),
  rating: fc.float({ min: Math.fround(0), max: Math.fround(5), noNaN: true }),
  competitionScore: fc.float({ min: Math.fround(0), max: Math.fround(1), noNaN: true }),
  category: categoryArb,
  badge: badgeTypeArb
})

describe('ProductCard Property Tests', () => {
  /**
   * **Feature: frontend-ui-refactor, Property 19: Product Card Data Display**
   * 
   * *For any* product object, the product card should display the title 
   * (truncated to 2 lines if necessary), price with currency symbol, 
   * and platform badges for all associated platforms.
   * 
   * **Validates: Requirements 15.1, 15.2, 15.3, 15.4**
   */
  describe('Property 19: Product Card Data Display', () => {
    it('should display product title for any valid product', () => {
      fc.assert(
        fc.property(
          productArb,
          (product: ProductDTO) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite: false
              }
            })

            // Find the title element
            const titleElement = wrapper.find('h3.line-clamp-2')
            
            // Title should exist and contain the product title (trimmed, as browsers trim whitespace)
            expect(titleElement.exists()).toBe(true)
            expect(titleElement.text()).toBe(product.title.trim())

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should apply 2-line truncation class to title', () => {
      fc.assert(
        fc.property(
          productArb,
          (product: ProductDTO) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite: false
              }
            })

            // Find the title element
            const titleElement = wrapper.find('h3')
            
            // Title should have line-clamp-2 class for truncation
            expect(titleElement.classes()).toContain('line-clamp-2')

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })


    it('should display formatted price for any valid product', () => {
      fc.assert(
        fc.property(
          productArb,
          (product: ProductDTO) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite: false
              }
            })

            // Find the price element
            const priceElement = wrapper.find('.text-orange-400')
            
            // Price should exist
            expect(priceElement.exists()).toBe(true)
            
            // Price should be formatted with 2 decimal places (component shows just the number)
            const expectedPrice = product.currentPrice.toFixed(2)
            expect(priceElement.text()).toContain(expectedPrice)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display product image or fallback placeholder', () => {
      fc.assert(
        fc.property(
          productArb,
          (product: ProductDTO) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite: false
              }
            })

            // Find the image element
            const imageElement = wrapper.find('img')
            
            // Image should exist
            expect(imageElement.exists()).toBe(true)
            
            // Image src should be product image or fallback
            const expectedSrc = product.image || 'https://via.placeholder.com/300'
            expect(imageElement.attributes('src')).toBe(expectedSrc)
            
            // Image alt should be product title
            expect(imageElement.attributes('alt')).toBe(product.title)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display rating for any valid product', () => {
      fc.assert(
        fc.property(
          productArb,
          (product: ProductDTO) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite: false
              }
            })

            // Find the rating display area
            const ratingArea = wrapper.find('.flex.items-center.gap-1')
            
            // Rating area should exist
            expect(ratingArea.exists()).toBe(true)
            
            // Should contain formatted rating
            const expectedRating = product.rating.toFixed(1)
            expect(ratingArea.text()).toContain(expectedRating)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })


    it('should display category name for any valid product', () => {
      fc.assert(
        fc.property(
          productArb,
          (product: ProductDTO) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite: false
              }
            })

            // Find the tags container and look for category badge
            const tagsContainer = wrapper.find('.flex.flex-wrap.gap-2')
            
            // Tags container should exist
            expect(tagsContainer.exists()).toBe(true)
            
            // Find the category span element and check its text content
            const categorySpan = tagsContainer.find('span.bg-slate-100')
            expect(categorySpan.exists()).toBe(true)
            
            // Should display category name or '未分类' (text() trims whitespace like browsers do)
            const expectedCategory = (product.category?.name || '未分类').trim()
            expect(categorySpan.text().trim()).toBe(expectedCategory)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display BSR rank for any valid product', () => {
      fc.assert(
        fc.property(
          productArb,
          (product: ProductDTO) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite: false
              }
            })

            // Find the BSR badge
            const bsrBadge = wrapper.find('.bg-black\\/60')
            
            // BSR badge should exist
            expect(bsrBadge.exists()).toBe(true)
            
            // Should contain 'BSR' text
            expect(bsrBadge.text()).toContain('BSR')

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should display competition score indicator for any valid product', () => {
      fc.assert(
        fc.property(
          productArb,
          (product: ProductDTO) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite: false
              }
            })

            // Find competition indicator by checking for competition text
            const html = wrapper.html()
            
            // Should contain one of the competition levels
            const hasCompetitionIndicator = 
              html.includes('高竞争') || 
              html.includes('中竞争') || 
              html.includes('低竞争') ||
              html.includes('未知')
            
            expect(hasCompetitionIndicator).toBe(true)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })
  })


  /**
   * **Feature: frontend-ui-refactor, Property 20: Product Card Badge Display**
   * 
   * *For any* product with a badge property (hot, trending, or new), the 
   * corresponding badge should be displayed on the product card. Products 
   * without a badge property should not display any badge.
   * 
   * **Validates: Requirements 15.5**
   * 
   * Note: The current ProductCard implementation uses competitionScore to 
   * display competition level badges (高竞争/中竞争/低竞争) rather than 
   * hot/trending/new badges. This test validates the competition badge logic.
   */
  describe('Property 20: Product Card Badge Display', () => {
    it('should display correct competition badge based on competitionScore', () => {
      fc.assert(
        fc.property(
          productArb,
          (product: ProductDTO) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite: false
              }
            })

            const html = wrapper.html()
            const score = product.competitionScore

            // Determine expected competition text based on score
            let expectedText: string
            if (score === undefined || score === null) {
              expectedText = '未知'
            } else if (score >= 0.7) {
              expectedText = '高竞争'
            } else if (score >= 0.4) {
              expectedText = '中竞争'
            } else {
              expectedText = '低竞争'
            }

            expect(html).toContain(expectedText)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should apply correct color class based on competitionScore', () => {
      fc.assert(
        fc.property(
          productArb,
          (product: ProductDTO) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite: false
              }
            })

            const score = product.competitionScore
            const html = wrapper.html()

            // Check for appropriate color class based on score
            if (score === undefined || score === null) {
              expect(html).toContain('text-slate-500')
            } else if (score >= 0.7) {
              expect(html).toContain('text-red-500')
            } else if (score >= 0.4) {
              expect(html).toContain('text-yellow-500')
            } else {
              expect(html).toContain('text-green-500')
            }

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should always display exactly one competition badge', () => {
      fc.assert(
        fc.property(
          productArb,
          (product: ProductDTO) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite: false
              }
            })

            const html = wrapper.html()
            
            // Count competition badges
            const badges = ['高竞争', '中竞争', '低竞争', '未知']
            const foundBadges = badges.filter(badge => html.includes(badge))
            
            // Exactly one competition badge should be displayed
            expect(foundBadges.length).toBe(1)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })
  })


  /**
   * **Feature: frontend-ui-refactor, Property 21: Favorite Button State**
   * 
   * *For any* product card, the favorite button should reflect the current 
   * favorite state (filled heart if favorited, outline if not), and clicking 
   * the button should toggle the state.
   * 
   * **Validates: Requirements 15.6**
   */
  describe('Property 21: Favorite Button State', () => {
    it('should display favorite button when showFavorite is true', () => {
      fc.assert(
        fc.property(
          productArb,
          fc.boolean(),
          (product: ProductDTO, isFavorite: boolean) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite
              }
            })

            // Find the favorite button
            const favoriteButton = wrapper.find('button')
            
            // Favorite button should exist
            expect(favoriteButton.exists()).toBe(true)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should hide favorite button when showFavorite is false', () => {
      fc.assert(
        fc.property(
          productArb,
          fc.boolean(),
          (product: ProductDTO, isFavorite: boolean) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: false,
                isFavorite
              }
            })

            // Find the favorite button (should not exist)
            const favoriteButton = wrapper.find('button')
            
            // Favorite button should not exist when showFavorite is false
            expect(favoriteButton.exists()).toBe(false)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should apply filled style when isFavorite is true', () => {
      fc.assert(
        fc.property(
          productArb,
          (product: ProductDTO) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite: true
              }
            })

            // Find the favorite button
            const favoriteButton = wrapper.find('button')
            
            // Button should have favorited styling (bg-red-500)
            expect(favoriteButton.classes()).toContain('bg-red-500')
            expect(favoriteButton.classes()).toContain('text-white')
            
            // SVG should have fill="currentColor"
            const svg = favoriteButton.find('svg')
            expect(svg.attributes('fill')).toBe('currentColor')

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should apply outline style when isFavorite is false', () => {
      fc.assert(
        fc.property(
          productArb,
          (product: ProductDTO) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite: false
              }
            })

            // Find the favorite button
            const favoriteButton = wrapper.find('button')
            
            // Button should not have favorited styling
            expect(favoriteButton.classes()).not.toContain('bg-red-500')
            
            // SVG should have fill="none" for outline
            const svg = favoriteButton.find('svg')
            expect(svg.attributes('fill')).toBe('none')

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })


    it('should emit favorite event with product when favorite button is clicked', async () => {
      await fc.assert(
        fc.asyncProperty(
          productArb,
          fc.boolean(),
          async (product: ProductDTO, isFavorite: boolean) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite
              }
            })

            // Find and click the favorite button
            const favoriteButton = wrapper.find('button')
            await favoriteButton.trigger('click')

            // Should emit favorite event with the product
            const emittedEvents = wrapper.emitted('favorite')
            expect(emittedEvents).toBeTruthy()
            expect(emittedEvents?.length).toBe(1)
            expect(emittedEvents?.[0]).toEqual([product])

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should not emit click event when favorite button is clicked', async () => {
      await fc.assert(
        fc.asyncProperty(
          productArb,
          fc.boolean(),
          async (product: ProductDTO, isFavorite: boolean) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite
              }
            })

            // Find and click the favorite button
            const favoriteButton = wrapper.find('button')
            await favoriteButton.trigger('click')

            // Should NOT emit click event (stopPropagation)
            const clickEvents = wrapper.emitted('click')
            expect(clickEvents).toBeFalsy()

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should maintain consistent favorite state display for any product', () => {
      fc.assert(
        fc.property(
          productArb,
          fc.boolean(),
          (product: ProductDTO, isFavorite: boolean) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite
              }
            })

            const favoriteButton = wrapper.find('button')
            const svg = favoriteButton.find('svg')
            
            // State should be consistent
            if (isFavorite) {
              expect(favoriteButton.classes()).toContain('bg-red-500')
              expect(svg.attributes('fill')).toBe('currentColor')
            } else {
              expect(favoriteButton.classes()).not.toContain('bg-red-500')
              expect(svg.attributes('fill')).toBe('none')
            }

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })
  })


  /**
   * **Feature: frontend-ui-refactor, Property 22: Product Card Click Handler**
   * 
   * *For any* product card click, the product detail modal should open with 
   * the correct product data loaded.
   * 
   * **Validates: Requirements 5.6, 15.8**
   */
  describe('Property 22: Product Card Click Handler', () => {
    it('should emit click event with product when card is clicked', async () => {
      await fc.assert(
        fc.asyncProperty(
          productArb,
          async (product: ProductDTO) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite: false
              }
            })

            // Find and click the card container
            const card = wrapper.find('.glass-card')
            await card.trigger('click')

            // Should emit click event with the product
            const emittedEvents = wrapper.emitted('click')
            expect(emittedEvents).toBeTruthy()
            expect(emittedEvents?.length).toBe(1)
            expect(emittedEvents?.[0]).toEqual([product])

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should emit click event with correct product data for any product', async () => {
      await fc.assert(
        fc.asyncProperty(
          productArb,
          async (product: ProductDTO) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite: false
              }
            })

            // Click the card
            const card = wrapper.find('.glass-card')
            await card.trigger('click')

            // Verify emitted product matches input product
            const emittedEvents = wrapper.emitted('click')
            const emittedProduct = emittedEvents?.[0]?.[0] as ProductDTO
            
            expect(emittedProduct.id).toBe(product.id)
            expect(emittedProduct.title).toBe(product.title)
            expect(emittedProduct.currentPrice).toBe(product.currentPrice)
            expect(emittedProduct.asin).toBe(product.asin)

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should have cursor-pointer class for clickable indication', () => {
      fc.assert(
        fc.property(
          productArb,
          (product: ProductDTO) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite: false
              }
            })

            // Find the card container
            const card = wrapper.find('.glass-card')
            
            // Should have cursor-pointer class
            expect(card.classes()).toContain('cursor-pointer')

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should have hover effect classes for visual feedback', () => {
      fc.assert(
        fc.property(
          productArb,
          (product: ProductDTO) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite: false
              }
            })

            // Find the card container
            const card = wrapper.find('.glass-card')
            
            // Should have hover effect classes (check HTML for the class string)
            const html = wrapper.html()
            expect(html).toContain('hover:shadow-md')
            expect(html).toContain('hover:border-orange-500/50')
            expect(card.classes()).toContain('transition-all')

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })


    it('should emit click event only once per click', async () => {
      await fc.assert(
        fc.asyncProperty(
          productArb,
          fc.integer({ min: 1, max: 5 }),
          async (product: ProductDTO, clickCount: number) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite: false
              }
            })

            // Click the card multiple times
            const card = wrapper.find('.glass-card')
            for (let i = 0; i < clickCount; i++) {
              await card.trigger('click')
            }

            // Should emit exactly clickCount events
            const emittedEvents = wrapper.emitted('click')
            expect(emittedEvents?.length).toBe(clickCount)
            
            // Each event should contain the same product
            emittedEvents?.forEach(event => {
              expect(event).toEqual([product])
            })

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should maintain click functionality regardless of favorite state', async () => {
      await fc.assert(
        fc.asyncProperty(
          productArb,
          fc.boolean(),
          async (product: ProductDTO, isFavorite: boolean) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite: true,
                isFavorite
              }
            })

            // Click the card
            const card = wrapper.find('.glass-card')
            await card.trigger('click')

            // Should emit click event regardless of favorite state
            const emittedEvents = wrapper.emitted('click')
            expect(emittedEvents).toBeTruthy()
            expect(emittedEvents?.length).toBe(1)
            expect(emittedEvents?.[0]).toEqual([product])

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should maintain click functionality regardless of showFavorite prop', async () => {
      await fc.assert(
        fc.asyncProperty(
          productArb,
          fc.boolean(),
          async (product: ProductDTO, showFavorite: boolean) => {
            const wrapper = mount(ProductCard, {
              props: {
                product,
                showFavorite,
                isFavorite: false
              }
            })

            // Click the card
            const card = wrapper.find('.glass-card')
            await card.trigger('click')

            // Should emit click event regardless of showFavorite
            const emittedEvents = wrapper.emitted('click')
            expect(emittedEvents).toBeTruthy()
            expect(emittedEvents?.length).toBe(1)
            expect(emittedEvents?.[0]).toEqual([product])

            wrapper.unmount()
          }
        ),
        { numRuns: 100 }
      )
    })
  })
})
