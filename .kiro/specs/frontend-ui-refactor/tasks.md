# Implementation Plan: Frontend UI Refactor

## Overview

This implementation plan breaks down the frontend refactoring into discrete, incremental tasks. Each task builds on previous work and includes testing requirements. The implementation follows the UI design in `docs/3.UI-Design/index.html` using Vue 3, TypeScript, and Tailwind CSS.

## Tasks

- [x] 1. Set up theme system and CSS variables
  - [x] 1.1 Create CSS variables file with dark/light mode support
    - Create `src/styles/variables.css` with all CSS custom properties
    - Define color palette, glassmorphism variables, shadows, transitions, and spacing
    - Add light mode overrides using `html.light` selector
    - _Requirements: 1.1, 1.2, 1.6_
  
  - [x] 1.2 Create useTheme composable
    - Implement theme state management with Pinia or ref
    - Add toggle, setTheme functions
    - Implement localStorage persistence
    - Add initialization from stored preference
    - _Requirements: 1.3, 1.4, 1.5_
  
  - [x] 1.3 Write property tests for theme system
    - **Property 1: Theme Toggle Round-Trip**
    - **Property 2: Theme Persistence**
    - **Validates: Requirements 1.3, 1.4, 1.5**

- [x] 2. Implement aurora background and glassmorphism styles
  - [x] 2.1 Create global styles for aurora background
    - Create `src/styles/aurora.css` with radial gradient backgrounds
    - Add animation keyframes for subtle movement
    - Implement theme-aware color adjustments
    - _Requirements: 2.1, 2.3_
  
  - [x] 2.2 Create glassmorphism utility classes
    - Create `src/styles/glassmorphism.css` with glass-card classes
    - Add backdrop-filter blur effects
    - Implement hover states with enhanced visual feedback
    - Add theme-aware glass backgrounds
    - _Requirements: 2.2, 2.4, 2.5_
  
  - [x] 2.3 Update main style.css to import all style modules
    - Import variables.css, aurora.css, glassmorphism.css
    - Ensure proper cascade order
    - _Requirements: 2.1, 2.2_

- [x] 3. Checkpoint - Verify theme and styles
  - Ensure theme toggle works correctly
  - Verify aurora background displays properly
  - Check glassmorphism effects on sample elements
  - Ask the user if questions arise

- [x] 4. Implement Sidebar component
  - [x] 4.1 Create Sidebar.vue component
    - Implement navigation items with icons (Dashboard, Search, Favorites, Analysis, Subscription, Profile)
    - Add FlashSell logo and branding
    - Implement collapse/expand functionality
    - Add active state highlighting based on current route
    - _Requirements: 3.1, 3.2, 3.4, 3.5_
  
  - [x] 4.2 Add user profile section to Sidebar
    - Display user avatar, name, and plan when logged in
    - Add logout button for authenticated users
    - Implement conditional rendering based on auth state
    - _Requirements: 3.6, 3.7_
  
  - [x] 4.3 Implement mobile responsive behavior
    - Hide sidebar by default on mobile
    - Add hamburger menu toggle button
    - Implement slide-in animation for mobile
    - _Requirements: 3.8, 12.1_
  
  - [x] 4.4 Write property tests for Sidebar
    - **Property 5: Sidebar Collapse State**
    - **Property 6: Active Navigation Highlighting**
    - **Property 7: User Authentication State Display**
    - **Validates: Requirements 3.2, 3.3, 3.4, 3.6, 3.7**

- [x] 5. Implement MainLayout component
  - [x] 5.1 Create MainLayout.vue
    - Integrate Sidebar component
    - Add main content area with proper margins
    - Implement responsive layout adjustments when sidebar collapses
    - Add aurora background to layout
    - _Requirements: 3.3, 12.1_
  
  - [x] 5.2 Create TopNav component (optional header)
    - Add language toggle button
    - Add theme toggle button
    - Display current language badge
    - _Requirements: 5.8, 13.3_

- [x] 6. Implement internationalization (i18n) system
  - [x] 6.1 Create i18n store and composable
    - Create `src/stores/i18n.ts` with locale state
    - Create `src/composables/useI18n.ts` with t() function
    - Implement localStorage persistence for language preference
    - _Requirements: 13.1, 13.2, 13.4_
  
  - [x] 6.2 Create translation files
    - Create `src/locales/zh.ts` with Chinese translations
    - Create `src/locales/en.ts` with English translations
    - Cover all UI text: navigation, buttons, labels, messages
    - _Requirements: 13.1, 13.2_
  
  - [x] 6.3 Write property tests for i18n
    - **Property 14: Language Persistence**
    - **Property 15: Language Switch Reactivity**
    - **Validates: Requirements 13.4, 13.5**

