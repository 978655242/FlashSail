/**
 * Chinese (zh-CN) Translation Messages
 * 
 * Contains all UI text translations for the FlashSell application.
 * This is the default language.
 * 
 * Requirements: 13.1, 13.2
 * - 13.1: Support Chinese (zh-CN) as the default language
 * - 13.2: Support English (en) as an alternative language
 */

import type { Messages } from '@/stores/i18n'

export const zhMessages: Messages = {
  // Navigation
  nav: {
    main: '主导航',
    dashboard: '仪表盘',
    search: 'AI 搜索',
    favorites: '收藏夹',
    analysis: '市场分析',
    account: '账户',
    subscription: '订阅',
    profile: '个人资料',
    logout: '退出登录'
  },

  // Dashboard page
  dashboard: {
    welcome: '欢迎回来',
    welcomeGuest: '欢迎使用 FlashSell',
    todayNewProducts: '今日新品发现',
    potentialHotProducts: '潜力爆品推荐',
    favoritesCount: '收藏产品数',
    aiAccuracy: 'AI推荐准确率',
    aiRecommendations: 'AI爆品推荐',
    viewAll: '查看全部',
    trendingCategories: '热门品类',
    recentActivity: '最近动态',
    noActivity: '暂无动态'
  },

  // Search page
  search: {
    title: 'AI 智能选品',
    subtitle: '告诉我你想找什么产品，我来帮你分析',
    placeholder: '描述你想找的产品...',
    send: '发送',
    suggestions: {
      hotProducts: '热门爆品',
      trending: '趋势上升',
      highProfit: '高利润',
      newArrivals: '新品上架'
    },
    filters: {
      category: '品类',
      priceRange: '价格区间',
      platform: '平台',
      all: '全部'
    },
    results: '搜索结果',
    noResults: '未找到相关产品',
    aiSummary: 'AI 分析摘要',
    thinking: {
      analyzing: '正在分析您的需求...',
      searching: '正在搜索相关产品...',
      evaluating: '正在评估产品数据...',
      generating: '正在生成推荐结果...'
    }
  },

  // Product related
  product: {
    price: '价格',
    originalPrice: '原价',
    sales: '销量',
    rating: '评分',
    reviews: '评价',
    addToFavorites: '加入收藏',
    removeFromFavorites: '取消收藏',
    viewOnPlatform: '查看原链接',
    share: '分享',
    priceHistory: '价格走势',
    competitiveAnalysis: '竞争分析',
    aiAnalysis: 'AI 分析',
    confidence: '置信度',
    recommendation: {
      buy: '建议购买',
      watch: '建议观望',
      skip: '不建议'
    },
    highlights: '亮点',
    platformComparison: '多平台比价',
    bestPrice: '最低价',
    shipping: '运费',
    availability: '库存',
    metrics: {
      priceCompetitiveness: '价格竞争力',
      marketDemand: '市场需求',
      profitPotential: '利润潜力',
      competitionLevel: '竞争程度',
      trendScore: '趋势评分'
    }
  },

  // Favorites page
  favorites: {
    title: '我的收藏',
    total: '共 {count} 件商品',
    empty: '暂无收藏商品',
    emptyHint: '浏览商品并点击收藏按钮添加到这里',
    remove: '移除',
    removeConfirm: '确定要取消收藏吗？'
  },

  // Analysis page
  analysis: {
    title: '市场分析',
    marketTrend: '市场趋势',
    categoryPerformance: '品类表现',
    platformComparison: '平台对比',
    timeRange: {
      week: '近7天',
      month: '近30天',
      quarter: '近3个月',
      year: '近1年'
    },
    growth: '增长',
    decline: '下降'
  },

  // Subscription page
  subscription: {
    title: '订阅计划',
    currentPlan: '当前计划',
    plans: {
      free: {
        name: '免费版',
        description: '基础功能体验'
      },
      pro: {
        name: '专业版',
        description: '适合个人卖家'
      },
      enterprise: {
        name: '企业版',
        description: '适合团队使用'
      }
    },
    features: '功能特性',
    price: '价格',
    perMonth: '/月',
    upgrade: '升级',
    downgrade: '降级',
    current: '当前'
  },

  // Profile page
  profile: {
    title: '个人资料',
    avatar: '头像',
    nickname: '昵称',
    phone: '手机号',
    email: '邮箱',
    editNickname: '修改昵称',
    statistics: '账户统计',
    memberSince: '注册时间',
    totalSearches: '搜索次数',
    totalFavorites: '收藏数量'
  },

  // Login page
  login: {
    title: '登录',
    register: '注册',
    phone: '手机号',
    phonePlaceholder: '请输入手机号',
    verifyCode: '验证码',
    verifyCodePlaceholder: '请输入验证码',
    sendCode: '发送验证码',
    resendCode: '重新发送',
    countdown: '{seconds}秒后重发',
    loginButton: '登录',
    registerButton: '注册',
    switchToRegister: '没有账号？立即注册',
    switchToLogin: '已有账号？立即登录',
    agreement: '登录即表示同意',
    termsOfService: '服务条款',
    and: '和',
    privacyPolicy: '隐私政策'
  },

  // Platforms
  platforms: {
    amazon: '亚马逊',
    ebay: 'eBay',
    aliexpress: '速卖通',
    tiktok: 'TikTok'
  },

  // Badges
  badges: {
    hot: '热门',
    trending: '趋势',
    new: '新品'
  },

  // Common UI elements
  common: {
    loading: '加载中...',
    error: '出错了',
    retry: '重试',
    cancel: '取消',
    confirm: '确认',
    save: '保存',
    delete: '删除',
    edit: '编辑',
    close: '关闭',
    back: '返回',
    next: '下一步',
    previous: '上一步',
    submit: '提交',
    search: '搜索',
    filter: '筛选',
    sort: '排序',
    more: '更多',
    less: '收起',
    all: '全部',
    none: '无',
    yes: '是',
    no: '否',
    ok: '确定',
    noData: '暂无数据',
    networkError: '网络连接失败',
    serverError: '服务器错误',
    timeout: '请求超时',
    unknownError: '未知错误'
  },

  // Error messages
  error: {
    networkMessage: '请检查您的网络连接后重试',
    serverMessage: '服务器暂时无法响应，请稍后重试',
    timeoutMessage: '服务器响应时间过长，请稍后重试',
    unknownMessage: '发生未知错误，请稍后重试',
    loadFailed: '加载失败',
    saveFailed: '保存失败',
    deleteFailed: '删除失败',
    operationFailed: '操作失败，请重试'
  },

  // Validation messages
  validation: {
    required: '此项为必填',
    phoneInvalid: '请输入正确的手机号',
    codeInvalid: '请输入6位验证码',
    nicknameTooLong: '昵称不能超过20个字符'
  },

  // Theme
  theme: {
    dark: '深色模式',
    light: '浅色模式',
    toggle: '切换主题'
  },

  // Language
  language: {
    zh: '中文',
    en: 'English',
    toggle: '切换语言'
  }
}

export default zhMessages
