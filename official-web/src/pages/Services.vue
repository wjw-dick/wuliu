<template>
  <div class="container section">
    <h2 style="margin-top:0">服务网络</h2>
    <p class="sub">覆盖主要城市的营业网点与中转场</p>
    <table class="table">
      <thead><tr><th>城市</th><th>网点名称</th><th>类型</th><th>地址</th><th>电话</th></tr></thead>
      <tbody>
        <tr v-for="s in sites" :key="s.id">
          <td>{{ s.city }}</td><td>{{ s.name }}</td><td>{{ s.siteType }}</td><td>{{ s.address }}</td><td>{{ s.phone }}</td>
        </tr>
        <tr v-if="!sites.length"><td colspan="5" style="text-align:center;color:#909399">加载中…</td></tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get } from '../api/http'
const sites = ref([])
onMounted(async () => { try { const r = await get('/public/sites'); sites.value = r.data } catch (e) {} })
</script>
