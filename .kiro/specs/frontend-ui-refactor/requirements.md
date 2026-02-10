# Requirements Document

## Introduction

This document defines the requirements for refactoring the FlashSell frontend to match the UI design in `docs/3.UI-Design/index.html`. The refactoring involves implementing a modern glassmorphism design with aurora background effects, dark/light theme support, collapsible sidebar navigation, and complete integration with the existing backend APIs.

## Glossary

- **Frontend**: The Vue 3 + TypeScript web application in `flashsell-web` directory
- **UI_Design**: The reference HTML design file at `docs/3.UI-Design/index.html`
- **Glassmorphism**: A design style featuring frosted glass effects with blur and transparency
- **Aurora_Background**: Animated gradient background effect with radial gradients
- **Theme_System**: The dark/light mode switching functionality
- **Sidebar**: The collapsible left navigation panel
- **Product_Card**: A card component displaying product information
- **Product_Modal**: A detailed product view popup with multi-platform comparison
- **API_Client**: The Axios-based HTTP client for backend communication

## Requirements

### Requirement 1: Theme System Implementation

**User Story:** As a user, I want to switch between dark and light themes, so that I can use the application comfortably in different lighting conditions.

#### Acceptance Criteria

1. THE Theme_System SHALL support dark mode as the default theme
2. THE Theme_System SHALL support light mode with appropriate color adjustments
3. WHEN a user clicks the theme toggle button, THE Theme_System SHALL switch between dark and light modes
4. THE Theme_System SHALL persist the user's theme preference in local storage
5. WHEN the application loads, THE Theme_System SHALL restore the user's previously selected theme
6. THE Theme_System SHALL define CSS variables for all theme colors (--bg-dark, --bg-card, --text-primary, --text-secondary, --border, etc.)

### Requirement 2: Aurora Background and Glassmorphism Effects

**User Story:** As a user, I want a visually appealing interface with modern design effects, so that the application feels premium and engaging.

#### Acceptance Criteria

1. THE Frontend SHALL display an aurora background effect using radial gradients
2. THE Frontend SHALL apply glassmorphism effects to card components with backdrop-filter blur
3. THE Aurora_Background SHALL adapt colors based on the current theme (dark/light)
4. THE glassmorphism cards SHALL have semi-transparent backgrounds with subtle borders
5. WHEN hovering over glass cards, THE Frontend SHALL display enhanced visual feedback with border color changes

### Requirement 3: Sidebar Navigation

**User Story:** As a user, I want a sidebar navigation that can be collapsed, so that I can maximize screen space when needed.

#### Acceptance Criteria

1. THE Sidebar SHALL display navigation items: Dashboard, Search, Favorites, Analysis, Subscription, Profile
2. THE Sidebar SHALL support collapsing to show only icons
3. WHEN the sidebar is collapsed, THE main content area SHALL expand to fill the available space
4. THE Sidebar SHALL highlight the currently active navigation item
5. THE Sidebar SHALL display the FlashSell logo and branding
6. THE Sidebar SHALL display user profile information when logged in
7. THE Sidebar SHALL provide a logout button for authenticated users
8. WHEN on mobile devices, THE Sidebar SHALL be hidden by default and accessible via a toggle button

### Requirement 4: Login and Registration Page

**User Story:** As a user, I want to log in or register using my phone number and verification code, so that I can access personalized features.

#### Acceptance Criteria

1. THE Login page SHALL display a form with phone number and verification code inputs
2. THE Login page SHALL provide a button to send verification code
3. WHEN the verification code is sent, THE Login page SHALL display a countdown timer (60 seconds)
4. THE Login page SHALL validate phone number format (Chinese mobile: 1[3-9]XXXXXXXXX)
5. THE Login page SHALL support switching between login and registration modes
6. WHEN login is successful, THE Frontend SHALL redirect to the dashboard or previous page
7. THE Login page SHALL display the FlashSell branding and logo
8. IF login fails, THEN THE Login page SHALL display an appropriate error message

### Requirement 5: Dashboard Page

**User Story:** As a user, I want to see an overview of key metrics and AI recommendations on the dashboard, so that I can quickly understand the platform's value.

#### Acceptance Criteria

