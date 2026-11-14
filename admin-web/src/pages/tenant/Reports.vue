<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">报表中心</div>
        <div class="page-sub">运单、营收、利润、时效、异常等多维度报表与导出</div>
      </div>
    </div>

    <div class="chart-row mb">
      <div class="card" v-for="t in types" :key="t">
        <div class="page-title" style="font-size:15px;margin-bottom:12px;">{{ t }}</div>
        <el-table :data="reportData[t] || []" v-loading="loading" border height="300">
          <el-table-column v-for="col in columnsOf(t)" :key="col" :prop="col" :label="col" min-width="110" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { get } from '../../api/http'

const types = ['dailyWaybills', 'revenueByRoute', 'profitByVehicle', 'timeliness', 'exceptionByType']
const reportData = reactive({})
const loading = ref(false)
function columnsOf(t) {
  const rows = reportData[t] || []
  return rows.length ? Object.keys(rows[0]).filter((k) => k !== 'id') : ['指标', '数值']
}
async function load() {
  loading.value = true
  try {
    for (const t of types) {
      const r = await get(`/tenant/reports/${t}`)
      reportData[t] = r.data.list || r.data.items || Object.entries(r.data).map(([k, v]) => ({ 指标: k, 数值: v }))
    }
  } finally { loading.value = false }
}
onMounted(load)
</script>
