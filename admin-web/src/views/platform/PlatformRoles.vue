<template>
  <div class="page">
    <div class="page-title-row">
      <div>
        <div class="page-title">平台角色</div>
        <div class="page-subtitle">平台超管、运营、财务独立 RBAC，不与租户角色共用</div>
      </div>
      <el-button type="primary" @click="openForm()">
        <el-icon><Plus /></el-icon> 新增角色
      </el-button>
    </div>

    <PageTable
      :data="list"
      :loading="loading"
      :total="total"
      :page="page"
      :size="size"
      :columns="columns"
      title="角色列表"
      action-width="190"
      @search="search"
      @reset="resetFilters"
      @page-change="onPageChange"
      @size-change="onSizeChange"
    >
      <template #filter>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="角色名称 / 编码" clearable @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable>
            <el-option label="正常" value="正常" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
      </template>

      <template #name="{ row }">
        <span class="strong">{{ row.name }}</span>
        <div v-if="row.code === 'PLATFORM_ADMIN'" class="builtin-badge">内置角色</div>
      </template>
      <template #code="{ row }"><span class="mono muted">{{ row.code }}</span></template>
      <template #permissions="{ row }">
        <el-tag size="small" effect="plain" type="info">
          {{ row.permissions?.includes('*') ? '全部权限' : `${row.permissions?.length || 0} 项权限` }}
        </el-tag>
      </template>
      <template #status="{ row }"><StatusTag :status="row.status" /></template>
      <template #actions="{ row }">
        <el-button link type="primary" @click="openPerms(row)">分配权限</el-button>
        <el-button link type="primary" @click="openForm(row)">编辑</el-button>
        <el-button link type="danger" @click="removeRole(row)">删除</el-button>
      </template>
    </PageTable>

    <el-dialog v-model="formVisible" :title="editing ? '编辑角色' : '新增角色'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" placeholder="如：平台运营" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" :disabled="!!editing" placeholder="如：PLATFORM_OPS" />
        </el-form-item>
        <el-form-item label="角色说明">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="描述该角色的职责范围" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio-button value="正常">正常</el-radio-button>
            <el-radio-button value="停用">停用</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="permVisible" size="480px">
      <template #header>
        <div>
          <div class="drawer-title">分配权限</div>
          <div class="muted">{{ permRole?.name }} · {{ permRole?.userCount || 0 }} 个账号使用</div>
        </div>
      </template>
      <div class="perm-toolbar">
        <el-checkbox v-model="expandAll" @change="toggleExpand">展开全部</el-checkbox>
        <el-checkbox v-model="checkAll" @change="toggleCheckAll">全选</el-checkbox>
        <span class="muted">共 {{ leafCodes.length }} 项</span>
      </div>
      <el-tree
        ref="treeRef"
        :data="permissionTree"
        show-checkbox
        node-key="id"
        :props="{ label: 'label', children: 'children' }"
        :default-expand-all="true"
        class="perm-tree"
        @check="syncCheckAll"
      />
      <div class="perm-footer">
        <el-button @click="permVisible = false">取消</el-button>
        <el-button type="primary" :loading="permSaving" @click="savePerms">保存权限</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { nextTick, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import PageTable, { type TableColumn } from '@/components/PageTable.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useTable } from '@/composables/useTable'
import {
  createPlatformRole,
  deletePlatformRole,
  getPlatformRoles,
  updatePlatformRole
} from '@/api/platform'

const { list, loading, total, page, size, query, search, reset, onPageChange, onSizeChange, load } = useTable(getPlatformRoles)

const columns: TableColumn[] = [
  { prop: 'name', label: '角色名称', minWidth: 170, slot: 'name' },
  { prop: 'code', label: '角色编码', minWidth: 150, slot: 'code' },
  { prop: 'description', label: '说明', minWidth: 220 },
  { prop: 'permissions', label: '权限范围', width: 120, slot: 'permissions' },
  { prop: 'userCount', label: '账号数', width: 80, align: 'center' },
  { prop: 'status', label: '状态', width: 86, slot: 'status' }
]

const permissionTree = [
  {
    id: 'g-tenant',
    label: '租户运营',
    children: [
      { id: 'tenant:list', label: '租户列表与详情' },
      { id: 'tenant:create', label: '开通租户' },
      { id: 'tenant:edit', label: '编辑租户' },
      { id: 'tenant:renew', label: '租户续期' },
      { id: 'tenant:suspend', label: '停用 / 启用租户' },
      { id: 'tenant:delete', label: '删除归档租户' },
      { id: 'license:list', label: '查看资质' },
      { id: 'license:review', label: '审核资质' }
    ]
  },
  {
    id: 'g-commerce',
    label: '商业运营',
    children: [
      { id: 'plan:list', label: '查看套餐' },
      { id: 'plan:manage', label: '套餐增删改' },
      { id: 'bill:list', label: '查看账单' },
      { id: 'bill:pay', label: '收款登记' },
      { id: 'bill:create', label: '生成账单' }
    ]
  },
  {
    id: 'g-system',
    label: '平台管理',
    children: [
      { id: 'platform-user:list', label: '平台账号管理' },
      { id: 'platform-role:list', label: '平台角色管理' },
      { id: 'audit:list', label: '审计日志查询' },
      { id: 'platform-announcement:list', label: '平台公告管理' },
      { id: 'platform-dict:list', label: '平台字典管理' },
      { id: 'platform-config:edit', label: '平台参数配置' }
    ]
  }
]

const leafCodes = permissionTree.flatMap((g) => g.children.map((c) => c.id))

/* ---------------- 角色表单 ---------------- */
const formVisible = ref(false)
const saving = ref(false)
const editing = ref<any>(null)
const formRef = ref<FormInstance>()
const form = reactive({ name: '', code: '', description: '', status: '正常' })
const rules: FormRules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z_]{3,40}$/, message: '使用大写字母与下划线', trigger: 'blur' }
  ]
}