- [x] 7. Checkpoint - Verify layout and navigation
  - Ensure sidebar navigation works
  - Verify layout responds to sidebar collapse
  - Check i18n language switching
  - Ask the user if questions arise

- [x] 8. Implement Login page
  - [x] 8.1 Create Login.vue page component
    - Implement login/register form with phone and verification code inputs
    - Add FlashSell branding and logo
    - Implement mode switching between login and register
    - Add form validation with error display
    - _Requirements: 4.1, 4.2, 4.5, 4.7_
  
  - [x] 8.2 Implement phone validation
    - Create `src/utils/validators.ts` with validatePhone function
    - Implement Chinese mobile number pattern validation
    - Add real-time validation feedback
    - _Requirements: 4.4_
  
  - [x] 8.3 Implement verification code countdown
    - Add 60-second countdown after sending code
    - Disable send button during countdown
    - Show countdown timer in button
    - _Requirements: 4.3_
  
  - [x] 8.4 Integrate with auth API
    - Connect to /api/auth/send-code endpoint
    - Connect to /api/auth/login and /api/auth/register endpoints
    - Handle success redirect to dashboard
    - Display error messages on failure
    - _Requirements: 4.6, 4.8_
  
  - [x] 8.5 Write property tests for Login
    - **Property 3: Phone Number Validation**
    - **Property 4: Login Mode Toggle**
    - **Validates: Requirements 4.4, 4.5**

- [x] 9. Implement common UI components
  - [x] 9.1 Create GlassCard.vue component
    - Implement glassmorphism card with configurable props
    - Add hover effects
    - Support different sizes and variants
    - _Requirements: 2.2, 2.4_
  
  - [x] 9.2 Create Button.vue component
    - Implement primary, secondary, ghost variants
    - Add loading state with spinner
    - Support disabled state
    - _Requirements: 4.2, 9.6_
  
  - [x] 9.3 Create Badge.vue component
    - Implement hot, trending, new, platform variants
    - Support different colors for platforms (Amazon, eBay, etc.)
    - _Requirements: 15.4, 15.5_
  
  - [x] 9.4 Create Skeleton.vue component
    - Implement skeleton loader for cards, lists, text
    - Add shimmer animation
    - _Requirements: 14.1_
  
  - [x] 9.5 Create Toast.vue component and useToast composable
    - Implement success, error, warning, info variants
    - Add auto-dismiss with configurable duration
    - Support multiple toasts
    - _Requirements: 14.2, 14.3_

- [x] 10. Implement ProductCard component
  - [x] 10.1 Create ProductCard.vue
    - Display product image with fallback placeholder
    - Show title with 2-line truncation
    - Display price with currency formatting
    - Add platform badges
    - Show hot/trending badge when applicable
    - Implement favorite button with toggle state
    - Add hover lift effect
    - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.5, 15.6, 15.7_
  
  - [x] 10.2 Create ProductGrid.vue
    - Implement responsive grid layout (1-4 columns)
    - Support loading state with skeleton cards
    - Handle empty state
    - _Requirements: 12.4, 14.1_
  
  - [x] 10.3 Write property tests for ProductCard
    - **Property 19: Product Card Data Display**
    - **Property 20: Product Card Badge Display**
    - **Property 21: Favorite Button State**
    - **Property 22: Product Card Click Handler**
    - **Validates: Requirements 15.1-15.8**

- [x] 11. Checkpoint - Verify common components
  - Test all common UI components
  - Verify ProductCard displays correctly
  - Check responsive grid behavior
  - Ask the user if questions arise

