<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">异常处理</div>
        <div class="page-sub">异常上报、升级、处理闭环、卡滞扫描</div>
      </div>
      <div>
        <el-button @click="scan"><el-icon><Search /></el-icon>&nbsp;扫描超时</el-button>
        <el-button type="primary" @click="openReport"><el-icon><Plus /></el-icon>&nbsp;上报异常</el-button>
      </div>
    </div>

    <div class="card">
      <div class="toolbar">
        <el-select v-model="query.status" placeholder="状态" clearable>
          <el-option v-for="s in ['待处理', '处理中', '已处理', '已升级']" :key="s" :label="s" :value="s" />
        </el-select>
        <el-select v-model="query.recordType" placeholder="类型" clearable>
          <el-option v-for="s in ['延误', '破损', '货物丢失', '拒收', '地址异常', '超时']" :key="s" :label="s" :value="s" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="waybillNo" label="运单号" width="160" />
        <el-table-column prop="recordType" label="类型" width="110" />
        <el-table-column prop="description" label="描述" min-width="220" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="tag(row.status)" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="reporter" label="上报人" width="110" />
        <el-table-column prop="createdAt" label="上报时间" width="170" />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status !== '已处理'" size="small" type="primary" @click="handle(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 14px; justify-content: flex-end" layout="total, prev, pager, next"
        :total="total" :page-size="size" :current-page="page" @current-change="(p) => { page = p; load() }" />
    </div>

    <el-dialog v-model="reportDialog" title="上报异常" width="520px">
      <el-form label-width="110px">
        <el-form-item label="运单ID" required><el-input-number v-model="reportForm.waybillId" :min="1" /></el-form-item>
        <el-form-item label="异常类型"><el-select v-model="reportForm.recordType" style="width:100%">
          <el-option v-for="s in ['延误', '破损', '货物丢失', '拒收', '地址异常']" :key="s" :label="s" :value="s" />
        </el-select></el-form-item>
        <el-form-item label="描述"><el-input v-model="reportForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="升级"><el-switch v-model="reportForm.escalate" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialog = false">取消</el-button>
        <el-button type="primary" @click="report">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="handleDialog" title="异常处理" width="520px">
      <el-form label-width="110px">
        <el-form-item label="处理结论"><el-input v-model="handleForm.conclusion" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="处理人"><el-input v-model="handleForm.assignee" /></el-form-item>
        <el-form-item label="升级"><el-switch v-model="handleForm.escalate" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialog = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { get, post } from '../../api/http'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const query = reactive({})
const reportDialog = ref(false)
const handleDialog = ref(false)
const reportForm = reactive({})
const handleForm = reactive({})
const handleId = ref(null)
function tag(s) { return { 待处理: 'danger', 处理中: 'warning', 已处理: 'success', 已升级: 'danger' }[s] || 'info' }

async function load() {
  loading.value = true
  try { const r = await get('/tenant/exceptions', { ...query, page: page.value, size: size.value }); rows.value = r.data.list; total.value = r.data.total }
  finally { loading.value = false }
}
function openReport() { Object.assign(reportForm, { waybillId: 1, recordType: '延误', description: '', escalate: false }); reportDialog.value = true }
async function report() { await post('/tenant/exceptions', { ...reportForm }); ElMessage.success('已上报'); reportDialog.value = false; load() }
function handle(row) { handleId.value = row.id; Object.assign(handleForm, { conclusion: '', assignee: '', escalate: false }); handleDialog.value = true }
async function submitHandle() { await post(`/tenant/exceptions/${handleId.value}/handle`, { ...handleForm }); ElMessage.success('已处理'); handleDialog.value = false; load() }
async function scan() { const r = await post('/tenant/timeouts/scan'); ElMessage.success(`扫描完成，发现 ${r.data?.count ?? 0} 个超时`); load() }
onMounted(load)
</script>
