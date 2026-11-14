<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">理赔售后</div>
        <div class="page-sub">理赔登记、责任认定、赔付审批、投诉管理</div>
      </div>
      <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>&nbsp;登记理赔</el-button>
    </div>

    <div class="card mb">
      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="claimNo" label="理赔单号" width="150" />
        <el-table-column prop="waybillNo" label="运单号" width="150" />
        <el-table-column prop="recordType" label="类型" width="100" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="amount" label="赔付(元)" width="100" />
        <el-table-column prop="liability" label="责任" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="tag(row.status)" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === '待责任认定'" size="small" @click="liability(row)">责任</el-button>
            <el-button v-if="row.status === '待审批'" size="small" type="primary" @click="approve(row)">审批</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
      <div class="page-title" style="font-size:15px;margin-bottom:12px;">投诉记录</div>
      <el-table :data="complaints" border stripe>
        <el-table-column prop="waybillNo" label="运单号" width="150" />
        <el-table-column prop="content" label="投诉内容" min-width="220" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="createdAt" label="时间" width="170" />
      </el-table>
    </div>

    <el-dialog v-model="dialog" title="登记理赔" width="520px">
      <el-form label-width="110px">
        <el-form-item label="运单ID" required><el-input-number v-model="form.waybillId" :min="1" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.recordType" style="width:100%">
          <el-option v-for="s in ['破损', '货物丢失', '货损', '时效赔偿']" :key="s" :label="s" :value="s" />
        </el-select></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="create">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post } from '../../api/http'

const rows = ref([])
const complaints = ref([])
const loading = ref(false)
const dialog = ref(false)
const form = reactive({})
function tag(s) { return { 待责任认定: 'warning', 待审批: 'primary', 已赔付: 'success', 已关闭: 'info' }[s] || 'info' }

async function load() {
  loading.value = true
  try {
    const r = await get('/tenant/claims'); rows.value = r.data.list
    const c = await get('/tenant/complaints'); complaints.value = c.data
  } finally { loading.value = false }
}
function openCreate() { Object.assign(form, { waybillId: 1, recordType: '破损', description: '' }); dialog.value = true }
async function create() { await post('/tenant/claims', { ...form }); ElMessage.success('已登记'); dialog.value = false; load() }
async function liability(row) {
  const { value } = await ElMessageBox.prompt('责任认定（物流/客户/第三方/待定）', '责任认定', { confirmButtonText: '确认', cancelButtonText: '取消', inputValue: '物流' })
  await post(`/tenant/claims/${row.id}/liability`, { liability: value }); ElMessage.success('已认定'); load()
}
async function approve(row) {
  const { value } = await ElMessageBox.prompt(`赔付金额（元）`, '赔付审批', { confirmButtonText: '确认', cancelButtonText: '取消', inputValue: String(row.amount || 0) })
  await post(`/tenant/claims/${row.id}/approve`, { pass: true, amount: value }); ElMessage.success('已审批'); load()
}
onMounted(load)
</script>
