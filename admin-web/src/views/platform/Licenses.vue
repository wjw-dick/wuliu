<template>
  <div class="page">
    <div class="page-title-row">
      <div>
        <div class="page-title">资质审核</div>
        <div class="page-subtitle">审核租户营业执照、道路运输经营许可证与法人证件，驳回需填写中文原因</div>
      </div>
      <el-button @click="load">
        <el-icon><Refresh /></el-icon> 刷新
      </el-button>
    </div>

    <PageTable
      :data="list"
      :loading="loading"
      :total="total"
      :page="page"
      :size="size"
      :columns="columns"
      title="待审核资质"
      empty-text="暂无待审核资质"
      action-width="140"
      @search="search"
      @reset="resetFilters"
      @page-change="onPageChange"
      @size-change="onSizeChange"
    >
      <template #filter>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="租户名称 / 证照编号" clearable @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable>
            <el-option label="待审核" value="待审核" />
            <el-option label="已通过" value="已通过" />
            <el-option label="已驳回" value="已驳回" />
          </el-select>
        </el-form-item>
        <el-form-item label="证照类型">
          <el-select v-model="query.type" placeholder="全部类型" clearable>
            <el-option label="营业执照" value="营业执照" />
            <el-option label="道路运输经营许可证" value="道路运输经营许可证" />
            <el-option label="法人身份证" value="法人身份证" />
          </el-select>
        </el-form-item>
      </template>

      <template #tenantName="{ row }">
        <span class="strong">{{ row.tenantName }}</span>
        <div class="muted mono">租户 ID {{ row.tenantId }}</div>
      </template>
      <template #images="{ row }">
        <div class="thumb-row">
          <div v-for="(img, i) in row.images" :key="i" class="mini-thumb" @click="previewLicense(row)">
            <el-icon><Document /></el-icon>
          </div>
          <span class="muted">{{ row.images?.length || 1 }} 张</span>
        </div>
      </template>
      <template #status="{ row }">
        <StatusTag :status="row.status" />
        <div v-if="row.rejectReason" class="reject-reason ellipsis" :title="row.rejectReason">{{ row.rejectReason }}</div>
      </template>
      <template #actions="{ row }">
        <el-button link type="primary" @click="openReview(row)">查看</el-button>
        <el-button v-if="row.status === '待审核'" link type="primary" @click="openReview(row)">审核</el-button>
      </template>
    </PageTable>

    <el-dialog v-model="reviewVisible" title="资质审核" width="820px" top="5vh">
      <div v-if="reviewRow" class="review-layout">
        <div class="preview-pane">
          <div class="preview-title">
            <span>{{ reviewRow.licenseType }}</span>
            <StatusTag :status="reviewRow.status" />
          </div>
          <div class="license-paper">
            <div class="paper-head">
              <div class="paper-emblem">营业执照</div>
              <div class="paper-no">{{ reviewRow.licenseNo }}</div>
            </div>
            <div class="paper-line w70"></div>
            <div class="paper-line w90"></div>
            <div class="paper-line w60"></div>
            <div class="paper-line w80"></div>
            <div class="paper-label">名称：{{ reviewRow.tenantName }}</div>
            <div class="paper-label">类型：有限责任公司（自然人投资或控股）</div>
            <div class="paper-label">经营范围：道路货物运输、仓储服务、供应链管理</div>
            <div class="paper-seal">全达<br />物流</div>
          </div>
          <div class="preview-thumbs">
            <div v-for="(img, i) in reviewRow.images" :key="i" class="preview-thumb">
              <el-icon><Picture /></el-icon>
              <span>{{ img }}</span>
            </div>
          </div>
          <div class="preview-meta">
            <span>提交时间：{{ reviewRow.submittedAt }}</span>
            <span>证照编号：{{ reviewRow.licenseNo }}</span>
          </div>
        </div>

        <div class="review-pane">
          <div class="section-title">审核结论</div>
          <el-radio-group v-model="reviewPass" class="review-radio">
            <el-radio-button :value="true">通过</el-radio-button>
            <el-radio-button :value="false">驳回</el-radio-button>
          </el-radio-group>
          <div class="reason-label">
            {{ reviewPass ? '审核备注（选填）' : '驳回原因' }}
            <span v-if="!reviewPass" class="danger-text">*</span>
          </div>
          <el-input
            v-model="reviewReason"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            :placeholder="reviewPass ? '可填写审核说明，将同步到租户' : '请填写具体驳回原因，租户可据此补充材料'"
          />
          <div v-if="!reviewPass" class="quick-reasons">
            <span v-for="r in rejectPresets" :key="r" @click="reviewReason = r">{{ r }}</span>
          </div>
          <el-alert
            class="mt-16"
            :type="reviewPass ? 'success' : 'warning'"
            :closable="false"
            show-icon
            :title="reviewPass ? '通过后租户可正常使用全部业务功能。' : '驳回后系统将向租户推送补充材料提醒。'"
          />
          <div class="review-info">
            <div><span>主体名称</span><strong>{{ reviewRow.tenantName }}</strong></div>
            <div><span>证照类型</span><strong>{{ reviewRow.licenseType }}</strong></div>
            <div><span>当前状态</span><StatusTag :status="reviewRow.status" /></div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewLoading" @click="submitReview">确认审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageTable, { type TableColumn } from '@/components/PageTable.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useTable } from '@/composables/useTable'
