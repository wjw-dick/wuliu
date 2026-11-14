<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">在途跟踪</div>
        <div class="page-sub">运单轨迹查询、节点回放、卡滞看板</div>
      </div>
      <el-button @click="openStagnant"><el-icon><Warning /></el-icon>&nbsp;卡滞看板</el-button>
    </div>

    <div class="card mb">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="运单号" clearable @keyup.enter="load" />
        <el-select v-model="query.status" placeholder="状态" clearable>
          <el-option v-for="s in ['运输中', '派送中', '到达中转']" :key="s" :label="s" :value="s" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" border stripe @row-click="openTrack" style="cursor:pointer">
        <el-table-column prop="waybillNo" label="运单号" width="160" />
        <el-table-column prop="origin" label="出发" width="120" />
        <el-table-column prop="destination" label="到达" width="120" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }"><el-tag :type="tag(row.status)" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="plateNo" label="车牌" width="120" />
        <el-table-column prop="driverName" label="司机" width="110" />
        <el-table-column prop="remark" label="备注" min-width="160" />
      </el-table>
    </div>

    <el-drawer v-model="drawer" title="运单轨迹" size="520px">
      <template v-if="trackData.waybillNo">
        <h3>{{ trackData.waybillNo }}（{{ trackData.status }}）</h3>
        <el-timeline>
          <el-timeline-item v-for="n in trackData.nodes || []" :key="n.time + n.node" :timestamp="n.time" :type="n.type || 'primary'">
            {{ n.node }}<span v-if="n.address" class="addr">（{{ n.address }}）</span>
          </el-timeline-item>
        </el-timeline>
      </template>
    </el-drawer>

    <el-dialog v-model="stagDialog" title="卡滞看板" width="720px">
      <el-table :data="stagList" border stripe>
        <el-table-column prop="waybillNo" label="运单号" width="160" />
        <el-table-column prop="origin" label="出发" width="110" />
        <el-table-column prop="destination" label="到达" width="110" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="stagnantHours" label="卡滞(小时)" width="110" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { get } from '../../api/http'

const rows = ref([])
const loading = ref(false)
const query = reactive({})
const drawer = ref(false)
const stagDialog = ref(false)
const stagList = ref([])
const trackData = reactive({})
function tag(s) { return { 运输中: 'primary', 派送中: 'warning', 到达中转: 'primary' }[s] || 'primary' }

async function load() {
  loading.value = true
  try { const r = await get('/tenant/waybills', { ...query, page: 1, size: 50 }); rows.value = r.data.list }
  finally { loading.value = false }
}
async function openTrack(row) {
  const r = await get(`/tenant/tracks/${row.id}`)
  Object.assign(trackData, r.data)
  drawer.value = true
}
async function openStagnant() {
  const r = await get('/tenant/stagnant')
  stagList.value = r.data.list || []
  stagDialog.value = true
}
onMounted(load)
</script>

<style scoped>
.addr { color: #909399; font-size: 12px; }
</style>
