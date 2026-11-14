<template>
  <div class="page dashboard">
    <div class="page-title-row">
      <div>
        <div class="page-title">平台工作台</div>
        <div class="page-subtitle">掌握租户健康度、平台运单与营收概况</div>
      </div>
      <div class="flex gap-8">
        <el-button @click="router.push('/platform/licenses')">
          <el-icon><DocumentChecked /></el-icon> 资质审核
        </el-button>
        <el-button type="primary" @click="router.push('/platform/tenants?create=1')">
          <el-icon><Plus /></el-icon> 开通租户
        </el-button>
      </div>
    </div>

    <div class="kpi-grid">
      <KpiCard title="租户总数" :value="data.tenantTotal" unit="家" icon="OfficeBuilding" color="#1f6feb" :trend="8.2" trend-label="较上月" clickable @click="router.push('/platform/tenants')" />
      <KpiCard title="试用中" :value="data.trialCount" unit="家" icon="Clock" color="#f59e0b" trend-label="7 天内到期" extra="2 家" clickable @click="router.push('/platform/tenants')" />
      <KpiCard title="欠费租户" :value="data.arrearsCount" unit="家" icon="Warning" color="#dc2626" trend-label="待催缴" extra="¥ 8,600" clickable @click="router.push('/platform/bills')" />
      <KpiCard title="即将到期" :value="data.expiringCount" unit="家" icon="Calendar" color="#0ea5e9" trend-label="30 天内" extra="需跟进" clickable @click="router.push('/platform/tenants')" />
    </div>

    <div class="kpi-grid mt-12">
      <KpiCard title="平台运单总量" :value="formatNumber(data.waybillTotal)" unit="票" icon="Tickets" color="#1f6feb" :trend="12.6" trend-label="较上月" />
      <KpiCard title="异常运单" :value="data.exceptionCount" unit="票" icon="Bell" color="#dc2626" :trend="-3.4" trend-label="较上月" />
      <KpiCard title="本月平台营收" :value="formatNumber(data.monthlyRevenue)" prefix="¥" icon="Money" color="#16a34a" :trend="10.8" trend-label="较上月" />
      <KpiCard title="活跃租户" :value="data.activeTenant" unit="家" icon="DataLine" color="#7c3aed" :trend="4.1" trend-label="近 30 天" />
    </div>

    <div class="dash-grid mt-12">
      <el-card shadow="never" class="chart-card">
        <template #header>
          <div class="flex-between">
            <span>平台营收与新增租户趋势</span>
            <el-tag size="small" type="info" effect="plain">近 6 个月</el-tag>
          </div>
        </template>
        <ChartBox :option="trendOption" :height="286" />
      </el-card>

      <el-card shadow="never" class="chart-card">
        <template #header>
          <div class="flex-between">
            <span>租户状态分布</span>
            <span class="link-text" @click="router.push('/platform/tenants')">查看全部</span>
          </div>
        </template>
        <ChartBox :option="statusOption" :height="286" />
      </el-card>
    </div>

    <div class="dash-bottom mt-12">
      <el-card shadow="never">
        <template #header>
          <div class="flex-between">
            <span>即将到期租户</span>
            <span class="link-text" @click="router.push('/platform/tenants')">租户管理</span>
          </div>
        </template>
        <el-table :data="data.expiringTenants" size="small" stripe>
          <el-table-column prop="name" label="企业名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="planName" label="套餐" width="90" />
          <el-table-column prop="seats" label="席位" width="70" align="center" />
          <el-table-column prop="expireAt" label="到期日" width="110">
            <template #default="{ row }">
              <span :class="daysLeft(row.expireAt) <= 30 ? 'danger-text' : ''">{{ row.expireAt }}</span>
            </template>
          </el-table-column>
          <el-table-column label="剩余" width="90">
            <template #default="{ row }">
              <span :class="daysLeft(row.expireAt) <= 7 ? 'danger-text' : 'warn-text'">
                {{ daysLeft(row.expireAt) >= 0 ? `${daysLeft(row.expireAt)} 天` : '已到期' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }"><StatusTag :status="row.status" /></template>
          </el-table-column>
          <el-table-column label="操作" width="90" align="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="renew(row)">续期</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card shadow="never" class="side-card">
        <template #header>平台待办</template>
        <div class="todo-item" v-for="(item, i) in todos" :key="i" @click="router.push(item.path)">
          <span class="todo-dot" :class="item.level"></span>
          <div class="todo-body">
            <div class="todo-title">{{ item.title }}</div>
            <div class="todo-sub">{{ item.desc }}</div>
          </div>
          <el-icon class="todo-arrow"><ArrowRight /></el-icon>
        </div>
        <div class="divider"></div>
        <div class="quick-title">快捷入口</div>
        <div class="quick-grid">
          <div v-for="q in quicks" :key="q.title" class="quick-item" @click="router.push(q.path)">
            <el-icon :style="{ color: q.color }"><component :is="q.icon" /></el-icon>
            <span>{{ q.title }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="renewVisible" title="租户续期" width="400px">
      <div class="renew-tenant">{{ renewTarget?.name }}</div>
      <el-form label-width="80px">
        <el-form-item label="续期时长">
          <el-select v-model="renewMonths" class="w-full">
            <el-option v-for="m in [1, 3, 6, 12, 24, 36]" :key="m" :label="`${m} 个月`" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="当前到期">
          <span>{{ renewTarget?.expireAt || '—' }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renewVisible = false">取消</el-button>
        <el-button type="primary" :loading="renewLoading" @click="submitRenew">确认续期</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import KpiCard from '@/components/KpiCard.vue'
import ChartBox from '@/components/ChartBox.vue'
import StatusTag from '@/components/StatusTag.vue'
import { getPlatformDashboard, renewTenant } from '@/api/platform'
import { formatNumber } from '@/utils/format'

const router = useRouter()

const data = ref<any>({
  tenantTotal: 0,
  trialCount: 0,
  arrearsCount: 0,
  expiringCount: 0,
  waybillTotal: 0,
  exceptionCount: 0,
  monthlyRevenue: 0,
  activeTenant: 0,
  expiringTenants: [],
  trend: { categories: [], newTenants: [], revenue: [] }
})

const todos = [
  { title: '2 家租户处于欠费状态', desc: '已逾期 3 天，建议发送催缴通知', level: 'danger', path: '/platform/bills' },
  { title: '3 份资质待审核', desc: '营业执照与道路运输经营许可证', level: 'warning', path: '/platform/licenses' },
  { title: '5 家租户 30 天内到期', desc: '续期跟进可减少流失', level: 'primary', path: '/platform/tenants' },
  { title: '本月账单待核销 4 笔', desc: '合计 ¥ 32,998', level: 'primary', path: '/platform/bills' }
]
const quicks = [
  { title: '账单收款', icon: 'Wallet', color: '#1f6feb', path: '/platform/bills' },
  { title: '套餐管理', icon: 'Tickets', color: '#16a34a', path: '/platform/plans' },
  { title: '审计日志', icon: 'List', color: '#7c3aed', path: '/platform/audits' },
  { title: '发布公告', icon: 'Bell', color: '#f59e0b', path: '/platform/announcements' }
]

const renewVisible = ref(false)
const renewLoading = ref(false)
const renewTarget = ref<any>(null)
const renewMonths = ref(12)
const reloadKey = ref(0)

const trendOption = computed(() => ({
  grid: { left: 46, right: 46, top: 38, bottom: 26 },
  tooltip: { trigger: 'axis' },
  legend: { right: 0, top: 0, itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 11 } },
  xAxis: {
    type: 'category',
    data: data.value.trend?.categories || [],
    axisLine: { lineStyle: { color: '#e6eaf0' } },
    axisLabel: { color: '#8492a6', fontSize: 11 },
    axisTick: { show: false }
  },
  yAxis: [
    {
      type: 'value',
      name: '营收（元）',
      nameTextStyle: { fontSize: 10, color: '#8492a6' },
      splitLine: { lineStyle: { color: '#f0f3f8' } },
      axisLabel: { color: '#8492a6', fontSize: 10 }
    },
    {
      type: 'value',
      name: '新增',
      nameTextStyle: { fontSize: 10, color: '#8492a6' },
      splitLine: { show: false },
      axisLabel: { color: '#8492a6', fontSize: 10 }
    }
  ],
  series: [
    {
      name: '平台营收',
      type: 'line',
      smooth: true,
      data: data.value.trend?.revenue || [],
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { width: 2.5, color: '#1f6feb' },
      itemStyle: { color: '#1f6feb' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(31,111,235,.22)' },
            { offset: 1, color: 'rgba(31,111,235,0)' }
          ]
        }
      }
    },
    {
      name: '新增租户',
      type: 'bar',
      yAxisIndex: 1,
      barWidth: 14,
      data: data.value.trend?.newTenants || [],
      itemStyle: { color: 'rgba(14,165,233,.55)', borderRadius: [3, 3, 0, 0] }
    }
  ]
}))

