<template>
  <div class="page">
    <div class="page-title-row">
      <div>
        <div class="page-title">套餐管理</div>
        <div class="page-subtitle">配置席位上限、存储额度、短信条数与功能开关，套餐变更实时同步开通页</div>
      </div>
      <el-button type="primary" @click="openForm()">
        <el-icon><Plus /></el-icon> 新增套餐
      </el-button>
    </div>

    <div v-loading="loading" class="plan-grid">
      <div v-for="plan in plans" :key="plan.id" class="plan-card" :class="{ featured: plan.name === '专业版' }">
        <div v-if="plan.name === '专业版'" class="ribbon">推荐</div>
        <div class="plan-head">
          <div>
            <div class="plan-name">{{ plan.name }}</div>
            <div class="plan-desc">{{ plan.remark }}</div>
          </div>
          <StatusTag :status="plan.status || '正常'" />
        </div>
        <div class="plan-price">
          <span class="currency">¥</span>
          <span class="num">{{ formatNumber(plan.price) }}</span>
          <span class="period">/ {{ plan.months || 12 }} 个月</span>
        </div>
        <div class="plan-specs">
          <div><el-icon><User /></el-icon><span>席位上限</span><strong>{{ plan.seatLimit }} 个</strong></div>
          <div><el-icon><FolderOpened /></el-icon><span>存储额度</span><strong>{{ plan.storageGb }} GB</strong></div>
          <div><el-icon><ChatDotRound /></el-icon><span>短信条数</span><strong>{{ formatNumber(plan.smsCount) }} 条</strong></div>
        </div>
        <div class="plan-features">
          <span v-for="f in plan.features" :key="f"><el-icon><Select /></el-icon>{{ f }}</span>
        </div>
        <div class="plan-footer">
          <el-button size="small" @click="openForm(plan)">编辑</el-button>
          <el-button size="small" type="danger" plain @click="removePlan(plan)">删除</el-button>
        </div>
      </div>

      <div class="plan-card add-card" @click="openForm()">
        <el-icon><Plus /></el-icon>
        <span>新增套餐</span>
        <small>为不同规模客户配置差异化能力</small>
      </div>
    </div>

    <el-card shadow="never" class="mt-12">
      <template #header>
        <div class="flex-between">
          <span>席位增购记录</span>
          <span class="muted">近 30 天</span>
        </div>
      </template>
      <el-table :data="seatRecords" size="small" stripe>
        <el-table-column prop="no" label="增购单号" min-width="140" />
        <el-table-column prop="tenantName" label="租户" min-width="180" />
        <el-table-column prop="seats" label="增购席位" width="100" align="center" />
        <el-table-column label="金额" width="120" align="right">
          <template #default="{ row }"><MoneyText :value="row.amount" /></template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVisible" :title="editing ? '编辑套餐' : '新增套餐'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <div class="form-grid">
          <el-form-item label="套餐名称" prop="name">
            <el-input v-model="form.name" placeholder="如：专业版" />
          </el-form-item>
          <el-form-item label="套餐类型" prop="type">
            <el-select v-model="form.type" class="w-full">
              <el-option v-for="t in DICTS['套餐类型']" :key="t" :label="t" :value="t" />
            </el-select>
          </el-form-item>
          <el-form-item label="年费价格" prop="price">
            <el-input-number v-model="form.price" :min="0" :step="500" class="w-full" />
          </el-form-item>
          <el-form-item label="服务月数" prop="months">
            <el-input-number v-model="form.months" :min="1" :max="60" class="w-full" />
          </el-form-item>
          <el-form-item label="席位上限" prop="seatLimit">
            <el-input-number v-model="form.seatLimit" :min="1" :max="1000" class="w-full" />
          </el-form-item>
          <el-form-item label="存储额度" prop="storageGb">
            <el-input-number v-model="form.storageGb" :min="1" :max="10240" class="w-full" />
          </el-form-item>
          <el-form-item label="短信条数" prop="smsCount">
            <el-input-number v-model="form.smsCount" :min="0" :step="1000" class="w-full" />
          </el-form-item>
          <el-form-item label="排序" prop="sort">
            <el-input-number v-model="form.sort" :min="1" :max="99" class="w-full" />
          </el-form-item>
        </div>
        <el-form-item label="功能开关">
          <el-select v-model="form.features" multiple filterable allow-create default-first-option class="w-full" placeholder="选择或输入功能名称">
            <el-option v-for="f in featureOptions" :key="f" :label="f" :value="f" />
          </el-select>
        </el-form-item>
        <el-form-item label="套餐状态">
          <el-radio-group v-model="form.status">
            <el-radio-button value="正常">上架</el-radio-button>
            <el-radio-button value="停用">下架</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="套餐适用客户说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存套餐</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import MoneyText from '@/components/MoneyText.vue'
import { createPlan, deletePlan, getPlans, getSeats, updatePlan } from '@/api/platform'
import { DICTS } from '@/utils/dict'
import { formatNumber } from '@/utils/format'

const loading = ref(false)
const plans = ref<any[]>([])
const seatRecords = ref<any[]>([])
const featureOptions = ['运单管理', '客户档案', '基础报表', '调度看板', '财务对账', '客户门户', '轨迹回放', '理赔管理', '开放接口', '专属部署', 'SSO 单点登录', '专属客户成功']

