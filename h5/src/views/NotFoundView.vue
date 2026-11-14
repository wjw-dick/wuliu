<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<template>
  <section class="page page--no-bar not-found">
    <div class="not-found__code">404</div>
    <h1>页面不存在</h1>
    <p>您访问的链接可能已失效。可以返回报价分享页，或前往司机端。</p>
    <div class="not-found__actions">
      <RouterLink class="btn btn--primary btn--block" to="/quote-share">返回报价分享</RouterLink>
      <RouterLink class="btn btn--outline btn--block" to="/driver">司机端</RouterLink>
    </div>
  </section>
</template>

<style scoped>
.not-found {
  text-align: center;
  padding-top: 60px;
}

.not-found__code {
  font-size: 84px;
  line-height: 1;
  font-weight: 800;
  letter-spacing: -0.05em;
  background: linear-gradient(120deg, #bcd7f8, #1f6feb);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.not-found h1 {
  font-size: 20px;
  margin: 14px 0 6px;
}

.not-found p {
  font-size: 13px;
  color: var(--muted);
  margin-bottom: 22px;
}

.not-found__actions {
  display: grid;
  gap: 10px;
}
</style>
