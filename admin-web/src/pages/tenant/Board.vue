<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">调度看板</div>
        <div class="page-sub">待调度运单池 + 派车/改派/取消 + 运力状态</div>
      </div>
      <el-button @click="load"><el-icon><Refresh /></el-icon>&nbsp;刷新</el-button>
    </div>

    <div class="chart-row mb">
      <div class="card">
        <div class="page-title" style="font-size:15px;margin-bottom:12px;">待调度运单</div>
        <el-table :data="pool" v-loading="loading" border height="360">
          <el-table-column prop="waybillNo" label="运单号" width="150" />
          <el-table-column prop="origin" label="出发" width="110" />
          <el-table-column prop="destination" label="到达" width="110" />
          <el-table-column prop="cargoName" label="货物" width="100" />
          <el-table-column prop="weight" label="kg" width="70" />
          <el-table-column prop="totalAmount" label="金额" width="90" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }"><el-button size="small" type="primary" @click="openAssign(row)">派车</el-button></template>
          </el-table-column>
        </el-table>
      </div>
      <div class="card">
        <div class="page-title" style="font-size:15px;margin-bottom:12px;">运力状态</div>
        <el-table :data="board.vehicles || []" v-loading="loading" border height="360">
          <el-table-column prop="plateNo" label="车牌" width="110" />
          <el-table-column prop="vehicleType" label="车型" width="90" />
          <el-table-column prop="status" label="状态" width="90" />
          <el-table-column prop="currentCity" label="当前位置" min-width="140" />
          <el-table-column prop="driverName" label="司机" width="90" />
        </el-table>
      </div>
    </div>

    <div class="card">
      <div class="page-title" style="font-size:15px;margin-bottom:12px;">今日派车计划</div>
      <el-table :data="board.todayDispatches || []" border stripe>
        <el-table-column prop="waybillNo" label="运单号" width="160" />
        <el-table-column prop="plateNo" label="车牌" width="120" />
        <el-table-column prop="driverName" label="司机" width="110" />
        <el-table-column prop="origin" label="出发" width="120" />
        <el-table-column prop="destination" label="到达" width="120" />
        <el-table-column prop="pickupTime" label="提货时间" width="170" />
        <el-table-column prop="status" label="状态" width="110" />
      </el-table>
    </div>

    <el-dialog v-model="assignDialog" title="派车调度" width="520px">
      <el-form label-width="100px">
        <el-form-item label="车辆"><el-select v-model="assignForm.vehicleId" style="width:100%">
          <el-option v-for="v in board.vehicles || []" :key="v.id" :label="`${v.plateNo}（${v.status}）`" :value="v.id" />
        </el-select></el-form-item>
        <el-form-item label="司机"><el-select v-model="assignForm.driverId" filterable style="width:100%">
          <el-option v-for="d in drivers" :key="d.id" :label="`${d.name}（${d.phone}）`" :value="d.id" />
        </el-select></el-form-item>
        <el-form-item label="承运商"><el-select v-model="assignForm.carrierId" clearable style="width:100%">
          <el-option v-for="c in carriers" :key="c.id" :label="c.name" :value="c.id" />
        </el-select></el-form-item>
        <el-form-item label="备注"><el-input v-model="assignForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialog = false">取消</el-button>
        <el-button type="primary" @click="assign">确认派车</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { get, post } from '../../api/http'

const pool = ref([])
const board = reactive({})
const drivers = ref([])
const carriers = ref([])
const loading = ref(false)
const assignDialog = ref(false)
const assignForm = reactive({})
const assignWaybillId = ref(null)

async function load() {
  loading.value = true
  try {
    const p = await get('/tenant/dispatch/pool'); pool.value = p.data
    const b = await get('/tenant/dispatch/board'); Object.assign(board, b.data)
    const d = await get('/tenant/drivers', { page: 1, size: 100 }); drivers.value = d.data.list
    const c = await get('/tenant/carriers', { page: 1, size: 100 }); carriers.value = c.data.list
  } finally { loading.value = false }
}
function openAssign(row) {
  assignWaybillId.value = row.id
  Object.assign(assignForm, {})
  assignDialog.value = true
}
async function assign() {
  if (!assignForm.vehicleId) return ElMessage.warning('请选择车辆')
  await post('/tenant/dispatch/assign', { waybillId: assignWaybillId.value, ...assignForm })
  ElMessage.success('派车成功'); assignDialog.value = false; load()
}
onMounted(load)
</script>
