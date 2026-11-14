<template>
  <aside class="sidebar" :class="{ collapsed: app.sidebarCollapsed }">
    <div class="logo" @click="goHome">
      <div class="logo-mark">全</div>
      <div v-show="!app.sidebarCollapsed" class="logo-text">
        <strong>全达物流</strong>
        <span>{{ isPlatform ? '平台运营端' : '租户管理后台' }}</span>
      </div>
    </div>

    <el-scrollbar class="menu-scroll">
      <el-menu
        :default-active="activeMenu"
        :collapse="app.sidebarCollapsed"
        :collapse-transition="false"
        background-color="transparent"
        text-color="#a7b3c6"
        active-text-color="#ffffff"
        router
        class="side-menu"
      >
        <template v-for="group in menus" :key="group.key">
          <el-sub-menu v-if="group.children.length > 1" :index="group.key">
            <template #title>
              <el-icon><component :is="group.icon" /></el-icon>
              <span>{{ group.title }}</span>
            </template>
            <el-menu-item v-for="item in group.children" :key="item.path" :index="item.path">
              <el-icon><component :is="item.icon" /></el-icon>
              <template #title>{{ item.title }}</template>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="group.children[0].path">
            <el-icon><component :is="group.icon" /></el-icon>
            <template #title>{{ group.children[0].title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-scrollbar>

    <div class="sidebar-footer">
      <div v-show="!app.sidebarCollapsed" class="tenant-badge">
        <el-icon><OfficeBuilding /></el-icon>
        <span class="ellipsis">{{ isPlatform ? '平台运营中心' : auth.tenantName }}</span>
      </div>
      <button class="collapse-btn" title="折叠菜单" @click="app.toggleSidebar()">
        <el-icon><component :is="app.sidebarCollapsed ? 'Expand' : 'Fold'" /></el-icon>
      </button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { buildMenus } from '@/router/menu'
import { layoutRoutes } from '@/router/routes'

const app = useAppStore()
const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const isPlatform = computed(() => auth.scope === 'platform')
const menus = computed(() => buildMenus(layoutRoutes, auth.scope, (p) => auth.hasPerm(p)))
const activeMenu = computed(() => (route.meta.activeMenu as string) || route.path)

function goHome(): void {
  router.push(isPlatform.value ? '/platform/dashboard' : '/tenant/dashboard')
}
</script>

<style scoped lang="scss">
.sidebar {
  width: 216px;
  flex: 0 0 216px;
  background: var(--sidebar-bg);
  height: 100vh;
  display: flex;
  flex-direction: column;
  transition: width 0.2s ease, flex-basis 0.2s ease;
  overflow: hidden;
  z-index: 20;

  &.collapsed {
    width: 64px;
    flex-basis: 64px;
  }
}

.logo {
  height: 56px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex: 0 0 56px;
}
.logo-mark {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: linear-gradient(135deg, #3b8af7, #1f6feb);
  color: #fff;
  font-weight: 700;
  font-size: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(31, 111, 235, 0.45);
  flex: 0 0 30px;
}
.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
  white-space: nowrap;

  strong {
    color: #fff;
    font-size: 14px;
    letter-spacing: 1px;
  }
  span {
    color: #7f90a8;
    font-size: 10px;
    margin-top: 3px;
  }
}
.menu-scroll {
  flex: 1;
  min-height: 0;
  padding: 8px 0;
}

.side-menu {
  border-right: none;
  padding: 0 8px;

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    height: 40px;
    line-height: 40px;
    border-radius: 6px;
    margin-bottom: 2px;
    font-size: 13px;
  }
  :deep(.el-menu-item:hover),
  :deep(.el-sub-menu__title:hover) {
    background: rgba(255, 255, 255, 0.06) !important;
    color: #fff;
  }
  :deep(.el-menu-item.is-active) {
    background: linear-gradient(90deg, #1f6feb, #2a7bf7) !important;
    color: #fff !important;
    box-shadow: 0 3px 10px rgba(31, 111, 235, 0.35);
  }
  :deep(.el-sub-menu .el-menu-item) {
    font-size: 12.5px;
    padding-left: 44px !important;
  }
  :deep(.el-icon) {
    font-size: 16px;
    margin-right: 8px;
  }
}

.sidebar-footer {
  flex: 0 0 auto;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  padding: 10px 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
}
.tenant-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #8fa0b8;
  font-size: 11px;
  min-width: 0;
  max-width: 148px;
}
.collapse-btn {
  border: none;
  background: rgba(255, 255, 255, 0.06);
  color: #a7b3c6;
  width: 30px;
  height: 30px;
  border-radius: 6px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 30px;

  &:hover {
    background: rgba(255, 255, 255, 0.14);
    color: #fff;
  }
}

:deep(.el-menu--collapse) {
  width: 48px;
}
</style>
