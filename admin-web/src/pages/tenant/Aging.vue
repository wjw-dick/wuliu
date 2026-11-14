<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">账龄催收</div>
        <div class="page-sub">应收账龄分析、逾期催收、代收货款管理</div>
      </div>
    </div>

    <div class="stat-grid mb" v-if="aging.summary">
      <div class="stat-card"><div class="label">应收总额</div><div class="value">{{ aging.summary.total || 0 }}</div></div>
      <div class="stat-card" style="border-left-color:#e6a23c"><div class="label">逾期金额</div><div class="value">{{ aging.summary.overdue || 0 }}</div></div>
      <div class="stat-card" style="border-left-color:#f56c6c"><div class="label">30天以上</div><div class="value">{{ aging.summary.over30 || 0 }}</div></div>
    </div>

    <div class="card">
      <div class="page-title" style="font-size:15px;margin-bottom:12px;">账龄分布</div>
      <el-table :data="aging.buckets || []" v-loading="loading" border>
        <el-table-column prop="bucket" label="账龄区间" min-width="180" />
        <el-table-column prop="amount" label="金额(元)" width="150" />
        <el-table-column prop="count" label="笔数" width="100" />
        <el-table-column label="占比" min-width="200">
          <template #default="{ row }">
            <el-progress :percentage="pct(row.amount)" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card" style="margin-top:16px">
      <div class="page-title" style="font-size:15px;margin-bottom:12px;">逾期应收明细</div>
      <el-table :data="aging.overdueItems || []" border stripe>
        <el-table-column prop="waybillNo" label="运单号" width="160" />
        <el-table-column prop="customerName" label="客户" min-width="160" />
        <el-table-column prop="amount" label="金额(元)" width="110" />
        <el-table-column prop="dueDate" label="到期日" width="150" />
        <el-table-column prop="overdueDays" label="逾期天数" width="100" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { get } from '../../api/http'

const aging = reactive({})
const loading = ref(false)
function pct(amount) {
  const total = aging.summary?.total || 0
  return total ? Math.round((amount / total) * 100) : 0
}
async function load() {
  loading.value = true
  try { const r = await get('/tenant/aging'); Object.assign(aging, r.data) }
  finally { loading.value = false }
}
onMounted(load)
</script>
