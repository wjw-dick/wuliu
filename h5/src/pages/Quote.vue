<template>
  <div style="padding-bottom:70px">
    <div class="topbar"><span class="back" @click="$router.back()">←</span>在线询价</div>
    <div class="pad">
      <div class="card">
        <div class="row2">
          <div><label>出发地 *</label><input v-model="form.origin" placeholder="如：上海" /></div>
          <div><label>目的地 *</label><input v-model="form.destination" placeholder="如：北京" /></div>
        </div>
        <label>货物名称</label><input v-model="form.cargoType" placeholder="普货" />
        <label>车型</label>
        <select v-model="form.vehicleType">
          <option v-for="t in ['4.2米', '6.8米', '9.6米', '13米', '17.5米']" :key="t">{{ t }}</option>
        </select>
        <div class="row2">
          <div><label>重量(kg)</label><input v-model.number="form.weight" type="number" /></div>
          <div><label>体积(m³)</label><input v-model.number="form.volume" type="number" /></div>
        </div>
        <label>件数</label><input v-model.number="form.pieces" type="number" />
        <button class="btn" :disabled="loading" @click="submit">{{ loading ? '计算中…' : '获取报价' }}</button>
        <div v-if="msg" class="msg" :class="ok ? 'ok' : 'err'">{{ msg }}</div>
      </div>

      <div v-if="result" class="card">
        <h3>参考报价</h3>
        <p><span class="big">{{ result.total }}</span> 元</p>
        <p style="color:#909399;font-size:13px">运费 {{ result.freight }} + 附加 {{ result.surcharge }} · 计费 {{ result.chargeWeight }}kg</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { post } from '../api/http'
const form = reactive({ origin: '上海', destination: '北京', cargoType: '普货', vehicleType: '9.6米', weight: 500, volume: 3, pieces: 10 })
const result = ref(null)
const loading = ref(false)
const msg = ref('')
const ok = ref(false)
async function submit() {
  if (!form.origin || !form.destination) { msg.value = '请填写出发地和目的地'; ok.value = false; return }
  loading.value = true; msg.value = ''
  try { result.value = (await post('/public/quote', { ...form })).data; ok.value = true }
  catch (e) { msg.value = e.message; ok.value = false }
  finally { loading.value = false }
}
</script>
