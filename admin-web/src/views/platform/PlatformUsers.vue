<template>
  <div class="page">
    <div class="page-title-row">
      <div>
        <div class="page-title">平台账号</div>
        <div class="page-subtitle">平台运营人员账号，独立于租户账号体系</div>
      </div>
      <el-button type="primary" @click="openForm()">
        <el-icon><Plus /></el-icon> 新增账号
      </el-button>
    </div>

    <PageTable
      :data="list"
      :loading="loading"
      :total="total"
      :page="page"
      :size="size"
      :columns="columns"
      title="账号列表"
      empty-action-text="新增平台账号"
      action-width="120"
      @search="search"
      @reset="resetFilters"
      @page-change="onPageChange"
      @size-change="onSizeChange"
      @empty-action="openForm()"
    >
      <template #filter>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="账号 / 昵称 / 手机号" clearable @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.roleName" placeholder="全部角色" clearable>
            <el-option v-for="r in roles" :key="r.id" :label="r.name" :value="r.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable>
            <el-option label="正常" value="正常" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
      </template>

      <template #username="{ row }">
        <div class="user-cell">
          <el-avatar :size="26" class="mini-avatar">{{ (row.nickname || row.username).slice(0, 1) }}</el-avatar>
          <div>
            <div class="strong">{{ row.nickname }}</div>
            <div class="muted mono">{{ row.username }}</div>
          </div>
        </div>
      </template>
      <template #status="{ row }">
        <StatusTag :status="row.status" />
      </template>
      <template #actions="{ row }">
        <el-button link type="primary" @click="openForm(row)">编辑</el-button>
        <el-button link type="danger" @click="removeUser(row)">删除</el-button>
      </template>
    </PageTable>

    <el-dialog v-model="formVisible" :title="editing ? '编辑平台账号' : '新增平台账号'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="82px">
        <div class="form-grid">
          <el-form-item label="登录账号" prop="username">
            <el-input v-model="form.username" :disabled="!!editing" placeholder="英文或数字" />
          </el-form-item>
          <el-form-item label="显示昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="如：运营小周" />
          </el-form-item>
          <el-form-item label="所属角色" prop="roleName">
            <el-select v-model="form.roleName" class="w-full">
              <el-option v-for="r in roles" :key="r.id" :label="r.name" :value="r.name" />
            </el-select>
          </el-form-item>
          <el-form-item label="手机号" prop="mobile">
            <el-input v-model="form.mobile" maxlength="11" placeholder="11 位手机号" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="用于接收系统通知" />
          </el-form-item>
          <el-form-item label="账号状态">
            <el-radio-group v-model="form.status">
              <el-radio-button value="正常">正常</el-radio-button>
              <el-radio-button value="停用">停用</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </div>
        <el-form-item v-if="!editing" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-alert type="info" :closable="false" show-icon title="平台账号仅可访问平台运营端，无法进入租户后台。" />
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import PageTable, { type TableColumn } from '@/components/PageTable.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useTable } from '@/composables/useTable'
import {
  createPlatformUser,
  deletePlatformUser,
  getPlatformRoles,
  getPlatformUsers,
  updatePlatformUser
} from '@/api/platform'

const { list, loading, total, page, size, query, search, reset, onPageChange, onSizeChange, load } = useTable(getPlatformUsers)

const roles = ref<any[]>([])
const columns: TableColumn[] = [
  { prop: 'username', label: '账号', minWidth: 180, slot: 'username' },
  { prop: 'roleName', label: '角色', width: 120 },
  { prop: 'mobile', label: '手机号', width: 130 },
  { prop: 'email', label: '邮箱', minWidth: 190 },
  { prop: 'status', label: '状态', width: 86, slot: 'status' },
  { prop: 'lastLoginAt', label: '最近登录', width: 150 }
]

const formVisible = ref(false)
const saving = ref(false)
const editing = ref<any>(null)
const formRef = ref<FormInstance>()
const form = reactive({
  username: '',
  nickname: '',
  roleName: '平台运营',
  mobile: '',
  email: '',
  status: '正常',
  password: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入登录账号', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{3,20}$/, message: '3-20 位英文、数字或下划线', trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入显示昵称', trigger: 'blur' }],
  roleName: [{ required: true, message: '请选择角色', trigger: 'change' }],
  mobile: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ]
}

function resetFilters(): void {
  reset({ keyword: '', roleName: '', status: '' })
}

function openForm(row?: any): void {
  editing.value = row || null
  Object.assign(form, {
    username: row?.username || '',
    nickname: row?.nickname || '',
    roleName: row?.roleName || '平台运营',
    mobile: row?.mobile || '',
    email: row?.email || '',
    status: row?.status || '正常',
    password: ''
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
      await updatePlatformUser(editing.value.id, { ...form, password: undefined })
      ElMessage.success('账号已更新')
    } else {
      await createPlatformUser({ ...form })
      ElMessage.success('账号已创建')
    }
    formVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

function removeUser(row: any): void {
  ElMessageBox.confirm(`确认删除平台账号「${row.nickname}」？删除后立即失效。`, '删除账号', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger',
    type: 'warning'
  })
    .then(async () => {
      await deletePlatformUser(row.id)
      ElMessage.success('账号已删除')
      load()
    })
    .catch(() => undefined)
}

onMounted(async () => {
  try {
    const res: any = await getPlatformRoles({ page: 1, size: 50 })
    roles.value = Array.isArray(res) ? res : res?.list || []
  } catch {
    roles.value = []
  }
})
</script>

<style scoped lang="scss">
.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.mini-avatar {
  background: linear-gradient(135deg, #5a93f1, #1f6feb);
  color: #fff;
  font-size: 11px;
  flex: 0 0 26px;
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 14px;
}
</style>