1. THE Dashboard SHALL display a welcome message with the user's name (if logged in)
2. THE Dashboard SHALL display four stat cards: Today's New Products, Potential Hot Products, Favorites Count, AI Recommendation Accuracy
3. THE Dashboard SHALL display an AI Hot Products recommendation section with product cards
4. THE Dashboard SHALL display a trending categories section
5. THE Dashboard SHALL display a recent activity section
6. WHEN a user clicks on a product card, THE Frontend SHALL open the product detail modal
7. THE Dashboard SHALL integrate with the /api/dashboard/* backend endpoints
8. THE Dashboard SHALL support language toggle (Chinese/English)

### Requirement 6: AI Search Page (Chat-style Interface)

**User Story:** As a user, I want to search for products using natural language in a chat-like interface, so that I can find products intuitively.

#### Acceptance Criteria

1. THE Search page SHALL display a chat-style interface with AI avatar and welcome message
2. THE Search page SHALL provide a text input area at the bottom for entering search queries
3. THE Search page SHALL display quick suggestion chips for common search queries
4. WHEN a user submits a search query, THE Frontend SHALL display the query as a user message bubble
5. WHEN searching, THE Frontend SHALL display an AI thinking indicator with progress steps
6. WHEN search results are returned, THE Frontend SHALL display them in a grid layout
7. THE Search page SHALL support filter tabs for category, price range, and platform
8. THE Search page SHALL integrate with the /api/search endpoint
9. THE Search page SHALL display AI analysis summary for search results

### Requirement 7: Favorites Page

**User Story:** As a user, I want to view and manage my favorite products, so that I can track products I'm interested in.

#### Acceptance Criteria

1. THE Favorites page SHALL display a grid of favorited product cards
2. THE Favorites page SHALL support removing products from favorites
3. THE Favorites page SHALL display an empty state when no favorites exist
4. THE Favorites page SHALL integrate with the /api/favorites/* endpoints
5. WHEN a product is removed from favorites, THE Frontend SHALL update the UI immediately
6. THE Favorites page SHALL display the total count of favorites

### Requirement 8: Market Analysis Page

**User Story:** As a user, I want to view market analysis and trends, so that I can make informed product selection decisions.

#### Acceptance Criteria

1. THE Analysis page SHALL display market trend charts using ECharts
2. THE Analysis page SHALL display category performance data
3. THE Analysis page SHALL display platform comparison data (Amazon, eBay, AliExpress, TikTok)
4. THE Analysis page SHALL integrate with the /api/market/* endpoints
5. THE Analysis page SHALL support filtering by time period and category

### Requirement 9: Product Detail Modal

**User Story:** As a user, I want to view detailed product information in a modal, so that I can analyze products without leaving the current page.

#### Acceptance Criteria

1. THE Product_Modal SHALL display product title, description, and price information
2. THE Product_Modal SHALL display multi-platform price comparison (Amazon, eBay, AliExpress, TikTok)
3. THE Product_Modal SHALL display a price trend chart
4. THE Product_Modal SHALL display a competitive analysis radar chart
5. THE Product_Modal SHALL display AI analysis summary with confidence score
6. THE Product_Modal SHALL provide action buttons: Add to Favorites, View on Platform, Share
7. WHEN clicking outside the modal or the close button, THE Product_Modal SHALL close
8. THE Product_Modal SHALL integrate with the /api/products/{id} endpoint

### Requirement 10: Subscription Page

**User Story:** As a user, I want to view and manage my subscription plan, so that I can access premium features.

#### Acceptance Criteria

1. THE Subscription page SHALL display available subscription plans (Free, Pro, Enterprise)
2. THE Subscription page SHALL highlight the user's current plan
3. THE Subscription page SHALL display plan features and pricing
4. THE Subscription page SHALL provide upgrade/downgrade buttons
5. THE Subscription page SHALL integrate with the /api/subscription/* endpoints

### Requirement 11: Profile Page

**User Story:** As a user, I want to view and edit my profile settings, so that I can manage my account.

#### Acceptance Criteria

1. THE Profile page SHALL display user information (name, phone, avatar)
2. THE Profile page SHALL allow editing user nickname
3. THE Profile page SHALL display account statistics
4. THE Profile page SHALL provide a logout button
5. THE Profile page SHALL integrate with the /api/user/* endpoints

### Requirement 12: Responsive Design

**User Story:** As a user, I want the application to work well on different screen sizes, so that I can use it on desktop and mobile devices.

#### Acceptance Criteria

1. THE Frontend SHALL be responsive and work on screens from 320px to 1920px width
2. THE Frontend SHALL use a mobile-first approach with breakpoints at sm(640px), md(768px), lg(1024px), xl(1280px)
3. WHEN on mobile devices, THE Sidebar SHALL be hidden and accessible via a hamburger menu
4. THE product grid SHALL adjust columns based on screen width (1-4 columns)

### Requirement 13: Internationalization (i18n)

**User Story:** As a user, I want to use the application in Chinese or English, so that I can understand the interface in my preferred language.

#### Acceptance Criteria

1. THE Frontend SHALL support Chinese (zh-CN) as the default language
2. THE Frontend SHALL support English (en) as an alternative language
3. THE Frontend SHALL provide a language toggle button in the header
4. THE Frontend SHALL persist the user's language preference in local storage
5. WHEN the language is changed, THE Frontend SHALL update all UI text immediately

### Requirement 14: Loading States and Error Handling

**User Story:** As a user, I want to see loading indicators and error messages, so that I understand the application state.

#### Acceptance Criteria

1. WHEN data is loading, THE Frontend SHALL display skeleton loaders or loading spinners
2. IF an API request fails, THEN THE Frontend SHALL display an error message with retry option
3. IF the network is unavailable, THEN THE Frontend SHALL display a network error message
4. THE Frontend SHALL implement automatic retry for failed requests (max 2 retries)

### Requirement 15: Product Card Component

**User Story:** As a user, I want product cards to display key information at a glance, so that I can quickly evaluate products.

#### Acceptance Criteria

1. THE Product_Card SHALL display product image with fallback placeholder
2. THE Product_Card SHALL display product title (max 2 lines with ellipsis)
3. THE Product_Card SHALL display product price with currency symbol
4. THE Product_Card SHALL display platform badges (Amazon, eBay, etc.)
5. THE Product_Card SHALL display a hot/trending badge when applicable
6. THE Product_Card SHALL provide a favorite button with toggle state
7. WHEN hovering over a product card, THE Frontend SHALL display a subtle lift effect
8. WHEN clicking a product card, THE Frontend SHALL open the product detail modal
