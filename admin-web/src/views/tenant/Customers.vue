<template>
  <div class="page">
    <div class="page-title-row">
      <div>
        <div class="page-title">客户档案</div>
        <div class="page-subtitle">企业 / 个体客户、联系人、地址簿、协议价与跟进记录一站式维护</div>
      </div>
      <div class="flex gap-8">
        <el-button @click="exportCustomers">
          <el-icon><Download /></el-icon> 导出
        </el-button>
        <el-button type="primary" @click="openForm()">
          <el-icon><Plus /></el-icon> 新增客户
        </el-button>
      </div>
    </div>

    <PageTable
      :data="list"
      :loading="loading"
      :total="total"
      :page="page"
      :size="size"
      :columns="columns"
      title="客户列表"
      empty-action-text="录入第一个客户"
      action-width="150"
      @search="search"
      @reset="resetFilters"
      @page-change="onPageChange"
      @size-change="onSizeChange"
      @empty-action="openForm()"
    >
      <template #filter>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="客户名称 / 联系人 / 电话" clearable @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="客户类型">
          <el-select v-model="query.type" placeholder="全部类型" clearable>
            <el-option v-for="t in DICTS['客户类型']" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="结算方式">
          <el-select v-model="query.settleType" placeholder="全部方式" clearable>
            <el-option v-for="t in DICTS['结算方式']" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户等级">
          <el-select v-model="query.level" placeholder="全部等级" clearable>
            <el-option v-for="t in DICTS['客户等级']" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
      </template>
      <template #filter-advanced>
        <el-form-item label="销售负责人">
          <el-input v-model="query.salesName" placeholder="销售姓名" clearable />
        </el-form-item>
        <el-form-item label="客户状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable>
            <el-option label="正常" value="正常" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
      </template>

      <template #name="{ row }">
        <span class="strong link-text" @click="openDetail(row)">{{ row.name }}</span>
        <div class="muted mono">{{ row.customerNo }}</div>
      </template>
      <template #contact="{ row }">
        <div>{{ row.contact }}</div>
        <div class="muted">{{ row.phone }}</div>
      </template>
      <template #receivable="{ row }">
        <MoneyText :value="row.receivable" :colored="row.receivable > 0" />
      </template>
      <template #creditLimit="{ row }">
        <MoneyText :value="row.creditLimit" />
        <div v-if="row.creditLimit" class="muted credit-tip">已用 {{ usagePercent(row) }}%</div>
      </template>
      <template #status="{ row }"><StatusTag :status="row.status" /></template>
      <template #actions="{ row }">
        <el-button link type="primary" @click="openDetail(row)">详情</el-button>
        <el-button link type="primary" @click="openForm(row)">编辑</el-button>
        <el-button link type="danger" @click="removeCustomer(row)">删除</el-button>
      </template>
    </PageTable>

    <!-- 新增 / 编辑 -->
    <el-dialog v-model="formVisible" :title="editing ? '编辑客户' : '新增客户'" width="760px" top="5vh" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <div class="form-grid">
          <el-form-item label="客户名称" prop="name" class="span-2">
            <el-input v-model="form.name" placeholder="企业全称或个体姓名" />
          </el-form-item>
          <el-form-item label="客户类型" prop="type">
            <el-select v-model="form.type" class="w-full">
              <el-option v-for="t in DICTS['客户类型']" :key="t" :label="t" :value="t" />
            </el-select>
          </el-form-item>
          <el-form-item label="客户等级">
            <el-select v-model="form.level" class="w-full">
              <el-option v-for="t in DICTS['客户等级']" :key="t" :label="t" :value="t" />
            </el-select>
          </el-form-item>
          <el-form-item label="联系人" prop="contact">
            <el-input v-model="form.contact" placeholder="主要联系人" />
          </el-form-item>
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="form.phone" maxlength="11" placeholder="11 位手机号" />
          </el-form-item>
          <el-form-item label="结算方式" prop="settleType">
            <el-select v-model="form.settleType" class="w-full">
              <el-option v-for="t in DICTS['结算方式']" :key="t" :label="t" :value="t" />
            </el-select>
          </el-form-item>
          <el-form-item label="信用额度">
            <el-input-number v-model="form.creditLimit" :min="0" :step="10000" class="w-full" />
          </el-form-item>
          <el-form-item label="客户来源">
            <el-select v-model="form.source" class="w-full">
              <el-option v-for="t in DICTS['客户来源']" :key="t" :label="t" :value="t" />
            </el-select>
          </el-form-item>
          <el-form-item label="销售负责人">
            <el-input v-model="form.salesName" placeholder="如：王销售" />
          </el-form-item>
          <el-form-item label="归属网点">
            <el-select v-model="form.orgName" class="w-full">
              <el-option v-for="o in ['上海总部', '杭州网点', '苏州网点']" :key="o" :label="o" :value="o" />
            </el-select>
          </el-form-item>
          <el-form-item label="纳税人识别号">
            <el-input v-model="form.taxNo" placeholder="开票所需税号" />
          </el-form-item>
          <el-form-item label="详细地址" class="span-2">
            <el-input v-model="form.address" placeholder="常用发货地址" />
          </el-form-item>
          <el-form-item label="备注" class="span-2">
            <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="特殊约定、开票要求等" />
          </el-form-item>
        </div>
        <UploadCard v-model="attachments" title="营业执照 / 合同附件" tip="支持多图排序与 PDF，仅内部可见" :max="5" />
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存客户</el-button>
      </template>
    </el-dialog>

    <!-- 客户详情 -->
    <el-drawer v-model="detailVisible" size="780px" :with-header="true">
      <template #header>
        <div class="drawer-head">
          <div>
            <div class="drawer-title">{{ detail?.name }}</div>
            <div class="muted mono">{{ detail?.customerNo }} · {{ detail?.type }} · {{ detail?.level }}</div>
          </div>
          <StatusTag :status="detail?.status" />
        </div>
      </template>
      <div v-if="detail" v-loading="detailLoading" class="detail-body">
        <div class="stat-strip">
          <div><span>累计运单</span><strong>{{ detail.stats?.waybillCount || 0 }} 票</strong></div>
          <div><span>本月运单</span><strong>{{ detail.stats?.monthWaybills || 0 }} 票</strong></div>
          <div><span>应收余额</span><strong class="danger-text">{{ formatMoney(detail.receivable) }}</strong></div>
          <div><span>信用额度</span><strong>{{ formatMoney(detail.creditLimit) }}</strong></div>
          <div><span>结算方式</span><strong>{{ detail.settleType }}</strong></div>
        </div>

        <div class="detail-actions">
          <el-button size="small" type="primary" plain @click="editFromDetail">编辑档案</el-button>
          <el-button size="small" @click="createWaybill">新建运单</el-button>
          <el-button size="small" @click="openContact()">添加联系人</el-button>
          <el-button size="small" @click="openAddress()">添加地址</el-button>
        </div>

        <el-tabs v-model="detailTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="客户类型">{{ detail.type }}</el-descriptions-item>
              <el-descriptions-item label="客户等级">{{ detail.level }}</el-descriptions-item>
              <el-descriptions-item label="联系人">{{ detail.contact }} · {{ detail.phone }}</el-descriptions-item>
              <el-descriptions-item label="客户来源">{{ detail.source }}</el-descriptions-item>
              <el-descriptions-item label="销售负责人">{{ detail.salesName }}</el-descriptions-item>
              <el-descriptions-item label="归属网点">{{ detail.orgName }}</el-descriptions-item>
              <el-descriptions-item label="纳税人识别号" :span="2">{{ detail.taxNo || '—' }}</el-descriptions-item>
              <el-descriptions-item label="地址" :span="2">{{ detail.address }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ detail.createdAt }}</el-descriptions-item>
              <el-descriptions-item label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
            </el-descriptions>
            <div class="mini-title mt-16">客户协议与信用</div>
            <div class="credit-bar">
              <div class="credit-labels">
                <span>信用额度使用情况</span>
                <span>{{ formatMoney(detail.receivable) }} / {{ formatMoney(detail.creditLimit) }}</span>
              </div>
              <el-progress :percentage="Math.min(100, usagePercent(detail))" :stroke-width="8" :status="usagePercent(detail) > 85 ? 'exception' : ''" />
            </div>
          </el-tab-pane>

          <el-tab-pane :label="`联系人 (${detail.contacts?.length || 0})`" name="contacts">
            <div class="tab-toolbar">
              <span class="muted">维护客户对接人，可设置主要联系人</span>
              <el-button size="small" type="primary" plain @click="openContact()">添加联系人</el-button>
            </div>
            <el-table :data="detail.contacts" size="small" stripe border>
              <el-table-column prop="name" label="姓名" width="100" />
              <el-table-column prop="title" label="职务" width="110" />
              <el-table-column prop="phone" label="电话" width="130" />
              <el-table-column prop="email" label="邮箱" min-width="160" />
              <el-table-column label="主要" width="70" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.primary" size="small" type="success" effect="light">主要</el-tag>
                  <span v-else class="muted">—</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="right">
                <template #default="{ row, $index }">
                  <el-button link type="primary" @click="openContact(row, $index)">编辑</el-button>
                  <el-button link type="danger" @click="removeContact($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane :label="`地址簿 (${detail.addresses?.length || 0})`" name="addresses">
            <div class="tab-toolbar">
              <span class="muted">常用发货 / 收货地址，下单时可直接选择</span>
              <el-button size="small" type="primary" plain @click="openAddress()">添加地址</el-button>
            </div>
            <div class="address-list">
              <div v-for="(addr, index) in detail.addresses" :key="addr.id" class="address-card">
                <div class="address-head">
                  <span class="addr-type">{{ addr.type }}</span>
                  <span class="strong">{{ addr.name }}</span>
                  <span class="muted">{{ addr.contact }} · {{ addr.phone }}</span>
                  <div class="addr-actions">
                    <el-button link type="primary" @click="openAddress(addr, index)">编辑</el-button>
                    <el-button link type="danger" @click="removeAddress(index)">删除</el-button>
                  </div>
                </div>
                <div class="address-detail">{{ addr.province }} {{ addr.city }} {{ addr.detail }}</div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane :label="`协议价 (${detail.agreements?.length || 0})`" name="agreements">
            <el-table :data="detail.agreements" size="small" stripe border>
              <el-table-column label="线路" min-width="140">
                <template #default="{ row }">{{ row.origin }} → {{ row.destination }}</template>
              </el-table-column>
              <el-table-column prop="vehicleType" label="车型" width="110" />
              <el-table-column label="协议价" width="110" align="right">
                <template #default="{ row }"><MoneyText :value="row.price" strong /></template>
              </el-table-column>
              <el-table-column label="有效期" width="180">
                <template #default="{ row }">{{ row.effectiveFrom }} ~ {{ row.effectiveTo }}</template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="90">
                <template #default="{ row }"><StatusTag :status="row.status" /></template>
              </el-table-column>
            </el-table>
            <div class="mt-12">
              <el-button size="small" @click="router.push('/tenant/agreements')">管理协议价</el-button>
            </div>
          </el-tab-pane>

          <el-tab-pane :label="`跟进记录 (${detail.followups?.length || 0})`" name="followups">
            <div class="followup-input">
              <el-input v-model="followupText" type="textarea" :rows="2" placeholder="记录本次回访内容，如：确认下月发货计划…" />
              <div class="followup-actions">
                <el-select v-model="followupType" style="width: 120px">
                  <el-option label="电话回访" value="电话回访" />
                  <el-option label="上门拜访" value="上门拜访" />
                  <el-option label="微信沟通" value="微信沟通" />
                </el-select>
                <el-button type="primary" :loading="followupSaving" @click="addFollowup">添加跟进</el-button>
              </div>
            </div>
            <el-timeline class="mt-12">
              <el-timeline-item
                v-for="f in detail.followups"
                :key="f.id"
                :timestamp="f.time"
                placement="top"
                :type="f.type === '系统提醒' ? 'warning' : 'primary'"
              >
                <div class="followup-item">
                  <div class="flex-between">
                    <span class="strong">{{ f.type }}</span>
                    <span class="muted">{{ f.operator }}</span>
                  </div>
                  <div class="followup-content">{{ f.content }}</div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>

    <!-- 联系人编辑 -->
    <el-dialog v-model="contactVisible" :title="contactEditing ? '编辑联系人' : '添加联系人'" width="440px">
      <el-form :model="contactForm" label-width="76px">
        <el-form-item label="姓名" required>
          <el-input v-model="contactForm.name" placeholder="联系人姓名" />
        </el-form-item>
        <el-form-item label="职务">
          <el-input v-model="contactForm.title" placeholder="如：物流经理" />
        </el-form-item>
        <el-form-item label="电话" required>
          <el-input v-model="contactForm.phone" maxlength="11" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="contactForm.email" placeholder="可选" />
        </el-form-item>
        <el-form-item label="主要联系人">
          <el-switch v-model="contactForm.primary" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="contactVisible = false">取消</el-button>
        <el-button type="primary" @click="saveContact">保存</el-button>
      </template>
    </el-dialog>

    <!-- 地址编辑 -->
    <el-dialog v-model="addressVisible" :title="addressEditing ? '编辑地址' : '添加地址'" width="500px">
      <el-form :model="addressForm" label-width="76px">
        <el-form-item label="地址名称" required>
          <el-input v-model="addressForm.name" placeholder="如：上海发货仓" />
        </el-form-item>
        <el-form-item label="地址类型">
          <el-radio-group v-model="addressForm.type">
            <el-radio-button value="发货">发货</el-radio-button>
            <el-radio-button value="收货">收货</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="联系人" required>
          <el-input v-model="addressForm.contact" placeholder="现场联系人" />
        </el-form-item>
        <el-form-item label="电话" required>
          <el-input v-model="addressForm.phone" maxlength="11" placeholder="联系电话" />
        </el-form-item>
        <el-form-item label="省份">
          <el-select v-model="addressForm.province" class="w-full">
            <el-option v-for="p in PROVINCES" :key="p" :label="p" :value="p" />
          </el-select>
        </el-form-item>
        <el-form-item label="城市">
          <el-select v-model="addressForm.city" class="w-full">
            <el-option v-for="c in CITIES" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细地址" required>
          <el-input v-model="addressForm.detail" type="textarea" :rows="2" placeholder="街道、门牌号、楼层" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import PageTable, { type TableColumn } from '@/components/PageTable.vue'