import { getLicenses, reviewLicense } from '@/api/platform'

const { list, loading, total, page, size, query, search, reset, onPageChange, onSizeChange, load } = useTable(getLicenses)

const columns: TableColumn[] = [
  { prop: 'tenantName', label: '租户', minWidth: 180, slot: 'tenantName' },
  { prop: 'licenseType', label: '证照类型', width: 150 },
  { prop: 'licenseNo', label: '证照编号', minWidth: 160 },
  { prop: 'images', label: '证照预览', width: 130, slot: 'images' },
  { prop: 'submittedAt', label: '提交时间', width: 146 },
  { prop: 'status', label: '审核状态', width: 130, slot: 'status' }
]

const rejectPresets = ['证照模糊无法辨认', '证照已过期', '主体名称与营业执照不一致', '缺少道路运输经营许可证']

const reviewVisible = ref(false)
const reviewLoading = ref(false)
const reviewRow = ref<any>(null)
const reviewPass = ref(true)
const reviewReason = ref('')

function resetFilters(): void {
  reset({ keyword: '', status: '待审核', type: '' })
}

function openReview(row: any): void {
  reviewRow.value = row
  reviewPass.value = true
  reviewReason.value = ''
  reviewVisible.value = true
}

function previewLicense(row: any): void {
  openReview(row)
}

async function submitReview(): Promise<void> {
  if (!reviewRow.value) return
  if (!reviewPass.value && !reviewReason.value.trim()) {
    ElMessage.warning('驳回时必须填写原因')
    return
  }
  reviewLoading.value = true
  try {
    await reviewLicense(reviewRow.value.id, reviewPass.value, reviewReason.value.trim())
    ElMessage.success(reviewPass.value ? '审核通过，已通知租户' : '已驳回，已推送补充材料提醒')
    reviewVisible.value = false
    load()
  } finally {
    reviewLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.thumb-row {
  display: flex;
  align-items: center;
  gap: 4px;
}
.mini-thumb {
  width: 26px;
  height: 20px;
  border-radius: 3px;
  background: #eef4ff;
  color: var(--brand);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 12px;
  border: 1px solid #d9e6fb;
}
.reject-reason {
  font-size: 10.5px;
  color: #dc2626;
  max-width: 130px;
}
.review-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(0, 1fr);
  gap: 20px;
}
.preview-pane {
  background: #f7f9fc;
  border-radius: 8px;
  padding: 14px;
}
.preview-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 10px;
}
.license-paper {
  background: #fffcf5;
  border: 1px solid #eadfc8;
  border-radius: 4px;
  padding: 18px 20px;
  position: relative;
  min-height: 220px;
  box-shadow: 0 4px 14px rgba(120, 90, 30, 0.08);
}
.paper-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 2px solid #c0392b;
  padding-bottom: 8px;
  margin-bottom: 12px;
}
.paper-emblem {
  color: #b03a2e;
  font-weight: 700;
  font-size: 16px;
  letter-spacing: 2px;
}
.paper-no {
  font-size: 10px;
  color: #8a7a5d;
  font-family: monospace;
}
.paper-line {
  height: 6px;
  background: #efe8da;
  border-radius: 2px;
  margin-bottom: 7px;
  &.w70 {
    width: 70%;
  }
  &.w90 {
    width: 90%;
  }
  &.w60 {
    width: 60%;
  }
  &.w80 {
    width: 80%;
  }
}
.paper-label {
  font-size: 10.5px;
  color: #6f614a;
  line-height: 1.9;
}
.paper-seal {
  position: absolute;
  right: 20px;
  bottom: 18px;
  width: 64px;
  height: 64px;
  border: 2px solid rgba(192, 57, 43, 0.55);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: rgba(192, 57, 43, 0.65);
  font-size: 10px;
  font-weight: 700;
  transform: rotate(-12deg);
  line-height: 1.2;
}
.preview-thumbs {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px;
  margin-top: 10px;
}
.preview-thumb {
  display: flex;
  align-items: center;
  gap: 5px;
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 6px;
  font-size: 10px;
  color: var(--text-secondary);
  overflow: hidden;
  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
.preview-meta {
  display: flex;
  flex-direction: column;
  gap: 3px;
  font-size: 10.5px;
  color: var(--text-muted);
  margin-top: 10px;
}
.review-pane {
  min-width: 0;
}
.section-title {
  font-size: 13px;
  font-weight: 600;
  border-left: 3px solid var(--brand);
  padding-left: 8px;
  margin-bottom: 12px;
}
.review-radio {
  margin-bottom: 16px;
  :deep(.el-radio-button__inner) {
    padding: 8px 30px;
  }
}
.reason-label {
  font-size: 12.5px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}
.quick-reasons {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
  span {
    font-size: 10.5px;
    color: var(--text-secondary);
    border: 1px solid var(--border-color);
    border-radius: 12px;
    padding: 2px 8px;
    cursor: pointer;
    &:hover {
      border-color: var(--brand);
      color: var(--brand);
    }
  }
}
.review-info {
  margin-top: 18px;
  border-top: 1px dashed var(--border-color);
  padding-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  div {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 12px;
    span {
      color: var(--text-muted);
    }
  }
}
</style>
