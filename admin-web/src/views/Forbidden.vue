<template>
  <div class="error-page">
    <div class="code">403</div>
    <div class="title">抱歉，您没有访问该页面的权限</div>
    <div class="desc">请联系租户管理员在「角色权限」中为当前账号授权，或返回工作台继续操作。</div>
    <div class="actions">
      <el-button type="primary" @click="goHome">返回工作台</el-button>
      <el-button @click="router.back()">返回上一页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

function goHome(): void {
  router.push(auth.scope === 'platform' ? '/platform/dashboard' : '/tenant/dashboard')
}
</script>

<style scoped>
.error-page {
  height: 100%;
  min-height: 70vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  text-align: center;
}
.code {
  font-size: 92px;
  font-weight: 800;
  color: #e3ebf8;
  line-height: 1;
  letter-spacing: 4px;
  font-family: 'DIN Alternate', Consolas, sans-serif;
}
.title {
  font-size: 18px;
  font-weight: 600;
  margin: 14px 0 8px;
}
.desc {
  font-size: 12.5px;
  color: var(--text-muted);
  max-width: 420px;
  line-height: 1.8;
}
.actions {
  margin-top: 20px;
  display: flex;
  gap: 8px;
}
</style>