import StatusTag from '@/components/StatusTag.vue'
import MoneyText from '@/components/MoneyText.vue'
import UploadCard from '@/components/UploadCard.vue'
import { useTable } from '@/composables/useTable'
import {
  addCustomerAddress,
  addCustomerContact,
  createCustomer,
  createFollowup,
  deleteCustomer,
  getCustomerDetail,
  getCustomers,
  updateCustomer
} from '@/api/tenant'
import { CITIES, DICTS, PROVINCES } from '@/utils/dict'
import { downloadText, formatMoney, toCsv, today } from '@/utils/format'

const router = useRouter()
const { list, loading, total, page, size, query, search, reset, onPageChange, onSizeChange, load } = useTable(getCustomers)

const columns: TableColumn[] = [
  { prop: 'name', label: '客户名称', minWidth: 210, slot: 'name' },
  { prop: 'type', label: '类型', width: 92 },
  { prop: 'level', label: '等级', width: 92 },
  { prop: 'contact', label: '联系人', width: 112, slot: 'contact' },
  { prop: 'settleType', label: '结算', width: 76 },
  { prop: 'receivable', label: '应收余额', width: 112, align: 'right', slot: 'receivable' },
  { prop: 'creditLimit', label: '信用额度', width: 120, align: 'right', slot: 'creditLimit' },
  { prop: 'status', label: '状态', width: 78, slot: 'status' }
]

