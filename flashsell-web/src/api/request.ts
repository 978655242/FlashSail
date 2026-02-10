import axios, { type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig, AxiosError } from 'axios'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// API 响应格式
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

// 错误类型
export enum ErrorType {
  NETWORK = 'NETWORK',
  TIMEOUT = 'TIMEOUT',
  SERVER = 'SERVER',
  BUSINESS = 'BUSINESS',
  AUTH = 'AUTH'
}

// 错误类型到 NetworkError 组件类型的映射
export type NetworkErrorType = 'network' | 'server' | 'timeout' | 'unknown'

// 自定义错误类
export class ApiError extends Error {
  /** Number of retry attempts made before this error was thrown */
  public retryCount: number

  constructor(
    public type: ErrorType,
    message: string,
    public originalError?: any,
    retryCount: number = 0
  ) {
    super(message)
    this.name = 'ApiError'
    this.retryCount = retryCount
  }

  /**
   * Check if this error can be retried by the user
   * Business and Auth errors should not be retried
   */
  get canRetry(): boolean {
    return this.type !== ErrorType.BUSINESS && this.type !== ErrorType.AUTH
  }

  /**
   * Get the error type for NetworkError component
   */
  get networkErrorType(): NetworkErrorType {
    switch (this.type) {
      case ErrorType.NETWORK:
        return 'network'
      case ErrorType.SERVER:
        return 'server'
      case ErrorType.TIMEOUT:
        return 'timeout'
      default:
        return 'unknown'
    }
  }
}

// 重试配置 - exported for testing
export const MAX_RETRY_TIMES = 2
export const RETRY_DELAY = 1000

/**
 * Classify an error and return appropriate display information
 * @param error - The error to classify
 * @returns Object with error type, message, and retry capability
 */
export function classifyError(error: unknown): {
  type: NetworkErrorType
  message: string
  canRetry: boolean
  isApiError: boolean
} {
  if (error instanceof ApiError) {
    return {
      type: error.networkErrorType,
      message: error.message,
      canRetry: error.canRetry,
      isApiError: true
    }
  }

  if (axios.isAxiosError(error)) {
    if (!error.response) {
      return {
        type: 'network',
        message: '网络连接失败，请检查网络',
        canRetry: true,
        isApiError: false
      }
    }
    if (error.response.status >= 500) {
      return {
        type: 'server',
        message: '服务器错误，请稍后重试',
        canRetry: true,
        isApiError: false
      }
    }
    if (error.code === 'ECONNABORTED') {
      return {
        type: 'timeout',
        message: '请求超时，请检查网络后重试',
        canRetry: true,
        isApiError: false
      }
    }
  }

  return {
    type: 'unknown',
    message: error instanceof Error ? error.message : '发生未知错误',
    canRetry: true,
    isApiError: false
  }
}

// 延迟函数
function delay(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}

// 判断是否为可重试的错误
function isRetryableError(error: AxiosError): boolean {
  // 网络错误
  if (!error.response) {
    return true
  }
  // 5xx 服务器错误
  const status = error.response.status
  return status >= 500 && status < 600
}

// 获取重试次数
function getRetryCount(config: any): number {
  return config?._retryCount || 0
}

// 设置重试次数
function setRetryCount(config: any, count: number): void {
  if (config) {
    config._retryCount = count
  }
}

// 显示错误提示
function showErrorToast(message: string): void {
  // 使用简单的 console.error，也可以集成第三方 toast 库
  console.error('[API Error]', message)
  // TODO: 可以集成 Element Plus 的 ElMessage 或其他 toast 库
  // ElMessage.error(message)
}

// 创建 axios 实例
const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig & { _retryCount?: number }) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data

    // 业务错误处理
    if (res.code !== 0 && res.code !== 200) {
      const error = new ApiError(ErrorType.BUSINESS, res.message || '业务错误')
      showErrorToast(res.message || '请求失败')
      return Promise.reject(error)
    }

    return response
  },
  async (error: AxiosError) => {
    const userStore = useUserStore()
    const config = error.config as any

    // 401 未授权处理
    if (error.response?.status === 401) {
      // Token 过期，尝试刷新
      if (userStore.refreshToken && !config?._retry && error.config) {
        config._retry = true
        try {
          const { data } = await axios.post<ApiResponse<{ token: string; refreshToken: string }>>(
            '/api/auth/refresh',
            { refreshToken: userStore.refreshToken }
          )

          if (data.code === 0 || data.code === 200) {
            userStore.updateToken(data.data.token, data.data.refreshToken)
            error.config.headers.Authorization = `Bearer ${data.data.token}`
            return request(error.config)
          }
        } catch {
          // 刷新失败，登出
          showErrorToast('登录已过期，请重新登录')
          userStore.logout()
          router.push({ name: 'Login' })
        }
      } else {
        // 没有 refresh token 或刷新失败
        showErrorToast('登录已过期，请重新登录')
        userStore.logout()
        router.push({ name: 'Login' })
      }

      const authError = new ApiError(ErrorType.AUTH, '未授权或登录已过期', error)
      return Promise.reject(authError)
    }

    // 超时错误
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      // 尝试重试
      const retryCount = getRetryCount(config)
      if (retryCount < MAX_RETRY_TIMES && error.config) {
        setRetryCount(config, retryCount + 1)
        await delay(RETRY_DELAY)
        return request(error.config)
      }

      const timeoutError = new ApiError(ErrorType.TIMEOUT, '请求超时，请检查网络后重试', error, retryCount)
      showErrorToast('请求超时，请检查网络后重试')
      return Promise.reject(timeoutError)
    }

    // 网络错误
    if (!error.response) {
      // 尝试重试
      const retryCount = getRetryCount(config)
      if (isRetryableError(error) && retryCount < MAX_RETRY_TIMES && error.config) {
        setRetryCount(config, retryCount + 1)
        await delay(RETRY_DELAY)
        return request(error.config)
      }

      const networkError = new ApiError(ErrorType.NETWORK, '网络连接失败，请检查网络', error, retryCount)
      showErrorToast('网络连接失败，请检查网络')
      return Promise.reject(networkError)
    }

    // 5xx 服务器错误
    if (error.response.status >= 500 && error.response.status < 600) {
      // 尝试重试
      const retryCount = getRetryCount(config)
      if (retryCount < MAX_RETRY_TIMES && error.config) {
        setRetryCount(config, retryCount + 1)
        await delay(RETRY_DELAY)
        return request(error.config)
      }

      const serverError = new ApiError(ErrorType.SERVER, '服务器错误，请稍后重试', error, retryCount)
      showErrorToast('服务器错误，请稍后重试')
      return Promise.reject(serverError)
    }

    // 4xx 其他客户端错误
    if (error.response.status >= 400 && error.response.status < 500) {
      const message = (error.response.data as any)?.message || '请求错误'
      const clientError = new ApiError(ErrorType.BUSINESS, message, error)
      showErrorToast(message)
      return Promise.reject(clientError)
    }

    // 未知错误
    const unknownError = new ApiError(ErrorType.NETWORK, '请求失败，请重试', error)
    showErrorToast('请求失败，请重试')
    return Promise.reject(unknownError)
  }
)

export default request
