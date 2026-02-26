<script setup lang="ts">
/**
 * Search Page - Chat-style AI Search Interface
 * 
 * Implements a chat-like interface for AI-powered product search.
 * 
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9
 * - Chat-style interface with AI avatar and welcome message
 * - Text input at bottom for search queries
 * - Quick suggestion chips
 * - User query displayed as message bubble
 * - AI thinking indicator with progress
 * - Results in grid layout
 * - Filter tabs for category, price, platform
 * - Integration with /api/search endpoint
 * - AI analysis summary display
 */
import { ref, computed, onMounted, nextTick } from 'vue'
import { useSearchStore } from '@/stores/search'
import { useFavoritesStore } from '@/stores/favorites'
import { useI18n } from '@/composables/useI18n'
import ChatMessage from '@/components/ChatMessage.vue'
import SearchInput from '@/components/SearchInput.vue'
import ThinkingIndicator from '@/components/ThinkingIndicator.vue'
import ProductCard from '@/components/ProductCard.vue'
import ProductDetailModal from '@/components/ProductDetailModal.vue'
import type { ThinkingStep } from '@/components/ThinkingIndicator.vue'
import type { ProductDTO } from '@/types/product'

interface ChatMessageData {
  id: string
  type: 'user' | 'ai'
  content: string
  timestamp: Date
  products?: ProductDTO[]
}

const searchStore = useSearchStore()
const favoritesStore = useFavoritesStore()
const { t } = useI18n()

// Chat state
const chatMessages = ref<ChatMessageData[]>([])
const searchQuery = ref('')
const isSearching = ref(false)
const chatContainerRef = ref<HTMLDivElement | null>(null)

// Thinking indicator state
const thinkingSteps = ref<ThinkingStep[]>([])

// Filter state
const activeFilter = ref<'all' | 'category' | 'price' | 'platform'>('all')
const priceRange = ref<{ min?: number; max?: number }>({})
const selectedPlatforms = ref<string[]>([])

// Suggestion chips
const suggestions = computed(() => [
  { id: 'hot', label: t('search.suggestions.hotProducts', '热门爆品'), query: '热门爆品' },
  { id: 'trending', label: t('search.suggestions.trending', '趋势上升'), query: '趋势上升产品' },
  { id: 'profit', label: t('search.suggestions.highProfit', '高利润'), query: '高利润产品' },
  { id: 'new', label: t('search.suggestions.newArrivals', '新品上架'), query: '新品上架' }
])

// Platform options
const platformOptions = [
  { id: 'amazon', label: t('platforms.amazon', '亚马逊') },
  { id: 'ebay', label: t('platforms.ebay', 'eBay') },
  { id: 'aliexpress', label: t('platforms.aliexpress', '速卖通') },
  { id: 'tiktok', label: t('platforms.tiktok', 'TikTok') }
]

// Price range options
const priceRangeOptions = [
  { id: 'all', label: t('search.filters.all', '全部'), min: undefined, max: undefined },
  { id: 'low', label: '$0-$25', min: 0, max: 25 },
  { id: 'mid', label: '$25-$100', min: 25, max: 100 },
  { id: 'high', label: '$100+', min: 100, max: undefined }
]

onMounted(() => {
  // Load categories
  searchStore.fetchCategories()
  // Load favorites
  favoritesStore.fetchFavorites()
})

/** Scroll chat to bottom */
function scrollToBottom() {
  nextTick(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  })
}

/** Update thinking steps during search */
function updateThinkingSteps(activeStep: number) {
  const steps: ThinkingStep[] = [
    { id: 'analyzing', label: t('search.thinking.analyzing', '正在分析您的需求...'), status: 'pending' },
    { id: 'searching', label: t('search.thinking.searching', '正在搜索相关产品...'), status: 'pending' },
    { id: 'evaluating', label: t('search.thinking.evaluating', '正在评估产品数据...'), status: 'pending' },
    { id: 'generating', label: t('search.thinking.generating', '正在生成推荐结果...'), status: 'pending' }
  ]
  
  steps.forEach((step, index) => {
    if (index < activeStep) {
      step.status = 'completed'
    } else if (index === activeStep) {
      step.status = 'active'
    }
  })
  
  thinkingSteps.value = steps
}

