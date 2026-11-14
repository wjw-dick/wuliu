<template>
  <div class="page">
    <div class="page-title-row">
      <div>
        <div class="page-title">租户管理</div>
        <div class="page-subtitle">开通、续期、停用租户，掌握每个企业的套餐与席位使用情况</div>
      </div>
      <div class="flex gap-8">
        <el-button @click="exportList">
          <el-icon><Download /></el-icon> 导出
        </el-button>
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon> 开通租户
        </el-button>
      </div>
    </div>

    <div class="overview-strip">
      <div class="ov-item"><span>租户总数</span><strong>{{ total }}</strong></div>
      <div class="ov-item"><span>正常运行</span><strong class="success-text">{{ statusCount('正常') }}</strong></div>
      <div class="ov-item"><span>试用中</span><strong class="warn-text">{{ statusCount('试用中') }}</strong></div>
      <div class="ov-item"><span>欠费 / 到期</span><strong class="danger-text">{{ statusCount('欠费') + statusCount('已到期') }}</strong></div>
      <div class="ov-item"><span>席位总数</span><strong>{{ list.reduce((s: number, r: any) => s + Number(r.seats || 0), 0) }}</strong></div>
    </div>

    <PageTable
      :data="list"
      :loading="loading"
      :total="total"
      :page="page"
      :size="size"
      :columns="columns"
      title="租户列表"
      empty-action-text="开通第一个租户"
      action-width="150"
      @search="search"
      @reset="resetFilters"
      @page-change="onPageChange"
      @size-change="onSizeChange"
      @empty-action="openCreate"
    >
      <template #filter>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="企业名称 / 联系人 / 电话" clearable @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="租户状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable>
            <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="套餐">
          <el-select v-model="query.planName" placeholder="全部套餐" clearable>
            <el-option v-for="p in planOptions" :key="p.name" :label="p.name" :value="p.name" />
          </el-select>
        </el-form-item>
      </template>
      <template #filter-advanced>
        <el-form-item label="所在地区">
          <el-select v-model="query.region" placeholder="全部地区" clearable>
            <el-option v-for="r in PROVINCES" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="query.contact" placeholder="联系人姓名" clearable />
        </el-form-item>
      </template>

      <template #name="{ row }">
        <span class="link-text strong" @click="openDetail(row)">{{ row.name }}</span>
        <div class="muted mono">{{ row.tenantNo }}</div>
      </template>
      <template #contact="{ row }">
        <div>{{ row.contact }}</div>
        <div class="muted">{{ row.phone }}</div>
      </template>
      <template #seats="{ row }">
        <div class="seat-cell">
          <span>{{ row.usedSeats }} / {{ row.seats }}</span>
          <el-progress :percentage="Math.min(100, Math.round((row.usedSeats / Math.max(1, row.seats)) * 100))" :show-text="false" :stroke-width="4" />
        </div>
      </template>
      <template #expireAt="{ row }">
        <span :class="daysLeft(row.expireAt) <= 30 ? 'danger-text' : ''">{{ row.expireAt }}</span>
        <div class="muted">{{ daysLeft(row.expireAt) >= 0 ? `剩余 ${daysLeft(row.expireAt)} 天` : '已到期' }}</div>
      </template>
      <template #status="{ row }">
        <StatusTag :status="row.status" />
      </template>
      <template #actions="{ row }">
        <el-button link type="primary" @click="openDetail(row)">详情</el-button>
        <el-button link type="primary" @click="openRenew(row)">续期</el-button>
        <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, row)">
          <el-button link type="primary">
            更多<el-icon><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-if="row.status === '已停用'" command="enable">启用租户</el-dropdown-item>
              <el-dropdown-item v-else command="suspend">停用租户</el-dropdown-item>
              <el-dropdown-item divided command="delete">删除归档</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
    </PageTable>

    <!-- 开通租户 -->
    <el-dialog v-model="createVisible" title="开通租户" width="780px" :close-on-click-modal="false" top="4vh">
      <el-form ref="createRef" :model="form" :rules="formRules" label-width="94px">
        <div class="section-title"><el-icon><OfficeBuilding /></el-icon> 企业信息</div>
        <div class="form-grid">
          <el-form-item label="企业名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入营业执照企业全称" />
          </el-form-item>
          <el-form-item label="统一信用代码" prop="creditCode">
            <el-input v-model="form.creditCode" placeholder="18 位统一社会信用代码" />
          </el-form-item>
          <el-form-item label="所属行业" prop="industry">
            <el-select v-model="form.industry" class="w-full">
              <el-option v-for="o in industryOptions" :key="o" :label="o" :value="o" />
            </el-select>
          </el-form-item>
          <el-form-item label="所在地区" prop="region">
            <el-select v-model="form.region" class="w-full">
              <el-option v-for="r in PROVINCES" :key="r" :label="r" :value="r" />
            </el-select>
          </el-form-item>
          <el-form-item label="联系人" prop="contact">
            <el-input v-model="form.contact" placeholder="管理员姓名" />
          </el-form-item>
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="form.phone" placeholder="11 位手机号" maxlength="11" />
          </el-form-item>
          <el-form-item label="详细地址" class="span-2">
            <el-input v-model="form.address" placeholder="请输入企业办公地址" />
          </el-form-item>
        </div>

        <div class="section-title mt-8"><el-icon><UploadFilled /></el-icon> 资质证照</div>
        <UploadCard v-model="licenseImages" title="营业执照 / 道路运输经营许可证" tip="请上传加盖公章的证照扫描件，支持多图排序" :max="5" />

        <div class="section-title mt-16"><el-icon><Tickets /></el-icon> 套餐与席位</div>
        <div class="form-grid">
          <el-form-item label="开通套餐" prop="planId">
            <el-select v-model="form.planId" class="w-full" placeholder="请选择套餐">
              <el-option v-for="p in plans" :key="p.id" :label="`${p.name} · ¥${p.price}/年`" :value="p.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="席位数量" prop="seats">
            <el-input-number v-model="form.seats" :min="1" :max="500" class="w-full" />
          </el-form-item>
          <el-form-item label="备注" class="span-2">
            <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="内部备注，如来源渠道、特殊约定" />
          </el-form-item>
        </div>
        <el-alert type="info" :closable="false" show-icon title="开通后将自动发送账号与初始密码短信，资质审核通过前租户可登录但部分功能受限。" />
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="submitCreate">确认开通</el-button>
      </template>
    </el-dialog>

    <!-- 续期 -->
    <el-dialog v-model="renewVisible" title="租户续期" width="420px">
      <div class="renew-box">
        <div class="strong">{{ renewTarget?.name }}</div>
        <div class="muted">当前到期日：{{ renewTarget?.expireAt }}</div>
      </div>
      <el-form label-width="84px">
        <el-form-item label="续期时长">
          <el-radio-group v-model="renewMonths">
            <el-radio-button v-for="m in [3, 6, 12, 24]" :key="m" :value="m">{{ m }} 个月</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="预计到期">
          <span class="strong">{{ predictedExpire }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renewVisible = false">取消</el-button>
        <el-button type="primary" :loading="renewLoading" @click="submitRenew">确认续期</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-drawer v-model="detailVisible" size="660px" :with-header="true">
      <template #header>
        <div class="drawer-head">
          <div>
            <div class="drawer-title">{{ detail?.name }}</div>
            <div class="muted mono">{{ detail?.tenantNo }} · {{ detail?.industry }}</div>
          </div>
          <StatusTag :status="detail?.status" />
        </div>
      </template>
      <div v-if="detail" class="detail-body">
        <div class="detail-actions">
          <el-button size="small" type="primary" plain @click="openRenew(detail)">续期</el-button>
          <el-button size="small" @click="handleCommand(detail.status === '已停用' ? 'enable' : 'suspend', detail)">
            {{ detail.status === '已停用' ? '启用' : '停用' }}
          </el-button>
          <el-button size="small" @click="router.push('/platform/bills')">查看账单</el-button>
        </div>

        <el-tabs v-model="detailTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="企业名称" :span="2">{{ detail.name }}</el-descriptions-item>
              <el-descriptions-item label="信用代码">{{ detail.creditCode }}</el-descriptions-item>
              <el-descriptions-item label="所属行业">{{ detail.industry }}</el-descriptions-item>
              <el-descriptions-item label="所在地区">{{ detail.region }}</el-descriptions-item>
              <el-descriptions-item label="联系人">{{ detail.contact }} · {{ detail.phone }}</el-descriptions-item>
              <el-descriptions-item label="详细地址" :span="2">{{ detail.address }}</el-descriptions-item>
              <el-descriptions-item label="开通套餐">{{ detail.planName }}</el-descriptions-item>
              <el-descriptions-item label="席位">{{ detail.usedSeats }} / {{ detail.seats }}</el-descriptions-item>
              <el-descriptions-item label="开通时间">{{ detail.createdAt }}</el-descriptions-item>
              <el-descriptions-item label="到期时间">{{ detail.expireAt }}</el-descriptions-item>
              <el-descriptions-item label="账户余额">
                <MoneyText :value="detail.balance" :colored="true" />
              </el-descriptions-item>
              <el-descriptions-item label="累计运单">{{ detail.waybillCount }} 票</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <el-tab-pane label="资质证照" name="license">
            <div class="license-list">
              <div class="license-item" v-for="lic in detailLicenses" :key="lic.name">
                <div class="license-preview">
                  <el-icon><Document /></el-icon>
                  <span>{{ lic.name }}</span>
                </div>
                <div class="license-info">
                  <div class="strong">{{ lic.name }}</div>
                  <div class="muted">证照编号：{{ lic.no }}</div>
                  <div class="muted">有效期至：{{ lic.expireAt }}</div>
                  <StatusTag :status="lic.status" />
                </div>
              </div>
            </div>
            <el-alert class="mt-12" type="info" :closable="false" show-icon title="证照原件由平台资质审核统一管理，可前往「资质审核」查看与审核。" />
          </el-tab-pane>

          <el-tab-pane label="账单记录" name="bills">
            <el-table :data="tenantBills" size="small" stripe>
              <el-table-column prop="billNo" label="账单号" min-width="120" />
              <el-table-column prop="type" label="类型" width="90" />
              <el-table-column label="金额" width="110" align="right">
                <template #default="{ row }"><MoneyText :value="row.amount" /></template>
              </el-table-column>
              <el-table-column label="状态" width="90">
                <template #default="{ row }"><StatusTag :status="row.status" /></template>
              </el-table-column>
              <el-table-column prop="dueDate" label="到期日" width="100" />
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="使用情况" name="usage">
            <div class="usage-block">
              <div class="usage-row">
                <span>席位使用</span><span>{{ detail.usedSeats }} / {{ detail.seats }}</span>
              </div>
              <el-progress :percentage="Math.round((detail.usedSeats / Math.max(1, detail.seats)) * 100)" :stroke-width="8" />
            </div>
            <div class="usage-block">
              <div class="usage-row"><span>存储空间</span><span>67 / 100 GB</span></div>
              <el-progress :percentage="67" :stroke-width="8" color="#0ea5e9" />
            </div>
            <div class="usage-block">
              <div class="usage-row"><span>短信条数</span><span>4,280 / 10,000</span></div>
              <el-progress :percentage="43" :stroke-width="8" color="#16a34a" />
            </div>
            <div class="usage-grid">
              <div><span>本月运单</span><strong>168 票</strong></div>
              <div><span>本月营收</span><strong>¥ 82,600</strong></div>
              <div><span>异常率</span><strong>2.8%</strong></div>
              <div><span>活跃账号</span><strong>{{ detail.usedSeats }} 个</strong></div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>

    <ConfirmDanger
      v-model="dangerVisible"
      :title="dangerMode === 'delete' ? '删除租户' : '停用租户'"
      :content="dangerMode === 'delete' ? '删除后租户将归档，数据保留 90 天，期间可申请恢复。' : '停用后该租户所有账号将无法登录，业务数据保留。'"
      :confirm-text="dangerMode === 'delete' ? '确认删除' : '确认停用'"
      :reason-label="dangerMode === 'delete' ? '删除原因' : '停用原因'"
      :presets="dangerMode === 'delete' ? [] : ['长期欠费', '违规经营', '客户申请停用']"
      :show-button="false"
      :loading="dangerLoading"
      @confirm="confirmDanger"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import PageTable, { type TableColumn } from '@/components/PageTable.vue'
import StatusTag from '@/components/StatusTag.vue'
import MoneyText from '@/components/MoneyText.vue'
import UploadCard from '@/components/UploadCard.vue'
import ConfirmDanger from '@/components/ConfirmDanger.vue'
import { useTable } from '@/composables/useTable'
import {
  createTenant,
  deleteTenant,
  enableTenant,
  getBills,
  getPlans,
  getTenants,
  renewTenant,
  suspendTenant
} from '@/api/platform'
import { DICTS, PROVINCES } from '@/utils/dict'
import { daysFromNow, downloadText, toCsv } from '@/utils/format'

const router = useRouter()
const route = useRoute()
const { list, loading, total, page, size, query, search, reset, onPageChange, onSizeChange, load } = useTable(getTenants)

const statusOptions = DICTS['租户状态'].map((v) => ({ label: v, value: v }))
const industryOptions = DICTS['行业分类']

const plans = ref<any[]>([])
const planOptions = computed(() => plans.value)

const columns: TableColumn[] = [
  { prop: 'name', label: '企业名称', minWidth: 190, slot: 'name' },
  { prop: 'industry', label: '行业', width: 92 },
  { prop: 'contact', label: '联系人', width: 112, slot: 'contact' },
  { prop: 'planName', label: '套餐', width: 82 },
  { prop: 'seats', label: '席位', width: 96, slot: 'seats' },
  { prop: 'expireAt', label: '服务到期', width: 112, slot: 'expireAt' },
  { prop: 'status', label: '状态', width: 88, slot: 'status' },
  { prop: 'createdAt', label: '开通时间', width: 106 }
]

function statusCount(status: string): number {
  return list.value.filter((r: any) => r.status === status).length
}

function daysLeft(date: string): number {
  if (!date) return 0
  return Math.ceil((new Date(date).getTime() - Date.now()) / 86400000)
}

function resetFilters(): void {
  reset({ keyword: '', status: '', planName: '', region: '', contact: '' })
}

async function loadPlans(): Promise<void> {
  try {
    const res: any = await getPlans({ page: 1, size: 50 })
    plans.value = Array.isArray(res) ? res : res?.list || []
  } catch {
    plans.value = []
  }
}

/* ---------------- 开通 ---------------- */
const createVisible = ref(false)
const createLoading = ref(false)
const createRef = ref<FormInstance>()
const licenseImages = ref<string[]>([])
const form = reactive({
  name: '',
  creditCode: '',
  industry: '制造业',
  region: '上海市',
  contact: '',
  phone: '',
  address: '',
  planId: undefined as number | undefined,
  seats: 10,
  remark: ''
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  creditCode: [
    { required: true, message: '请输入统一信用代码', trigger: 'blur' },
    { pattern: /^[0-9A-Z]{18}$/, message: '请输入 18 位统一社会信用代码', trigger: 'blur' }
  ],
  industry: [{ required: true, message: '请选择行业', trigger: 'change' }],
  region: [{ required: true, message: '请选择地区', trigger: 'change' }],
  contact: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  planId: [{ required: true, message: '请选择开通套餐', trigger: 'change' }],
  seats: [{ required: true, message: '请输入席位数量', trigger: 'change' }]
}

function openCreate(): void {
  Object.assign(form, {
    name: '',
    creditCode: '',
    industry: '制造业',
    region: '上海市',
    contact: '',
    phone: '',
    address: '',
    planId: plans.value[0]?.id,
    seats: 10,
    remark: ''
  })
  licenseImages.value = []
  createVisible.value = true
}

async function submitCreate(): Promise<void> {
  if (!createRef.value) return
  const valid = await createRef.value.validate().catch(() => false)
  if (!valid) return
  createLoading.value = true
  try {
    await createTenant({
      ...form,
      planName: plans.value.find((p) => p.id === form.planId)?.name,
      licenseImages: licenseImages.value
    } as any)
    ElMessage.success('租户开通成功，已发送账号短信')
    createVisible.value = false
    load()
  } finally {
    createLoading.value = false
  }
}

/* ---------------- 详情 ---------------- */
const detailVisible = ref(false)
const detailTab = ref('basic')
const detail = ref<any>(null)
const tenantBills = ref<any[]>([])

const detailLicenses = computed(() => {
  if (!detail.value) return []
  return [
    { name: '营业执照', no: detail.value.creditCode, expireAt: '长期有效', status: detail.value.licenseStatus },
    { name: '道路运输经营许可证', no: `DL${detail.value.tenantNo}`, expireAt: detail.value.expireAt, status: detail.value.licenseStatus === '待审核' ? '待审核' : '已通过' },
    { name: '法人身份证', no: `3101********${String(detail.value.id).slice(-4)}`, expireAt: '2029-12-31', status: '已通过' }
  ]
})

async function openDetail(row: any): Promise<void> {
  detail.value = row
  detailTab.value = 'basic'
  detailVisible.value = true
  try {
    const res: any = await getBills({ keyword: row.name, page: 1, size: 8 })
    tenantBills.value = Array.isArray(res) ? res : res?.list || []
  } catch {
    tenantBills.value = []
  }
}

/* ---------------- 续期 ---------------- */
const renewVisible = ref(false)
const renewLoading = ref(false)
const renewTarget = ref<any>(null)
const renewMonths = ref(12)
const predictedExpire = computed(() => {
  const base = renewTarget.value?.expireAt ? new Date(renewTarget.value.expireAt) : new Date()
  const now = new Date()
  const start = base.getTime() > now.getTime() ? base : now
  const d = new Date(start)
  d.setMonth(d.getMonth() + renewMonths.value)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
})

function openRenew(row: any): void {
  renewTarget.value = row
  renewMonths.value = 12
  renewVisible.value = true
}

async function submitRenew(): Promise<void> {
  if (!renewTarget.value) return
  renewLoading.value = true
  try {
    await renewTenant(renewTarget.value.id, renewMonths.value)
    ElMessage.success('续期成功')
    renewVisible.value = false
    load()
  } finally {
    renewLoading.value = false
  }
}

/* ---------------- 停用 / 启用 / 删除 ---------------- */
const dangerVisible = ref(false)
const dangerLoading = ref(false)
const dangerMode = ref<'suspend' | 'delete'>('suspend')
const dangerTarget = ref<any>(null)

function handleCommand(command: string, row: any): void {
  if (command === 'enable') {
    ElMessageBox.confirm(`确认启用「${row.name}」？启用后账号可正常登录。`, '启用租户', {
      confirmButtonText: '确认启用',
      cancelButtonText: '取消',
      type: 'info'
    })
      .then(async () => {
        await enableTenant(row.id)
        ElMessage.success('租户已启用')
        load()
      })
      .catch(() => undefined)
  } else {
    dangerMode.value = command === 'delete' ? 'delete' : 'suspend'
    dangerTarget.value = row
    dangerVisible.value = true
  }
}

async function confirmDanger(reason: string): Promise<void> {
  if (!dangerTarget.value) return
  dangerLoading.value = true
  try {
    if (dangerMode.value === 'delete') {
      await deleteTenant(dangerTarget.value.id)
      ElMessage.success('租户已删除归档')
    } else {
      await suspendTenant(dangerTarget.value.id, reason)
      ElMessage.success('租户已停用')
    }
    dangerVisible.value = false
    load()
  } finally {
    dangerLoading.value = false
  }
}

/* ---------------- 导出 ---------------- */
function exportList(): void {
  const headers = ['租户编号', '企业名称', '行业', '联系人', '电话', '套餐', '席位', '到期日', '状态']
  const rows = list.value.map((r: any) => [
    r.tenantNo,
    r.name,
    r.industry,
    r.contact,
    r.phone,
    r.planName,
    `${r.usedSeats}/${r.seats}`,
    r.expireAt,
    r.status
  ])
  downloadText(`租户列表-${daysFromNow(0)}.csv`, toCsv(headers, rows), 'text/csv;charset=utf-8')
  ElMessage.success('导出成功')
}

onMounted(async () => {
  await loadPlans()
  if (route.query.create) openCreate()
})
</script>

<style scoped lang="scss">
.overview-strip {
  display: flex;
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: var(--radius);
  box-shadow: var(--shadow-card);
  padding: 10px 16px;
  margin-bottom: 10px;
  gap: 0;
}
.ov-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 0 14px;
  border-right: 1px solid #f0f3f8;
  &:last-child {
    border-right: none;
  }
  span {
    font-size: 11px;
    color: var(--text-muted);
  }
  strong {
    font-size: 18px;
    font-family: 'DIN Alternate', Consolas, sans-serif;
    color: var(--text-main);
  }
}
.seat-cell {
  font-size: 11.5px;
  min-width: 68px;
  .el-progress {
    margin-top: 3px;
  }
}
.section-title {
  font-size: 13px;
  padding-left: 8px;
  border-left: 3px solid var(--brand);
  margin: 4px 0 14px;
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
  .span-2 {
    grid-column: span 2;
  }
}
.renew-box {
  background: #f7f9fc;
  border-radius: 6px;
  padding: 10px 12px;
  margin-bottom: 14px;
  display: flex;
  flex-direction: column;
  gap: 3px;
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
.detail-body {
  padding-bottom: 20px;
}
.detail-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}
.license-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.license-item {
  display: flex;
  gap: 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 10px;
}
.license-preview {
  width: 100px;
  height: 74px;
  border-radius: 4px;
  background: linear-gradient(135deg, #eef4ff, #dfeaff);
  color: var(--brand);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  font-size: 10px;
  flex: 0 0 100px;
  .el-icon {
    font-size: 22px;
  }
}
.license-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  min-width: 0;
}
.usage-block {
  margin-bottom: 16px;
}
.usage-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}
.usage-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin-top: 18px;
  div {
    background: #f7f9fc;
    border-radius: 6px;
    padding: 10px;
    display: flex;
    flex-direction: column;
    gap: 4px;
    span {
      font-size: 11px;
      color: var(--text-muted);
    }
    strong {
      font-size: 15px;
    }
  }
}
</style>
