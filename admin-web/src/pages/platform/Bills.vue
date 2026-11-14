<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">账单与收款</div>
        <div class="page-sub">租户账单、收款核销、欠费治理</div>
      </div>
      <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>&nbsp;生成账单</el-button>
    </div>
    <div class="card">
      <div class="toolbar">
        <el-select v-model="status" placeholder="状态" clearable @change="load">
          <el-option v-for="s in ['待支付', '部分支付', '已结清', '逾期']" :key="s" :label="s" :value="s" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="billNo" label="账单号" width="160" />
        <el-table-column prop="tenantId" label="租户ID" width="90" />
        <el-table-column prop="billType" label="类型" width="100" />
        <el-table-column prop="amount" label="金额(元)" width="110" />
        <el-table-column prop="paidAmount" label="已收(元)" width="110" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="tag(row.status)" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="dueDate" label="到期日" width="170" />
        <el-table-column prop="paidAt" label="收款时间" width="170" />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === '待支付' || row.status === '部分支付'" size="small" type="success" @click="pay(row)">收款</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 14px; justify-content: flex-end" layout="total, prev, pager, next"
        :total="total" :page-size="size" :current-page="page" @current-change="(p) => { page = p; load() }" />
    </div>

    <el-dialog v-model="dialog" title="生成账单" width="520px">
      <el-form label-width="120px">
        <el-form-item label="租户ID" required><el-input-number v-model="form.tenantId" :min="1" /></el-form-item>
        <el-form-item label="账单类型"><el-select v-model="form.billType" style="width:100%">
          <el-option v-for="t in ['订阅年费', '席位增购', '短信费', '增值服务']" :key="t" :label="t" :value="t" />
        </el-select></el-form-item>
        <el-form-item label="金额(元)" required><el-input-number v-model="form.amount" :min="0" /></el-form-item>
        <el-form-item label="到期日"><el-date-picker v-model="form.dueDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="create">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post } from '../../api/http'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const status = ref('')
const dialog = ref(false)
const form = reactive({ tenantId: 1, billType: '订阅年费', amount: 0, dueDate: '' })
function tag(s) { return { 待支付: 'warning', 部分支付: 'primary', 已结清: 'success', 逾期: 'danger' }[s] || 'info' }

async function load() {
  loading.value = true
  try { const r = await get('/platform/bills', { status, page: page.value, size: size.value }); rows.value = r.data.list; total.value = r.data.total }
  finally { loading.value = false }
}
function openCreate() { Object.assign(form, { tenantId: 1, billType: '订阅年费', amount: 0, dueDate: '' }); dialog.value = true }
async function create() { await post('/platform/bills', { ...form }); ElMessage.success('已生成'); dialog.value = false; load() }
async function pay(row) {
  const { value } = await ElMessageBox.prompt(`请输入实收金额（应收 ${row.amount} 元）`, '收款登记', { confirmButtonText: '确认', cancelButtonText: '取消', inputValue: String(row.amount - (row.paidAmount || 0)) })
  await post(`/platform/bills/${row.id}/pay`, { amount: value }); ElMessage.success('收款成功'); load()
}
onMounted(load)
</script>
