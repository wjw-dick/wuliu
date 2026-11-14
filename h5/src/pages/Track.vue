<template>
  <div style="padding-bottom:70px">
    <div class="topbar"><span class="back" @click="$router.back()">←</span>运单跟踪</div>
    <div class="pad">
      <div class="card">
        <label>运单号</label><input v-model="no" placeholder="输入运单号" />
        <label>手机号（可选）</label><input v-model="phone" placeholder="收货人手机号" />
        <button class="btn" :disabled="loading" @click="track">查询</button>
        <div v-if="msg" class="msg" :class="ok ? 'ok' : 'err'">{{ msg }}</div>
      </div>

      <div v-if="result" class="card">
        <h3>{{ result.waybillNo }} · {{ result.status }}</h3>
        <p style="color:#606266">{{ result.origin }} → {{ result.destination }}</p>
        <div class="tl">
          <div class="tl-item" v-for="n in result.nodes || []" :key="n.time + n.node">
            <div class="t">{{ n.time }}</div>
            <div class="n">{{ n.node }}</div>
            <div class="t">{{ n.address }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { get } from '../api/http'
const route = useRoute()
const no = ref(route.query.no || '')
const phone = ref('')
const result = ref(null)
const loading = ref(false)
const msg = ref('')
const ok = ref(false)
async function track() {
  if (!no.value) { msg.value = '请输入运单号'; ok.value = false; return }
  loading.value = true; msg.value = ''
  try { result.value = (await get('/public/track', { waybillNo: no.value, phone: phone.value || undefined })).data; ok.value = true }
  catch (e) { msg.value = e.message; ok.value = false }
  finally { loading.value = false }
}
onMounted(() => { if (no.value) track() })
</script>
