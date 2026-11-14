<template>
  <div class="page">
    <div class="page-title-row">
      <div>
        <div class="page-title">账单收款</div>
        <div class="page-subtitle">套餐续费、席位增购与短信包账单统一管理，欠费自动转态</div>
      </div>
      <el-button @click="exportBills">
        <el-icon><Download /></el-icon> 导出账单
      </el-button>
    </div>

    <div class="bill-kpi">
      <div class="bill-kpi-item">
        <span>账单总额</span>
        <strong>{{ formatMoney(summary.total) }}</strong>
      </div>
      <div class="bill-kpi-item">
        <span>已收金额</span>
        <strong class="success-text">{{ formatMoney(summary.paid) }}</strong>
      </div>
      <div class="bill-kpi-item">
        <span>待收金额</span>
        <strong class="warn-text">{{ formatMoney(summary.unpaid) }}</strong>
      </div>
      <div class="bill-kpi-item">
        <span>逾期金额</span>
        <strong class="danger-text">{{ formatMoney(summary.overdue) }}</strong>
      </div>
      <div class="bill-kpi-item">
        <span>结算率</span>
        <strong>{{ summary.total ? ((summary.paid / summary.total) * 100).toFixed(1) : '0.0' }}%</strong>
      </div>
    </div>

    <PageTable
      :data="list"
      :loading="loading"
      :total="total"
      :page="page"
      :size="size"
      :columns="columns"
      title="账单列表"
      empty-text="暂无账单记录"
      action-width="110"
      @search="search"
      @reset="resetFilters"
      @page-change="onPageChange"
      @size-change="onSizeChange"
    >
      <template #filter>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="账单号 / 租户名称" clearable @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="账单状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable>
            <el-option v-for="s in ['待支付', '部分收款', '已结清', '已逾期']" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="账单类型">
          <el-select v-model="query.type" placeholder="全部类型" clearable>
            <el-option v-for="t in ['套餐续费', '席位增购', '短信包']" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
      </template>

      <template #billNo="{ row }">
        <span class="mono">{{ row.billNo }}</span>
        <div class="muted">{{ row.type }}</div>
      </template>
      <template #tenantName="{ row }">
        <span class="strong">{{ row.tenantName }}</span>
      </template>
      <template #amount="{ row }">
        <MoneyText :value="row.amount" strong />
      </template>
      <template #paidAmount="{ row }">
        <MoneyText :value="row.paidAmount" :colored="true" />
      </template>
      <template #status="{ row }">
        <StatusTag :status="row.status" />
      </template>
      <template #dueDate="{ row }">
        <span :class="row.status !== '已结清' && isOverdue(row.dueDate) ? 'danger-text' : ''">{{ row.dueDate }}</span>
      </template>
      <template #actions="{ row }">
        <el-button v-if="row.status !== '已结清'" link type="primary" @click="openPay(row)">收款登记</el-button>
        <el-button v-else link @click="openPay(row)">查看</el-button>
      </template>
    </PageTable>

    <el-dialog v-model="payVisible" title="收款登记" width="500px">
      <div v-if="payRow" class="pay-summary">
        <div class="pay-head">
          <span class="strong">{{ payRow.tenantName }}</span>
          <StatusTag :status="payRow.status" />
        </div>
        <div class="pay-grid">
          <div><span>账单号</span><strong class="mono">{{ payRow.billNo }}</strong></div>
          <div><span>账单金额</span><strong>{{ formatMoney(payRow.amount) }}</strong></div>
          <div><span>已收金额</span><strong class="success-text">{{ formatMoney(payRow.paidAmount) }}</strong></div>
          <div><span>待收金额</span><strong class="warn-text">{{ formatMoney(outstanding) }}</strong></div>
        </div>
      </div>

      <el-form ref="payFormRef" :model="payForm" :rules="payRules" label-width="90px" :disabled="payRow?.status === '已结清'">
        <el-form-item label="收款金额" prop="amount">
          <el-input-number v-model="payForm.amount" :min="0.01" :max="Math.max(0.01, outstanding)" :precision="2" class="w-full" />
        </el-form-item>
        <el-form-item label="收款方式" prop="method">
          <el-select v-model="payForm.method" class="w-full">
            <el-option v-for="m in DICTS['支付方式']" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="收款备注">
          <el-input v-model="payForm.remark" type="textarea" :rows="2" placeholder="如：银行流水号、差额说明" />
        </el-form-item>
      </el-form>
      <el-alert v-if="payRow?.status === '已结清'" type="success" :closable="false" show-icon title="该账单已结清，以下为收款记录。" />
      <template #footer>
        <el-button @click="payVisible = false">关闭</el-button>
        <el-button v-if="payRow?.status !== '已结清'" type="primary" :loading="payLoading" @click="submitPay">确认收款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import PageTable, { type TableColumn } from '@/components/PageTable.vue'