const statusOption = computed(() => {
  const tenants = [
    { name: '正常', value: 7 },
    { name: '试用中', value: 2 },
    { name: '欠费', value: 1 },
    { name: '已到期', value: 1 },
    { name: '已停用', value: 1 }
  ]
  return {
    tooltip: { trigger: 'item', formatter: '{b}：{c} 家（{d}%）' },
    legend: { bottom: 0, itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 11 } },
    color: ['#16a34a', '#f59e0b', '#dc2626', '#0ea5e9', '#94a3b8'],
    series: [
      {
        type: 'pie',
        radius: ['45%', '68%'],
        center: ['50%', '44%'],
        padAngle: 2,
        itemStyle: { borderRadius: 4 },
        label: { show: false },
        data: tenants
      }
    ]
  }
})

function daysLeft(date: string): number {
  if (!date) return 0
  return Math.ceil((new Date(date).getTime() - Date.now()) / 86400000)
}

function renew(row: any): void {
  renewTarget.value = row
  renewMonths.value = 12
  renewVisible.value = true
}

async function submitRenew(): Promise<void> {
  if (!renewTarget.value) return
  renewLoading.value = true
  try {
    await renewTenant(renewTarget.value.id, renewMonths.value)
    ElMessage.success(`已为「${renewTarget.value.name}」续期 ${renewMonths.value} 个月`)
    renewVisible.value = false
    load()
  } finally {
    renewLoading.value = false
  }
}

