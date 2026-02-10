import { ref, readonly } from 'vue'

export interface ToastOptions {
  message: string
  type?: 'success' | 'error' | 'warning' | 'info'
  duration?: number
}

const visible = ref(false)
const message = ref('')
const type = ref<'success' | 'error' | 'warning' | 'info'>('info')
const duration = ref(3000)

export function useToast() {
  function show(options: ToastOptions) {
    message.value = options.message
    type.value = options.type || 'info'
    duration.value = options.duration || 3000
    visible.value = true
  }

  function success(msg: string, dur?: number) {
    show({ message: msg, type: 'success', duration: dur })
  }

  function error(msg: string, dur?: number) {
    show({ message: msg, type: 'error', duration: dur })
  }

  function warning(msg: string, dur?: number) {
    show({ message: msg, type: 'warning', duration: dur })
  }

  function info(msg: string, dur?: number) {
    show({ message: msg, type: 'info', duration: dur })
  }

  function close() {
    visible.value = false
  }

  return {
    visible: readonly(visible),
    message: readonly(message),
    type: readonly(type),
    duration: readonly(duration),
    show,
    success,
    error,
    warning,
    info,
    close
  }
}
