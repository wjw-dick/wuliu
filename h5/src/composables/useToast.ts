import { ref } from 'vue'

export type ToastType = 'info' | 'success' | 'error'

export interface ToastItem {
  id: number
  message: string
  type: ToastType
}

export const toasts = ref<ToastItem[]>([])
let seq = 0

export function showToast(message: string, type: ToastType = 'info', duration = 2400) {
  const id = ++seq
  toasts.value.push({ id, message, type })
  window.setTimeout(() => {
    toasts.value = toasts.value.filter((item) => item.id !== id)
  }, duration)
}