import StatusTag from '@/components/StatusTag.vue'
import MoneyText from '@/components/MoneyText.vue'
import { useTable } from '@/composables/useTable'
import { getBills, payBill } from '@/api/platform'
import { DICTS } from '@/utils/dict'
import { downloadText, formatMoney, toCsv } from '@/utils/format'

const { list, loading, total, page, size, query, search, reset, onPageChange, onSizeChange, load } = useTable(getBills)

const columns: TableColumn[] = [
  { prop: 'billNo', label: '账单', minWidth: 160, slot: 'billNo' },
  { prop: 'tenantName', label: '租户', minWidth: 190, slot: 'tenantName' },
  { prop: 'amount', label: '金额', width: 115, align: 'right', slot: 'amount' },
  { prop: 'paidAmount', label: '已收', width: 110, align: 'right', slot: 'paidAmount' },
  { prop: 'status', label: '状态', width: 92, slot: 'status' },
  { prop: 'dueDate', label: '到期日', width: 110, slot: 'dueDate' },
  { prop: 'createdAt', label: '生成日期', width: 108 }
]

const summary = computed(() => {
  const s = { total: 0, paid: 0, unpaid: 0, overdue: 0 }
  list.value.forEach((r: any) => {
    s.total += Number(r.amount || 0)
    s.paid += Number(r.paidAmount || 0)
    s.unpaid += Math.max(0, Number(r.amount || 0) - Number(r.paidAmount || 0))
    if (r.status === '已逾期') s.overdue += Number(r.amount || 0) - Number(r.paidAmount || 0)
  })
  return s
})

function isOverdue(date: string): boolean {
  return date ? new Date(date).getTime() < Date.now() : false
}

function resetFilters(): void {
  reset({ keyword: '', status: '', type: '' })
}

const payVisible = ref(false)
const payLoading = ref(false)
const payRow = ref<any>(null)
const payFormRef = ref<FormInstance>()
const payForm = reactive({ amount: 0, method: '银行转账', remark: '' })

const outstanding = computed(() => {
  if (!payRow.value) return 0
  return Math.max(0, Number(payRow.value.amount || 0) - Number(payRow.value.paidAmount || 0))
})

const payRules: FormRules = {
  amount: [{ required: true, message: '请输入收款金额', trigger: 'change' }],
  method: [{ required: true, message: '请选择收款方式', trigger: 'change' }]
}

function openPay(row: any): void {
  payRow.value = row
  payForm.amount = Math.max(0.01, Number(row.amount || 0) - Number(row.paidAmount || 0))
  payForm.method = '银行转账'
  payForm.remark = ''
  payVisible.value = true
}

async function submitPay(): Promise<void> {
  if (!payFormRef.value || !payRow.value) return
  const valid = await payFormRef.value.validate().catch(() => false)
  if (!valid) return
  payLoading.value = true
  try {
    await payBill(payRow.value.id, { ...payForm })
    ElMessage.success('收款登记成功')
    payVisible.value = false
    load()
  } finally {
    payLoading.value = false
  }
}

function exportBills(): void {
  const headers = ['账单号', '租户', '类型', '金额', '已收', '状态', '到期日', '生成日期']
  const rows = list.value.map((r: any) => [r.billNo, r.tenantName, r.type, r.amount, r.paidAmount, r.status, r.dueDate, r.createdAt])
  downloadText('平台账单.csv', toCsv(headers, rows), 'text/csv;charset=utf-8')
  ElMessage.success('导出成功')
}
</script>

<style scoped lang="scss">
.bill-kpi {
  display: flex;
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: var(--radius);
  box-shadow: var(--shadow-card);
  padding: 12px 16px;
  margin-bottom: 10px;
}
.bill-kpi-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding: 0 16px;
  border-right: 1px solid #f0f3f8;
  &:last-child {
    border-right: none;
  }
  span {
    font-size: 11px;
    color: var(--text-muted);
  }
  strong {
    font-size: 18px;
    font-family: 'DIN Alternate', Consolas, sans-serif;
  }
}
.pay-summary {
  background: #f7f9fc;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 16px;
}
.pay-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.pay-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  div {
    display: flex;
    flex-direction: column;
    gap: 2px;
    span {
      font-size: 10.5px;
      color: var(--text-muted);
    }
    strong {
      font-size: 12.5px;
    }
  }
}
</style>