- [x] 12. Implement ProductModal component
  - [x] 12.1 Create ProductModal.vue
    - Implement modal overlay with backdrop blur
    - Display product title, description, price
    - Add close button and click-outside-to-close
    - Implement slide-in animation
    - _Requirements: 9.1, 9.7_
  
  - [x] 12.2 Add multi-platform comparison section
    - Display price comparison cards for Amazon, eBay, AliExpress, TikTok
    - Show platform-specific metrics (rating, reviews, shipping)
    - Highlight best price platform
    - _Requirements: 9.2_
  
  - [x] 12.3 Add price trend chart
    - Integrate ECharts for price history visualization
    - Create PriceTrendChart.vue component
    - Display 30-day price trend
    - _Requirements: 9.3_
  
  - [x] 12.4 Add competitive analysis radar chart
    - Create RadarChart.vue component
    - Display metrics: price competitiveness, market demand, profit potential, competition level, trend score
    - _Requirements: 9.4_
  
  - [x] 12.5 Add AI analysis section
    - Display AI summary with confidence score
    - Show recommendation (buy/watch/skip)
    - List key highlights
    - _Requirements: 9.5_
  
  - [x] 12.6 Add action buttons
    - Add to Favorites button
    - View on Platform button
    - Share button
    - _Requirements: 9.6_
  
  - [x] 12.7 Integrate with product API
    - Connect to /api/products/{id} endpoint
    - Handle loading state
    - Handle error state
    - _Requirements: 9.8_
  
  - [x] 12.8 Write property tests for ProductModal
    - **Property 12: Product Modal Display**
    - **Property 13: Modal Close Behavior**
    - **Validates: Requirements 9.1, 9.2, 9.5, 9.7**

- [x] 13. Implement Dashboard page
  - [x] 13.1 Create Dashboard.vue
    - Add welcome message with user name (conditional)
    - Implement four stat cards with icons and change indicators
    - Add language toggle in header
    - _Requirements: 5.1, 5.2, 5.8_
  
  - [x] 13.2 Add AI recommendations section
    - Display product grid with AI-recommended products
    - Add "View All" link
    - Integrate with /api/dashboard/recommendations endpoint
    - _Requirements: 5.3, 5.7_
  
  - [x] 13.3 Add trending categories section
    - Display category cards with icons
    - Show category growth indicators
    - _Requirements: 5.4_
  
  - [x] 13.4 Add recent activity section
    - Display activity list with timestamps
    - Show activity type icons
    - _Requirements: 5.5_
  
  - [x] 13.5 Wire up product card click to modal
    - Open ProductModal when clicking product card
    - Fetch product details from API
    - _Requirements: 5.6_
  
  - [x] 13.6 Write property tests for Dashboard
    - **Property 25: Dashboard Welcome Message**
    - **Validates: Requirements 5.1**

- [x] 14. Checkpoint - Verify Dashboard
  - Ensure Dashboard displays all sections
  - Verify stat cards show correct data
  - Test product modal opening
  - Ask the user if questions arise

- [x] 15. Implement Search page (Chat-style interface)
  - [x] 15.1 Create Search.vue with chat layout
    - Implement chat container with AI avatar and welcome message
    - Add suggestion chips for quick searches
    - Position search input at bottom
    - _Requirements: 6.1, 6.2, 6.3_
  
  - [x] 15.2 Create ChatMessage.vue component
    - Implement user message bubble (right-aligned, orange)
    - Implement AI message bubble (left-aligned, with avatar)
    - Support typing indicator
    - _Requirements: 6.4_
  
  - [x] 15.3 Create SearchInput.vue component
    - Implement text area with auto-resize
    - Add send button with active state
    - Add image upload button (optional)
    - Show character count
    - _Requirements: 6.2_
  
  - [x] 15.4 Create ThinkingIndicator.vue component
    - Display AI thinking steps with progress
    - Show spinner animation
    - Implement step completion states
    - _Requirements: 6.5_
  
  - [x] 15.5 Implement search functionality
    - Connect to /api/search endpoint
    - Display user query as message
    - Show thinking indicator during search
    - Display results in grid layout
    - Show AI summary
    - _Requirements: 6.4, 6.5, 6.6, 6.8, 6.9_
  
  - [x] 15.6 Add filter tabs
    - Implement category, price range, platform filters
    - Update search results on filter change
    - _Requirements: 6.7_
  
  - [x] 15.7 Write property tests for Search
    - **Property 8: Search Query Display**
    - **Property 9: Search Loading State**
    - **Validates: Requirements 6.4, 6.5, 6.6**

