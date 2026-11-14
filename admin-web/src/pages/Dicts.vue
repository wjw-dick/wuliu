<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">数据字典</div>
        <div class="page-sub">字典类型与明细项维护（演示环境可增删改）</div>
      </div>
    </div>
    <div class="chart-row">
      <div class="card">
        <div class="page-title" style="font-size:15px;margin-bottom:12px;">字典类型</div>
        <el-table :data="types" v-loading="loading" border height="420">
          <el-table-column prop="code" label="编码" width="140" />
          <el-table-column prop="name" label="名称" min-width="140" />
          <el-table-column prop="remark" label="说明" min-width="140" />
        </el-table>
      </div>
      <div class="card">
        <div class="page-title" style="font-size:15px;margin-bottom:12px;">字典项（{{ currentType }}）</div>
        <div class="toolbar">
          <el-input v-model="newItem.label" placeholder="标签" style="width:120px" />
          <el-input v-model="newItem.code" placeholder="编码" style="width:120px" />
          <el-button type="primary" @click="addItem">添加</el-button>
        </div>
        <el-table :data="currentItems" v-loading="loading" border height="360">
          <el-table-column prop="label" label="标签" width="140" />
          <el-table-column prop="code" label="编码" width="140" />
          <el-table-column prop="sortNo" label="排序" width="80" />
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button size="small" type="danger" link @click="delItem(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { get, post, del } from '../api/http'

const types = ref([])
const items = ref([])
const loading = ref(false)
const currentType = ref('')
const newItem = reactive({ label: '', code: '' })
const currentItems = computed(() => items.value.filter((i) => i.typeCode === currentType.value))

async function load() {
  loading.value = true
  try {
    const r = await get('/tenant/system/dicts')
    types.value = r.data.types || []
    items.value = r.data.all || []
    if (!currentType.value && types.value.length) currentType.value = types.value[0].code
  } finally { loading.value = false }
}

async function addItem() {
  if (!currentType.value) return ElMessage.warning('请选择字典类型')
  if (!newItem.label) return ElMessage.warning('请填写标签')
  await post(`/tenant/system/dicts/${currentType.value}/items`, { ...newItem, sortNo: 1 })
  ElMessage.success('已添加'); newItem.label = ''; newItem.code = ''; load()
}

async function delItem(row) {
  await del(`/tenant/system/dicts/items/${row.id}`); ElMessage.success('已删除'); load()
}
onMounted(load)
</script>
