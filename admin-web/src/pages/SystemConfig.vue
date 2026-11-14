<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">{{ platform ? '平台参数' : '系统参数' }}</div>
        <div class="page-sub">键值对配置（含开关、JSON 高级项）</div>
      </div>
      <el-button type="primary" :loading="saving" @click="save"><el-icon><Check /></el-icon>&nbsp;保存全部</el-button>
    </div>
    <div class="card">
      <el-table :data="configs" v-loading="loading" border>
        <el-table-column prop="configKey" label="配置键" width="260" />
        <el-table-column prop="remark" label="说明" width="260" />
        <el-table-column label="值" min-width="300">
          <template #default="{ row }">
            <el-input v-model="row.configValue" />
          </template>
        </el-table-column>
      </el-table>
      <el-button style="margin-top: 12px" @click="addRow"><el-icon><Plus /></el-icon>&nbsp;新增配置</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { get, put } from '../api/http'

const props = defineProps({ platform: { type: Boolean, default: false } })
const configs = ref([])
const loading = ref(false)
const saving = ref(false)

async function load() {
  loading.value = true
  try { const r = await get(props.platform ? '/platform/configs' : '/tenant/system/configs'); configs.value = r.data }
  finally { loading.value = false }
}
function addRow() { configs.value.push({ configKey: '', configValue: '', remark: '' }) }
async function save() {
  saving.value = true
  try {
    await put(props.platform ? '/platform/configs' : '/tenant/system/configs', configs.value)
    ElMessage.success('保存成功')
  } finally { saving.value = false }
}
onMounted(load)
</script>
