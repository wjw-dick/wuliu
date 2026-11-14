import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { demoState } from '@/utils/demo-state'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const mobileMenuOpen = ref(false)
  const demoActive = computed(() => demoState.active)

  function toggleSidebar(): void {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setCollapsed(value: boolean): void {
    sidebarCollapsed.value = value
  }

  return { sidebarCollapsed, mobileMenuOpen, demoActive, toggleSidebar, setCollapsed }
})
