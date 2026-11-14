<template>
  <div class="tabs-bar">
    <el-scrollbar ref="scrollRef" class="tabs-scroll">
      <div class="tabs">
        <div
          v-for="tab in tabs.tabs"
          :key="tab.path"
          class="tab"
          :class="{ active: isActive(tab) }"
          @click="go(tab)"
          @contextmenu.prevent="openMenu($event, tab)"
        >
          <span class="tab-title">{{ tab.title }}</span>
          <el-icon v-if="!tab.affix" class="close-icon" @click.stop="closeTab(tab)"><Close /></el-icon>
        </div>
      </div>
    </el-scrollbar>
    <el-dropdown trigger="click" @command="handleCommand">
      <button class="more-btn" title="页签操作">
        <el-icon><MoreFilled /></el-icon>
      </button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="refresh">刷新当前页</el-dropdown-item>
          <el-dropdown-item command="others">关闭其他页签</el-dropdown-item>
          <el-dropdown-item command="all">关闭全部页签</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useTabsStore, type TabItem } from '@/stores/tabs'
import { useAuthStore } from '@/stores/auth'

const tabs = useTabsStore()
const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

function isActive(tab: TabItem): boolean {
  return route.path === tab.path
}

function go(tab: TabItem): void {
  if (route.fullPath !== tab.fullPath) router.push(tab.fullPath)
}

function dashboardPath(): string {
  return auth.scope === 'platform' ? '/platform/dashboard' : '/tenant/dashboard'
}

function closeTab(tab: TabItem): void {
  if (tab.affix) return
  const wasActive = isActive(tab)
  tabs.removeTab(tab.path)
  if (wasActive) {
    const last = tabs.tabs[tabs.tabs.length - 1]
    router.push(last?.fullPath || dashboardPath())
  }
}

function openMenu(_e: MouseEvent, tab: TabItem): void {
  if (!tab.affix) closeTab(tab)
}

function handleCommand(command: string): void {
  if (command === 'refresh') {
    const current = route.fullPath
    router.replace({ path: '/redirect', query: { path: current } }).catch(() => {
      router.go(0)
    })
  } else if (command === 'others') {
    tabs.removeOthers(route.path)
  } else if (command === 'all') {
    tabs.removeAll()
    router.push(dashboardPath())
  }
}
</script>

<style scoped lang="scss">
.tabs-bar {
  height: 38px;
  flex: 0 0 38px;
  background: #fff;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  min-width: 0;
}
.tabs-scroll {
  flex: 1;
  min-width: 0;
  :deep(.el-scrollbar__view) {
    height: 38px;
  }
}
.tabs {
  display: flex;
  align-items: center;
  height: 38px;
  padding: 0 6px;
  gap: 2px;
  white-space: nowrap;
}
.tab {
  height: 30px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 0 9px;
  border-radius: 4px;
  font-size: 12px;
  color: var(--text-secondary);
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.15s;
  flex: 0 0 auto;

  &:hover {
    background: #f4f7fc;
    color: var(--text-main);
    .close-icon {
      opacity: 1;
    }
  }
  &.active {
    background: #ecf3ff;
    color: var(--brand);
    border-color: #d6e4ff;
    font-weight: 500;
  }
}
.tab-title {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
}
.close-icon {
  font-size: 12px;
  border-radius: 50%;
  padding: 1px;
  opacity: 0.55;
  &:hover {
    background: rgba(31, 111, 235, 0.15);
    color: var(--brand);
  }
}
.more-btn {
  border: none;
  background: transparent;
  width: 34px;
  height: 36px;
  border-left: 1px solid var(--border-color);
  color: var(--text-secondary);
  cursor: pointer;
  &:hover {
    color: var(--brand);
    background: #f7f9fc;
  }
}
</style>
