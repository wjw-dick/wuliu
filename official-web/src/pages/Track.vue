<template>
  <div class="container section">
    <h2 style="margin-top:0">运单跟踪</h2>
    <p class="sub">输入运单号或手机号查询货物轨迹</p>
    <div class="toolbar">
      <input v-model="waybillNo" placeholder="运单号" style="width:260px" />
      <input v-model="phone" placeholder="收货人手机号（可选）" style="width:220px" />
      <button class="btn-primary" :disabled="loading" @click="track">查询</button>
    </div>
    <div v-if="msg" class="msg" :class="ok ? 'ok' : 'err'">{{ msg }}</div>

    <div v-if="result" class="result-box">
      <h4>{{ result.waybillNo }} · {{ result.status }}</h4>
      <p>{{ result.origin }} → {{ result.destination }}</p>
      <p v-if="result.cargoName">货物：{{ result.cargoName }}（{{ result.weight }}kg）</p>
      <table class="table mt">
        <thead><tr><th>时间</th><th>节点</th><th>位置</th></tr></thead>
        <tbody>
          <tr v-for="n in result.nodes || []" :key="n.time + n.node">
            <td>{{ n.time }}</td><td>{{ n.node }}</td><td>{{ n.address }}</td>
          </tr>
          <tr v-if="!(result.nodes || []).length"><td colspan="3" style="text-align:center;color:#909399">暂无轨迹</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { get } from '../api/http'
const waybillNo = ref('')
const phone = ref('')
const result = ref(null)
const loading = ref(false)
const msg = ref('')
const ok = ref(false)

async function track() {
  if (!waybillNo.value) { msg.value = '请输入运单号'; ok.value = false; return }
  loading.value = true; msg.value = ''
  try {
    const r = await get('/public/track', { waybillNo: waybillNo.value, phone: phone.value || undefined })
    result.value = r.data; ok.value = true
  } catch (e) { msg.value = e.message; ok.value = false }
  finally { loading.value = false }
}
</script>
