<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">对象存储</div>
        <div class="page-sub">本地 / S3 / OSS / COS / MinIO 多驱动存储配置</div>
      </div>
      <el-button type="primary" :loading="applying" @click="apply"><el-icon><Check /></el-icon>&nbsp;应用配置</el-button>
    </div>

    <div class="card mb">
      <div class="page-title" style="font-size:15px;margin-bottom:12px;">当前存储</div>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="驱动">{{ current.driver }}</el-descriptions-item>
        <el-descriptions-item label="Bucket">{{ current.bucket }}</el-descriptions-item>
        <el-descriptions-item label="域名">{{ current.domain }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="card">
      <div class="page-title" style="font-size:15px;margin-bottom:12px;">切换驱动</div>
      <el-form :model="form" label-width="130px" style="max-width:640px">
        <el-form-item label="驱动"><el-select v-model="form.driver" style="width:100%">
          <el-option v-for="d in ['local', 's3', 'oss', 'cos', 'minio']" :key="d" :label="d" :value="d" />
        </el-select></el-form-item>
        <template v-if="form.driver !== 'local'">
          <el-form-item label="Endpoint"><el-input v-model="form.config.endpoint" /></el-form-item>
          <el-form-item label="AccessKey"><el-input v-model="form.config.accessKey" /></el-form-item>
          <el-form-item label="SecretKey"><el-input v-model="form.config.secretKey" type="password" show-password /></el-form-item>
          <el-form-item label="Bucket"><el-input v-model="form.config.bucket" /></el-form-item>
          <el-form-item label="区域"><el-input v-model="form.config.region" /></el-form-item>
        </template>
        <el-form-item>
          <el-button @click="test"><el-icon><Connection /></el-icon>&nbsp;连接测试</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { get, post } from '../../api/http'

const current = reactive({})
const form = reactive({ driver: 'local', config: {} })
const applying = ref(false)
async function load() {
  const r = await get('/tenant/system/storage')
  Object.assign(current, r.data)
}
async function test() {
  const r = await post('/tenant/system/storage/test', { driver: form.driver, config: form.config })
  ElMessage.success(r.data.ok ? '连接成功' : '连接失败')
}
async function apply() {
  applying.value = true
  try { await post('/tenant/system/storage/apply', { driver: form.driver, config: form.config }); ElMessage.success('已应用'); load() }
  finally { applying.value = false }
}
onMounted(load)
</script>
