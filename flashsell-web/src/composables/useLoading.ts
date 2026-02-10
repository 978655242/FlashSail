import { ref, computed, type Ref } from 'vue'

export interface LoadingState {
  isLoading: boolean
  error: string | null
  errorType: 'network' | 'server' | 'timeout' | 'unknown' | null
}

export function useLoading() {
  const loadingStates = ref<Record<string, LoadingState>>({})

  function startLoading(key: string = 'default') {
    loadingStates.value[key] = { isLoading: true, error: null, errorType: null }
  }

  function stopLoading(key: string = 'default') {
    if (loadingStates.value[key]) {
      loadingStates.value[key].isLoading = false
    }
  }

  function setError(error: string, key: string = 'default', errorType: LoadingState['errorType'] = 'unknown') {
    loadingStates.value[key] = { isLoading: false, error, errorType }
  }

  function clearError(key: string = 'default') {
    if (loadingStates.value[key]) {
      loadingStates.value[key].error = null
      loadingStates.value[key].errorType = null
    }
  }

  function isLoading(key: string = 'default'): Ref<boolean> {
    return computed(() => loadingStates.value[key]?.isLoading ?? false)
  }

  function getError(key: string = 'default'): Ref<string | null> {
    return computed(() => loadingStates.value[key]?.error ?? null)
  }

  function getErrorType(key: string = 'default'): Ref<LoadingState['errorType']> {
    return computed(() => loadingStates.value[key]?.errorType ?? null)
  }

  // 解析错误类型
  function parseErrorType(err: unknown): LoadingState['errorType'] {
    if (err instanceof Error) {
      const message = err.message.toLowerCase()
      if (message.includes('network') || message.includes('fetch')) {
        return 'network'
      }
      if (message.includes('timeout') || message.includes('timed out')) {
        return 'timeout'
      }
      if (message.includes('500') || message.includes('server')) {
        return 'server'
      }
    }
    return 'unknown'
  }

  // 包装异步函数，自动管理加载状态
  async function withLoading<T>(
    fn: () => Promise<T>,
    key: string = 'default'
  ): Promise<T | null> {
    startLoading(key)
    try {
      const result = await fn()
      stopLoading(key)
      return result
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '操作失败，请重试'
      const errorType = parseErrorType(err)
      setError(errorMessage, key, errorType)
      return null
    }
  }

  // 重置所有状态
  function reset(key?: string) {
    if (key) {
      delete loadingStates.value[key]
    } else {
      loadingStates.value = {}
    }
  }

  return {
    loadingStates,
    startLoading,
    stopLoading,
    setError,
    clearError,
    isLoading,
    getError,
    getErrorType,
    withLoading,
    reset
  }
}