function resetFilters(): void {
  reset({ keyword: '', status: '' })
}

function openForm(row?: any): void {
  editing.value = row || null
  Object.assign(form, {
    name: row?.name || '',
    code: row?.code || '',
    description: row?.description || '',
    status: row?.status || '正常'
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
      await updatePlatformRole(editing.value.id, { ...form })
      ElMessage.success('角色已更新')
    } else {
      await createPlatformRole({ ...form, permissions: [] })
      ElMessage.success('角色已创建，请继续分配权限')
    }
    formVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

function removeRole(row: any): void {
  if (row.code === 'PLATFORM_ADMIN') {
    ElMessage.warning('内置平台超管角色不可删除')
    return
  }
  ElMessageBox.confirm(`确认删除角色「${row.name}」？该角色下账号将失去对应权限。`, '删除角色', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger',
    type: 'warning'
  })
    .then(async () => {
      await deletePlatformRole(row.id)
      ElMessage.success('角色已删除')
      load()
    })
    .catch(() => undefined)
}

/* ---------------- 权限分配 ---------------- */
const permVisible = ref(false)
const permSaving = ref(false)
const permRole = ref<any>(null)
const treeRef = ref<any>()
const expandAll = ref(true)
const checkAll = ref(false)

function openPerms(row: any): void {
  permRole.value = row
  permVisible.value = true
  const checked = row.permissions?.includes('*') ? [...leafCodes] : (row.permissions || []).filter((p: string) => leafCodes.includes(p))
  checkAll.value = checked.length === leafCodes.length
  nextTick(() => treeRef.value?.setCheckedKeys(checked))
}

function toggleExpand(value: boolean | string | number): void {
  const nodes = treeRef.value?.store?.nodesMap || {}
  Object.values(nodes).forEach((node: any) => (node.expanded = !!value))
}

function toggleCheckAll(value: boolean | string | number): void {
  treeRef.value?.setCheckedKeys(value ? leafCodes : [])
}

function syncCheckAll(): void {
  const checked = treeRef.value?.getCheckedKeys(false) || []
  checkAll.value = leafCodes.every((c) => checked.includes(c))
}

async function savePerms(): Promise<void> {
  if (!permRole.value) return
  const checked = treeRef.value?.getCheckedKeys(false) || []
  permSaving.value = true
  try {
    await updatePlatformRole(permRole.value.id, { permissions: checked })
    ElMessage.success('权限已保存')
    permVisible.value = false
    load()
  } finally {
    permSaving.value = false
  }
}
</script>

<style scoped lang="scss">
.builtin-badge {
  display: inline-block;
  font-size: 9px;
  color: var(--brand);
  background: #eef4ff;
  padding: 0 5px;
  border-radius: 3px;
  margin-top: 2px;
}
.drawer-title {
  font-size: 16px;
  font-weight: 600;
}
.perm-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 10px;
  .muted {
    margin-left: auto;
    font-size: 11px;
  }
}
.perm-tree {
  :deep(.el-tree-node__content) {
    height: 32px;
  }
}
.perm-footer {
  border-top: 1px solid var(--border-color);
  padding-top: 12px;
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