/** Handle search submission */
async function handleSearch(query: string) {
  if (!query.trim()) return
  
  // Add user message
  const userMessage: ChatMessageData = {
    id: `user-${Date.now()}`,
    type: 'user',
    content: query,
    timestamp: new Date()
  }
  chatMessages.value.push(userMessage)
  scrollToBottom()
  
  // Clear input
  searchQuery.value = ''
  
  // Start search
  isSearching.value = true
  
  // Simulate thinking steps progression
  updateThinkingSteps(0)
  scrollToBottom()
  
  const stepInterval = setInterval(() => {
    const currentActive = thinkingSteps.value.findIndex(s => s.status === 'active')
    if (currentActive < 3) {
      updateThinkingSteps(currentActive + 1)
    }
  }, 800)
  
  try {
    // Perform search
    await searchStore.search(query)
    
    // Clear interval
    clearInterval(stepInterval)
    
    // Add AI response
    const aiMessage: ChatMessageData = {
      id: `ai-${Date.now()}`,
      type: 'ai',
      content: searchStore.aiSummary || t('search.results', '搜索结果'),
      timestamp: new Date(),
      products: searchStore.searchResults
    }
    chatMessages.value.push(aiMessage)

  } catch (error) {
    clearInterval(stepInterval)

    // Add error message
    const errorMessage: ChatMessageData = {
      id: `ai-error-${Date.now()}`,
      type: 'ai',
      content: searchStore.error || t('common.error', '出错了'),
      timestamp: new Date()
    }
    chatMessages.value.push(errorMessage)
  } finally {
    isSearching.value = false
    thinkingSteps.value = []
    scrollToBottom()
  }
}

/** Handle suggestion chip click */
function handleSuggestionClick(query: string) {
  searchQuery.value = query
  handleSearch(query)
}

/** Handle product click */
function handleProductClick(product: ProductDTO) {
  searchStore.fetchProductDetail(product.id)
}

/** Handle favorite toggle */
function handleFavorite(product: ProductDTO) {
  if (favoritesStore.isFavorite(product.id)) {
    favoritesStore.removeFavorite(product.id)
  } else {
    favoritesStore.addFavorite(product.id)
  }
}

/** Handle filter change */
function handleFilterChange(filter: typeof activeFilter.value) {
  activeFilter.value = filter
}

/** Handle price range change */
function handlePriceRangeChange(option: typeof priceRangeOptions[0]) {
  priceRange.value = { min: option.min, max: option.max }
  searchStore.updateFilters({
    priceMin: option.min,
    priceMax: option.max
  })
  // Re-search if we have a query
  if (searchStore.searchQuery) {
    handleSearch(searchStore.searchQuery)
  }
}

/** Handle platform toggle */
function handlePlatformToggle(platformId: string) {
  const index = selectedPlatforms.value.indexOf(platformId)
  if (index === -1) {
    selectedPlatforms.value.push(platformId)
  } else {
    selectedPlatforms.value.splice(index, 1)
  }
}

/** Get latest products from chat messages */
const latestProducts = computed(() => {
  const lastAiMessage = [...chatMessages.value].reverse().find(m => m.type === 'ai' && m.products?.length)
  return lastAiMessage?.products || []
})

/** Whether to show results grid */
const showResults = computed(() => {
  return !isSearching.value && latestProducts.value.length > 0
})

/** Whether chat has started */
const hasChatStarted = computed(() => chatMessages.value.length > 0)
</script>

