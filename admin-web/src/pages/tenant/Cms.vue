<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">内容 CMS</div>
        <div class="page-sub">官网/小程序/H5 内容管理（文章、案例、帮助、Banner）</div>
      </div>
      <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>&nbsp;新增内容</el-button>
    </div>

    <div class="card">
      <div class="toolbar">
        <el-select v-model="contentType" placeholder="类型" clearable @change="load">
          <el-option v-for="t in ['新闻', '案例', '帮助', 'Banner', '公告']" :key="t" :label="t" :value="t" />
        </el-select>
      </div>
      <el-table :data="rows" v-loading="loading" border>
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column prop="contentType" label="类型" width="110" />
        <el-table-column prop="lang" label="语言" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="row.status === '已发布' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="publishedAt" label="发布时间" width="170" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" @click="edit(row)">编辑</el-button>
            <el-popconfirm title="确定删除？" @confirm="remove(row)">
              <template #reference><el-button size="small" type="danger">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog" :title="editing ? '编辑内容' : '新增内容'" width="640px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.contentType" style="width:100%">
          <el-option v-for="t in ['新闻', '案例', '帮助', 'Banner', '公告']" :key="t" :label="t" :value="t" />
        </el-select></el-form-item>
        <el-form-item label="摘要"><el-input v-model="form.summary" /></el-form-item>
        <el-form-item label="正文"><el-input v-model="form.content" type="textarea" :rows="8" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="form.status" style="width:100%">
          <el-option v-for="s in ['草稿', '已发布']" :key="s" :label="s" :value="s" />
        </el-select></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { get, post, put, del } from '../../api/http'

const rows = ref([])
const loading = ref(false)
const contentType = ref('')
const dialog = ref(false)
const editing = ref(false)
const form = reactive({})
async function load() {
  loading.value = true
  try { const r = await get('/tenant/system/cms/contents', { contentType }); rows.value = r.data }
  finally { loading.value = false }
}
function openCreate() { Object.keys(form).forEach((k) => delete form[k]); Object.assign(form, { contentType: '新闻', status: '草稿', lang: 'zh-CN' }); editing.value = false; dialog.value = true }
function edit(row) { Object.keys(form).forEach((k) => delete form[k]); Object.assign(form, row); editing.value = true; dialog.value = true }
async function save() {
  if (editing.value) await put(`/tenant/system/cms/contents/${form.id}`, { ...form })
  else await post('/tenant/system/cms/contents', { ...form })
  ElMessage.success('保存成功'); dialog.value = false; load()
}
async function remove(row) { await del(`/tenant/system/cms/contents/${row.id}`); ElMessage.success('已删除'); load() }
onMounted(load)
</script>
