/**
 * English (en) Translation Messages
 * 
 * Contains all UI text translations for the FlashSell application.
 * This is the alternative language.
 * 
 * Requirements: 13.1, 13.2
 * - 13.1: Support Chinese (zh-CN) as the default language
 * - 13.2: Support English (en) as an alternative language
 */

import type { Messages } from '@/stores/i18n'

export const enMessages: Messages = {
  // Navigation
  nav: {
    main: 'Main Navigation',
    dashboard: 'Dashboard',
    search: 'AI Search',
    favorites: 'Favorites',
    analysis: 'Market Analysis',
    account: 'Account',
    subscription: 'Subscription',
    profile: 'Profile',
    logout: 'Logout'
  },

  // Dashboard page
  dashboard: {
    welcome: 'Welcome back',
    welcomeGuest: 'Welcome to FlashSell',
    todayNewProducts: 'New Products Today',
    potentialHotProducts: 'Potential Hot Products',
    favoritesCount: 'Favorites Count',
    aiAccuracy: 'AI Accuracy',
    aiRecommendations: 'AI Recommendations',
    viewAll: 'View All',
    trendingCategories: 'Trending Categories',
    recentActivity: 'Recent Activity',
    noActivity: 'No activity yet'
  },

  // Search page
  search: {
    title: 'AI Product Search',
    subtitle: 'Tell me what products you are looking for, and I will analyze them for you',
    placeholder: 'Describe the products you want to find...',
    send: 'Send',
    suggestions: {
      hotProducts: 'Hot Products',
      trending: 'Trending',
      highProfit: 'High Profit',
      newArrivals: 'New Arrivals'
    },
    filters: {
      category: 'Category',
      priceRange: 'Price Range',
      platform: 'Platform',
      all: 'All'
    },
    results: 'Search Results',
    noResults: 'No products found',
    aiSummary: 'AI Analysis Summary',
    thinking: {
      analyzing: 'Analyzing your requirements...',
      searching: 'Searching for related products...',
      evaluating: 'Evaluating product data...',
      generating: 'Generating recommendations...'
    }
  },

  // Product related
  product: {
    price: 'Price',
    originalPrice: 'Original Price',
    sales: 'Sales',
    rating: 'Rating',
    reviews: 'Reviews',
    addToFavorites: 'Add to Favorites',
    removeFromFavorites: 'Remove from Favorites',
    viewOnPlatform: 'View on Platform',
    share: 'Share',
    priceHistory: 'Price History',
    competitiveAnalysis: 'Competitive Analysis',
    aiAnalysis: 'AI Analysis',
    confidence: 'Confidence',
    recommendation: {
      buy: 'Recommended',
      watch: 'Watch',
      skip: 'Not Recommended'
    },
    highlights: 'Highlights',
    platformComparison: 'Platform Comparison',
    bestPrice: 'Best Price',
    shipping: 'Shipping',
    availability: 'Availability',
    metrics: {
      priceCompetitiveness: 'Price Competitiveness',
      marketDemand: 'Market Demand',
      profitPotential: 'Profit Potential',
      competitionLevel: 'Competition Level',
      trendScore: 'Trend Score'
    }
  },

  // Favorites page
  favorites: {
    title: 'My Favorites',
    total: '{count} items in total',
    empty: 'No favorites yet',
    emptyHint: 'Browse products and click the favorite button to add them here',
    remove: 'Remove',
    removeConfirm: 'Are you sure you want to remove this from favorites?'
  },

  // Analysis page
  analysis: {
    title: 'Market Analysis',
    marketTrend: 'Market Trend',
    categoryPerformance: 'Category Performance',
    platformComparison: 'Platform Comparison',
    timeRange: {
      week: 'Last 7 days',
      month: 'Last 30 days',
      quarter: 'Last 3 months',
      year: 'Last year'
    },
    growth: 'Growth',
    decline: 'Decline'
  },

  // Subscription page
  subscription: {
    title: 'Subscription Plans',
    currentPlan: 'Current Plan',
    plans: {
      free: {
        name: 'Free',
        description: 'Basic features'
      },
      pro: {
        name: 'Pro',
        description: 'For individual sellers'
      },
      enterprise: {
        name: 'Enterprise',
        description: 'For teams'
      }
    },
    features: 'Features',
    price: 'Price',
    perMonth: '/month',
    upgrade: 'Upgrade',
    downgrade: 'Downgrade',
    current: 'Current'
  },

  // Profile page
  profile: {
    title: 'Profile',
    avatar: 'Avatar',
    nickname: 'Nickname',
    phone: 'Phone',
    email: 'Email',
    editNickname: 'Edit Nickname',
    statistics: 'Account Statistics',
    memberSince: 'Member Since',
    totalSearches: 'Total Searches',
    totalFavorites: 'Total Favorites'
  },

  // Login page
  login: {
    title: 'Login',
    register: 'Register',
    phone: 'Phone Number',
    phonePlaceholder: 'Enter your phone number',
    verifyCode: 'Verification Code',
    verifyCodePlaceholder: 'Enter verification code',
    sendCode: 'Send Code',
    resendCode: 'Resend',
    countdown: 'Resend in {seconds}s',
    loginButton: 'Login',
    registerButton: 'Register',
    switchToRegister: "Don't have an account? Register now",
    switchToLogin: 'Already have an account? Login now',
    agreement: 'By logging in, you agree to our',
    termsOfService: 'Terms of Service',
    and: 'and',
    privacyPolicy: 'Privacy Policy'
  },

  // Platforms
  platforms: {
    amazon: 'Amazon',
    ebay: 'eBay',
    aliexpress: 'AliExpress',
    tiktok: 'TikTok'
  },

  // Badges
  badges: {
    hot: 'Hot',
    trending: 'Trending',
    new: 'New'
  },

  // Common UI elements
  common: {
    loading: 'Loading...',
    error: 'Something went wrong',
    retry: 'Retry',
    cancel: 'Cancel',
    confirm: 'Confirm',
    save: 'Save',
    delete: 'Delete',
    edit: 'Edit',
    close: 'Close',
    back: 'Back',
    next: 'Next',
    previous: 'Previous',
    submit: 'Submit',
    search: 'Search',
    filter: 'Filter',
    sort: 'Sort',
    more: 'More',
    less: 'Less',
    all: 'All',
    none: 'None',
    yes: 'Yes',
    no: 'No',
    ok: 'OK',
    noData: 'No data available',
    networkError: 'Network connection failed',
    serverError: 'Server error',
    timeout: 'Request timeout',
    unknownError: 'Unknown error'
  },

  // Error messages
  error: {
    networkMessage: 'Please check your network connection and try again',
    serverMessage: 'Server is temporarily unavailable, please try again later',
    timeoutMessage: 'Server response took too long, please try again later',
    unknownMessage: 'An unknown error occurred, please try again later',
    loadFailed: 'Failed to load',
    saveFailed: 'Failed to save',
    deleteFailed: 'Failed to delete',
    operationFailed: 'Operation failed, please try again'
  },

  // Validation messages
  validation: {
    required: 'This field is required',
    phoneInvalid: 'Please enter a valid phone number',
    codeInvalid: 'Please enter a 6-digit verification code',
    nicknameTooLong: 'Nickname cannot exceed 20 characters'
  },

  // Theme
  theme: {
    dark: 'Dark Mode',
    light: 'Light Mode',
    toggle: 'Toggle Theme'
  },

  // Language
  language: {
    zh: '中文',
    en: 'English',
    toggle: 'Toggle Language'
  }
}

export default enMessages
