<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">{{ cfg.title }}</div>
        <div class="page-sub">{{ cfg.scope === 'platform' ? '平台运营中心' : '租户运营后台' }} · 演示数据</div>
      </div>
      <el-button v-if="!cfg.readonly" type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>&nbsp;新增{{ cfg.singular || '' }}
      </el-button>
    </div>

    <div class="card">
      <div v-if="cfg.search?.length" class="toolbar">
        <template v-for="s in cfg.search" :key="s.prop">
          <el-input
            v-if="s.type !== 'select'"
            v-model="searchForm[s.prop]"
            :placeholder="s.placeholder || s.label"
            clearable
            @keyup.enter="load"
          />
          <el-select v-else v-model="searchForm[s.prop]" :placeholder="s.placeholder || s.label" clearable>
            <el-option v-for="o in s.options" :key="o" :label="o" :value="o" />
          </el-select>
        </template>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column v-for="col in cfg.columns" :key="col.prop" :prop="col.prop" :label="col.label"
          :width="col.width" :min-width="col.minWidth" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag v-if="col.type === 'tag'" :type="col.tagMap?.[row[col.prop]] || 'info'" size="small">
              {{ row[col.prop] }}
            </el-tag>
            <template v-else>{{ row[col.prop] ?? '-' }}</template>
          </template>
        </el-table-column>
        <el-table-column v-if="!cfg.readonly" label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该记录吗？" @confirm="remove(row)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="cfg.pageMode !== false"
        style="margin-top: 14px; justify-content: flex-end"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="onPage"
      />
    </div>

    <el-dialog v-model="dialog" :title="editing ? `编辑${cfg.title}` : `新增${cfg.title}`" width="560px">
      <el-form :model="form" label-width="120px">
        <el-form-item v-for="f in cfg.form" :key="f.prop" :label="f.label" :required="f.required">
          <el-input v-if="!f.type || f.type === 'text'" v-model="form[f.prop]" :placeholder="f.label" />
          <el-input v-else-if="f.type === 'textarea'" v-model="form[f.prop]" type="textarea" :rows="4" />
          <el-input v-else-if="f.type === 'password'" v-model="form[f.prop]" type="password" show-password />
          <el-input-number v-else-if="f.type === 'number'" v-model="form[f.prop]" :min="0" style="width:100%" />
          <el-date-picker v-else-if="f.type === 'date'" v-model="form[f.prop]" type="date" value-format="YYYY-MM-DD" style="width:100%" />
          <el-select v-else-if="f.type === 'select'" v-model="form[f.prop]" style="width:100%">
            <el-option v-for="o in f.options" :key="o" :label="o" :value="o" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { crudConfigs } from '../crudConfigs'
import { get, post, put, del } from '../api/http'

const props = defineProps({ configKey: { type: String, required: true } })
const cfg = computed(() => crudConfigs[props.configKey])

const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const saving = ref(false)
const dialog = ref(false)
const editing = ref(false)
const searchForm = reactive({})
const form = reactive({})

function resetForm() {
  Object.keys(form).forEach((k) => delete form[k])
  cfg.value.form?.forEach((f) => {
    form[f.prop] = f.type === 'number' ? 0 : ''
  })
}

async function load() {
  loading.value = true
  try {
    const params = { ...searchForm }
    if (cfg.value.pageMode !== false) { params.page = page.value; params.size = size.value }
    const res = await get(cfg.value.endpoint, params)
    const data = res.data
    if (Array.isArray(data)) {
      rows.value = data
      total.value = data.length
    } else if (data && Array.isArray(data.list)) {
      rows.value = data.list
      total.value = data.total ?? data.list.length
    } else {
      rows.value = []
      total.value = 0
    }
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  Object.keys(searchForm).forEach((k) => delete searchForm[k])
  page.value = 1
  load()
}

function onPage(p) { page.value = p; load() }

function openCreate() {
  resetForm()
  editing.value = false
  dialog.value = true
}

function openEdit(row) {
  resetForm()
  editing.value = true
  Object.assign(form, row)
  dialog.value = true
}

async function save() {
  for (const f of cfg.value.form || []) {
    if (f.required && !form[f.prop]) return ElMessage.warning(`请填写${f.label}`)
  }
  saving.value = true
  try {
    if (editing.value) await put(`${cfg.value.endpoint}/${form.id}`, { ...form })
    else await post(cfg.value.endpoint, { ...form })
    ElMessage.success('保存成功')
    dialog.value = false
    load()
  } catch (e) {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  await del(`${cfg.value.endpoint}/${row.id}`)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
