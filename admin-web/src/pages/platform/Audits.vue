<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">审计日志</div>
        <div class="page-sub">全平台操作审计（谁、何时、做了什么）</div>
      </div>
    </div>
    <div class="card">
      <div class="toolbar">
        <el-input v-model="query.action" placeholder="操作动作" clearable @keyup.enter="load" />
        <el-input v-model="query.operatorName" placeholder="操作人" clearable @keyup.enter="load" />
        <el-button type="primary" @click="load">查询</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="action" label="动作" width="140" />
        <el-table-column prop="targetType" label="对象类型" width="120" />
        <el-table-column prop="targetId" label="对象ID" width="100" />
        <el-table-column prop="detail" label="详情" min-width="240" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column prop="createdAt" label="时间" width="170" />
      </el-table>
      <el-pagination style="margin-top: 14px; justify-content: flex-end" layout="total, prev, pager, next"
        :total="total" :page-size="size" :current-page="page" @current-change="(p) => { page = p; load() }" />
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { get } from '../../api/http'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const query = reactive({})

async function load() {
  loading.value = true
  try { const r = await get('/platform/audits', { ...query, page: page.value, size: size.value }); rows.value = r.data.list; total.value = r.data.total }
  finally { loading.value = false }
}
onMounted(load)
</script>
