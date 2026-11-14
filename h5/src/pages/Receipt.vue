<template>
  <div style="padding-bottom:20px">
    <div class="topbar"><span class="back" @click="$router.back()">←</span>电子回单</div>
    <div class="pad">
      <div class="card" v-if="receipt">
        <h3>签收凭证 · {{ receipt.waybillNo }}</h3>
        <p>签收人：{{ receipt.signerName }}</p>
        <p>签收时间：{{ receipt.signedAt }}</p>
        <p>运单状态：{{ receipt.status }}</p>
        <img v-if="receipt.signImage" :src="receipt.signImage" style="width:100%;border-radius:8px" />
        <p style="color:#909399;font-size:13px">本回单已由收货人电子签收确认</p>
      </div>
      <div class="card" v-else>
        <p style="color:#909399">加载中…</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { get } from '../api/http'
const route = useRoute()
const receipt = ref(null)
onMounted(async () => {
  try { receipt.value = (await get(`/h5/receipt/${route.params.waybillNo}`)).data }
  catch (e) {}
})
</script>
