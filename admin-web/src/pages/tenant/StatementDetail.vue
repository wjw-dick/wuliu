<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">对账详情</div>
        <div class="page-sub">{{ detail.statement?.statementNo || '' }} · {{ detail.statement?.status }}</div>
      </div>
      <div>
        <el-button @click="$router.back()">返回</el-button>
        <el-button v-if="detail.statement?.status === '待客户确认'" type="success" @click="confirm(true)">确认</el-button>
        <el-button v-if="detail.statement?.status === '待客户确认'" type="danger" @click="confirm(false)">异议</el-button>
        <el-button type="primary" @click="download">下载PDF</el-button>
      </div>
    </div>

    <div class="card mb">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="对账单号">{{ detail.statement?.statementNo }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detail.statement?.type }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag size="small">{{ detail.statement?.status }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="客户/承运商">{{ detail.statement?.partyName }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ detail.statement?.amount }} 元</el-descriptions-item>
        <el-descriptions-item label="期间">{{ detail.statement?.periodStart }} ~ {{ detail.statement?.periodEnd }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="card">
      <div class="page-title" style="font-size:15px;margin-bottom:12px;">明细</div>
      <el-table :data="detail.items || []" border stripe>
        <el-table-column prop="waybillNo" label="运单号" width="160" />
        <el-table-column prop="date" label="日期" width="150" />
        <el-table-column prop="origin" label="出发" width="120" />
        <el-table-column prop="destination" label="到达" width="120" />
        <el-table-column prop="cargoName" label="货物" width="110" />
        <el-table-column prop="weight" label="kg" width="80" />
        <el-table-column prop="amount" label="金额(元)" width="110" />
        <el-table-column prop="remark" label="备注" min-width="140" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post } from '../../api/http'

const route = useRoute()
const detail = reactive({})
async function load() {
  const r = await get(`/tenant/statements/${route.params.id}`)
  Object.assign(detail, r.data)
}
async function confirm(pass) {
  let reason = ''
  if (!pass) {
    const r = await ElMessageBox.prompt('请输入异议原因', '对账异议', { confirmButtonText: '提交', cancelButtonText: '取消' })
    reason = r.value
  }
  await post(`/tenant/statements/${route.params.id}/confirm`, { pass, reason })
  ElMessage.success('操作成功'); load()
}
function download() { window.open(`/api/tenant/statements/${route.params.id}/pdf`, '_blank') }
onMounted(load)
</script>
