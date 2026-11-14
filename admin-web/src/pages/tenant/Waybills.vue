<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">运单管理</div>
        <div class="page-sub">运单全生命周期：受理 → 调度 → 提货 → 运输 → 签收 → 结算</div>
      </div>
      <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>&nbsp;新建运单</el-button>
    </div>

    <div class="card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="运单号/客户/车牌" clearable @keyup.enter="load" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width:140px">
          <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
        </el-select>
        <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束" />
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="waybillNo" label="运单号" width="160">
          <template #default="{ row }"><el-link type="primary" @click="$router.push(`/tenant/waybill/${row.id}`)">{{ row.waybillNo }}</el-link></template>
        </el-table-column>
        <el-table-column prop="origin" label="出发地" min-width="110" />
        <el-table-column prop="destination" label="目的地" min-width="110" />
        <el-table-column prop="customerName" label="客户" min-width="130" />
        <el-table-column prop="cargoName" label="货物" width="110" />
        <el-table-column prop="weight" label="重量(kg)" width="90" />
        <el-table-column prop="totalAmount" label="金额(元)" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="tag(row.status)" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openAction(row)">操作</el-button>
            <el-button size="small" type="primary" @click="$router.push(`/tenant/waybill/${row.id}`)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 14px; justify-content: flex-end" layout="total, prev, pager, next"
        :total="total" :page-size="size" :current-page="page" @current-change="(p) => { page = p; load() }" />
    </div>

    <el-dialog v-model="createDialog" title="新建运单" width="700px" top="4vh">
      <el-form :model="form" label-width="120px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="客户" required>
            <el-select v-model="form.customerId" filterable style="width:100%" @change="pickCustomer">
              <el-option v-for="c in customers" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="货物名称" required><el-input v-model="form.cargoName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="出发地" required><el-input v-model="form.origin" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="目的地" required><el-input v-model="form.destination" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="重量(kg)"><el-input-number v-model="form.weight" :min="0" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="体积(m³)"><el-input-number v-model="form.volume" :min="0" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="件数"><el-input-number v-model="form.pieces" :min="1" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="车型"><el-select v-model="form.vehicleType" style="width:100%">
            <el-option v-for="t in ['4.2米', '6.8米', '9.6米', '13米', '17.5米']" :key="t" :label="t" :value="t" />
          </el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="运输方式"><el-select v-model="form.transportMode" style="width:100%">
            <el-option v-for="t in ['零担', '整车', '快运']" :key="t" :label="t" :value="t" />
          </el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="提货方式"><el-select v-model="form.pickupType" style="width:100%">
            <el-option v-for="t in ['上门提货', '自送到站']" :key="t" :label="t" :value="t" />
          </el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="送货方式"><el-select v-model="form.deliveryType" style="width:100%">
            <el-option v-for="t in ['送货上门', '客户自提']" :key="t" :label="t" :value="t" />
          </el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="结算方式"><el-select v-model="form.settleType" style="width:100%">
            <el-option v-for="t in ['现结', '月结', '货到付款']" :key="t" :label="t" :value="t" />
          </el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="代收货款(元)"><el-input-number v-model="form.codAmount" :min="0" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="发货人"><el-input v-model="form.senderName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="发货人电话"><el-input v-model="form.senderPhone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="收货人"><el-input v-model="form.receiverName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="收货人电话"><el-input v-model="form.receiverPhone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="发货地址"><el-input v-model="form.senderAddress" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="收货地址"><el-input v-model="form.receiverAddress" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="createDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="create">创建并计费</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="actionDialog" title="运单操作" width="440px">
      <p>当前状态：<el-tag :type="tag(acting.status)" size="small">{{ acting.status }}</el-tag></p>
      <el-form label-width="100px">
        <el-form-item label="动作">
          <el-select v-model="action" style="width:100%">
            <el-option v-for="a in availableActions" :key="a" :label="a" :value="a" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="action === '取消' || action === '驳回'" label="原因"><el-input v-model="reason" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actionDialog = false">取消</el-button>
        <el-button type="primary" @click="doAction">执行</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post } from '../../api/http'

const statuses = ['待受理', '已受理', '待调度', '已派车', '待提货', '已提货', '运输中', '到达中转', '派送中', '已签收', '已异常', '已取消']
const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const saving = ref(false)
const query = reactive({})
const range = ref(null)
const customers = ref([])
const createDialog = ref(false)
const actionDialog = ref(false)
const acting = reactive({})
const action = ref('')
const reason = ref('')
const form = reactive({})

function tag(s) {
  return { 待受理: 'info', 已受理: 'primary', 待调度: 'primary', 已派车: 'warning', 待提货: 'warning', 已提货: 'warning', 运输中: 'primary', 到达中转: 'primary', 派送中: 'warning', 已签收: 'success', 已异常: 'danger', 已取消: 'info' }[s] || 'info'
}

async function load() {
  loading.value = true
  try {
    const params = { ...query, page: page.value, size: size.value }
    if (range.value?.[0]) params.startDate = range.value[0]
    if (range.value?.[1]) params.endDate = range.value[1]
    const r = await get('/tenant/waybills', params)
    rows.value = r.data.list; total.value = r.data.total
  } finally { loading.value = false }
}
function reset() { Object.keys(query).forEach((k) => delete query[k]); range.value = null; page.value = 1; load() }

async function openCreate() {
  if (!customers.value.length) {
    const r = await get('/tenant/customers', { page: 1, size: 100 })
    customers.value = r.data.list
  }
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, { cargoName: '普货', origin: '上海', destination: '北京', weight: 500, volume: 3, pieces: 10, vehicleType: '9.6米', transportMode: '零担', pickupType: '上门提货', deliveryType: '送货上门', settleType: '现结', codAmount: 0, senderName: '发货人', senderPhone: '13800000001', receiverName: '收货人', receiverPhone: '13700000001', senderAddress: '上海市示例路1号', receiverAddress: '北京市示例路2号' })
  createDialog.value = true
}
function pickCustomer(id) {
  const c = customers.value.find((x) => x.id === id)
  if (c) { form.senderName = c.contactName; form.senderPhone = c.contactPhone; form.senderAddress = c.address || form.senderAddress }
}
async function create() {
  if (!form.cargoName || !form.origin || !form.destination) return ElMessage.warning('请填写必填项')
  saving.value = true
  try {
    const r = await post('/tenant/waybills', { ...form, items: [], photos: [] })
    ElMessage.success('运单创建成功：' + (r.data.waybillNo || ''))
    createDialog.value = false; load()
  } finally { saving.value = false }
}

const availableActions = ['受理', '取消', '驳回']
function openAction(row) {
  Object.assign(acting, row)
  action.value = row.status === '待受理' ? '受理' : '取消'
  reason.value = ''
  actionDialog.value = true
}
async function doAction() {
  await post(`/tenant/waybills/${acting.id}/action`, { action: action.value, reason: reason.value })
  ElMessage.success('操作成功'); actionDialog.value = false; load()
}
onMounted(load)
</script>
