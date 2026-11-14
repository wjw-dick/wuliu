<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">对账单</div>
        <div class="page-sub">对账单生成、客户确认、异议、开票、收款</div>
      </div>
      <el-button type="primary" @click="openGenerate"><el-icon><Plus /></el-icon>&nbsp;生成对账单</el-button>
    </div>

    <div class="card">
      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="statementNo" label="对账单号" width="170">
          <template #default="{ row }"><el-link type="primary" @click="$router.push(`/tenant/statement/${row.id}`)">{{ row.statementNo }}</el-link></template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="partyName" label="客户/承运商" min-width="180" />
        <el-table-column prop="periodStart" label="起始" width="150" />
        <el-table-column prop="periodEnd" label="截止" width="150" />
        <el-table-column prop="amount" label="金额(元)" width="110" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }"><el-tag :type="tag(row.status)" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/tenant/statement/${row.id}`)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="genDialog" title="生成对账单" width="560px">
      <el-form label-width="110px">
        <el-form-item label="类型"><el-select v-model="genForm.type" style="width:100%">
          <el-option v-for="t in ['客户', '承运商']" :key="t" :label="t" :value="t" />
        </el-select></el-form-item>
        <el-form-item label="客户/承运商ID"><el-input-number v-model="genForm.partyId" :min="1" /></el-form-item>
        <el-form-item label="期间"><el-date-picker v-model="genRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="genDialog = false">取消</el-button>
        <el-button type="primary" @click="generate">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { get, post } from '../../api/http'

const rows = ref([])
const loading = ref(false)
const genDialog = ref(false)
const genForm = reactive({ type: '客户', partyId: 1 })
const genRange = ref(null)
function tag(s) { return { 待客户确认: 'warning', 已确认: 'success', 有异议: 'danger', 已对账: 'success' }[s] || 'info' }

async function load() {
  loading.value = true
  try { const r = await get('/tenant/statements'); rows.value = r.data.list }
  finally { loading.value = false }
}
function openGenerate() { Object.assign(genForm, { type: '客户', partyId: 1 }); genRange.value = null; genDialog.value = true }
async function generate() {
  await post('/tenant/statements', { type: genForm.type, partyId: genForm.partyId, periodStart: genRange.value?.[0], periodEnd: genRange.value?.[1] })
  ElMessage.success('已生成'); genDialog.value = false; load()
}
onMounted(load)
</script>
