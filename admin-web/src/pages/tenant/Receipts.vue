<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">签收与回单</div>
        <div class="page-sub">电子签收凭证审核、回单上传与退回任务</div>
      </div>
    </div>

    <div class="chart-row mb">
      <div class="card">
        <div class="page-title" style="font-size:15px;margin-bottom:12px;">签收记录</div>
        <div class="toolbar">
          <el-select v-model="status" placeholder="状态" clearable @change="loadReceipts">
            <el-option v-for="s in ['待审核', '已通过', '有异议']" :key="s" :label="s" :value="s" />
          </el-select>
        </div>
        <el-table :data="receipts" v-loading="loading" border height="340">
          <el-table-column prop="waybillNo" label="运单号" width="150" />
          <el-table-column prop="signerName" label="签收人" width="100" />
          <el-table-column prop="signedAt" label="签收时间" width="160" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }"><el-tag :type="tag(row.status)" size="small">{{ row.status }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="110">
            <template #default="{ row }">
              <el-button v-if="row.status === '待审核'" size="small" type="primary" @click="review(row)">审核</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="card">
        <div class="page-title" style="font-size:15px;margin-bottom:12px;">退回任务</div>
        <el-table :data="returns" v-loading="loading" border height="340">
          <el-table-column prop="waybillNo" label="运单号" width="150" />
          <el-table-column prop="reason" label="退回原因" min-width="200" />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column label="操作" width="110">
            <template #default="{ row }">
              <el-button v-if="row.status !== '已完成'" size="small" type="success" @click="complete(row)">完成</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post } from '../../api/http'

const receipts = ref([])
const returns = ref([])
const loading = ref(false)
const status = ref('')
function tag(s) { return { 待审核: 'warning', 已通过: 'success', 有异议: 'danger' }[s] || 'info' }

async function loadReceipts() {
  loading.value = true
  try { const r = await get('/tenant/receipts', { status }); receipts.value = r.data.list }
  finally { loading.value = false }
}
async function loadReturns() {
  const r = await get('/tenant/returns')
  returns.value = r.data.list
}
async function review(row) {
  const { value } = await ElMessageBox.prompt('审核意见（通过/驳回）', '回单审核', { confirmButtonText: '确认', cancelButtonText: '取消', inputValue: '通过' })
  await post(`/tenant/receipts/${row.id}/review`, { pass: value === '通过' })
  ElMessage.success('已审核'); loadReceipts()
}
async function complete(row) { await post(`/tenant/returns/${row.id}/complete`); ElMessage.success('已完成'); loadReturns() }
onMounted(() => { loadReceipts(); loadReturns() })
</script>
