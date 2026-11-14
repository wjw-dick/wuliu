<template>
  <div class="error-page">
    <div class="code">404</div>
    <div class="title">页面走丢了</div>
    <div class="desc">您访问的地址不存在或已被移除，请检查链接是否正确。</div>
    <div class="actions">
      <el-button type="primary" @click="router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<style scoped>
.error-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--page-bg);
  text-align: center;
}
.code {
  font-size: 92px;
  font-weight: 800;
  color: #dbe6f7;
  line-height: 1;
}
.title {
  font-size: 18px;
  font-weight: 600;
  margin: 14px 0 8px;
}
.desc {
  font-size: 12.5px;
  color: var(--text-muted);
}
.actions {
  margin-top: 20px;
}
</style>
