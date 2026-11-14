<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">工作台</div>
        <div class="page-sub">租户运营总览 · {{ auth.user?.tenantName || '当前租户' }}</div>
      </div>
      <el-button @click="load"><el-icon><Refresh /></el-icon>&nbsp;刷新</el-button>
    </div>

    <div class="stat-grid mb">
      <div class="stat-card"><div class="label">今日运单</div><div class="value">{{ data.todayNew ?? 0 }}</div><div class="hint">累计 {{ data.totalWaybills ?? 0 }}</div></div>
      <div class="stat-card" style="border-left-color:#67c23a"><div class="label">在途运单</div><div class="value">{{ data.inTransit ?? 0 }}</div><div class="hint">待调度 {{ data.pendingDispatch ?? 0 }}</div></div>
      <div class="stat-card" style="border-left-color:#e6a23c"><div class="label">今日营收(元)</div><div class="value">{{ data.todayRevenue ?? 0 }}</div><div class="hint">应收账款 {{ data.unpaidTotal ?? 0 }}</div></div>
      <div class="stat-card" style="border-left-color:#f56c6c"><div class="label">待处理异常</div><div class="value">{{ data.pendingException ?? 0 }}</div><div class="hint">欠款客户 {{ data.debtCustomers ?? 0 }}</div></div>
    </div>

    <div class="chart-row mb">
      <div class="card">
        <div class="page-title" style="font-size:15px;margin-bottom:12px;">近7日运单量</div>
        <div ref="chart1" style="height:260px"></div>
      </div>
      <div class="card">
        <div class="page-title" style="font-size:15px;margin-bottom:12px;">运单状态分布</div>
        <div ref="chart2" style="height:260px"></div>
      </div>
    </div>

    <div class="card">
      <div class="page-title" style="font-size:15px;margin-bottom:12px;">快捷入口</div>
      <div class="quick">
        <el-button type="primary" @click="$router.push('/tenant/waybills')">新建运单</el-button>
        <el-button @click="$router.push('/tenant/board')">调度看板</el-button>
        <el-button @click="$router.push('/tenant/quotes')">询价报价</el-button>
        <el-button @click="$router.push('/tenant/statements')">对账单</el-button>
        <el-button @click="$router.push('/tenant/reports')">报表中心</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { get } from '../../api/http'
import { useAuth } from '../../store/auth'

const auth = useAuth()
const data = reactive({})
const chart1 = ref(null)
const chart2 = ref(null)
let c1, c2

async function load() {
  const r = await get('/tenant/dashboard')
  Object.assign(data, r.data)
  await nextTick()
  renderCharts()
}

function renderCharts() {
  if (chart1.value) {
    c1 = c1 || echarts.init(chart1.value)
    const trend = data.trend || []
    c1.setOption({
      tooltip: { trigger: 'axis' }, grid: { left: 40, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: trend.map((t) => t.date) },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: trend.map((t) => t.tickets), itemStyle: { color: '#409eff' }, barMaxWidth: 30 }]
    })
  }
  if (chart2.value) {
    c2 = c2 || echarts.init(chart2.value)
    const dist = Object.entries(data.statusDistribution || {}).map(([status, count]) => ({ status, count }))
    c2.setOption({
      tooltip: { trigger: 'item' },
      series: [{ type: 'pie', radius: ['40%', '68%'], data: dist.map((d) => ({ name: d.status, value: d.count })), label: { formatter: '{b}: {c}' } }]
    })
  }
}

function onResize() { c1?.resize(); c2?.resize() }
onMounted(() => { load(); window.addEventListener('resize', onResize) })
onBeforeUnmount(() => { window.removeEventListener('resize', onResize); c1?.dispose(); c2?.dispose() })
</script>

<style scoped>
.quick { display: flex; gap: 10px; flex-wrap: wrap; }
</style>
