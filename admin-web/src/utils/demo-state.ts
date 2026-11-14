import { reactive } from 'vue'

export const demoState = reactive({
  active: false,
  reason: ''
})

export function markDemoMode(reason = '后端未连接'): void {
  if (demoState.active) return
  demoState.active = true
  demoState.reason = reason
}
