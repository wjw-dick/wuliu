<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<template>
  <section class="not-found">
    <div class="container not-found__inner">
      <span class="not-found__code">404</span>
      <h1>这个页面走丢了</h1>
      <p>您访问的地址不存在或已被移除。可以返回首页，或者直接在线询价、查询运单。</p>
      <div class="not-found__actions">
        <RouterLink class="btn btn--primary" to="/">返回首页</RouterLink>
        <RouterLink class="btn btn--outline" to="/quote">在线询价</RouterLink>
        <RouterLink class="btn btn--outline" to="/track">运单查询</RouterLink>
      </div>
    </div>
  </section>
</template>

<style scoped>
.not-found {
  padding: 100px 0 120px;
  background: var(--soft);
}

.not-found__inner {
  text-align: center;
  max-width: 620px;
}

.not-found__code {
  display: block;
  font-size: clamp(80px, 14vw, 150px);
  font-weight: 800;
  line-height: 1;
  letter-spacing: -0.06em;
  background: linear-gradient(120deg, #bcd7f8, #1f6feb);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.not-found h1 {
  font-size: 28px;
  margin: 18px 0 10px;
}

.not-found p {
  font-size: 14px;
  color: var(--muted);
  line-height: 1.8;
}

.not-found__actions {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 28px;
}
</style>
