<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink } from 'vue-router'

const open = ref(false)
const officialUrl = import.meta.env.VITE_OFFICIAL_URL || 'http://localhost:5174'

const menu = [
  { label: '报价分享', desc: '查看并转发本次报价', to: '/quote-share', icon: '¥' },
  { label: '示例电子回单', desc: '查看签收信息与回单照片', to: '/receipt/YH0001202611140001', icon: '单' },
  { label: '司机免安装端', desc: '节点上报与回单上传', to: '/driver', icon: '车' }
]
</script>

<template>
  <header class="h5-header">
    <RouterLink class="h5-brand" to="/quote-share" aria-label="驰远物流移动服务首页">
      <span class="h5-brand__mark" aria-hidden="true"><i></i><i></i><i></i></span>
      <span class="h5-brand__text">
        <strong>驰远物流</strong>
        <small>移动服务</small>
      </span>
    </RouterLink>
    <div class="h5-header__actions">
      <a class="h5-header__call" href="tel:4008801688" aria-label="拨打客服热线">
        <span aria-hidden="true">☎</span>
      </a>
      <button class="h5-header__menu" type="button" aria-label="打开菜单" @click="open = true">
        <span></span><span></span><span></span>
      </button>
    </div>
  </header>

  <Teleport to="body">
    <div v-if="open" class="nav-mask" @click.self="open = false">
      <div class="nav-sheet" role="dialog" aria-modal="true" aria-label="移动端导航">
        <div class="nav-sheet__handle"></div>
        <div class="nav-sheet__head">
          <strong>驰远物流 · 移动服务</strong>
          <button type="button" aria-label="关闭菜单" @click="open = false">×</button>
        </div>
        <nav class="nav-sheet__list">
          <RouterLink v-for="item in menu" :key="item.to" :to="item.to" @click="open = false">
            <span class="nav-sheet__icon">{{ item.icon }}</span>
            <span class="nav-sheet__text">
              <strong>{{ item.label }}</strong>
              <small>{{ item.desc }}</small>
            </span>
            <span class="nav-sheet__arrow">›</span>
          </RouterLink>
          <a :href="officialUrl" class="nav-sheet__plain">访问官方网站 ↗</a>
          <a href="tel:4008801688" class="nav-sheet__plain">客服热线 400-880-1688</a>
        </nav>
        <p class="nav-sheet__foot">驰远物流 · 让每一票货物准时抵达</p>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.h5-header {
  position: sticky;
  top: 0;
  z-index: 60;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  height: 54px;
  padding: 0 14px;
  background: rgba(255, 255, 255, 0.97);
  border-bottom: 1px solid var(--line);
  backdrop-filter: blur(10px);
}

.h5-brand {
  display: flex;
  align-items: center;
  gap: 8px;
}

.h5-brand__mark {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: linear-gradient(145deg, #1f6feb, #0b3d91);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
  box-shadow: 0 4px 10px rgba(31, 111, 235, 0.25);
}

.h5-brand__mark i {
  display: block;
  width: 3px;
  border-radius: 2px;
  background: #fff;
}

.h5-brand__mark i:nth-child(1) {
  height: 10px;
  opacity: 0.65;
}

.h5-brand__mark i:nth-child(2) {
  height: 17px;
}

.h5-brand__mark i:nth-child(3) {
  height: 12px;
  opacity: 0.82;
}

.h5-brand__text {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.h5-brand__text strong {
  font-size: 15px;
  letter-spacing: 0.04em;
}

.h5-brand__text small {
  font-size: 9.5px;
  color: var(--muted-light);
}

.h5-header__actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.h5-header__call {
  width: 40px;
  height: 40px;
  border-radius: 9px;
  display: grid;
  place-items: center;
  color: var(--primary);
  background: #eef5ff;
  font-size: 15px;
}

.h5-header__menu {
  width: 44px;
  height: 44px;
  border-radius: 9px;
  border: 1px solid var(--line);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.h5-header__menu span {
  display: block;
  width: 17px;
  height: 2px;
  border-radius: 2px;
  background: var(--ink);
}

.nav-mask {
  position: fixed;
  inset: 0;
  z-index: 300;
  background: rgba(7, 26, 51, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  animation: fadeIn 0.18s ease;
}

.nav-sheet {
  width: 100%;
  max-width: var(--app-width);
  background: #fff;
  border-radius: 18px 18px 0 0;
  padding: 0 18px calc(18px + env(safe-area-inset-bottom));
  animation: slideUp 0.22s ease;
}

.nav-sheet__handle {
  width: 38px;
  height: 4px;
  border-radius: 3px;
  background: #dfe6f0;
  margin: 10px auto 8px;
}

.nav-sheet__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 0 14px;
  border-bottom: 1px solid var(--line);
}

.nav-sheet__head strong {
  font-size: 15px;
}

.nav-sheet__head button {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  color: var(--muted);
  font-size: 22px;
  line-height: 1;
}

.nav-sheet__list {
  padding: 8px 0;
  display: flex;
  flex-direction: column;
}

.nav-sheet__list > a {
  display: flex;
  align-items: center;
  gap: 11px;
  min-height: 58px;
  padding: 9px 2px;
  border-bottom: 1px solid #f0f4f9;
}

.nav-sheet__icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: #eef5ff;
  color: var(--primary);
  display: grid;
  place-items: center;
  font-size: 15px;
  font-weight: 700;
  flex-shrink: 0;
}

.nav-sheet__text {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.nav-sheet__text strong {
  font-size: 14px;
}

.nav-sheet__text small {
  font-size: 11px;
  color: var(--muted-light);
}

.nav-sheet__arrow {
  color: #b9c4d2;
  font-size: 20px;
}

.nav-sheet__plain {
  font-size: 12.5px;
  color: var(--muted);
  padding: 12px 2px;
  min-height: 44px;
  display: flex;
  align-items: center;
}

.nav-sheet__foot {
  text-align: center;
  font-size: 10.5px;
  color: var(--muted-light);
  padding-top: 10px;
}

@keyframes slideUp {
  from {
    transform: translateY(30px);
    opacity: 0.6;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
}
</style>
