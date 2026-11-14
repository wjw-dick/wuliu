<template>
  <div class="page-table">
    <div v-if="$slots.filter" class="filter-card">
      <el-form class="filter-form" inline @submit.prevent>
        <slot name="filter" />
        <el-form-item class="filter-actions">
          <el-button type="primary" @click="$emit('search')">
            <el-icon><Search /></el-icon>
            <span>查询</span>
          </el-button>
          <el-button @click="$emit('reset')">
            <el-icon><RefreshLeft /></el-icon>
            <span>重置</span>
          </el-button>
          <el-button v-if="$slots['filter-advanced']" link type="primary" @click="advanced = !advanced">
            {{ advanced ? '收起' : '展开' }}
            <el-icon><component :is="advanced ? 'ArrowUp' : 'ArrowDown'" /></el-icon>
          </el-button>
        </el-form-item>
      </el-form>
      <div v-if="$slots['filter-advanced']" v-show="advanced" class="filter-advanced">
        <el-form inline @submit.prevent>
          <slot name="filter-advanced" />
        </el-form>
      </div>
    </div>

    <div class="table-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <div v-if="title" class="table-title">
            {{ title }}
            <span v-if="total" class="count">共 {{ total }} 条</span>
          </div>
          <slot name="toolbar-left" />
        </div>
        <div class="toolbar-right">
          <slot name="toolbar" />
        </div>
      </div>

      <div v-if="loading && !data.length" class="skeleton-wrap">
        <div class="skeleton-head">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="text" style="width: 12%" />
            </template>
          </el-skeleton>
        </div>
        <el-skeleton :rows="7" animated class="skeleton-body" />
      </div>

      <el-table
        v-else
        v-loading="loading"
        :data="data"
        :row-key="rowKey"
        stripe
        border
        highlight-current-row
        :header-cell-style="{ background: '#f7f9fc' }"
        class="data-table"
        @selection-change="(rows: any[]) => $emit('selection-change', rows)"
        @row-click="(row: any) => $emit('row-click', row)"
      >
        <el-table-column v-if="selectable" type="selection" width="42" align="center" />
        <el-table-column v-if="showIndex" type="index" label="#" width="50" align="center" />
        <el-table-column
          v-for="col in columns"
          :key="col.prop + (col.label || '')"
          :prop="col.prop"
          :label="col.label"
          :width="col.width"
          :min-width="col.minWidth || 110"
          :fixed="col.fixed"
          :align="col.align || 'left'"
          :sortable="col.sortable || false"
          :show-overflow-tooltip="col.overflow !== false"
        >
          <template #default="{ row, $index }">
            <slot v-if="col.slot" :name="col.slot" :row="row" :index="$index" />
            <StatusTag v-else-if="col.tag" :status="row[col.prop] || row.statusName" />
            <MoneyText v-else-if="col.money" :value="row[col.prop]" :strong="col.strong" />
            <span v-else-if="col.formatter">{{ col.formatter(row) || '—' }}</span>
            <span v-else>{{ row[col.prop] === null || row[col.prop] === undefined || row[col.prop] === '' ? '—' : row[col.prop] }}</span>
          </template>
        </el-table-column>

        <el-table-column v-if="$slots.actions" label="操作" fixed="right" :width="actionWidth" align="right">
          <template #default="{ row, $index }">
            <div class="row-actions">
              <slot name="actions" :row="row" :index="$index" />
            </div>
          </template>
        </el-table-column>

        <template #empty>
          <div class="empty-state">
            <div class="empty-illustration">
              <svg viewBox="0 0 120 90" width="120" height="90" aria-hidden="true">
                <rect x="18" y="34" width="52" height="36" rx="4" fill="#eaf1fe" stroke="#b9d0fb" stroke-width="1.5" />
                <path d="M18 46h52" stroke="#b9d0fb" stroke-width="1.5" />
                <path d="M70 46h19l11 12v12H70z" fill="#f2f6ff" stroke="#b9d0fb" stroke-width="1.5" />
                <circle cx="38" cy="72" r="5" fill="#fff" stroke="#8fb5f6" stroke-width="1.5" />
                <circle cx="86" cy="72" r="5" fill="#fff" stroke="#8fb5f6" stroke-width="1.5" />
                <path d="M30 40h20M30 52h12" stroke="#8fb5f6" stroke-width="2" stroke-linecap="round" opacity=".6" />
                <path d="M14 26l6 6 12-12" stroke="#1f6feb" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" opacity=".35" fill="none" />
              </svg>
            </div>
            <div class="empty-title">{{ emptyText || '暂无数据' }}</div>
            <div class="empty-desc">调整筛选条件，或先创建一条新记录</div>
            <el-button v-if="emptyActionText" type="primary" plain @click="$emit('empty-action')">
              <el-icon><Plus /></el-icon>
              <span>{{ emptyActionText }}</span>
            </el-button>
          </div>
        </template>
      </el-table>

      <div v-if="showPagination" class="pagination-bar">
        <el-pagination
          background
          small
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :current-page="page"
          :page-size="size"
          :page-sizes="[10, 20, 50, 100]"
          @current-change="(p: number) => $emit('page-change', p)"
          @size-change="(s: number) => $emit('size-change', s)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import StatusTag from './StatusTag.vue'
