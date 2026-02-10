<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useFavoritesStore } from '@/stores/favorites'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import ProductCard from '@/components/ProductCard.vue'
import ProductDetailModal from '@/components/ProductDetailModal.vue'
import LoadingState from '@/components/LoadingState.vue'
import EmptyState from '@/components/EmptyState.vue'
import PageHeader from '@/components/PageHeader.vue'
import type { ProductDTO } from '@/types/product'
import type { ProductDetailRes } from '@/types/product'
import type { Board } from '@/types/favorite'
import { getProductDetail } from '@/api/product'

const favoritesStore = useFavoritesStore()
const toast = useToast()
const confirm = useConfirm()

// 状态
const activeTab = ref<'favorites' | 'boards'>('favorites')
const selectedBoard = ref<Board | null>(null)
const showCreateBoardModal = ref(false)
const showAddToBoardModal = ref(false)
const newBoardName = ref('')
const selectedProducts = ref<Set<number>>(new Set())
const selectedProductDetail = ref<ProductDetailRes | null>(null)
const showProductModal = ref(false)
const loadingProductDetail = ref(false)
const draggedProduct = ref<ProductDTO | null>(null)

// 计算属性
const hasMoreFavorites = computed(() => {
  return favoritesStore.favorites.length < favoritesStore.total
})

const currentBoardProducts = computed(() => {
  return favoritesStore.currentBoardDetail?.products || []
})

// 生命周期
onMounted(async () => {
  await Promise.all([
    favoritesStore.fetchFavorites(),
    favoritesStore.fetchBoards()
  ])
})

// 收藏相关方法
async function handleRemoveFavorite(product: ProductDTO) {
  const confirmed = await confirm.show({
    title: '确认取消收藏',
    message: `确定要取消收藏 "${product.title}" 吗？`,
    type: 'warning'
  })
  
  if (confirmed) {
    try {
      await favoritesStore.removeFavorite(product.id)
      toast.success('已取消收藏')
    } catch (error) {
      toast.error('取消收藏失败')
    }
  }
}

async function loadMoreFavorites() {
  if (!favoritesStore.isLoading && hasMoreFavorites.value) {
    await favoritesStore.fetchFavorites(favoritesStore.page + 1)
  }
}

// 看板相关方法
async function handleSelectBoard(board: Board) {
  selectedBoard.value = board
  await favoritesStore.fetchBoardDetail(board.id)
}

function handleCreateBoard() {
  if (!favoritesStore.canCreateBoard) {
    toast.warning(`最多只能创建 ${favoritesStore.maxBoards} 个看板`)
    return
  }
  showCreateBoardModal.value = true
  newBoardName.value = ''
}

async function confirmCreateBoard() {
  if (!newBoardName.value.trim()) {
    toast.warning('请输入看板名称')
    return
  }

  try {
    await favoritesStore.createBoard(newBoardName.value.trim())
    showCreateBoardModal.value = false
    toast.success('看板创建成功')
  } catch (error) {
    toast.error('创建看板失败')
  }
}

async function handleDeleteBoard(board: Board) {
  const confirmed = await confirm.show({
    title: '确认删除看板',
    message: `确定要删除看板 "${board.name}" 吗？看板中的产品不会被删除。`,
    type: 'danger'
  })
  
  if (confirmed) {
    try {
      await favoritesStore.deleteBoard(board.id)
      if (selectedBoard.value?.id === board.id) {
        selectedBoard.value = null
      }
      toast.success('看板已删除')
    } catch (error) {
      toast.error('删除看板失败')
    }
  }
}

// 产品选择相关方法
function toggleProductSelection(productId: number) {
  if (selectedProducts.value.has(productId)) {
    selectedProducts.value.delete(productId)
  } else {
    selectedProducts.value.add(productId)
  }
}

function clearSelection() {
  selectedProducts.value.clear()
}

