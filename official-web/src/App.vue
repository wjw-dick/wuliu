<template>
  <div>
    <header class="header">
      <div class="container header-inner">
        <router-link to="/" class="logo">示例物流</router-link>
        <nav class="nav">
          <router-link to="/" exact-active-class="active">首页</router-link>
          <router-link to="/quote" active-class="active">在线询价</router-link>
          <router-link to="/track" active-class="active">运单跟踪</router-link>
          <router-link to="/services" active-class="active">服务网络</router-link>
          <router-link to="/about" active-class="active">关于我们</router-link>
        </nav>
      </div>
    </header>
    <main><router-view /></main>
    <footer class="footer">
      <div class="container footer-inner">
        <div>
          <h4>示例物流</h4>
          <p>专注零担与整车运输的数字化货运平台，覆盖全国主要城市。</p>
        </div>
        <div>
          <h4>服务</h4>
          <p>零担运输 · 整车包车 · 仓储配送 · 代收货款</p>
        </div>
        <div>
          <h4>联系方式</h4>
          <p>客服热线：400-800-0000<br />合作热线：13800000000</p>
        </div>
        <div>
          <h4>平台入口</h4>
          <p>
            <a href="http://localhost:5173" target="_blank">运营后台</a> ·
            <a href="http://localhost:5175" target="_blank">移动端</a>
          </p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
</script>