- [x] 16. Implement Favorites page
  - [x] 16.1 Create Favorites.vue
    - Display favorites grid using ProductGrid
    - Show total favorites count
    - Implement empty state when no favorites
    - _Requirements: 7.1, 7.3, 7.6_
  
  - [x] 16.2 Create favorites store
    - Create `src/stores/favorites.ts`
    - Implement add, remove, check favorite functions
    - Sync with /api/favorites/* endpoints
    - _Requirements: 7.4_
  
  - [x] 16.3 Implement remove from favorites
    - Add remove button to product cards in favorites
    - Update UI immediately on remove
    - Sync with backend
    - _Requirements: 7.2, 7.5_
  
  - [x] 16.4 Write property tests for Favorites
    - **Property 10: Favorites List Consistency**
    - **Property 11: Empty State Display**
    - **Validates: Requirements 7.1, 7.2, 7.3, 7.5, 7.6**

- [x] 17. Implement Analysis page
  - [x] 17.1 Create Analysis.vue
    - Implement page layout with chart sections
    - Add time period and category filters
    - _Requirements: 8.5_
  
  - [x] 17.2 Add market trend chart
    - Create TrendChart.vue using ECharts
    - Display market trend data over time
    - _Requirements: 8.1_
  
  - [x] 17.3 Add category performance section
    - Display category cards with performance metrics
    - Show growth indicators
    - _Requirements: 8.2_
  
  - [x] 17.4 Add platform comparison section
    - Display comparison table/cards for Amazon, eBay, AliExpress, TikTok
    - Show platform-specific metrics
    - _Requirements: 8.3_
  
  - [x] 17.5 Integrate with market API
    - Connect to /api/market/* endpoints
    - Handle loading and error states
    - _Requirements: 8.4_

- [x] 18. Checkpoint - Verify Search, Favorites, Analysis
  - Test search chat interface
  - Verify favorites add/remove functionality
  - Check analysis charts display
  - Ask the user if questions arise

- [x] 19. Implement Subscription page
  - [x] 19.1 Create Subscription.vue
    - Display subscription plan cards (Free, Pro, Enterprise)
    - Show plan features and pricing
    - Highlight current user plan
    - _Requirements: 10.1, 10.2, 10.3_
  
  - [x] 19.2 Add upgrade/downgrade functionality
    - Implement plan selection buttons
    - Connect to /api/subscription/* endpoints
    - Handle plan change flow
    - _Requirements: 10.4, 10.5_
  
  - [x] 19.3 Write property tests for Subscription
    - **Property 23: Subscription Plan Highlighting**
    - **Validates: Requirements 10.2**

- [x] 20. Implement Profile page
  - [x] 20.1 Create Profile.vue
    - Display user information (name, phone, avatar)
    - Show account statistics
    - Add logout button
    - _Requirements: 11.1, 11.3, 11.4_
  
  - [x] 20.2 Implement nickname editing
    - Add edit mode for nickname
    - Connect to /api/user/* endpoints
    - Handle save and cancel
    - _Requirements: 11.2, 11.5_
  
  - [x] 20.3 Write property tests for Profile
    - **Property 24: Profile Data Display**
    - **Validates: Requirements 11.1, 11.2**

- [x] 21. Implement error handling and loading states
  - [x] 21.1 Update API client with retry logic
    - Implement automatic retry (max 2) for network/server errors
    - Add proper error type classification
    - _Requirements: 14.4_
  
  - [x] 21.2 Add error display components
    - Create ErrorMessage.vue component
    - Add retry button functionality
    - Handle network error display
    - _Requirements: 14.2, 14.3_
  
  - [x] 21.3 Add loading states to all pages
    - Implement skeleton loaders for each page
    - Show loading spinners for actions
    - _Requirements: 14.1_
  
  - [x] 21.4 Write property tests for error handling
    - **Property 16: Loading State Display**
    - **Property 17: Error State Display**
    - **Property 18: API Retry Logic**
    - **Validates: Requirements 14.1, 14.2, 14.3, 14.4**

- [x] 22. Final integration and testing
  - [x] 22.1 Update router configuration
    - Configure all routes with proper meta
    - Add navigation guards for auth-required pages
    - Implement redirect logic
  
  - [x] 22.2 Update App.vue
    - Add aurora background
    - Configure theme initialization
    - Set up global error handling
  
  - [x] 22.3 Run all tests and fix issues
    - Run unit tests
    - Run property tests
    - Fix any failing tests

- [x] 23. Final checkpoint - Complete integration test
  - Test complete user flow: login → dashboard → search → favorites → logout
  - Verify all pages render correctly
  - Check responsive behavior on different screen sizes
  - Verify API integration works with backend
  - Ask the user if questions arise

## Notes

- All tasks are required for comprehensive implementation
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties
- Unit tests validate specific examples and edge cases
- The implementation follows the existing project structure in `flashsell-web`