function selectAll() {
  favoritesStore.favorites.forEach(p => selectedProducts.value.add(p.id))
}

// 添加到看板相关方法
function handleAddToBoard() {
  if (selectedProducts.value.size === 0) {
    toast.warning('请先选择要添加的产品')
    return
  }
  showAddToBoardModal.value = true
}

async function confirmAddToBoard(board: Board) {
  try {
    await favoritesStore.addProductsToBoard(board.id, Array.from(selectedProducts.value))
    showAddToBoardModal.value = false
    clearSelection()
    toast.success(`已添加 ${selectedProducts.value.size} 个产品到看板`)
  } catch (error) {
    toast.error('添加到看板失败')
  }
}

async function handleRemoveFromBoard(productId: number) {
  if (!selectedBoard.value) return
  
  const confirmed = await confirm.show({
    title: '确认移除',
    message: '确定要从看板中移除这个产品吗？',
    type: 'warning'
  })
  
  if (confirmed) {
    try {
      await favoritesStore.removeProductFromBoard(selectedBoard.value.id, productId)
      toast.success('已从看板移除')
    } catch (error) {
      toast.error('移除失败')
    }
  }
}

// 拖拽相关方法
function handleDragStart(product: ProductDTO) {
  draggedProduct.value = product
}

function handleDragEnd() {
  draggedProduct.value = null
}

function handleDragOver(event: DragEvent) {
  event.preventDefault()
}

async function handleDrop(board: Board) {
  if (!draggedProduct.value) return
  
  try {
    await favoritesStore.addProductsToBoard(board.id, [draggedProduct.value.id])
    toast.success(`已添加到看板 "${board.name}"`)
  } catch (error) {
    toast.error('添加到看板失败')
  } finally {
    draggedProduct.value = null
  }
}

// 产品详情相关方法
async function handleProductClick(product: ProductDTO) {
  loadingProductDetail.value = true
  showProductModal.value = true
  
  try {
    const { data } = await getProductDetail(product.id)
    if (data.code === 0 || data.code === 200) {
      selectedProductDetail.value = data.data
    }
  } catch (error) {
    toast.error('加载产品详情失败')
    showProductModal.value = false
  } finally {
    loadingProductDetail.value = false
  }
}

function handleCloseProductModal() {
  showProductModal.value = false
  selectedProductDetail.value = null
}
</script>

