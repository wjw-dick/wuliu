<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">公众号账号</div>
        <div class="page-sub">微信公众号/服务号账号与菜单绑定</div>
      </div>
      <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>&nbsp;新增账号</el-button>
    </div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" border>
        <el-table-column prop="appId" label="AppId" width="200" />
        <el-table-column prop="accountName" label="账号名称" min-width="180" />
        <el-table-column prop="accountType" label="类型" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="row.status === '正常' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="menuChannel" label="绑定菜单" width="140" />
      </el-table>
    </div>

    <el-dialog v-model="dialog" title="新增公众号账号" width="560px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="AppId"><el-input v-model="form.appId" /></el-form-item>
        <el-form-item label="AppSecret"><el-input v-model="form.appSecret" type="password" show-password /></el-form-item>
        <el-form-item label="账号名称"><el-input v-model="form.accountName" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.accountType" style="width:100%">
          <el-option v-for="t in ['订阅号', '服务号']" :key="t" :label="t" :value="t" />
        </el-select></el-form-item>
        <el-form-item label="绑定菜单"><el-select v-model="form.menuChannel" style="width:100%">
          <el-option v-for="c in ['官网导航', '小程序 tabBar', 'H5 底部导航', '公众号菜单']" :key="c" :label="c" :value="c" />
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
import { get, post } from '../../api/http'

const rows = ref([])
const loading = ref(false)
const dialog = ref(false)
const form = reactive({})
async function load() {
  loading.value = true
  try { const r = await get('/tenant/system/wechat'); rows.value = r.data }
  finally { loading.value = false }
}
function openCreate() { Object.keys(form).forEach((k) => delete form[k]); Object.assign(form, { accountType: '服务号', menuChannel: '公众号菜单', status: '正常' }); dialog.value = true }
async function save() { await post('/tenant/system/wechat', { ...form }); ElMessage.success('已保存'); dialog.value = false; load() }
onMounted(load)
</script>
