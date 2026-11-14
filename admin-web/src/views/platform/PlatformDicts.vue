<template>
  <div class="page">
    <div class="page-title-row">
      <div>
        <div class="page-title">平台字典</div>
        <div class="page-subtitle">行业分类、套餐类型、试用天数等公共字典，中文名称必填，编码只读</div>
      </div>
      <el-button type="primary" @click="openForm()">
        <el-icon><Plus /></el-icon> 新增字典分类
      </el-button>
    </div>

    <PageTable
      :data="list"
      :loading="loading"
      :total="total"
      :page="page"
      :size="size"
      :columns="columns"
      title="字典分类"
      action-width="190"
      @search="search"
      @reset="resetFilters"
      @page-change="onPageChange"
      @size-change="onSizeChange"
    >
      <template #filter>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="字典名称 / 编码" clearable @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="字典属性">
          <el-select v-model="query.builtin" placeholder="全部" clearable>
            <el-option label="系统内置" :value="true" />
            <el-option label="自定义" :value="false" />
          </el-select>
        </el-form-item>
      </template>

      <template #name="{ row }">
        <span class="strong link-text" @click="openItems(row)">{{ row.name }}</span>
        <div class="muted mono">{{ row.code }}</div>
      </template>
      <template #itemCount="{ row }">
        <el-button link type="primary" @click="openItems(row)">{{ row.itemCount || 0 }} 项</el-button>
      </template>
      <template #builtin="{ row }">
        <el-tag size="small" :type="row.builtin ? 'info' : 'primary'" effect="plain">{{ row.builtin ? '系统内置' : '自定义' }}</el-tag>
      </template>
      <template #actions="{ row }">
        <el-button link type="primary" @click="openItems(row)">字典项</el-button>
        <el-button link type="primary" @click="openForm(row)">编辑</el-button>
        <el-button link type="danger" :disabled="row.builtin" @click="removeRow(row)">删除</el-button>
      </template>
    </PageTable>

    <el-dialog v-model="formVisible" :title="editing ? '编辑字典分类' : '新增字典分类'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="如：行业分类" />
        </el-form-item>
        <el-form-item label="分类编码" prop="code">
          <el-input v-model="form.code" :disabled="!!editing" placeholder="如：industry" />
          <div class="muted form-tip">编码创建后不可修改，仅允许小写字母与下划线</div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="说明该字典的使用场景" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="itemVisible" size="560px">
      <template #header>
        <div>
          <div class="drawer-title">{{ currentCategory?.name }}</div>
          <div class="muted mono">{{ currentCategory?.code }} · 中文名称必填，编码只读</div>
        </div>
      </template>
      <div class="item-toolbar">
        <el-input v-model="newItemName" placeholder="新增字典项中文名称" @keyup.enter="addItem" />
        <el-button type="primary" @click="addItem">新增字典项</el-button>
      </div>
      <el-table :data="items" size="small" stripe border>
        <el-table-column label="编码（只读）" width="160">
          <template #default="{ row }"><span class="mono muted">{{ row.code }}</span></template>
        </el-table-column>
        <el-table-column label="中文名称">
          <template #default="{ row }">
            <el-input v-model="row.name" size="small" placeholder="必填中文名称" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="70" align="right">
          <template #default="{ $index }">
            <el-button link type="danger" @click="items.splice($index, 1)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="item-tip">
        <el-alert type="info" :closable="false" show-icon title="字典项编码仅存数据库内部，页面与导出只展示中文名称。排序按列表顺序生效。" />
      </div>
      <div class="drawer-footer">
        <el-button @click="itemVisible = false">取消</el-button>
        <el-button type="primary" :loading="itemSaving" @click="saveItems">保存字典项</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import PageTable, { type TableColumn } from '@/components/PageTable.vue'
import { useTable } from '@/composables/useTable'
import {
  createPlatformDict,
  deletePlatformDict,
  getPlatformDicts,
  updatePlatformDict
} from '@/api/platform'
import { DICTS } from '@/utils/dict'

const { list, loading, total, page, size, query, search, reset, onPageChange, onSizeChange, load } = useTable(getPlatformDicts)

const columns: TableColumn[] = [
  { prop: 'name', label: '字典名称', minWidth: 180, slot: 'name' },
  { prop: 'itemCount', label: '字典项', width: 90, slot: 'itemCount' },
  { prop: 'builtin', label: '属性', width: 100, slot: 'builtin' },
  { prop: 'remark', label: '说明', minWidth: 260 },
  { prop: 'code', label: '编码', width: 150, formatter: (row) => row.code }
]

const formVisible = ref(false)
const saving = ref(false)
const editing = ref<any>(null)
const formRef = ref<FormInstance>()
const form = reactive({ name: '', code: '', remark: '' })
const rules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入分类编码', trigger: 'blur' },
    { pattern: /^[a-z_]{2,40}$/, message: '仅允许小写字母与下划线', trigger: 'blur' }
  ]
}

function resetFilters(): void {
  reset({ keyword: '', builtin: '' })
}

function openForm(row?: any): void {
  editing.value = row || null
  Object.assign(form, { name: row?.name || '', code: row?.code || '', remark: row?.remark || '' })
  formVisible.value = true
}

async function submitForm(): Promise<void> {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editing.value) {
      await updatePlatformDict(editing.value.id, { ...form })
      ElMessage.success('字典分类已更新')
    } else {
      await createPlatformDict({ ...form, builtin: false, itemCount: 0 })
      ElMessage.success('字典分类已创建')
    }
    formVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

function removeRow(row: any): void {
  ElMessageBox.confirm(`确认删除字典分类「${row.name}」？关联字典项将一并移除。`, '删除字典', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger',
    type: 'warning'
  })
    .then(async () => {
      await deletePlatformDict(row.id)
      ElMessage.success('字典分类已删除')
      load()
    })
    .catch(() => undefined)
}

/* ---------------- 字典项 ---------------- */
const itemVisible = ref(false)
const itemSaving = ref(false)
const currentCategory = ref<any>(null)
const items = ref<{ code: string; name: string }[]>([])
const newItemName = ref('')

function openItems(row: any): void {
  currentCategory.value = row
  const seed = DICTS[row.name] || ['默认项一', '默认项二', '默认项三']
  items.value = seed.map((name, i) => ({ code: `${row.code}_${String(i + 1).padStart(2, '0')}`, name }))
  newItemName.value = ''
  itemVisible.value = true
}

function addItem(): void {
  const name = newItemName.value.trim()
  if (!name) {
    ElMessage.warning('请输入字典项中文名称')
    return
  }
  items.value.push({
    code: `${currentCategory.value.code}_${String(items.value.length + 1).padStart(2, '0')}`,
    name
  })
  newItemName.value = ''
}

async function saveItems(): Promise<void> {
  const invalid = items.value.find((i) => !i.name.trim())
  if (invalid) {
    ElMessage.warning('所有字典项都必须填写中文名称')
    return
  }
  if (!currentCategory.value) return
  itemSaving.value = true
  try {
    await updatePlatformDict(currentCategory.value.id, { items: items.value })
    ElMessage.success('字典项已保存')
    itemVisible.value = false
    load()
  } finally {
    itemSaving.value = false
  }
}
</script>

<style scoped lang="scss">
.form-tip {
  font-size: 10.5px;
  line-height: 1.6;
  margin-top: 2px;
}
.drawer-title {
  font-size: 16px;
  font-weight: 600;
}
.item-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}
.item-tip {
  margin-top: 10px;
}
.drawer-footer {
  margin-top: 16px;
  border-top: 1px solid var(--border-color);
  padding-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
