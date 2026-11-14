<template>
  <div style="padding-bottom:70px">
    <div class="topbar">示例物流</div>
    <div class="pad">
      <div class="card">
        <h3>快捷询价</h3>
        <label>出发地</label><input v-model="q.origin" placeholder="如：上海" />
        <label>目的地</label><input v-model="q.destination" placeholder="如：北京" />
        <label>重量(kg)</label><input v-model.number="q.weight" type="number" />
        <button class="btn" @click="quickQuote">立即询价</button>
        <div v-if="quote" class="msg ok">参考价：{{ quote.total }} 元（{{ quote.chargeWeight }}kg）</div>
      </div>

      <div class="card">
        <h3>运单跟踪</h3>
        <label>运单号</label><input v-model="waybillNo" placeholder="输入运单号" />
        <button class="btn ghost" @click="$router.push(`/track?no=${waybillNo}`)">查询轨迹</button>
      </div>
    </div>

    <nav class="bottom-nav">
      <router-link to="/" exact-active-class="active"><span class="ic">🏠</span>首页</router-link>
      <router-link to="/quote" active-class="active"><span class="ic">💰</span>询价</router-link>
      <router-link to="/track" active-class="active"><span class="ic">📦</span>跟踪</router-link>
    </nav>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { post } from '../api/http'
const q = reactive({ origin: '', destination: '', weight: 500 })
const quote = ref(null)
const waybillNo = ref('')
async function quickQuote() {
  try { const r = await post('/public/quote', { ...q, volume: 2, pieces: 5, vehicleType: '9.6米', cargoType: '普货' }); quote.value = r.data }
  catch (e) { quote.value = null }
}
</script>
