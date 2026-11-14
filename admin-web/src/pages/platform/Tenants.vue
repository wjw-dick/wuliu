<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">租户管理</div>
        <div class="page-sub">开通 / 续费 / 停用 / 归档租户</div>
      </div>
      <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>&nbsp;开通租户</el-button>
    </div>

    <div class="card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="企业名称/编码" clearable @keyup.enter="load" />
        <el-select v-model="query.status" placeholder="状态" clearable>
          <el-option v-for="s in ['试用中', '正常', '欠费', '已停用']" :key="s" :label="s" :value="s" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="name" label="企业名称" min-width="200" />
        <el-table-column prop="industry" label="行业" width="110" />
        <el-table-column label="席位" width="90">
          <template #default="{ row }">{{ row.seatUsed }}/{{ row.seatLimit }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="tag(row.status)" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="contactName" label="联系人" width="110" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column prop="serviceEnd" label="服务到期" width="170" />
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openRenew(row)">续费</el-button>
            <el-button v-if="row.status === '欠费'" size="small" type="success" @click="enable(row)">恢复</el-button>
            <el-button v-if="row.status !== '已停用'" size="small" type="warning" @click="suspend(row)">停用</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 14px; justify-content: flex-end" layout="total, prev, pager, next"
        :total="total" :page-size="size" :current-page="page" @current-change="(p) => { page = p; load() }" />
    </div>

    <el-dialog v-model="createDialog" title="开通租户" width="620px">
      <el-form :model="createForm" label-width="120px">
        <el-form-item label="企业名称" required><el-input v-model="createForm.name" /></el-form-item>
        <el-form-item label="统一信用代码"><el-input v-model="createForm.creditCode" /></el-form-item>
        <el-form-item label="所属行业"><el-input v-model="createForm.industry" /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="createForm.contactName" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="createForm.contactPhone" /></el-form-item>
        <el-form-item label="套餐"><el-select v-model="createForm.planId" style="width:100%">
          <el-option v-for="p in plans" :key="p.id" :label="p.name" :value="p.id" />
        </el-select></el-form-item>
        <el-form-item label="席位"><el-input-number v-model="createForm.seatLimit" :min="1" /></el-form-item>
        <el-form-item label="管理员账号"><el-input v-model="createForm.adminUsername" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="create">开通</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="renewDialog" title="租户续费" width="420px">
      <el-form label-width="100px">
        <el-form-item label="续费月数"><el-input-number v-model="renewMonths" :min="1" :max="36" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renewDialog = false">取消</el-button>
        <el-button type="primary" @click="doRenew">确认续费</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post, put } from '../../api/http'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const saving = ref(false)
const query = reactive({})
const plans = ref([])
const createDialog = ref(false)
const renewDialog = ref(false)
const renewMonths = ref(1)
const renewId = ref(null)
const createForm = reactive({ name: '', creditCode: '', industry: '综合物流', contactName: '', contactPhone: '', planId: 2, seatLimit: 10, adminUsername: 'admin' })

function tag(s) { return { 试用中: 'warning', 正常: 'success', 欠费: 'danger', 已停用: 'info' }[s] || 'info' }

async function load() {
  loading.value = true
  try {
    const res = await get('/platform/tenants', { ...query, page: page.value, size: size.value })
    rows.value = res.data.list
    total.value = res.data.total
  } finally { loading.value = false }
}
function reset() { Object.keys(query).forEach((k) => delete query[k]); page.value = 1; load() }

function openCreate() { Object.assign(createForm, { name: '', creditCode: '', industry: '综合物流', contactName: '', contactPhone: '', planId: 2, seatLimit: 10, adminUsername: 'admin' }); createDialog.value = true }
async function create() {
  if (!createForm.name) return ElMessage.warning('请填写企业名称')
  saving.value = true
  try { await post('/platform/tenants', { ...createForm }); ElMessage.success('开通成功'); createDialog.value = false; load() }
  finally { saving.value = false }
}

function openRenew(row) { renewId.value = row.id; renewMonths.value = 1; renewDialog.value = true }
async function doRenew() { await post(`/platform/tenants/${renewId.value}/renew`, { months: renewMonths.value }); ElMessage.success('续费成功'); renewDialog.value = false; load() }
async function enable(row) { await post(`/platform/tenants/${row.id}/enable`); ElMessage.success('已恢复'); load() }
async function suspend(row) {
  const { value } = await ElMessageBox.prompt('请输入停用原因', '停用租户', { confirmButtonText: '确认', cancelButtonText: '取消' })
  await post(`/platform/tenants/${row.id}/suspend`, { reason: value || '运营暂停' }); ElMessage.success('已停用'); load()
}

onMounted(async () => { load(); const r = await get('/platform/plans'); plans.value = r.data })
</script>
