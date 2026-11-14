<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">平台工作台</div>
        <div class="page-sub">平台运营总览 · 租户 / 账单 / 运单全局指标</div>
      </div>
      <el-button @click="load"><el-icon><Refresh /></el-icon>&nbsp;刷新</el-button>
    </div>

    <div class="stat-grid mb">
      <div class="stat-card"><div class="label">租户总数</div><div class="value">{{ data.tenantTotal ?? 0 }}</div><div class="hint">试用 {{ data.trialCount ?? 0 }} · 欠费 {{ data.overdueCount ?? 0 }}</div></div>
      <div class="stat-card" style="border-left-color:#67c23a"><div class="label">30天内到期</div><div class="value">{{ data.expiringCount ?? 0 }}</div><div class="hint">需跟进续费</div></div>
      <div class="stat-card" style="border-left-color:#e6a23c"><div class="label">平台运单总量</div><div class="value">{{ data.waybillTotal ?? 0 }}</div><div class="hint">全部租户合计</div></div>
      <div class="stat-card" style="border-left-color:#f56c6c"><div class="label">待处理异常</div><div class="value">{{ data.exceptionTotal ?? 0 }}</div><div class="hint">需平台协调</div></div>
    </div>

    <div class="card">
      <div class="page-title" style="margin-bottom: 12px; font-size: 16px;">即将到期租户</div>
      <el-table :data="data.expiringTenants || []" border stripe>
        <el-table-column prop="name" label="企业名称" min-width="220" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="serviceEnd" label="服务到期" width="180" />
        <el-table-column label="操作" width="120">
          <template #default>
            <el-button size="small" type="primary" link @click="$router.push('/platform/tenants')">去续费</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { get } from '../../api/http'

const data = reactive({})
async function load() {
  const res = await get('/platform/dashboard')
  Object.assign(data, res.data)
}
onMounted(load)
</script>