<template>
  <div class="h-full flex flex-col">
    <!-- Chat Container -->
    <div
      ref="chatContainerRef"
      class="flex-1 overflow-y-auto px-4 py-6"
    >
      <!-- Welcome Message (shown when no chat) -->
      <div v-if="!hasChatStarted" class="flex flex-col items-center justify-center h-full">
        <!-- AI Avatar -->
        <div class="w-20 h-20 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center text-white mb-6 shadow-lg shadow-purple-500/25">
          <svg class="w-10 h-10" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
          </svg>
        </div>
        
        <!-- Welcome Text -->
        <h1 class="text-2xl font-bold text-[var(--text-primary)] mb-2">{{ t('search.title') }}</h1>
        <p class="text-[var(--text-muted)] text-center max-w-md mb-8">{{ t('search.subtitle') }}</p>
        
        <!-- Suggestion Chips -->
        <div class="flex flex-wrap justify-center gap-2">
          <button
            v-for="suggestion in suggestions"
            :key="suggestion.id"
            class="px-4 py-2 glass-card text-sm text-slate-300 hover:text-white hover:border-orange-500/50 transition-all duration-200"
            @click="handleSuggestionClick(suggestion.query)"
          >
            {{ suggestion.label }}
          </button>
        </div>
      </div>

      <!-- Chat Messages -->
      <div v-else class="max-w-4xl mx-auto">
        <ChatMessage
          v-for="message in chatMessages"
          :key="message.id"
          :type="message.type"
          :content="message.content"
          :timestamp="message.timestamp"
        />
        
        <!-- Thinking Indicator -->
        <ThinkingIndicator
          v-if="isSearching"
          :steps="thinkingSteps"
          :show="true"
        />
        
        <!-- Results Section -->
        <div v-if="showResults" class="mt-6">
          <!-- Filter Tabs -->
          <div class="flex flex-wrap gap-2 mb-4">
            <button
              :class="[
                'px-3 py-1.5 text-sm rounded-lg transition-colors',
                activeFilter === 'all'
                  ? 'bg-orange-500 text-white'
                  : 'glass-card text-[var(--text-muted)] hover:text-[var(--text-primary)]'
              ]"
              @click="handleFilterChange('all')"
            >
              {{ t('search.filters.all') }}
            </button>
            <button
              :class="[
                'px-3 py-1.5 text-sm rounded-lg transition-colors',
                activeFilter === 'category'
                  ? 'bg-orange-500 text-white'
                  : 'glass-card text-[var(--text-muted)] hover:text-[var(--text-primary)]'
              ]"
              @click="handleFilterChange('category')"
            >
              {{ t('search.filters.category') }}
            </button>
            <button
              :class="[
                'px-3 py-1.5 text-sm rounded-lg transition-colors',
                activeFilter === 'price'
                  ? 'bg-orange-500 text-white'
                  : 'glass-card text-[var(--text-muted)] hover:text-[var(--text-primary)]'
              ]"
              @click="handleFilterChange('price')"
            >
              {{ t('search.filters.priceRange') }}
            </button>
            <button
              :class="[
                'px-3 py-1.5 text-sm rounded-lg transition-colors',
                activeFilter === 'platform'
                  ? 'bg-orange-500 text-white'
                  : 'glass-card text-[var(--text-muted)] hover:text-[var(--text-primary)]'
              ]"
              @click="handleFilterChange('platform')"
            >
              {{ t('search.filters.platform') }}
            </button>
          </div>

          <!-- Category Filter Panel -->
          <div v-if="activeFilter === 'category'" class="glass-card p-4 mb-4 rounded-xl">
            <div class="flex flex-wrap gap-2">
              <button
                v-for="group in searchStore.categoryGroups"
                :key="group.id"
                :class="[
                  'px-3 py-1.5 text-sm rounded-lg transition-colors',
                  searchStore.filters.categoryId === group.id
                    ? 'bg-orange-500 text-white'
                    : 'filter-btn text-[var(--text-secondary)]'
                ]"
                @click="searchStore.updateFilters({ categoryId: group.id })"
              >
                {{ group.name }}
              </button>
            </div>
          </div>

          <!-- Price Range Filter Panel -->
          <div v-if="activeFilter === 'price'" class="glass-card p-4 mb-4 rounded-xl">
            <div class="flex flex-wrap gap-2">
              <button
                v-for="option in priceRangeOptions"
                :key="option.id"
                :class="[
                  'px-3 py-1.5 text-sm rounded-lg transition-colors',
                  priceRange.min === option.min && priceRange.max === option.max
                    ? 'bg-orange-500 text-white'
                    : 'filter-btn text-[var(--text-secondary)]'
                ]"
                @click="handlePriceRangeChange(option)"
              >
                {{ option.label }}
              </button>
            </div>
          </div>

          <!-- Platform Filter Panel -->
          <div v-if="activeFilter === 'platform'" class="glass-card p-4 mb-4 rounded-xl">
            <div class="flex flex-wrap gap-2">
              <button
                v-for="platform in platformOptions"
                :key="platform.id"
                :class="[
                  'px-3 py-1.5 text-sm rounded-lg transition-colors',
                  selectedPlatforms.includes(platform.id)
                    ? 'bg-orange-500 text-white'
                    : 'filter-btn text-[var(--text-secondary)]'
                ]"
                @click="handlePlatformToggle(platform.id)"
              >
                {{ platform.label }}
              </button>
            </div>
          </div>

          <!-- AI Summary -->
          <div v-if="searchStore.aiSummary" class="glass-card p-4 mb-4 rounded-xl border-orange-500/30">
            <div class="flex items-start gap-3">
              <div class="flex-shrink-0 w-8 h-8 bg-orange-500/20 rounded-lg flex items-center justify-center">
                <svg class="w-5 h-5 text-orange-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
                </svg>
              </div>
              <div>
                <h4 class="text-sm font-medium text-orange-400 mb-1">{{ t('search.aiSummary') }}</h4>
                <p class="text-sm text-[var(--text-secondary)]">{{ searchStore.aiSummary }}</p>
              </div>
            </div>
          </div>

          <!-- Results Count -->
          <p class="text-sm text-[var(--text-muted)] mb-4">
            {{ t('search.results') }}: <span class="text-[var(--text-primary)] font-medium">{{ searchStore.total }}</span>
          </p>

          <!-- Product Grid -->
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            <ProductCard
              v-for="product in latestProducts"
              :key="product.id"
              :product="product"
              :is-favorite="favoritesStore.isFavorite(product.id)"
              @click="handleProductClick"
              @favorite="handleFavorite"
            />
          </div>

          <!-- Load More -->
          <div v-if="searchStore.hasMore" class="mt-6 flex justify-center">
            <button
              :disabled="searchStore.isLoadingMore"
              class="px-6 py-2.5 glass-card text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:border-orange-500/50 transition-all disabled:opacity-50"
              @click="searchStore.loadMore"
            >
              <span v-if="searchStore.isLoadingMore" class="flex items-center gap-2">
                <svg class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                </svg>
                {{ t('common.loading') }}
              </span>
              <span v-else>{{ t('common.more') }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Search Input (Fixed at Bottom) -->
    <div class="search-input-footer flex-shrink-0 p-4">
      <div class="max-w-4xl mx-auto">
        <!-- Suggestion Chips (when chat started but no search in progress) -->
        <div v-if="hasChatStarted && !isSearching" class="flex flex-wrap gap-2 mb-3">
          <button
            v-for="suggestion in suggestions"
            :key="suggestion.id"
            class="px-3 py-1 text-xs glass-card text-[var(--text-muted)] hover:text-[var(--text-primary)] hover:border-orange-500/50 transition-all"
            @click="handleSuggestionClick(suggestion.query)"
          >
            {{ suggestion.label }}
          </button>
        </div>
        
        <SearchInput
          v-model="searchQuery"
          :disabled="isSearching"
          :placeholder="t('search.placeholder')"
          @submit="handleSearch"
        />
      </div>
    </div>

    <!-- Product Detail Modal -->
    <ProductDetailModal
      :show="searchStore.showProductModal"
      :product="searchStore.selectedProduct"
      :loading="searchStore.isProductDetailLoading"
      @close="searchStore.closeProductModal"
    />
  </div>
</template>

<style scoped>
/* Filter button background */
.filter-btn {
  background: var(--bg-card-hover);
}

.filter-btn:hover {
  background: var(--bg-card);
}

/* Search input footer */
.search-input-footer {
  border-top: 1px solid var(--border-subtle);
}

/* Light mode adjustments */
:global(html.light) .filter-btn {
  background: #f1f5f9;
}

:global(html.light) .filter-btn:hover {
  background: #e2e8f0;
}

:global(html.light) .search-input-footer {
  border-top-color: #e2e8f0;
}
</style>
