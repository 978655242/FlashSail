<script setup lang="ts">
import { ref, computed } from 'vue'
import type { CategoryGroup, Category } from '@/types/product'

interface Props {
  groups: CategoryGroup[]
  selectedCategoryId?: number
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  selectedCategoryId: undefined,
  loading: false
})

const emit = defineEmits<{
  select: [categoryId: number | undefined]
}>()

const expandedGroups = ref<Set<number>>(new Set())

const selectedCategory = computed(() => {
  if (!props.selectedCategoryId) return null
  for (const group of props.groups) {
    const category = group.categories.find(c => c.id === props.selectedCategoryId)
    if (category) return { group, category }
  }
  return null
})

function toggleGroup(groupId: number) {
  if (expandedGroups.value.has(groupId)) {
    expandedGroups.value.delete(groupId)
  } else {
    expandedGroups.value.add(groupId)
  }
}

function selectCategory(category: Category | null) {
  emit('select', category?.id)
}

function isGroupExpanded(groupId: number) {
  return expandedGroups.value.has(groupId)
}
</script>

<template>
  <div class="glass-card rounded-xl shadow-sm">
    <!-- 标题 -->
    <div class="px-4 py-3 border-b border-slate-200 dark:border-slate-700">
      <h3 class="font-medium text-slate-800 dark:text-white">品类筛选</h3>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="p-4 space-y-3">
      <div v-for="i in 4" :key="i" class="animate-pulse">
        <div class="h-5 bg-slate-200 dark:bg-slate-700 rounded w-24 mb-2"></div>
        <div class="space-y-2 pl-4">
          <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-20"></div>
          <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-16"></div>
        </div>
      </div>
    </div>

    <!-- 品类列表 -->
    <div v-else class="p-2">
      <!-- 全部品类 -->
      <button
        :class="[
          'w-full flex items-center gap-2 px-3 py-2 rounded-lg text-left transition-colors',
          !selectedCategoryId
            ? 'bg-orange-50 dark:bg-orange-900/30 text-orange-500'
            : 'text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-700/50'
        ]"
        @click="selectCategory(null)"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 10h16M4 14h16M4 18h16" />
        </svg>
        <span class="text-sm font-medium">全部品类</span>
      </button>

      <!-- 品类组 -->
      <div v-for="group in groups" :key="group.id" class="mt-1">
        <!-- 组标题 -->
        <button
          class="w-full flex items-center justify-between px-3 py-2 rounded-lg text-left hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors"
          @click="toggleGroup(group.id)"
        >
          <span class="text-sm font-medium text-slate-700 dark:text-slate-300">{{ group.name }}</span>
          <svg
            :class="[
              'w-4 h-4 text-slate-400 transition-transform duration-200',
              isGroupExpanded(group.id) ? 'rotate-180' : ''
            ]"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
          </svg>
        </button>

        <!-- 子品类 -->
        <Transition
          enter-active-class="transition ease-out duration-200"
          enter-from-class="opacity-0 -translate-y-1"
          enter-to-class="opacity-100 translate-y-0"
          leave-active-class="transition ease-in duration-150"
          leave-from-class="opacity-100 translate-y-0"
          leave-to-class="opacity-0 -translate-y-1"
        >
          <div v-if="isGroupExpanded(group.id)" class="pl-4 space-y-0.5">
            <button
              v-for="category in group.categories"
              :key="category.id"
              :class="[
                'w-full flex items-center justify-between px-3 py-1.5 rounded-lg text-left transition-colors',
                selectedCategoryId === category.id
                  ? 'bg-orange-50 dark:bg-orange-900/30 text-orange-500'
                  : 'text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-700/50'
              ]"
              @click="selectCategory(category)"
            >
              <span class="text-sm">{{ category.name }}</span>
              <span class="text-xs text-slate-400">{{ category.productCount }}</span>
            </button>
          </div>
        </Transition>
      </div>
    </div>

    <!-- 已选品类提示 -->
    <div v-if="selectedCategory" class="px-4 py-3 border-t border-slate-200 dark:border-slate-700">
      <div class="flex items-center justify-between">
        <span class="text-xs text-slate-500 dark:text-slate-400">已选择</span>
        <button
          class="text-xs text-orange-500 hover:text-orange-600"
          @click="selectCategory(null)"
        >
          清除
        </button>
      </div>
      <div class="mt-1 flex items-center gap-2">
        <span class="px-2 py-1 text-xs bg-orange-100 dark:bg-orange-900/30 text-orange-500 rounded">
          {{ selectedCategory.group.name }}
        </span>
        <span class="text-slate-400">›</span>
        <span class="text-sm text-slate-700 dark:text-slate-300">{{ selectedCategory.category.name }}</span>
      </div>
    </div>
  </div>
</template>