function usagePercent(row: any): number {
  if (!row?.creditLimit) return 0
  return Math.round((Number(row.receivable || 0) / Number(row.creditLimit)) * 100)
}

function resetFilters(): void {
  reset({ keyword: '', type: '', settleType: '', level: '', salesName: '', status: '' })
}

/* ---------------- 新增 / 编辑 ---------------- */
const formVisible = ref(false)
const saving = ref(false)
const editing = ref<any>(null)
const formRef = ref<FormInstance>()
const attachments = ref<string[]>([])
const form = reactive({
  name: '',
  type: '企业客户',
  level: '普通客户',
  contact: '',
  phone: '',
  settleType: '月结',
  creditLimit: 0,
  source: '销售开发',
  salesName: '',
  orgName: '上海总部',
  taxNo: '',
  address: '',
  remark: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择客户类型', trigger: 'change' }],
  contact: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  settleType: [{ required: true, message: '请选择结算方式', trigger: 'change' }]
}

function openForm(row?: any): void {
  editing.value = row || null
  Object.assign(form, {
    name: row?.name || '',
    type: row?.type || '企业客户',
    level: row?.level || '普通客户',
    contact: row?.contact || '',
    phone: row?.phone || '',
    settleType: row?.settleType || '月结',
    creditLimit: row?.creditLimit ?? 0,
    source: row?.source || '销售开发',
    salesName: row?.salesName || '',
    orgName: row?.orgName || '上海总部',
    taxNo: row?.taxNo || '',
    address: row?.address || '',
    remark: row?.remark || ''
  })
  attachments.value = []
  formVisible.value = true
}

async function submitForm(): Promise<void> {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editing.value) {
      await updateCustomer(editing.value.id, { ...form })
      ElMessage.success('客户已更新')
    } else {
      await createCustomer({ ...form, attachments: attachments.value })
      ElMessage.success('客户已创建')
    }
    formVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

function removeCustomer(row: any): void {
  const extra = row.receivable > 0 ? `该客户尚有应收 ${formatMoney(row.receivable)}，` : ''
  ElMessageBox.confirm(`${extra}确认删除客户「${row.name}」？历史运单将保留归档。`, '删除客户', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger',
    type: 'warning'
  })
    .then(async () => {
      await deleteCustomer(row.id)
      ElMessage.success('客户已删除')
      load()
    })
    .catch(() => undefined)
}

