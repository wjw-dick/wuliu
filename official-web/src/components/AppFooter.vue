<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<template>
  <footer class="site-footer">
    <div class="container footer-main">
      <div class="footer-brand">
        <div class="footer-logo">
          <span class="footer-logo__mark" aria-hidden="true"></span>
          <strong>驰远物流</strong>
        </div>
        <p>
          专注长三角合同物流与零担专线，以数字化调度与全程可视化，为制造、商贸与电商企业提供确定的运输服务。
        </p>
        <a class="footer-phone" href="tel:4008801688">
          <span>服务热线</span>
          <strong>400-880-1688</strong>
        </a>
        <p class="footer-hours">周一至周日 07:00-22:00（节假日无休）</p>
      </div>

      <div class="footer-col">
        <h3>产品服务</h3>
        <RouterLink to="/services">零担专线</RouterLink>
        <RouterLink to="/services">整车包车</RouterLink>
        <RouterLink to="/services">同城配送</RouterLink>
        <RouterLink to="/services">仓储与装卸</RouterLink>
        <RouterLink to="/monthly">企业月结</RouterLink>
      </div>

      <div class="footer-col">
        <h3>快速入口</h3>
        <RouterLink to="/quote">在线询价</RouterLink>
        <RouterLink to="/track">运单查询</RouterLink>
        <RouterLink to="/sites">网点查询</RouterLink>
        <RouterLink to="/news">资讯中心</RouterLink>
        <RouterLink to="/contact">联系我们</RouterLink>
      </div>

      <div class="footer-col">
        <h3>关于我们</h3>
        <RouterLink to="/about">公司简介</RouterLink>
        <RouterLink to="/join">加入我们</RouterLink>
        <RouterLink to="/privacy">隐私政策</RouterLink>
        <RouterLink to="/terms">服务条款</RouterLink>
        <RouterLink to="/contact">商务合作</RouterLink>
      </div>
    </div>

    <div class="footer-bottom">
      <div class="container footer-bottom__inner">
        <p>© 2026 上海驰远物流有限公司 版权所有</p>
        <p>沪ICP备2026001688号-1 · 道路运输经营许可证 沪交运管许可青字 310118008866 号</p>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  background: #071a33;
  color: rgba(255, 255, 255, 0.62);
  font-size: 13px;
}

.footer-main {
  display: grid;
  grid-template-columns: 1.7fr 1fr 1fr 1fr;
  gap: 46px;
  padding-top: 64px;
  padding-bottom: 46px;
}

.footer-brand p {
  max-width: 340px;
  line-height: 1.8;
  margin-top: 14px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #fff;
}

.footer-logo__mark {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: linear-gradient(145deg, #4096ff, #1f6feb);
  position: relative;
}

.footer-logo__mark::before,
.footer-logo__mark::after {
  content: '';
  position: absolute;
  background: #fff;
  border-radius: 2px;
}

.footer-logo__mark::before {
  width: 3px;
  height: 14px;
  left: 10px;
  top: 8px;
}

.footer-logo__mark::after {
  width: 3px;
  height: 9px;
  left: 16px;
  top: 11px;
  opacity: 0.8;
}

.footer-logo strong {
  font-size: 18px;
  letter-spacing: 0.08em;
}

.footer-phone {
  display: inline-flex;
  flex-direction: column;
  margin-top: 20px;
  line-height: 1.3;
}

.footer-phone span {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.45);
}

.footer-phone strong {
  font-size: 22px;
  color: #fff;
  letter-spacing: 0.04em;
}

.footer-hours {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.45);
  margin-top: 4px;
}

.footer-col {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.footer-col h3 {
  color: #fff;
  font-size: 14px;
  margin-bottom: 6px;
}

.footer-col a {
  transition: color 0.15s ease;
  width: fit-content;
}

.footer-col a:hover {
  color: #fff;
}

.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.09);
}

.footer-bottom__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-top: 18px;
  padding-bottom: 18px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
  flex-wrap: wrap;
}

@media (max-width: 900px) {
  .footer-main {
    grid-template-columns: 1fr 1fr;
    gap: 34px;
    padding-top: 46px;
  }

  .footer-brand {
    grid-column: 1 / -1;
  }
}

@media (max-width: 520px) {
  .footer-main {
    grid-template-columns: 1fr;
  }
}
</style>
