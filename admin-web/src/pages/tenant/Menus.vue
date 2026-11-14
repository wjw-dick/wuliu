<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">菜单可视化</div>
        <div class="page-sub">各渠道导航菜单可视化配置（官网/H5/小程序/公众号）</div>
      </div>
      <el-button type="primary" :loading="saving" @click="save"><el-icon><Check /></el-icon>&nbsp;保存</el-button>
    </div>

    <div class="card">
      <div class="toolbar">
        <el-radio-group v-model="channel" @change="load">
          <el-radio-button v-for="c in channels" :key="c" :value="c">{{ c }}</el-radio-button>
        </el-radio-group>
      </div>
      <el-tree :data="tree" :props="{ label: 'title', children: 'children' }" node-key="key" default-expand-all>
        <template #default="{ node, data }">
          <div class="node">
            <span>{{ data.title }}</span>
            <span class="node-actions">
              <el-button size="small" link @click="addChild(data)">加子项</el-button>
              <el-button size="small" link type="danger" @click="remove(node, data)">删除</el-button>
            </span>
          </div>
        </template>
      </el-tree>
      <el-button style="margin-top: 12px" @click="addRoot"><el-icon><Plus /></el-icon>&nbsp;新增根菜单</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, put } from '../../api/http'

const channels = ['官网导航', '小程序 tabBar', 'H5 底部导航', '公众号菜单']
const channel = ref('官网导航')
const tree = ref([])
const saving = ref(false)

function key() { return 'k' + Math.random().toString(36).slice(2, 8) }

async function load() {
  const r = await get('/tenant/system/menus', { channel: channel.value })
  tree.value = r.data.tree || []
}
function addRoot() {
  tree.value.push({ key: key(), title: '新菜单', path: '/', sortNo: tree.value.length + 1, children: [] })
}
function addChild(data) {
  data.children = data.children || []
  data.children.push({ key: key(), title: '子菜单', path: '/', sortNo: data.children.length + 1, children: [] })
}
function remove(node, data) {
  ElMessageBox.confirm('确定删除该菜单？', '提示', { type: 'warning' }).then(() => {
    node.parent.childNodes = node.parent.childNodes.filter((c) => c.data.key !== data.key)
    tree.value = [...tree.value]
  })
}
async function save() {
  saving.value = true
  try {
    await put('/tenant/system/menus', { channel: channel.value, items: tree.value })
    ElMessage.success('保存成功')
  } finally { saving.value = false }
}
onMounted(load)
</script>

<style scoped>
.node { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.node-actions { opacity: 0; transition: opacity .2s; }
.node:hover .node-actions { opacity: 1; }
</style>
