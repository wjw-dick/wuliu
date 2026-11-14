<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">消息配置</div>
        <div class="page-sub">消息模板（短信/公众号/站内信/App推送）与发送日志</div>
      </div>
      <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>&nbsp;新增模板</el-button>
    </div>

    <div class="card mb">
      <div class="page-title" style="font-size:15px;margin-bottom:12px;">消息模板</div>
      <el-table :data="templates" v-loading="loading" border>
        <el-table-column prop="code" label="编码" width="140" />
        <el-table-column prop="name" label="名称" width="160" />
        <el-table-column prop="channel" label="渠道" width="120" />
        <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
        <el-table-column prop="enabled" label="启用" width="80">
          <template #default="{ row }"><el-tag :type="row.enabled ? 'success' : 'info'" size="small">{{ row.enabled ? '是' : '否' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="test(row)">测试</el-button>
            <el-button size="small" type="primary" @click="edit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
      <div class="page-title" style="font-size:15px;margin-bottom:12px;">发送日志</div>
      <el-table :data="logs" v-loading="loading" border>
        <el-table-column prop="channel" label="渠道" width="120" />
        <el-table-column prop="receiver" label="接收人" width="140" />
        <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="row.status === '发送成功' ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="170" />
      </el-table>
    </div>

    <el-dialog v-model="dialog" :title="editing ? '编辑模板' : '新增模板'" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="编码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="渠道"><el-select v-model="form.channel" style="width:100%">
          <el-option v-for="c in ['短信', '公众号', '站内信', 'App推送']" :key="c" :label="c" :value="c" />
        </el-select></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" /></el-form-item>
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
import { get, post, put } from '../../api/http'

const templates = ref([])
const logs = ref([])
const loading = ref(false)
const dialog = ref(false)
const editing = ref(false)
const form = reactive({})

async function load() {
  loading.value = true
  try {
    const t = await get('/tenant/system/message-templates'); templates.value = t.data
    const l = await get('/tenant/system/message-logs'); logs.value = l.data
  } finally { loading.value = false }
}
function openCreate() { Object.keys(form).forEach((k) => delete form[k]); Object.assign(form, { channel: '短信', enabled: 1 }); editing.value = false; dialog.value = true }
function edit(row) { Object.keys(form).forEach((k) => delete form[k]); Object.assign(form, row); editing.value = true; dialog.value = true }
async function save() {
  if (editing.value) await put(`/tenant/system/message-templates/${form.id}`, { ...form })
  else await post('/tenant/system/message-templates', { ...form })
  ElMessage.success('保存成功'); dialog.value = false; load()
}
async function test(row) { await post(`/tenant/system/message-templates/${row.id}/test`, { receiver: '13800000000' }); ElMessage.success('已发送测试'); load() }
onMounted(load)
</script>