import MoneyText from './MoneyText.vue'

export interface TableColumn {
  prop: string
  label: string
  width?: string | number
  minWidth?: string | number
  fixed?: 'left' | 'right' | boolean
  align?: 'left' | 'center' | 'right'
  sortable?: boolean
  slot?: string
  tag?: boolean
  money?: boolean
  strong?: boolean
  overflow?: boolean
  formatter?: (row: any) => string
}

withDefaults(
  defineProps<{
    data: any[]
    loading?: boolean
    total?: number
    page?: number
    size?: number
    columns?: TableColumn[]
    title?: string
    rowKey?: string
    selectable?: boolean
    showIndex?: boolean
    showPagination?: boolean
    emptyText?: string
    emptyActionText?: string
    actionWidth?: string | number
  }>(),
  {
    loading: false,
    total: 0,
    page: 1,
    size: 10,
    columns: () => [],
    showPagination: true,
    emptyText: '暂无数据',
    emptyActionText: '',
    actionWidth: 170
  }
)

defineEmits<{
  (e: 'search'): void
  (e: 'reset'): void
  (e: 'page-change', page: number): void
  (e: 'size-change', size: number): void
  (e: 'empty-action'): void
  (e: 'selection-change', rows: any[]): void
  (e: 'row-click', row: any): void
}>()

const advanced = ref(false)
</script>

<style scoped lang="scss">
.page-table {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}
.filter-card {
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: var(--radius);
  box-shadow: var(--shadow-card);
  padding: 10px 12px 2px;
}
.filter-form {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  :deep(.el-form-item) {
    margin-bottom: 8px;
    margin-right: 12px;
  }
  :deep(.el-form-item__label) {
    padding-right: 6px;
    font-size: 12px;
  }
  :deep(.el-input),
  :deep(.el-select),
  :deep(.el-date-editor) {
    width: 172px;
  }
  :deep(.el-date-editor.el-input__wrapper) {
    width: 240px;
  }
}
.filter-actions {
  margin-left: auto;
  :deep(.el-form-item__content) {
    flex-wrap: nowrap;
  }
}
.filter-advanced {
  border-top: 1px dashed #edf0f5;
  padding-top: 8px;
  :deep(.el-form-item) {
    margin-bottom: 8px;
    margin-right: 12px;
  }
  :deep(.el-form-item__label) {
    padding-right: 6px;
    font-size: 12px;
  }
  :deep(.el-input),
  :deep(.el-select) {
    width: 172px;
  }
}
.table-card {
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: var(--radius);
  box-shadow: var(--shadow-card);
  padding: 10px 12px 12px;
  min-width: 0;
}
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
  min-height: 30px;
}
.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.table-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-main);
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.count {
  font-size: 11px;
  font-weight: 400;
  color: var(--text-muted);
}
.data-table {
  width: 100%;
  :deep(.el-table__inner-wrapper::before) {
    display: none;
  }
}
.row-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 2px;
  flex-wrap: nowrap;
  :deep(.el-button) {
    padding: 2px 4px;
    height: auto;
    font-size: 12px;
    margin-left: 0;
  }
}
.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding-top: 10px;
}
.skeleton-wrap {
  border: 1px solid #edf0f5;
  border-radius: 4px;
  padding: 12px;
  min-height: 360px;
}
.skeleton-head {
  background: #f7f9fc;
  padding: 10px 12px;
  margin: -12px -12px 12px;
  border-bottom: 1px solid #edf0f5;
}
.empty-state {
  padding: 28px 0 22px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.empty-illustration {
  opacity: 0.95;
  margin-bottom: 4px;
}
.empty-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}
.empty-desc {
  font-size: 11.5px;
  color: var(--text-muted);
  margin-bottom: 8px;
}
</style>
