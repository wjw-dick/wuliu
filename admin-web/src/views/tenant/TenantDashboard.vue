<template>
  <div class="page dashboard">
    <div class="page-title-row">
      <div>
        <div class="page-title">{{ greeting }}，{{ auth.displayName }}</div>
        <div class="page-subtitle">{{ auth.tenantName }} · 今日概览与待办提醒</div>
      </div>
      <div class="flex gap-8">
        <el-button @click="router.push('/tenant/dispatch/board')">
          <el-icon><Van /></el-icon> 调度看板
        </el-button>
        <el-button type="primary" @click="router.push('/tenant/waybills/create')">
          <el-icon><Plus /></el-icon> 新建运单
        </el-button>
      </div>
    </div>

    <div class="kpi-grid">
      <KpiCard title="今日新增运单" :value="data.todayNew" unit="票" icon="Tickets" color="#1f6feb" :trend="6.8" trend-label="较昨日" clickable @click="router.push('/tenant/waybills')" />
      <KpiCard title="在途运单" :value="data.inTransit" unit="票" icon="Van" color="#0ea5e9" trend-label="实时在途" extra="查看轨迹" clickable @click="router.push('/tenant/tracks')" />
      <KpiCard title="待调度" :value="data.waitDispatch" unit="票" icon="Clock" color="#f59e0b" trend-label="需 2 小时内派车" extra="去派车" clickable @click="router.push('/tenant/dispatch/board')" />
      <KpiCard title="异常待处理" :value="data.exceptions" unit="单" icon="Warning" color="#dc2626" trend-label="含 2 单升级" extra="去处理" clickable @click="router.push('/tenant/exceptions')" />
    </div>

    <div class="kpi-grid mt-12">
      <KpiCard title="今日营收" :value="formatNumber(data.todayRevenue)" prefix="¥" icon="Money" color="#16a34a" :trend="12.4" trend-label="较昨日" />
      <KpiCard title="欠款客户" :value="data.arrearsCustomers" unit="家" icon="Wallet" color="#f97316" trend-label="合计 ¥ 86,200" extra="催收" clickable @click="router.push('/tenant/aging')" />
      <KpiCard title="到期资质" :value="data.expiringQualifications" unit="项" icon="DocumentChecked" color="#dc2626" trend-label="30 天内到期" extra="去续期" clickable @click="router.push('/tenant/qualifications')" />
      <KpiCard title="本月营收" :value="formatNumber(data.monthRevenue)" prefix="¥" icon="TrendCharts" color="#7c3aed" :trend="8.6" trend-label="较上月" extra="毛利率 30.9%" />
    </div>

    <div class="dash-grid mt-12">
      <el-card shadow="never" class="chart-card">
        <template #header>
          <div class="flex-between">
            <span>近 7 日运单与营收趋势</span>
            <div class="flex gap-8">
              <el-tag size="small" type="primary" effect="plain">票数</el-tag>
              <el-tag size="small" color="#16a34a" effect="plain" class="green-tag">营收</el-tag>
            </div>
          </div>
        </template>
        <ChartBox :option="trendOption" :height="292" />
      </el-card>

      <el-card shadow="never" class="chart-card">
        <template #header>
          <div class="flex-between">
            <span>运单状态分布</span>
            <span class="muted">实时</span>
          </div>
        </template>
        <ChartBox :option="statusOption" :height="292" />
      </el-card>
    </div>

    <div class="dash-bottom mt-12">
      <el-card shadow="never">
        <template #header>
          <div class="flex-between">
            <span>即将到期资质</span>
            <span class="link-text" @click="router.push('/tenant/qualifications')">全部资质</span>
          </div>
        </template>
        <el-table :data="data.expiring" size="small" stripe>
          <el-table-column prop="ownerName" label="所属对象" min-width="130" />
          <el-table-column prop="ownerType" label="类型" width="80" />
          <el-table-column prop="certName" label="证照" min-width="130" />
          <el-table-column prop="expireAt" label="到期日" width="110">
            <template #default="{ row }">
              <span class="danger-text">{{ row.expireAt }}</span>
            </template>
          </el-table-column>
          <el-table-column label="剩余" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.daysLeft <= 10 ? 'danger' : 'warning'" effect="light">{{ row.daysLeft }} 天</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="right">
            <template #default>
              <el-button link type="primary" @click="router.push('/tenant/qualifications')">处理</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card shadow="never" class="todo-card">
        <template #header>
          <div class="flex-between">
            <span>今日待办</span>
            <el-tag size="small" type="danger" effect="plain">{{ data.todos.length }} 项</el-tag>
          </div>
        </template>
        <div v-for="(todo, i) in data.todos" :key="i" class="todo-item" @click="router.push(todoPath(todo.type))">
          <span class="todo-type" :class="todo.level">{{ todo.type }}</span>
          <div class="todo-body">
            <div class="todo-title">{{ todo.title }}</div>
            <div class="todo-time">{{ todo.time }}</div>
          </div>
          <el-icon class="todo-arrow"><ArrowRight /></el-icon>
        </div>
        <div class="divider"></div>
        <div class="quick-grid">
          <div v-for="q in quicks" :key="q.title" class="quick-item" @click="router.push(q.path)">
            <el-icon :style="{ color: q.color }"><component :is="q.icon" /></el-icon>
            <span>{{ q.title }}</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import KpiCard from '@/components/KpiCard.vue'
import ChartBox from '@/components/ChartBox.vue'
import { getTenantDashboard } from '@/api/tenant'
import { useAuthStore } from '@/stores/auth'
import { formatNumber } from '@/utils/format'

