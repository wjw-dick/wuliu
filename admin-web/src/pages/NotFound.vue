<template>
  <div class="notfound">
    <h1>404</h1>
    <p>页面不存在或已被移除</p>
    <el-button type="primary" @click="$router.push('/login')">返回登录</el-button>
  </div>
</template>

<script setup>
</script>

<style scoped>
.notfound { height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px; }
.notfound h1 { font-size: 72px; color: #409eff; margin: 0; }
.notfound p { color: #909399; }
</style>
