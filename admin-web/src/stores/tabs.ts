import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export interface TabItem {
  path: string
  fullPath: string
  name: string
  title: string
  affix?: boolean
}

function loadTabs(): TabItem[] {
  try {
    const raw = sessionStorage.getItem('wuliu_tabs')
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

export const useTabsStore = defineStore('tabs', () => {
  const tabs = ref<TabItem[]>(loadTabs())

  const cachedNames = computed(() => tabs.value.map((t) => t.name).filter(Boolean))

  function persist(): void {
    sessionStorage.setItem('wuliu_tabs', JSON.stringify(tabs.value))
  }

  function addTab(tab: TabItem): void {
    if (!tab.name) return
    const existing = tabs.value.find((t) => t.path === tab.path)
    if (existing) {
      existing.fullPath = tab.fullPath
      existing.title = tab.title
    } else {
      tabs.value.push(tab)
    }
    persist()
  }

  function removeTab(path: string): void {
    tabs.value = tabs.value.filter((t) => t.path !== path || t.affix)
    persist()
  }

  function removeOthers(path: string): void {
    tabs.value = tabs.value.filter((t) => t.affix || t.path === path)
    persist()
  }

  function removeAll(): void {
    tabs.value = tabs.value.filter((t) => t.affix)
    persist()
  }

  function updateTitle(path: string, title: string): void {
    const tab = tabs.value.find((t) => t.path === path)
    if (tab) {
      tab.title = title
      persist()
    }
  }

  function contains(path: string): boolean {
    return tabs.value.some((t) => t.path === path)
  }

  return { tabs, cachedNames, addTab, removeTab, removeOthers, removeAll, updateTitle, contains }
})
