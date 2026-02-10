import { createRouter, createWebHistory, type RouteRecordRaw, type RouteLocationNormalized, type NavigationGuardNext } from 'vue-router'
import { useUserStore } from '@/stores/user'

/**
 * Route meta interface for type safety
 */
declare module 'vue-router' {
  interface RouteMeta {
    /** Whether the route requires authentication */
    requiresAuth?: boolean
    /** Whether to hide the main layout (for login page) */
    hideLayout?: boolean
    /** Page title for document.title */
    title?: string
    /** Icon name for navigation */
    icon?: string
  }
}

/**
 * Route configuration following the design document
 * - Dashboard, Search, Analysis are public (requiresAuth: false)
 * - Favorites, Subscription, Profile require authentication (requiresAuth: true)
 * - Login page has hideLayout: true
 */
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { 
      requiresAuth: false, 
      hideLayout: true,
      title: '登录 - FlashSell'
    }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/Home.vue'),
        meta: { 
          requiresAuth: false,
          title: '仪表盘 - FlashSell',
          icon: 'dashboard'
        }
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/Search.vue'),
        meta: { 
          requiresAuth: false,
          title: 'AI 搜索 - FlashSell',
          icon: 'search'
        }
      },
      {
        path: 'favorites',
        name: 'Favorites',
        component: () => import('@/views/Favorites.vue'),
        meta: { 
          requiresAuth: true,
          title: '收藏夹 - FlashSell',
          icon: 'heart'
        }
      },
      {
        path: 'hot-products',
        name: 'HotProducts',
        component: () => import('@/views/HotProducts.vue'),
        meta: { 
          requiresAuth: false,
          title: '热门产品 - FlashSell',
          icon: 'fire'
        }
      },
      {
        path: 'market',
        name: 'Market',
        component: () => import('@/views/Market.vue'),
        meta: { 
          requiresAuth: false,
          title: '市场 - FlashSell',
          icon: 'chart'
        }
      },
      {
        path: 'analysis',
        name: 'Analysis',
        component: () => import('@/views/Analysis.vue'),
        meta: { 
          requiresAuth: false,
          title: '市场分析 - FlashSell',
          icon: 'analytics'
        }
      },
      {
        path: 'subscription',
        name: 'Subscription',
        component: () => import('@/views/Subscription.vue'),
        meta: { 
          requiresAuth: true,
          title: '订阅 - FlashSell',
          icon: 'subscription'
        }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { 
          requiresAuth: true,
          title: '个人资料 - FlashSell',
          icon: 'user'
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: {
      title: '页面未找到 - FlashSell'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  // Scroll to top on navigation
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  }
})

/**
 * Navigation guard for authentication
 * - Redirects unauthenticated users to login for protected routes
 * - Redirects authenticated users away from login page to dashboard
 * - Preserves the intended destination in query params for redirect after login
 */
router.beforeEach((to: RouteLocationNormalized, _from: RouteLocationNormalized, next: NavigationGuardNext) => {
  const userStore = useUserStore()
  const requiresAuth = to.meta.requiresAuth === true

  // Update document title
  if (to.meta.title) {
    document.title = to.meta.title as string
  } else {
    document.title = 'FlashSell - AI 智能选品平台'
  }

  // Check if route requires authentication
  if (requiresAuth && !userStore.isLoggedIn) {
    // Redirect to login with the intended destination
    next({ 
      name: 'Login', 
      query: { redirect: to.fullPath } 
    })
  } else if (to.name === 'Login' && userStore.isLoggedIn) {
    // Redirect logged-in users away from login page
    // Check if there's a redirect query param
    const redirectPath = to.query.redirect as string
    if (redirectPath && redirectPath !== '/login') {
      next(redirectPath)
    } else {
      next({ name: 'Dashboard' })
    }
  } else {
    next()
  }
})

/**
 * After each navigation, handle any post-navigation tasks
 */
router.afterEach((_to, _from) => {
  // Could add analytics tracking here
  // Could add loading state management here
})

/**
 * Handle navigation errors
 */
router.onError((error) => {
  console.error('Router error:', error)
  // Could add error reporting here
})

export default router
