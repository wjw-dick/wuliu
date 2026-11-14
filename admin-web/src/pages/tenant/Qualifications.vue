<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">资质到期</div>
        <div class="page-sub">驾驶证、从业资格、保险、年检、道路运输证到期预警</div>
      </div>
      <el-button @click="load"><el-icon><Refresh /></el-icon>&nbsp;刷新</el-button>
    </div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="type" label="类型" width="150" />
        <el-table-column prop="name" label="名称/车牌" min-width="160" />
        <el-table-column prop="expiryDate" label="到期日" width="150" />
        <el-table-column prop="daysLeft" label="剩余天数" width="110">
          <template #default="{ row }">
            <el-tag :type="row.daysLeft < 0 ? 'danger' : row.daysLeft < 15 ? 'warning' : 'success'" size="small">{{ row.daysLeft }} 天</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="预警级别" width="110" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get } from '../../api/http'

const rows = ref([])
const loading = ref(false)
async function load() {
  loading.value = true
  try { const r = await get('/tenant/qualifications/expiring'); rows.value = r.data }
  finally { loading.value = false }
}
onMounted(load)
</script>