<template>
  <div>
    <!-- 页面标题 -->
    <PageHeader
      title="收藏与看板"
      description="管理您收藏的产品和看板"
    />

    <div>
      <div class="flex gap-6">
        <!-- 左侧：看板列表 -->
        <div class="w-64 flex-shrink-0">
          <div class="glass-card p-4">
            <!-- 标签切换 -->
            <div class="flex gap-2 mb-4">
              <button
                :class="[
                  'flex-1 px-3 py-2 text-sm font-medium rounded-lg transition-colors',
                  activeTab === 'favorites'
                    ? 'bg-orange-500/20 text-orange-400'
                    : 'text-slate-400 hover:bg-slate-700/50'
                ]"
                @click="activeTab = 'favorites'; selectedBoard = null"
              >
                收藏夹
              </button>
              <button
                :class="[
                  'flex-1 px-3 py-2 text-sm font-medium rounded-lg transition-colors',
                  activeTab === 'boards'
                    ? 'bg-orange-500/20 text-orange-400'
                    : 'text-slate-400 hover:bg-slate-700/50'
                ]"
                @click="activeTab = 'boards'"
              >
                看板
              </button>
            </div>

            <!-- 收藏夹视图 -->
            <div v-if="activeTab === 'favorites'" class="space-y-2">
              <div class="flex items-center justify-between text-sm text-slate-400">
                <span>全部收藏</span>
                <span class="font-medium">{{ favoritesStore.total }}</span>
              </div>
            </div>

            <!-- 看板列表 -->
            <div v-else class="space-y-2">
              <div class="flex items-center justify-between mb-3">
                <span class="text-sm font-medium text-slate-300">
                  我的看板 ({{ favoritesStore.boards.length }}/{{ favoritesStore.maxBoards }})
                </span>
                <button
                  class="p-1 text-orange-400 hover:bg-orange-500/10 rounded transition-colors"
                  @click="handleCreateBoard"
                  :disabled="!favoritesStore.canCreateBoard"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                  </svg>
                </button>
              </div>

              <div
                v-for="board in favoritesStore.boards"
                :key="board.id"
                :class="[
                  'group p-3 rounded-lg cursor-pointer transition-all',
                  selectedBoard?.id === board.id
                    ? 'bg-orange-500/20 border-2 border-orange-500'
                    : 'border-2 border-transparent hover:bg-slate-700/50'
                ]"
                @click="handleSelectBoard(board)"
                @dragover="handleDragOver"
                @drop.prevent="handleDrop(board)"
              >
                <div class="flex items-center justify-between">
                  <div class="flex-1 min-w-0">
                    <div class="text-sm font-medium text-white truncate">
                      {{ board.name }}
                    </div>
                    <div class="text-xs text-slate-500 mt-1">
                      {{ board.productCount }} 个产品
                    </div>
                  </div>
                  <button
                    class="opacity-0 group-hover:opacity-100 p-1 text-red-400 hover:bg-red-500/10 rounded transition-all"
                    @click.stop="handleDeleteBoard(board)"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
              </div>

              <EmptyState
                v-if="favoritesStore.boards.length === 0"
                message="还没有看板"
                description="创建看板来组织您的产品"
              />
            </div>
          </div>
        </div>

        <!-- 右侧：产品网格 -->
        <div class="flex-1">
          <!-- 工具栏 -->
          <div v-if="activeTab === 'favorites'" class="glass-card p-4 mb-6">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-4">
                <button
                  class="text-sm text-orange-400 hover:underline"
                  @click="selectAll"
                >
                  全选
                </button>
                <button
                  v-if="selectedProducts.size > 0"
                  class="text-sm text-slate-400 hover:underline"
                  @click="clearSelection"
                >
                  取消选择
                </button>
                <span v-if="selectedProducts.size > 0" class="text-sm text-slate-500">
                  已选择 {{ selectedProducts.size }} 个产品
                </span>
              </div>
              <button
                v-if="selectedProducts.size > 0"
                class="px-4 py-2 btn-gradient-primary text-sm"
                @click="handleAddToBoard"
              >
                添加到看板
              </button>
            </div>
          </div>

          <!-- 看板标题 -->
          <div v-if="selectedBoard" class="glass-card p-4 mb-6">
            <h2 class="text-lg font-semibold text-white">
              {{ selectedBoard.name }}
            </h2>
            <p class="text-sm text-slate-400 mt-1">
              {{ selectedBoard.productCount }} 个产品
            </p>
          </div>

          <!-- 加载状态 -->
          <LoadingState v-if="favoritesStore.isLoading && favoritesStore.favorites.length === 0" />

          <!-- 空状态 -->
          <EmptyState
            v-else-if="!favoritesStore.isLoading && activeTab === 'favorites' && favoritesStore.favorites.length === 0"
            message="还没有收藏"
            description="在搜索结果中点击收藏按钮来添加产品"
          />

          <EmptyState
            v-else-if="!favoritesStore.isLoading && selectedBoard && currentBoardProducts.length === 0"
            message="看板为空"
            description="从收藏夹拖拽产品到看板，或使用批量添加功能"
          />

          <!-- 产品网格 -->
          <div
            v-else
            class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6"
          >
            <!-- 收藏夹产品 -->
            <template v-if="activeTab === 'favorites' && !selectedBoard">
              <div
                v-for="product in favoritesStore.favorites"
                :key="product.id"
                class="relative"
                draggable="true"
                @dragstart="handleDragStart(product)"
                @dragend="handleDragEnd"
              >
                <!-- 选择框 -->
                <div
                  class="absolute top-2 left-2 z-10"
                  @click.stop
                >
                  <input
                    type="checkbox"
                    :checked="selectedProducts.has(product.id)"
                    @change="toggleProductSelection(product.id)"
                    class="w-5 h-5 text-blue-600 bg-white border-gray-300 rounded focus:ring-blue-500 cursor-pointer"
                  />
                </div>
                
                <ProductCard
                  :product="product"
                  :is-favorite="true"
                  @click="handleProductClick(product)"
                  @favorite="handleRemoveFavorite(product)"
                />
              </div>
            </template>

            <!-- 看板产品 -->
            <template v-else-if="selectedBoard">
              <div
                v-for="product in currentBoardProducts"
                :key="product.id"
                class="relative group"
              >
                <!-- 移除按钮 -->
                <button
                  class="absolute top-2 right-2 z-10 p-2 bg-red-500 text-white rounded-full opacity-0 group-hover:opacity-100 transition-opacity"
                  @click.stop="handleRemoveFromBoard(product.id)"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
                
                <ProductCard
                  :product="product"
                  :show-favorite="false"
                  @click="handleProductClick(product)"
                />
              </div>
            </template>
          </div>

          <!-- 加载更多 -->
          <div
            v-if="activeTab === 'favorites' && !selectedBoard && hasMoreFavorites"
            class="mt-8 text-center"
          >
            <button
              class="px-6 py-3 bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
              @click="loadMoreFavorites"
              :disabled="favoritesStore.isLoading"
            >
              {{ favoritesStore.isLoading ? '加载中...' : '加载更多' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建看板弹窗 -->
    <div
      v-if="showCreateBoardModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click.self="showCreateBoardModal = false"
    >
      <div class="bg-white dark:bg-gray-800 rounded-xl p-6 w-full max-w-md">
        <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">创建新看板</h3>
        <input
          v-model="newBoardName"
          type="text"
          placeholder="输入看板名称"
          class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
          @keyup.enter="confirmCreateBoard"
        />
        <div class="flex gap-3 mt-6">
          <button
            class="flex-1 px-4 py-2 bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
            @click="showCreateBoardModal = false"
          >
            取消
          </button>
          <button
            class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            @click="confirmCreateBoard"
          >
            创建
          </button>
        </div>
      </div>
    </div>

    <!-- 添加到看板弹窗 -->
    <div
      v-if="showAddToBoardModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click.self="showAddToBoardModal = false"
    >
      <div class="bg-white dark:bg-gray-800 rounded-xl p-6 w-full max-w-md">
        <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">
          选择看板
        </h3>
        <div class="space-y-2 max-h-96 overflow-y-auto">
          <button
            v-for="board in favoritesStore.boards"
            :key="board.id"
            class="w-full p-3 text-left border border-gray-200 dark:border-gray-700 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
            @click="confirmAddToBoard(board)"
          >
            <div class="font-medium text-gray-900 dark:text-white">{{ board.name }}</div>
            <div class="text-sm text-gray-500 dark:text-gray-400 mt-1">
              {{ board.productCount }} 个产品
            </div>
          </button>
        </div>
        <button
          class="w-full mt-4 px-4 py-2 bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
          @click="showAddToBoardModal = false"
        >
          取消
        </button>
      </div>
    </div>

    <!-- 产品详情弹窗 -->
    <ProductDetailModal
      v-if="showProductModal"
      :show="showProductModal"
      :product="selectedProductDetail"
      :loading="loadingProductDetail"
      @close="handleCloseProductModal"
    />
  </div>
</template>

<style scoped>
/* 拖拽时的样式 */
[draggable="true"] {
  cursor: move;
}

[draggable="true"]:active {
  opacity: 0.5;
}
</style>
