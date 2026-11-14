<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">运单详情</div>
        <div class="page-sub">{{ wb.waybillNo || '' }} · {{ wb.origin }} → {{ wb.destination }}</div>
      </div>
      <div>
        <el-button @click="$router.back()">返回</el-button>
        <el-button type="primary" @click="print">打印托运单</el-button>
        <el-button v-if="wb.status === '待调度'" type="success" @click="toBoard">去派车</el-button>
      </div>
    </div>

    <div class="chart-row mb">
      <div class="card">
        <div class="page-title" style="font-size:15px;margin-bottom:12px;">运单信息</div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="运单号">{{ wb.waybillNo }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :type="tag(wb.status)" size="small">{{ wb.status }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="客户">{{ wb.customerName }}</el-descriptions-item>
          <el-descriptions-item label="货物">{{ wb.cargoName }}</el-descriptions-item>
          <el-descriptions-item label="重量">{{ wb.weight }}kg</el-descriptions-item>
          <el-descriptions-item label="体积">{{ wb.volume }}m³</el-descriptions-item>
          <el-descriptions-item label="发货人">{{ wb.senderName }} {{ wb.senderPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ wb.receiverName }} {{ wb.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="发货地址">{{ wb.senderAddress }}</el-descriptions-item>
          <el-descriptions-item label="收货地址">{{ wb.receiverAddress }}</el-descriptions-item>
          <el-descriptions-item label="结算方式">{{ wb.settleType }}</el-descriptions-item>
          <el-descriptions-item label="代收货款">{{ wb.codAmount }} 元</el-descriptions-item>
          <el-descriptions-item label="运费">{{ wb.freightAmount }} 元</el-descriptions-item>
          <el-descriptions-item label="附加费">{{ wb.surchargeAmount }} 元</el-descriptions-item>
          <el-descriptions-item label="费用合计">{{ wb.totalAmount }} 元</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ wb.createdAt }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="card">
        <div class="page-title" style="font-size:15px;margin-bottom:12px;">轨迹时间线</div>
        <el-timeline>
          <el-timeline-item v-for="t in timeline" :key="t.time + t.node" :timestamp="t.time" :type="t.type || 'primary'">
            {{ t.node }}<span v-if="t.address" class="addr">（{{ t.address }}）</span>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>

    <div class="card">
      <div class="page-title" style="font-size:15px;margin-bottom:12px;">状态流转</div>
      <div class="flow">
        <el-button v-if="wb.status === '待受理'" type="primary" @click="doAction('受理')">受理</el-button>
        <el-button v-if="wb.status === '待调度'" type="primary" @click="toBoard">派车调度</el-button>
        <el-button v-if="wb.status === '已派车'" type="primary" @click="doAction('提货')">确认提货</el-button>
        <el-button v-if="wb.status === '已提货'" type="primary" @click="doAction('发车')">发车运输</el-button>
        <el-button v-if="wb.status === '运输中'" type="primary" @click="doAction('到达')">到达目的地</el-button>
        <el-button v-if="wb.status === '到达中转' || wb.status === '派送中'" type="primary" @click="doAction('签收')">签收</el-button>
        <el-button type="danger" @click="doAction('取消')">取消运单</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post } from '../../api/http'

const route = useRoute()
const wb = reactive({})
const timeline = ref([])
function tag(s) { return { 已签收: 'success', 已异常: 'danger', 已取消: 'info', 待调度: 'primary', 已派车: 'warning' }[s] || 'primary' }

async function load() {
  const r = await get(`/tenant/waybills/${route.params.id}`)
  Object.assign(wb, r.data)
  const t = await get(`/tenant/waybills/${route.params.id}/timeline`)
  timeline.value = t.data?.timeline || t.data?.list || []
}

async function doAction(action) {
  let reason = ''
  if (action === '取消') {
    const r = await ElMessageBox.prompt('请输入取消原因', '取消运单', { confirmButtonText: '确认', cancelButtonText: '取消' })
    reason = r.value
  }
  await post(`/tenant/waybills/${route.params.id}/action`, { action, reason })
  ElMessage.success('操作成功'); load()
}

function toBoard() { window.open(`/tenant/board?waybillId=${wb.id}`, '_blank') }
function print() { window.open(`/api/tenant/waybills/${wb.id}/print`, '_blank') }

onMounted(load)
</script>

<style scoped>
.addr { color: #909399; font-size: 12px; }
.flow { display: flex; gap: 10px; flex-wrap: wrap; }
</style>