async function load(): Promise<void> {
  try {
    const res: any = await getPlatformDashboard()
    if (res) data.value = { ...data.value, ...res }
  } catch {
    /* 演示数据由请求拦截器兜底 */
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.dashboard {
  min-width: 0;
}
.dash-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.65fr) minmax(0, 1fr);
  gap: 12px;
}
.dash-bottom {
  display: grid;
  grid-template-columns: minmax(0, 1.65fr) minmax(300px, 1fr);
  gap: 12px;
}
.chart-card {
  :deep(.el-card__header) {
    padding: 11px 14px;
    font-size: 13.5px;
  }
  :deep(.el-card__body) {
    padding: 10px 12px;
  }
}
.side-card {
  :deep(.el-card__header) {
    padding: 11px 14px;
    font-size: 13.5px;
  }
}
.todo-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 9px 4px;
  border-bottom: 1px solid #f2f4f8;
  cursor: pointer;
  border-radius: 4px;
  &:last-of-type {
    border-bottom: none;
  }
  &:hover {
    background: #f7f9fc;
  }
}
.todo-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  margin-top: 5px;
  flex: 0 0 7px;
  background: var(--brand);
  &.warning {
    background: #f59e0b;
  }
  &.danger {
    background: #dc2626;
  }
}
.todo-body {
  min-width: 0;
  flex: 1;
}
.todo-title {
  font-size: 12.5px;
  font-weight: 500;
  line-height: 18px;
}
.todo-sub {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 1px;
}
.todo-arrow {
  color: #c0cbd9;
  font-size: 12px;
  margin-top: 4px;
}
.quick-title {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
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
.renew-tenant {
  background: #f7f9fc;
  border-radius: 6px;
  padding: 8px 10px;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 12px;
}
@media (max-width: 1200px) {
  .dash-grid,
  .dash-bottom {
    grid-template-columns: 1fr;
  }
}
</style>