const formVisible = ref(false)
const saving = ref(false)
const editing = ref<any>(null)
const formRef = ref<FormInstance>()
const form = reactive({
  name: '',
  type: '标准版',
  price: 6999,
  months: 12,
  seatLimit: 30,
  storageGb: 100,
  smsCount: 10000,
  sort: 1,
  status: '正常',
  features: [] as string[],
  remark: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择套餐类型', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'change' }],
  seatLimit: [{ required: true, message: '请输入席位上限', trigger: 'change' }]
}

async function loadPlans(): Promise<void> {
  loading.value = true
  try {
    const res: any = await getPlans({ page: 1, size: 50 })
    plans.value = Array.isArray(res) ? res : res?.list || []
  } catch {
    plans.value = []
  } finally {
    loading.value = false
  }
}

async function loadSeats(): Promise<void> {
  try {
    const res: any = await getSeats({ page: 1, size: 8 })
    seatRecords.value = Array.isArray(res) ? res : res?.list || []
  } catch {
    seatRecords.value = []
  }
}

function openForm(plan?: any): void {
  editing.value = plan || null
  Object.assign(form, {
    name: plan?.name || '',
    type: plan?.type || '标准版',
    price: plan?.price ?? 6999,
    months: plan?.months ?? 12,
    seatLimit: plan?.seatLimit ?? 30,
    storageGb: plan?.storageGb ?? 100,
    smsCount: plan?.smsCount ?? 10000,
    sort: plan?.sort ?? plans.value.length + 1,
    status: plan?.status || '正常',
    features: plan?.features ? [...plan.features] : ['运单管理', '客户档案', '基础报表'],
    remark: plan?.remark || ''
  })
  formVisible.value = true
}

async function submitForm(): Promise<void> {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editing.value) {
      await updatePlan(editing.value.id, { ...form })
      ElMessage.success('套餐已更新')
    } else {
      await createPlan({ ...form })
      ElMessage.success('套餐已创建')
    }
    formVisible.value = false
    loadPlans()
  } finally {
    saving.value = false
  }
}

function removePlan(plan: any): void {
  ElMessageBox.confirm(`确认删除套餐「${plan.name}」？已开通该套餐的租户不受影响。`, '删除套餐', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger',
    type: 'warning'
  })
    .then(async () => {
      await deletePlan(plan.id)
      ElMessage.success('套餐已删除')
      loadPlans()
    })
    .catch(() => undefined)
}

onMounted(() => {
  loadPlans()
  loadSeats()
})
</script>

<style scoped lang="scss">
.plan-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}
.plan-card {
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 16px;
  position: relative;
  box-shadow: var(--shadow-card);
  min-width: 0;
  transition: all 0.18s;

  &:hover {
    box-shadow: 0 8px 24px rgba(31, 111, 235, 0.12);
    transform: translateY(-2px);
  }
  &.featured {
    border-color: #9bc0fb;
    background: linear-gradient(180deg, #f8fbff 0%, #fff 40%);
  }
}
.ribbon {
  position: absolute;
  right: -1px;
  top: 12px;
  background: var(--brand);
  color: #fff;
  font-size: 10px;
  padding: 2px 9px;
  border-radius: 10px 0 0 10px;
}
.plan-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
}
.plan-name {
  font-size: 16px;
  font-weight: 700;
}
.plan-desc {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 3px;
  min-height: 15px;
}
.plan-price {
  margin: 14px 0 12px;
  display: flex;
  align-items: baseline;
  gap: 2px;
  .currency {
    font-size: 13px;
    color: var(--text-secondary);
  }
  .num {
    font-size: 30px;
    font-weight: 700;
    font-family: 'DIN Alternate', Consolas, sans-serif;
    color: var(--text-main);
  }
  .period {
    font-size: 11px;
    color: var(--text-muted);
    margin-left: 3px;
  }
}
.plan-specs {
  border-top: 1px dashed var(--border-color);
  border-bottom: 1px dashed var(--border-color);
  padding: 9px 0;
  display: flex;
  flex-direction: column;
  gap: 6px;

  div {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 11.5px;
    color: var(--text-muted);
    .el-icon {
      color: #9fb2cc;
      font-size: 13px;
    }
    strong {
      margin-left: auto;
      color: var(--text-main);
      font-size: 12px;
    }
  }
}
.plan-features {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: 10px 0;
  min-height: 72px;
  span {
    display: inline-flex;
    align-items: center;
    gap: 2px;
    font-size: 10px;
    color: #3d6fbd;
    background: #eef4ff;
    border-radius: 3px;
    padding: 2px 6px;
    .el-icon {
      font-size: 10px;
    }
  }
}
.plan-footer {
  display: flex;
  gap: 6px;
  .el-button {
    flex: 1;
  }
}
.add-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  color: var(--text-muted);
  border-style: dashed;
  min-height: 390px;
  cursor: pointer;
  .el-icon {
    font-size: 28px;
    color: #b9c8dc;
  }
  span {
    font-size: 13px;
    font-weight: 500;
  }
  small {
    font-size: 10.5px;
  }
  &:hover {
    border-color: var(--brand);
    color: var(--brand);
    .el-icon {
      color: var(--brand);
    }
  }
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 14px;
}
@media (max-width: 1300px) {
  .plan-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .add-card {
    min-height: 260px;
  }
}
</style>
