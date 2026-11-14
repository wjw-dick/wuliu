<template>
  <div>
    <section class="hero">
      <div class="container">
        <h1>货运数字化 · 多端运营平台</h1>
        <p>在线询价、一键下单、全程轨迹、电子签收、代收货款，让物流更高效透明。</p>
        <div style="display:flex;gap:14px">
          <router-link to="/quote" class="btn">立即询价</router-link>
          <router-link to="/track" class="btn ghost">查询运单</router-link>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="container">
        <h2>核心服务</h2>
        <p class="sub">覆盖全国主要城市的综合物流服务</p>
        <div class="grid3">
          <div class="card"><h3>零担运输</h3><p>按重量体积取大计费，首重一口价，适合中小批量货物运输，经济高效。</p></div>
          <div class="card"><h3>整车包车</h3><p>4.2米至17.5米多种车型，门到门直达，适合大批量与项目货物运输。</p></div>
          <div class="card"><h3>仓储配送</h3><p>中转仓、分拨中心协同，仓配一体化，支持一件代发与定时配送。</p></div>
          <div class="card"><h3>代收货款</h3><p>货到付款与代收货款专业服务，T+1 快速回款，渠道对账清晰。</p></div>
          <div class="card"><h3>全程轨迹</h3><p>提货、发车、中转、派送、签收全节点记录，客户实时掌握运单动态。</p></div>
          <div class="card"><h3>电子签收</h3><p>司机端拍照签收、电子回单，回单影像存档，理赔有据可依。</p></div>
        </div>
      </div>
    </section>

    <section class="section" style="background:#f7f9fc">
      <div class="container">
        <h2>服务网点</h2>
        <p class="sub">覆盖主要城市的营业网点与中转场</p>
        <table class="table">
          <thead><tr><th>城市</th><th>网点名称</th><th>类型</th><th>地址</th></tr></thead>
          <tbody>
            <tr v-for="s in sites" :key="s.id">
              <td>{{ s.city }}</td><td>{{ s.name }}</td><td>{{ s.siteType }}</td><td>{{ s.address }}</td>
            </tr>
            <tr v-if="!sites.length"><td colspan="4" style="text-align:center;color:#909399">加载中…</td></tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get } from '../api/http'
const sites = ref([])
onMounted(async () => {
  try { const r = await get('/public/sites'); sites.value = r.data } catch (e) { sites.value = [] }
})
</script>
