<template>
  <div class="container section">
    <h2 style="margin-top:0">在线询价</h2>
    <p class="sub">填写运输信息，即时获取参考报价</p>

    <div class="toolbar">
      <button class="btn-primary" @click="showGuide">计价规则说明</button>
    </div>

    <div class="form-row">
      <div><label>出发地 *</label><input v-model="form.origin" placeholder="如：上海" /></div>
      <div><label>目的地 *</label><input v-model="form.destination" placeholder="如：北京" /></div>
      <div><label>货物名称</label><input v-model="form.cargoType" placeholder="如：普货" /></div>
      <div><label>车型</label>
        <select v-model="form.vehicleType">
          <option v-for="t in ['4.2米', '6.8米', '9.6米', '13米', '17.5米']" :key="t">{{ t }}</option>
        </select>
      </div>
      <div><label>重量(kg)</label><input v-model.number="form.weight" type="number" min="0" /></div>
      <div><label>体积(m³)</label><input v-model.number="form.volume" type="number" min="0" /></div>
      <div><label>件数</label><input v-model.number="form.pieces" type="number" min="1" /></div>
    </div>
    <button class="btn-primary" :disabled="loading" @click="submit">
      {{ loading ? '计算中…' : '获取报价' }}
    </button>

    <div v-if="msg" class="msg" :class="ok ? 'ok' : 'err'">{{ msg }}</div>

    <div v-if="result" class="result-box">
      <h4>参考报价（{{ result.chargeWeight }}kg 计费）</h4>
      <p><strong style="font-size:24px;color:#1f6feb">{{ result.total }} 元</strong>
        <span style="color:#909399">（运费 {{ result.freight }} + 附加 {{ result.surcharge }}）</span>
      </p>
      <p>计费规则：{{ result.ruleDesc || '按重量体积取大' }}</p>
      <button class="btn-primary mt" @click="applyMonthly">企业月结申请</button>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { get, post } from '../api/http'
const form = reactive({ origin: '上海', destination: '北京', cargoType: '普货', vehicleType: '9.6米', weight: 500, volume: 3, pieces: 10 })
const result = ref(null)
const loading = ref(false)
const msg = ref('')
const ok = ref(false)

async function submit() {
  if (!form.origin || !form.destination) { msg.value = '请填写出发地和目的地'; ok.value = false; return }
  loading.value = true; msg.value = ''
  try {
    const r = await post('/public/quote', { ...form })
    result.value = r.data
    ok.value = true; msg.value = '报价成功'
  } catch (e) { msg.value = e.message; ok.value = false }
  finally { loading.value = false }
}
async function showGuide() {
  const r = await get('/public/price-guide')
  msg.value = `${r.data.mode}；首重 ${r.data.firstWeight}；续重 ${r.data.addWeight}；最低一票 ${r.data.minPrice}`
  ok.value = true
}
async function applyMonthly() {
  try { await post('/public/apply-monthly', { name: form.cargoType, phone: '13800000000', company: '示例企业' }); msg.value = '月结申请已提交，我们将在1个工作日内联系您'; ok.value = true }
  catch (e) { msg.value = e.message; ok.value = false }
}
</script>