/* ---------------- 详情 ---------------- */
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailTab = ref('basic')
const detail = ref<any>(null)

async function openDetail(row: any): Promise<void> {
  detailVisible.value = true
  detailTab.value = 'basic'
  detailLoading.value = true
  try {
    const res: any = await getCustomerDetail(row.id)
    detail.value = res?.customer || res
    if (!detail.value.contacts) detail.value.contacts = []
    if (!detail.value.addresses) detail.value.addresses = []
    if (!detail.value.agreements) detail.value.agreements = []
    if (!detail.value.followups) detail.value.followups = []
  } catch {
    detail.value = { ...row, contacts: [], addresses: [], agreements: [], followups: [], stats: {} }
  } finally {
    detailLoading.value = false
  }
}

function editFromDetail(): void {
  const row = { ...detail.value }
  detailVisible.value = false
  openForm(row)
}

function createWaybill(): void {
  router.push({ path: '/tenant/waybills/create', query: { customerId: detail.value?.id, customerName: detail.value?.name } })
}

/* ---------------- 联系人 ---------------- */
const contactVisible = ref(false)
const contactEditing = ref<number | null>(null)
const contactForm = reactive({ name: '', title: '', phone: '', email: '', primary: false })

function openContact(row?: any, index?: number): void {
  contactEditing.value = row ? index ?? null : null
  Object.assign(contactForm, {
    name: row?.name || '',
    title: row?.title || '',
    phone: row?.phone || '',
    email: row?.email || '',
    primary: row?.primary || false
  })
  contactVisible.value = true
}

