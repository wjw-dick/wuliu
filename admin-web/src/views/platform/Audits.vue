<template>
  <div class="page">
    <div class="page-title-row">
      <div>
        <div class="page-title">审计日志</div>
        <div class="page-subtitle">全量操作留痕，按租户、操作人、动作与时间检索，导出需填写用途</div>
      </div>
      <el-button @click="exportAudits">
        <el-icon><Download /></el-icon> 导出日志
      </el-button>
    </div>

    <PageTable
      :data="list"
      :loading="loading"
      :total="total"
      :page="page"
      :size="size"
      :columns="columns"
      title="操作记录"
      empty-text="暂无审计记录"
      @search="handleSearch"
      @reset="resetFilters"
      @page-change="onPageChange"
      @size-change="onSizeChange"
    >
      <template #filter>
        <el-form-item label="操作人">
          <el-input v-model="query.operator" placeholder="账号或昵称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="业务模块">
          <el-select v-model="query.module" placeholder="全部模块" clearable>
            <el-option v-for="m in modules" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作动作">
          <el-input v-model="query.action" placeholder="如：开通租户" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
      </template>
      <template #filter-advanced>
        <el-form-item label="租户 ID">
          <el-input v-model="query.tenantId" placeholder="输入租户 ID" clearable />
        </el-form-item>
        <el-form-item label="操作结果">
          <el-select v-model="query.result" placeholder="全部" clearable>
            <el-option label="成功" value="成功" />
            <el-option label="失败" value="失败" />
          </el-select>
        </el-form-item>
      </template>

      <template #createdAt="{ row }">
        <span class="mono">{{ row.createdAt }}</span>
      </template>
      <template #operator="{ row }">
        <div class="strong">{{ row.operatorName || row.operator }}</div>
        <div class="muted mono">{{ row.operator }}</div>
      </template>
      <template #target="{ row }">
        <span>{{ row.target }}</span>
        <div class="muted">{{ row.tenantName }}</div>
      </template>
      <template #result="{ row }">
        <el-tag :type="row.result === '成功' ? 'success' : 'danger'" size="small" effect="light">
          {{ row.result }}
        </el-tag>
      </template>
      <template #actions="{ row }">
        <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      </template>
    </PageTable>

    <el-drawer v-model="detailVisible" size="480px" title="操作详情">
      <div v-if="detail" class="audit-detail">
        <div class="detail-row"><span>操作时间</span><strong>{{ detail.createdAt }}</strong></div>
        <div class="detail-row"><span>操作人</span><strong>{{ detail.operatorName }}（{{ detail.operator }}）</strong></div>
        <div class="detail-row"><span>所属模块</span><strong>{{ detail.module }}</strong></div>
        <div class="detail-row"><span>操作动作</span><strong>{{ detail.action }}</strong></div>
        <div class="detail-row"><span>操作对象</span><strong>{{ detail.target }}</strong></div>
        <div class="detail-row"><span>关联租户</span><strong>{{ detail.tenantName || '—' }}</strong></div>
        <div class="detail-row"><span>来源 IP</span><strong class="mono">{{ detail.ip }}</strong></div>
        <div class="detail-row"><span>操作结果</span><StatusTag :status="detail.result" /></div>
        <div class="divider"></div>
        <div class="section-title">请求参数</div>
        <pre class="code-block">{
  "targetId": {{ detail.id }},
  "module": "{{ detail.module }}",
  "action": "{{ detail.action }}",
  "ip": "{{ detail.ip }}",
  "userAgent": "Chrome / Windows"
}</pre>
        <el-alert class="mt-12" type="info" :closable="false" show-icon title="审计日志不可修改与删除，保留 180 天。" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageTable, { type TableColumn } from '@/components/PageTable.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useTable } from '@/composables/useTable'
import { getAudits } from '@/api/platform'
import { downloadText, toCsv, today } from '@/utils/format'

const { list, loading, total, page, size, query, search, reset, onPageChange, onSizeChange } = useTable(getAudits)

const dateRange = ref<[string, string] | null>(null)
const modules = ['租户管理', '资质审核', '账单收款', '套餐管理', '平台账号', '平台角色', '公告管理']
const columns: TableColumn[] = [
  { prop: 'createdAt', label: '时间', width: 152, slot: 'createdAt' },
  { prop: 'operator', label: '操作人', width: 130, slot: 'operator' },
  { prop: 'module', label: '模块', width: 100 },
  { prop: 'action', label: '操作', width: 120 },
  { prop: 'target', label: '操作对象', minWidth: 190, slot: 'target' },
  { prop: 'ip', label: '来源 IP', width: 120 },
  { prop: 'result', label: '结果', width: 82, slot: 'result' }
]

const detailVisible = ref(false)
const detail = ref<any>(null)

function handleSearch(): void {
  query.value.start = dateRange.value?.[0] || ''
  query.value.end = dateRange.value?.[1] || ''
  search()
}

function resetFilters(): void {
  dateRange.value = null
  reset({ operator: '', module: '', action: '', tenantId: '', result: '', start: '', end: '' })
}

function openDetail(row: any): void {
  detail.value = row
  detailVisible.value = true
}

function exportAudits(): void {
  ElMessageBox.prompt('所有导出均记录操作人、条数与用途，请填写本次导出用途。', '导出审计日志', {
    confirmButtonText: '确认导出',
    cancelButtonText: '取消',
    inputPlaceholder: '如：季度合规检查',
    inputValidator: (value) => (value && value.trim().length >= 2 ? true : '请填写至少 2 个字的用途'),
    inputType: 'text'
  })
    .then(({ value }) => {
      const headers = ['时间', '操作人', '模块', '操作', '操作对象', '租户', 'IP', '结果']
      const rows = list.value.map((r: any) => [r.createdAt, r.operatorName, r.module, r.action, r.target, r.tenantName, r.ip, r.result])
      downloadText(`审计日志-${today()}.csv`, toCsv(headers, rows), 'text/csv;charset=utf-8')
      ElMessage.success(`导出成功，用途「${value}」已记录`)
    })
    .catch(() => undefined)
}
</script>

<style scoped lang="scss">
.audit-detail {
  font-size: 12.5px;
}
.detail-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 9px 0;
  border-bottom: 1px dashed #f0f3f8;
  span {
    color: var(--text-muted);
  }
  strong {
    text-align: right;
    font-weight: 500;
    word-break: break-all;
  }
}
.code-block {
  background: #0f1d33;
  color: #a7c7f5;
  border-radius: 6px;
  padding: 12px;
  font-size: 11px;
  line-height: 1.7;
  overflow-x: auto;
  margin: 0;
}
</style>
