<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">资质审核</div>
        <div class="page-sub">租户证照 / 经营资质待审核队列</div>
      </div>
    </div>
    <div class="card">
      <div class="toolbar">
        <el-radio-group v-model="status" @change="load">
          <el-radio-button value="待审核">待审核</el-radio-button>
          <el-radio-button value="已通过">已通过</el-radio-button>
          <el-radio-button value="已驳回">已驳回</el-radio-button>
        </el-radio-group>
      </div>
      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="licenseNo" label="证照编号" width="170" />
        <el-table-column prop="tenantId" label="租户ID" width="90" />
        <el-table-column label="营业执照" width="140">
          <template #default="{ row }">
            <el-image v-if="row.businessLicenseUrl" :src="row.businessLicenseUrl" style="width:40px;height:28px" fit="cover" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="tag(row.status)" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="驳回原因" min-width="180" />
        <el-table-column prop="reviewedAt" label="审核时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === '待审核'" size="small" type="success" @click="review(row, true)">通过</el-button>
            <el-button v-if="row.status === '待审核'" size="small" type="danger" @click="review(row, false)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post } from '../../api/http'

const rows = ref([])
const loading = ref(false)
const status = ref('待审核')
function tag(s) { return { 待审核: 'warning', 已通过: 'success', 已驳回: 'danger' }[s] || 'info' }

async function load() {
  loading.value = true
  try { const r = await get('/platform/licenses', { status: status.value, page: 1, size: 100 }); rows.value = r.data.list }
  finally { loading.value = false }
}

async function review(row, pass) {
  let reason = ''
  if (!pass) {
    const r = await ElMessageBox.prompt('请输入驳回原因', '驳回', { confirmButtonText: '确认', cancelButtonText: '取消' })
    reason = r.value
  }
  await post(`/platform/licenses/${row.id}/review`, { pass, reason })
  ElMessage.success(pass ? '已通过' : '已驳回')
  load()
}
onMounted(load)
</script>
