<template>
  <div class="login-page">
    <div class="login-box">
      <h1>示例物流云平台</h1>
      <p class="sub">物流货运数字化多端运营平台</p>
      <el-form @keyup.enter="onLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="账号" size="large">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password>
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-radio-group v-model="form.scope">
            <el-radio-button value="tenant">租户端</el-radio-button>
            <el-radio-button value="platform">平台端</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="onLogin">
          登 录
        </el-button>
        <p class="tip">演示账号：admin / admin123（租户端、平台端通用）</p>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuth } from '../store/auth'

const router = useRouter()
const auth = useAuth()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123', scope: 'tenant' })

async function onLogin() {
  if (!form.username || !form.password) return ElMessage.warning('请输入账号和密码')
  loading.value = true
  try {
    await auth.login(form.username, form.password, form.scope)
    ElMessage.success('登录成功')
    router.push(`/${form.scope}/dashboard`)
  } catch (e) {
    // 错误已由拦截器提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.tip { text-align: center; color: #909399; font-size: 12px; margin-top: 16px; }
</style>
