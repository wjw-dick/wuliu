<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">询价与报价</div>
        <div class="page-sub">计价试算、报价单管理、低于底价审批、转下单</div>
      </div>
      <el-button type="primary" @click="openSim"><el-icon><Calculator /></el-icon>&nbsp;计价试算</el-button>
    </div>

    <div class="card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="报价单号/出发地" clearable @keyup.enter="load" />
        <el-select v-model="query.status" placeholder="状态" clearable>
          <el-option v-for="s in ['待客户确认', '已确认', '已转化', '已失效']" :key="s" :label="s" :value="s" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="quoteNo" label="报价单号" width="150" />
        <el-table-column prop="origin" label="出发地" min-width="130" />
        <el-table-column prop="destination" label="目的地" min-width="130" />
        <el-table-column prop="cargoType" label="货物" width="110" />
        <el-table-column prop="weight" label="重量(kg)" width="100" />
        <el-table-column prop="volume" label="体积(m³)" width="100" />
        <el-table-column prop="totalPrice" label="总价(元)" width="100" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }"><el-tag :type="tag(row.status)" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === '待客户确认'" size="small" type="success" @click="convert(row)">转下单</el-button>
            <el-button v-if="row.approvalStatus === '待审批'" size="small" type="warning" @click="approve(row)">审批</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 14px; justify-content: flex-end" layout="total, prev, pager, next"
        :total="total" :page-size="size" :current-page="page" @current-change="(p) => { page = p; load() }" />
    </div>

    <el-dialog v-model="simDialog" title="计价试算" width="640px">
      <el-form :model="sim" label-width="110px">
        <el-form-item label="出发地" required><el-input v-model="sim.origin" /></el-form-item>
        <el-form-item label="目的地" required><el-input v-model="sim.destination" /></el-form-item>
        <el-form-item label="货物类型"><el-input v-model="sim.cargoType" /></el-form-item>
        <el-form-item label="重量(kg)"><el-input-number v-model="sim.weight" :min="0" /></el-form-item>
        <el-form-item label="体积(m³)"><el-input-number v-model="sim.volume" :min="0" /></el-form-item>
        <el-form-item label="件数"><el-input-number v-model="sim.pieces" :min="1" /></el-form-item>
        <el-form-item label="车型"><el-select v-model="sim.vehicleType" style="width:100%">
          <el-option v-for="t in ['4.2米', '6.8米', '9.6米', '13米', '17.5米']" :key="t" :label="t" :value="t" />
        </el-select></el-form-item>
        <el-form-item label="运输方式"><el-select v-model="sim.transportMode" style="width:100%">
          <el-option v-for="t in ['零担', '整车', '快运']" :key="t" :label="t" :value="t" />
        </el-select></el-form-item>
      </el-form>
      <el-divider>试算结果</el-divider>
      <el-descriptions v-if="result" :column="2" border>
        <el-descriptions-item label="计费重量">{{ result.chargeWeight }}kg</el-descriptions-item>
        <el-descriptions-item label="总费用">{{ result.total }} 元</el-descriptions-item>
        <el-descriptions-item label="运费">{{ result.freight }} 元</el-descriptions-item>
        <el-descriptions-item label="附加费">{{ result.surcharge }} 元</el-descriptions-item>
        <el-descriptions-item label="报价单号" :span="2">{{ result.quoteNo || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="simDialog = false">关闭</el-button>
        <el-button type="primary" @click="doSim">试算</el-button>
        <el-button v-if="result?.quoteNo" type="success" @click="saveQuote">保存为报价单</el-button>
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
const query = reactive({})
const simDialog = ref(false)
const result = ref(null)
const sim = reactive({ origin: '上海', destination: '北京', cargoType: '普货', weight: 500, volume: 3, pieces: 20, vehicleType: '9.6米', transportMode: '零担' })
function tag(s) { return { 待客户确认: 'warning', 已确认: 'primary', 已转化: 'success', 已失效: 'info' }[s] || 'info' }

async function load() {
  loading.value = true
  try { const r = await get('/tenant/quotes', { ...query, page: page.value, size: size.value }); rows.value = r.data.list; total.value = r.data.total }
  finally { loading.value = false }
}
function openSim() { result.value = null; simDialog.value = true }
async function doSim() {
  if (!sim.origin || !sim.destination) return ElMessage.warning('请填写出发地和目的地')
  const r = await post('/tenant/pricing/simulate', { ...sim, holiday: false })
  result.value = r.data
}
async function saveQuote() {
  await post('/tenant/quotes', {
    quoteNo: result.value.quoteNo, origin: sim.origin, destination: sim.destination,
    cargoType: sim.cargoType, weight: sim.weight, volume: sim.volume, pieces: sim.pieces,
    vehicleType: sim.vehicleType, totalPrice: result.value.total, status: '待客户确认'
  })
  ElMessage.success('已保存为报价单'); simDialog.value = false; load()
}
async function convert(row) {
  await ElMessageBox.confirm('确认将该报价单转为运单？', '转下单', { type: 'warning' })
  await post(`/tenant/quotes/${row.id}/convert`)
  ElMessage.success('已生成运单'); load()
}
async function approve(row) {
  const { value } = await ElMessageBox.prompt('审批意见（通过/驳回）', '报价审批', { confirmButtonText: '通过', cancelButtonText: '取消', inputValue: '通过' })
  await post(`/tenant/quotes/${row.id}/approve`, { pass: value === '通过' })
  ElMessage.success('已审批'); load()
}
onMounted(load)
</script>