const router = useRouter()
const auth = useAuthStore()

const data = ref<any>({
  todayNew: 0,
  inTransit: 0,
  waitDispatch: 0,
  exceptions: 0,
  todayRevenue: 0,
  arrearsCustomers: 0,
  expiringQualifications: 0,
  monthWaybills: 0,
  monthRevenue: 0,
  grossProfit: 0,
  statusDistribution: [],
  trend: { categories: [], waybills: [], revenue: [] },
  todos: [],
  expiring: []
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const quicks = [
  { title: '报价单', icon: 'PriceTag', color: '#1f6feb', path: '/tenant/quotes' },
  { title: '回单审核', icon: 'DocumentChecked', color: '#16a34a', path: '/tenant/receipts' },
  { title: '对账单', icon: 'Document', color: '#7c3aed', path: '/tenant/statements' },
  { title: '报表中心', icon: 'DataAnalysis', color: '#f59e0b', path: '/tenant/reports' }
]

function todoPath(type: string): string {
  const map: Record<string, string> = {
    调度: '/tenant/dispatch/board',
    异常: '/tenant/exceptions',
    回单: '/tenant/receipts',
    财务: '/tenant/statements'
  }
  return map[type] || '/tenant/waybills'
}

const trendOption = computed(() => ({
  grid: { left: 48, right: 52, top: 34, bottom: 26 },
  tooltip: { trigger: 'axis' },
  legend: { show: false },
  xAxis: {
    type: 'category',
    data: data.value.trend?.categories || [],
    axisLine: { lineStyle: { color: '#e6eaf0' } },
    axisTick: { show: false },
    axisLabel: { color: '#8492a6', fontSize: 11 }
  },
  yAxis: [
    {
      type: 'value',
      name: '票数',
      nameTextStyle: { fontSize: 10, color: '#8492a6' },
      splitLine: { lineStyle: { color: '#f0f3f8' } },
      axisLabel: { color: '#8492a6', fontSize: 10 }
    },
    {
      type: 'value',
      name: '营收（元）',
      nameTextStyle: { fontSize: 10, color: '#8492a6' },
      splitLine: { show: false },
      axisLabel: { color: '#8492a6', fontSize: 10, formatter: (v: number) => `${v / 1000}k` }
    }
  ],
  series: [
    {
      name: '运单票数',
      type: 'bar',
      barWidth: 18,
      data: data.value.trend?.waybills || [],
      itemStyle: {
        borderRadius: [3, 3, 0, 0],
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: '#4b96ff' },
            { offset: 1, color: '#a9c9fb' }
          ]
        }
      }
    },
    {
      name: '营收',
      type: 'line',
      smooth: true,
      yAxisIndex: 1,
      symbol: 'circle',
      symbolSize: 6,
      data: data.value.trend?.revenue || [],
      lineStyle: { width: 2.5, color: '#16a34a' },
      itemStyle: { color: '#16a34a' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(22,163,74,.16)' },
            { offset: 1, color: 'rgba(22,163,74,0)' }
          ]
        }
      }
    }
  ]
}))

const statusOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}：{c} 票（{d}%）' },
  legend: { bottom: 0, itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 10 }, itemGap: 8 },
  color: ['#f59e0b', '#1f6feb', '#0ea5e9', '#dc2626', '#16a34a', '#94a3b8'],
  series: [
    {
      type: 'pie',
      radius: ['46%', '68%'],
      center: ['50%', '43%'],
      padAngle: 2,
      itemStyle: { borderRadius: 5 },
      label: { show: false },
      data: data.value.statusDistribution || []
    }
  ]
}))

async function load(): Promise<void> {
  try {
    const res: any = await getTenantDashboard()
    if (res) data.value = { ...data.value, ...res }
  } catch {
    /* demo fallback */
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.dash-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(0, 1fr);
  gap: 12px;
}
.dash-bottom {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(300px, 1fr);
  gap: 12px;
}
.chart-card,
.todo-card {
  :deep(.el-card__header) {
    padding: 11px 14px;
    font-size: 13.5px;
  }
  :deep(.el-card__body) {
    padding: 10px 12px;
  }
}
.green-tag {
  color: #16a34a;
  border-color: #b7e4c7;
  background: #f0fdf4;
}
.todo-item {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 9px 4px;
  border-bottom: 1px solid #f2f4f8;
  cursor: pointer;
  &:last-of-type {
    border-bottom: none;
  }
  &:hover {
    background: #f7f9fc;
  }
}
.todo-type {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 3px;
  background: #eef4ff;
  color: var(--brand);
  flex: 0 0 auto;
  &.warning {
    background: #fff7e6;
    color: #b45309;
  }
  &.danger {
    background: #fef2f2;
    color: #dc2626;
  }
  &.primary {
    background: #eef4ff;
    color: var(--brand);
  }
}
.todo-body {
  flex: 1;
  min-width: 0;
}
.todo-title {
  font-size: 12.5px;
  font-weight: 500;
}
.todo-time {
  font-size: 10.5px;
  color: var(--text-muted);
  margin-top: 1px;
}
.todo-arrow {
  color: #c0cbd9;
  font-size: 12px;
}
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 6px;
}
.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 2px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 11px;
  color: var(--text-secondary);
  background: #f9fbfe;
  &:hover {
    background: #eef4ff;
    color: var(--brand);
  }
  .el-icon {
    font-size: 18px;
  }
}
@media (max-width: 1200px) {
  .dash-grid,
  .dash-bottom {
    grid-template-columns: 1fr;
  }
}
</style>
