<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '64px' : '220px'" class="aside">
      <div class="logo" @click="$router.push(`/${scope}/dashboard`)">
        <span v-if="!collapsed">示例物流云平台</span>
        <span v-else>云</span>
      </div>
      <el-menu
        :default-active="$route.path"
        :collapse="collapsed"
        :collapse-transition="false"
        router
        background-color="#1f2d3d"
        text-color="#cfd8e3"
        active-text-color="#ffffff"
      >
        <template v-for="item in menus" :key="item.path">
          <el-menu-item :index="item.path">
            <el-icon><component :is="item.icon || 'Menu'" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="left">
          <el-icon class="collapse-btn" @click="collapsed = !collapsed">
            <Fold v-if="!collapsed" />
            <Expand v-else />
          </el-icon>
          <span class="scope-tag">{{ scope === 'platform' ? '平台运营中心' : '租户运营后台' }}</span>
        </div>
        <div class="right">
          <el-dropdown @command="onCommand">
            <span class="user">
              <el-icon><UserFilled /></el-icon>
              {{ user?.nickname || user?.username || '账号' }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuth } from '../store/auth'
import { platformRoutes, tenantRoutes } from '../router'

const route = useRoute()
const router = useRouter()
const auth = useAuth()
const collapsed = ref(false)

const scope = computed(() => auth.scope || route.path.split('/')[1])
const user = computed(() => auth.user)

const menus = computed(() => {
  const list = scope.value === 'platform' ? platformRoutes : tenantRoutes
  return list.filter((r) => !r.meta?.hidden)
})

async function onCommand(cmd) {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
    auth.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout { height: 100%; }
.aside { background: #1f2d3d; transition: width .2s; overflow: hidden; }
.logo {
  height: 56px; display: flex; align-items: center; justify-content: center;
  color: #fff; font-weight: 700; font-size: 16px; background: #17222f; cursor: pointer;
}
.aside .el-menu { border-right: none; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; border-bottom: 1px solid #e4e7ed; height: 56px;
}
.collapse-btn { font-size: 20px; cursor: pointer; color: #606266; }
.scope-tag { margin-left: 12px; font-size: 14px; color: #606266; }
.right .user { cursor: pointer; display: flex; align-items: center; gap: 4px; color: #303133; }
.main { padding: 0; overflow: auto; background: #f5f7fa; }
</style>
