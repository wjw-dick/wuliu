<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">客户与协议</div>
        <div class="page-sub">客户档案、联系人、地址簿、协议价、线索、跟进</div>
      </div>
      <div>
        <el-button @click="tab = 'leads'">线索池</el-button>
        <el-button @click="tab = 'agreements'">协议价</el-button>
        <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>&nbsp;新增客户</el-button>
      </div>
    </div>

    <div class="card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="客户名称/编码" clearable @keyup.enter="load" />
        <el-select v-model="query.recordType" placeholder="类型" clearable>
          <el-option v-for="t in ['企业客户', '个人客户', '同行客户']" :key="t" :label="t" :value="t" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="code" label="编码" width="110" />
        <el-table-column prop="name" label="客户名称" min-width="180" />
        <el-table-column prop="recordType" label="类型" width="100" />
        <el-table-column prop="contactName" label="联系人" width="110" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column prop="settleType" label="结算" width="90" />
        <el-table-column prop="creditLimit" label="授信(元)" width="110" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }"><el-tag :type="tag(row.status)" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 14px; justify-content: flex-end" layout="total, prev, pager, next"
        :total="total" :page-size="size" :current-page="page" @current-change="(p) => { page = p; load() }" />
    </div>

    <el-dialog v-model="dialog" :title="editing ? '编辑客户' : '新增客户'" width="600px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="客户名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.recordType" style="width:100%">
          <el-option v-for="t in ['企业客户', '个人客户', '同行客户']" :key="t" :label="t" :value="t" />
        </el-select></el-form-item>
        <el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item>
        <el-form-item label="结算方式"><el-select v-model="form.settleType" style="width:100%">
          <el-option v-for="t in ['现结', '月结', '周结', '货到付款']" :key="t" :label="t" :value="t" />
        </el-select></el-form-item>
        <el-form-item label="授信额度(元)"><el-input-number v-model="form.creditLimit" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="form.status" style="width:100%">
          <el-option v-for="t in ['合作中', '暂停合作', '已终止']" :key="t" :label="t" :value="t" />
        </el-select></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawer" title="客户详情" size="600px">
      <template v-if="detail.customer">
        <h3>{{ detail.customer.name }}</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="编码">{{ detail.customer.code }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ detail.customer.recordType }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ detail.customer.contactName }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ detail.customer.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="结算">{{ detail.customer.settleType }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.customer.status }}</el-descriptions-item>
        </el-descriptions>
        <h4>协议价</h4>
        <el-table :data="detail.agreements || []" size="small" border>
          <el-table-column prop="origin" label="出发" />
          <el-table-column prop="destination" label="到达" />
          <el-table-column prop="price" label="价格(元)" width="90" />
          <el-table-column prop="status" label="状态" width="90" />
        </el-table>
        <h4>跟进记录</h4>
        <el-timeline>
          <el-timeline-item v-for="f in detail.followups || []" :key="f.id" :timestamp="f.createdAt">
            {{ f.content }}（{{ f.operator }}）
          </el-timeline-item>
        </el-timeline>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { get, post, put } from '../../api/http'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const query = reactive({})
const dialog = ref(false)
const drawer = ref(false)
const editing = ref(false)
const detail = reactive({})
const form = reactive({})
function tag(s) { return { 合作中: 'success', 暂停合作: 'warning', 已终止: 'info' }[s] || 'info' }

async function load() {
  loading.value = true
  try { const r = await get('/tenant/customers', { ...query, page: page.value, size: size.value }); rows.value = r.data.list; total.value = r.data.total }
  finally { loading.value = false }
}
function reset() { Object.keys(query).forEach((k) => delete query[k]); page.value = 1; load() }
function openCreate() { Object.keys(form).forEach((k) => delete form[k]); Object.assign(form, { recordType: '企业客户', settleType: '现结', status: '合作中', creditLimit: 0 }); editing.value = false; dialog.value = true }
function openEdit(row) { Object.keys(form).forEach((k) => delete form[k]); Object.assign(form, row); editing.value = true; dialog.value = true }
async function save() {
  if (!form.name) return ElMessage.warning('请填写客户名称')
  if (editing.value) await put(`/tenant/customers/${form.id}`, { ...form })
  else await post('/tenant/customers', { ...form })
  ElMessage.success('保存成功'); dialog.value = false; load()
}
async function openDetail(row) {
  const r = await get(`/tenant/customers/${row.id}`)
  Object.assign(detail, r.data)
  drawer.value = true
}
onMounted(load)
</script>
