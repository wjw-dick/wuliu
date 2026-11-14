<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">SSO 配置</div>
        <div class="page-sub">单点登录对接（企业微信/钉钉/飞书/OAuth2）</div>
      </div>
      <el-button type="primary" :loading="saving" @click="save"><el-icon><Check /></el-icon>&nbsp;保存</el-button>
    </div>
    <div class="card">
      <el-form :model="form" label-width="130px" style="max-width:640px">
        <el-form-item label="提供商"><el-select v-model="form.provider" style="width:100%">
          <el-option v-for="p in ['企业微信', '钉钉', '飞书', 'OAuth2']" :key="p" :label="p" :value="p" />
        </el-select></el-form-item>
        <el-form-item label="AppId/CorpId"><el-input v-model="form.appId" /></el-form-item>
        <el-form-item label="AppSecret"><el-input v-model="form.appSecret" type="password" show-password /></el-form-item>
        <el-form-item label="回调地址"><el-input v-model="form.callbackUrl" /></el-form-item>
        <el-form-item label="登录页开关"><el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { get, put } from '../../api/http'

const form = reactive({})
const saving = ref(false)
async function load() {
  const r = await get('/tenant/system/sso')
  Object.assign(form, r.data || { provider: '企业微信', enabled: 0 })
}
async function save() {
  saving.value = true
  try { await put('/tenant/system/sso', { ...form }); ElMessage.success('保存成功') }
  finally { saving.value = false }
}
onMounted(load)
</script>