async function saveContact(): Promise<void> {
  if (!contactForm.name.trim() || !contactForm.phone.trim()) {
    ElMessage.warning('姓名与电话为必填项')
    return
  }
  await addCustomerContact(detail.value.id, { ...contactForm })
  if (contactEditing.value !== null) {
    Object.assign(detail.value.contacts[contactEditing.value], contactForm)
  } else {
    detail.value.contacts.push({ id: Date.now(), ...contactForm })
  }
  ElMessage.success('联系人已保存')
  contactVisible.value = false
}

function removeContact(index: number): void {
  ElMessageBox.confirm('确认删除该联系人？', '删除联系人', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
    .then(() => {
      detail.value.contacts.splice(index, 1)
      ElMessage.success('已删除')
    })
    .catch(() => undefined)
}

/* ---------------- 地址 ---------------- */
const addressVisible = ref(false)
const addressEditing = ref<number | null>(null)
const addressForm = reactive({ name: '', type: '发货', contact: '', phone: '', province: '上海市', city: '上海', detail: '' })

function openAddress(row?: any, index?: number): void {
  addressEditing.value = row ? index ?? null : null
  Object.assign(addressForm, {
    name: row?.name || '',
    type: row?.type || '发货',
    contact: row?.contact || '',
    phone: row?.phone || '',
    province: row?.province || '上海市',
    city: row?.city || '上海',
    detail: row?.detail || ''
  })
  addressVisible.value = true
}

async function saveAddress(): Promise<void> {
  if (!addressForm.name.trim() || !addressForm.detail.trim()) {
    ElMessage.warning('地址名称与详细地址为必填项')
    return
  }
  await addCustomerAddress(detail.value.id, { ...addressForm })
  if (addressEditing.value !== null) {
    Object.assign(detail.value.addresses[addressEditing.value], addressForm)
  } else {
    detail.value.addresses.push({ id: Date.now(), ...addressForm })
  }
  ElMessage.success('地址已保存')
  addressVisible.value = false
}

function removeAddress(index: number): void {
  ElMessageBox.confirm('确认删除该地址？', '删除地址', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
    .then(() => {
      detail.value.addresses.splice(index, 1)
      ElMessage.success('已删除')
    })
    .catch(() => undefined)
}

/* ---------------- 跟进 ---------------- */
const followupText = ref('')
const followupType = ref('电话回访')
const followupSaving = ref(false)

async function addFollowup(): Promise<void> {
  if (!followupText.value.trim()) {
    ElMessage.warning('请输入跟进内容')
    return
  }
  followupSaving.value = true
  try {
    await createFollowup({ customerId: detail.value.id, type: followupType.value, content: followupText.value })
    detail.value.followups.unshift({
      id: Date.now(),
      time: new Date().toISOString().slice(0, 19).replace('T', ' '),
      type: followupType.value,
      content: followupText.value,
      operator: '当前账号'
    })
    followupText.value = ''
    ElMessage.success('跟进记录已添加')
  } finally {
    followupSaving.value = false
  }
}

function exportCustomers(): void {
  const headers = ['客户编号', '客户名称', '类型', '等级', '联系人', '电话', '结算方式', '应收', '信用额度', '状态']
  const rows = list.value.map((r: any) => [r.customerNo, r.name, r.type, r.level, r.contact, r.phone, r.settleType, r.receivable, r.creditLimit, r.status])
  downloadText(`客户档案-${today()}.csv`, toCsv(headers, rows), 'text/csv;charset=utf-8')
  ElMessage.success('导出成功')
}
</script>

<style scoped lang="scss">
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
  .span-2 {
    grid-column: span 2;
  }
}
.credit-tip {
  font-size: 10px;
}
.drawer-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  padding-right: 10px;
}
.drawer-title {
  font-size: 16px;
  font-weight: 600;
}
.stat-strip {
  display: flex;
  background: #f7f9fc;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 12px;
  div {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 3px;
    border-right: 1px solid #e8eef7;
    padding: 0 10px;
    &:last-child {
      border-right: none;
    }
    span {
      font-size: 10.5px;
      color: var(--text-muted);
    }
    strong {
      font-size: 13px;
    }
  }
}
.detail-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.mini-title {
  font-size: 12.5px;
  font-weight: 600;
  margin-bottom: 8px;
}
.credit-bar {
  background: #f7f9fc;
  border-radius: 6px;
  padding: 10px 12px;
}
.credit-labels {
  display: flex;
  justify-content: space-between;
  font-size: 11.5px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}
.tab-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 11.5px;
}
.address-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.address-card {
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 10px 12px;
  background: #fff;
}
.address-head {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
}
.addr-type {
  font-size: 10px;
  background: #eef4ff;
  color: var(--brand);
  padding: 1px 6px;
  border-radius: 3px;
  flex: 0 0 auto;
}
.addr-actions {
  margin-left: auto;
  display: flex;
}
.address-detail {
  font-size: 11.5px;
  color: var(--text-secondary);
  margin-top: 5px;
  padding-left: 2px;
}
.followup-input {
  background: #f7f9fc;
  border-radius: 6px;
  padding: 10px;
}
.followup-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}
.followup-item {
  background: #f9fbfe;
  border-radius: 6px;
  padding: 9px 12px;
}
.followup-content {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
  line-height: 1.7;
}
</style>
