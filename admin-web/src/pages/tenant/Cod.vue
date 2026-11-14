<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">代收货款</div>
        <div class="page-sub">代收登记、回款、渠道对账、未结清预警</div>
      </div>
      <el-button @click="load"><el-icon><Refresh /></el-icon>&nbsp;刷新</el-button>
    </div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="waybillNo" label="运单号" width="160" />
        <el-table-column prop="amount" label="代收金额(元)" width="140" />
        <el-table-column prop="channel" label="渠道" width="140" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }"><el-tag :type="tag(row.status)" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="channelNo" label="渠道流水号" min-width="180" />
        <el-table-column prop="settledAt" label="结清时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status !== '已结清'" size="small" type="success" @click="settle(row)">结清</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post } from '../../api/http'

const rows = ref([])
const loading = ref(false)
function tag(s) { return { 待结清: 'warning', 已结清: 'success', 逾期: 'danger' }[s] || 'info' }

async function load() {
  loading.value = true
  try { const r = await get('/tenant/cod'); rows.value = r.data.list }
  finally { loading.value = false }
}
async function settle(row) {
  const { value } = await ElMessageBox.prompt('请输入渠道流水号', '结清代收', { confirmButtonText: '确认', cancelButtonText: '取消' })
  await post(`/tenant/cod/${row.id}/settle`, { channelNo: value })
  ElMessage.success('已结清'); load()
}
onMounted(load)
</script>
