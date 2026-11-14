<script setup lang="ts">
import { ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'

const open = ref(false)
const route = useRoute()

const nav = [
  { label: '首页', to: '/' },
  { label: '服务与价格', to: '/services' },
  { label: '在线询价', to: '/quote' },
  { label: '运单查询', to: '/track' },
  { label: '网点查询', to: '/sites' },
  { label: '企业月结', to: '/monthly' },
  { label: '资讯中心', to: '/news' },
  { label: '关于我们', to: '/about' }
]

watch(
  () => route.fullPath,
  () => {
    open.value = false
  }
)
</script>

<template>
  <header class="site-header">
    <a class="skip-link" href="#main">跳到主要内容</a>
    <div class="header-strip">
      <div class="container header-strip__inner">
        <span>长三角 12 城次日达 · 全程可视化 · 保险可托底</span>
        <div class="header-strip__links">
          <a href="tel:4008801688">客服热线 400-880-1688</a>
          <span class="header-strip__sep">|</span>
          <RouterLink to="/sites">查找网点</RouterLink>
        </div>
      </div>
    </div>
    <div class="container header-inner">
      <RouterLink class="brand" to="/" aria-label="驰远物流首页">
        <span class="brand__mark" aria-hidden="true">
          <span></span><span></span><span></span>
        </span>
        <span class="brand__text">
          <strong>驰远物流</strong>
          <small>CHIYUAN LOGISTICS</small>
        </span>
      </RouterLink>

      <nav class="nav" :class="{ 'nav--open': open }" aria-label="主导航">
        <RouterLink
          v-for="item in nav"
          :key="item.to"
          :to="item.to"
          class="nav__link"
          :class="{ 'nav__link--active': route.path === item.to }"
        >
          {{ item.label }}
        </RouterLink>
        <a class="nav__phone-mobile" href="tel:4008801688">客服热线 400-880-1688</a>
      </nav>

      <div class="header-actions">
        <a class="hotline" href="tel:4008801688">
          <span class="hotline__icon" aria-hidden="true">☎</span>
          <span>
            <small>7×24 小时服务</small>
            <strong>400-880-1688</strong>
          </span>
        </a>
        <RouterLink class="btn btn--primary btn--sm header-quote" to="/quote">立即询价</RouterLink>
        <button
          class="menu-toggle"
          type="button"
          :aria-expanded="open"
          aria-label="展开导航菜单"
          @click="open = !open"
        >
          <span></span><span></span><span></span>
        </button>
      </div>
    </div>
  </header>
</template>

<style scoped>
.site-header {
  position: sticky;
  top: 0;
  z-index: 80;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid rgba(226, 233, 243, 0.9);
}

.header-strip {
  background: #071a33;
  color: rgba(255, 255, 255, 0.68);
  font-size: 12px;
}

.header-strip__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 34px;
  gap: 16px;
}

.header-strip__links {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-strip__links a:hover {
  color: #fff;
}

.header-strip__sep {
  color: rgba(255, 255, 255, 0.24);
}

.header-inner {
  display: flex;
  align-items: center;
  gap: 28px;
  min-height: 76px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 11px;
  flex-shrink: 0;
}

.brand__mark {
  width: 40px;
  height: 40px;
  border-radius: 11px;
  background: linear-gradient(145deg, #1f6feb, #0b3d91);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
  box-shadow: 0 6px 16px rgba(31, 111, 235, 0.28);
}

.brand__mark span {
  display: block;
  width: 4px;
  border-radius: 3px;
  background: #fff;
}

.brand__mark span:nth-child(1) {
  height: 11px;
  opacity: 0.65;
}

.brand__mark span:nth-child(2) {
  height: 20px;
}

.brand__mark span:nth-child(3) {
  height: 14px;
  opacity: 0.82;
}

.brand__text {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
}

.brand__text strong {
  font-size: 19px;
  letter-spacing: 0.08em;
}

.brand__text small {
  font-size: 9px;
  color: var(--muted-light);
  letter-spacing: 0.14em;
  margin-top: 3px;
}

.nav {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.nav__link {
  position: relative;
  padding: 9px 10px;
  font-size: 14px;
  color: #35475d;
  border-radius: 8px;
  white-space: nowrap;
  transition: color 0.15s ease, background 0.15s ease;
}

.nav__link:hover {
  color: var(--primary);
  background: #f4f8ff;
}

.nav__link--active {
  color: var(--primary);
  font-weight: 600;
}

.nav__link--active::after {
  content: '';
  position: absolute;
  left: 10px;
  right: 10px;
  bottom: 2px;
  height: 2px;
  border-radius: 2px;
  background: var(--primary);
}

.nav__phone-mobile {
  display: none;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-left: auto;
  flex-shrink: 0;
}

.hotline {
  display: flex;
  align-items: center;
  gap: 9px;
  line-height: 1.2;
}

.hotline__icon {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: var(--soft-blue);
  color: var(--primary);
  font-size: 15px;
}

.hotline small {
  display: block;
  font-size: 10px;
  color: var(--muted-light);
}

.hotline strong {
  display: block;
  font-size: 15px;
  color: var(--ink);
  letter-spacing: 0.02em;
}

.menu-toggle {
  display: none;
  width: 44px;
  height: 44px;
  padding: 11px;
  border: 1px solid var(--line);
  border-radius: 9px;
  background: #fff;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
}

.menu-toggle span {
  display: block;
  height: 2px;
  border-radius: 2px;
  background: var(--ink);
}

@media (max-width: 1180px) {
  .hotline {
    display: none;
  }

  .nav__link {
    padding: 9px 8px;
    font-size: 13.5px;
  }
}

@media (max-width: 1020px) {
  .header-inner {
    gap: 14px;
  }

  .nav {
    position: absolute;
    left: 0;
    right: 0;
    top: 100%;
    display: none;
    flex-direction: column;
    align-items: stretch;
    gap: 2px;
    background: #fff;
    border-bottom: 1px solid var(--line);
    box-shadow: 0 24px 40px rgba(7, 26, 51, 0.12);
    padding: 12px 18px 18px;
  }

  .nav--open {
    display: flex;
  }

  .nav__link {
    padding: 12px 10px;
    font-size: 15px;
  }

  .nav__link--active::after {
    display: none;
  }

  .nav__phone-mobile {
    display: block;
    margin-top: 8px;
    padding-top: 12px;
    border-top: 1px solid var(--line);
    color: var(--muted);
    font-size: 13px;
  }

  .menu-toggle {
    display: flex;
  }

  .site-header {
    position: sticky;
  }

  .header-actions {
    gap: 10px;
  }
}

@media (max-width: 640px) {
  .header-strip {
    display: none;
  }

  .header-inner {
    min-height: 64px;
  }

  .brand__mark {
    width: 36px;
    height: 36px;
  }

  .brand__text strong {
    font-size: 17px;
  }

  .brand__text small {
    display: none;
  }

  .header-quote {
    display: none;
  }
}
</style>
