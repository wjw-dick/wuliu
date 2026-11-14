<template>
  <div class="page">
    <div class="page-title-row">
      <div>
        <div class="page-title">平台公告</div>
        <div class="page-subtitle">发布版本通知、维护公告与运营政策，租户后台顶部同步展示</div>
      </div>
      <el-button type="primary" @click="openForm()">
        <el-icon><Plus /></el-icon> 发布公告
      </el-button>
    </div>

    <PageTable
      :data="list"
      :loading="loading"
      :total="total"
      :page="page"
      :size="size"
      :columns="columns"
      title="公告列表"
      empty-action-text="发布第一条公告"
      action-width="170"
      @search="search"
      @reset="resetFilters"
      @page-change="onPageChange"
      @size-change="onSizeChange"
      @empty-action="openForm()"
    >
      <template #filter>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="公告标题" clearable @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="公告类型">
          <el-select v-model="query.type" placeholder="全部类型" clearable>
            <el-option v-for="t in DICTS['公告类型']" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="发布状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable>
            <el-option label="已发布" value="已发布" />
            <el-option label="草稿" value="草稿" />
          </el-select>
        </el-form-item>
      </template>

      <template #title="{ row }">
        <span class="strong link-text" @click="openPreview(row)">{{ row.title }}</span>
      </template>
      <template #status="{ row }"><StatusTag :status="row.status" /></template>
      <template #actions="{ row }">
        <el-button link type="primary" @click="openPreview(row)">预览</el-button>
        <el-button link type="primary" @click="openForm(row)">编辑</el-button>
        <el-button link type="danger" @click="removeRow(row)">删除</el-button>
      </template>
    </PageTable>

    <el-dialog v-model="formVisible" :title="editing ? '编辑公告' : '发布公告'" width="860px" top="4vh" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="82px">
        <div class="form-grid">
          <el-form-item label="公告标题" prop="title" class="span-2">
            <el-input v-model="form.title" maxlength="60" show-word-limit placeholder="请输入公告标题" />
          </el-form-item>
          <el-form-item label="公告类型" prop="type">
            <el-select v-model="form.type" class="w-full">
              <el-option v-for="t in DICTS['公告类型']" :key="t" :label="t" :value="t" />
            </el-select>
          </el-form-item>
          <el-form-item label="发布状态">
            <el-radio-group v-model="form.status">
              <el-radio-button value="已发布">立即发布</el-radio-button>
              <el-radio-button value="草稿">保存草稿</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </div>
        <el-form-item label="公告正文" prop="content">
          <RichEditor v-model="form.content" :height="330" placeholder="请输入公告正文，支持富文本、图片与表格…" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存并发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" :title="previewRow?.title" width="760px" top="6vh">
      <div v-if="previewRow" class="preview-meta">
        <StatusTag :status="previewRow.status" />
        <el-tag size="small" effect="plain">{{ previewRow.type }}</el-tag>
        <span class="muted">{{ previewRow.publisher }} · {{ previewRow.publishedAt }}</span>
        <span class="muted">阅读 {{ previewRow.views }}</span>
      </div>
      <div class="preview-content" v-html="previewRow?.content"></div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary" @click="openForm(previewRow)">编辑公告</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import PageTable, { type TableColumn } from '@/components/PageTable.vue'
import StatusTag from '@/components/StatusTag.vue'
import RichEditor from '@/components/RichEditor.vue'
import { useTable } from '@/composables/useTable'
import {
  createPlatformAnnouncement,
  deletePlatformAnnouncement,
  getPlatformAnnouncements,
  updatePlatformAnnouncement
} from '@/api/platform'
import { DICTS } from '@/utils/dict'

const { list, loading, total, page, size, query, search, reset, onPageChange, onSizeChange, load } = useTable(getPlatformAnnouncements)

const columns: TableColumn[] = [
  { prop: 'title', label: '公告标题', minWidth: 300, slot: 'title' },
  { prop: 'type', label: '类型', width: 110 },
  { prop: 'status', label: '状态', width: 90, slot: 'status' },
  { prop: 'publisher', label: '发布人', width: 100 },
  { prop: 'publishedAt', label: '发布时间', width: 150 },
  { prop: 'views', label: '阅读量', width: 82, align: 'right' }
]

const formVisible = ref(false)
const saving = ref(false)
const editing = ref<any>(null)
const formRef = ref<FormInstance>()
const form = reactive({ title: '', type: '版本通知', status: '已发布', content: '' })
const rules: FormRules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入公告正文', trigger: 'blur' }]
}

const previewVisible = ref(false)
const previewRow = ref<any>(null)

function resetFilters(): void {
  reset({ keyword: '', type: '', status: '' })
}

function openForm(row?: any): void {
  editing.value = row || null
  Object.assign(form, {
    title: row?.title || '',
    type: row?.type || '版本通知',
    status: row?.status || '已发布',
    content: row?.content || ''
  })
  previewVisible.value = false
  formVisible.value = true
}

function openPreview(row: any): void {
  previewRow.value = row
  previewVisible.value = true
}

async function submitForm(): Promise<void> {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editing.value) {
      await updatePlatformAnnouncement(editing.value.id, { ...form })
      ElMessage.success('公告已更新')
    } else {
      await createPlatformAnnouncement({ ...form, publisher: '平台运营', publishedAt: new Date().toISOString().slice(0, 19).replace('T', ' ') })
      ElMessage.success(form.status === '已发布' ? '公告已发布' : '草稿已保存')
    }
    formVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

function removeRow(row: any): void {
  ElMessageBox.confirm(`确认删除公告「${row.title}」？删除后租户端将不再展示。`, '删除公告', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger',
    type: 'warning'
  })
    .then(async () => {
      await deletePlatformAnnouncement(row.id)
      ElMessage.success('公告已删除')
      load()
    })
    .catch(() => undefined)
}
</script>

<style scoped lang="scss">
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 14px;
  .span-2 {
    grid-column: span 2;
  }
}
.preview-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 14px;
  font-size: 12px;
  .muted:last-child {
    margin-left: auto;
  }
}
.preview-content {
  font-size: 13px;
  line-height: 1.9;
  color: var(--text-secondary);
  max-height: 58vh;
  overflow-y: auto;
  :deep(img) {
    max-width: 100%;
  }
  :deep(p) {
    margin: 0 0 10px;
  }
}
</style>
