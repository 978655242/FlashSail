import { ref, readonly } from 'vue'

export interface ConfirmOptions {
  title?: string
  message: string
  confirmText?: string
  cancelText?: string
  type?: 'info' | 'warning' | 'danger'
}

const visible = ref(false)
const title = ref('确认操作')
const message = ref('')
const confirmText = ref('确认')
const cancelText = ref('取消')
const type = ref<'info' | 'warning' | 'danger'>('info')
const loading = ref(false)

let resolvePromise: ((value: boolean) => void) | null = null

export function useConfirm() {
  function show(options: ConfirmOptions): Promise<boolean> {
    title.value = options.title || '确认操作'
    message.value = options.message
    confirmText.value = options.confirmText || '确认'
    cancelText.value = options.cancelText || '取消'
    type.value = options.type || 'info'
    loading.value = false
    visible.value = true

    return new Promise((resolve) => {
      resolvePromise = resolve
    })
  }

  function confirm() {
    if (resolvePromise) {
      resolvePromise(true)
      resolvePromise = null
    }
    visible.value = false
  }

  function cancel() {
    if (resolvePromise) {
      resolvePromise(false)
      resolvePromise = null
    }
    visible.value = false
  }

  function setLoading(value: boolean) {
    loading.value = value
  }

  // 便捷方法
  function danger(msg: string, options?: Partial<ConfirmOptions>): Promise<boolean> {
    return show({
      title: options?.title || '危险操作',
      message: msg,
      confirmText: options?.confirmText || '确认删除',
      cancelText: options?.cancelText || '取消',
      type: 'danger'
    })
  }

  function warning(msg: string, options?: Partial<ConfirmOptions>): Promise<boolean> {
    return show({
      title: options?.title || '警告',
      message: msg,
      confirmText: options?.confirmText || '继续',
      cancelText: options?.cancelText || '取消',
      type: 'warning'
    })
  }

  return {
    visible: readonly(visible),
    title: readonly(title),
    message: readonly(message),
    confirmText: readonly(confirmText),
    cancelText: readonly(cancelText),
    type: readonly(type),
    loading: readonly(loading),
    show,
    confirm,
    cancel,
    setLoading,
    danger,
    warning
  }
}
