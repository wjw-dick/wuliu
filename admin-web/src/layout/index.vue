<template>
  <div class="app-layout">
    <Sidebar />
    <div class="app-body">
      <Navbar />
      <TabsBar />
      <div v-if="app.demoActive" class="demo-banner">
        <el-icon><InfoFilled /></el-icon>
        <span><strong>演示数据：后端未连接</strong>，当前展示内置演示数据，所有操作仅在本地生效。</span>
        <el-button link size="small" @click="retry">重新连接</el-button>
      </div>
      <AppMain />
    </div>
  </div>
</template>

<script setup lang="ts">
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'
import TabsBar from './TabsBar.vue'
import AppMain from './AppMain.vue'
import { useAppStore } from '@/stores/app'

const app = useAppStore()

function retry(): void {
  window.location.reload()
}
</script>

<style scoped lang="scss">
.app-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}
.app-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.demo-banner {
  flex: 0 0 32px;
  height: 32px;
  background: #fff8e6;
  border-bottom: 1px solid #f5e1ab;
  color: #8a5a00;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 12px;
  font-size: 12px;
  z-index: 5;

  strong {
    font-weight: 600;
  }
  .el-button {
    margin-left: auto;
    color: #8a5a00;
  }
}
</style>
