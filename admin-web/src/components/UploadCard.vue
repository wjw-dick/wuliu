<template>
  <div class="upload-card">
    <div class="upload-head">
      <div class="head-text">
        <span class="upload-title">{{ title }}</span>
        <span class="upload-tip">{{ tip }}</span>
      </div>
      <span class="upload-count">{{ files.length }} / {{ max }}</span>
    </div>

    <div
      class="drop-zone"
      :class="{ dragging, disabled }"
      @click="openPicker"
      @dragover.prevent="dragging = true"
      @dragleave.prevent="dragging = false"
      @drop.prevent="onDrop"
    >
      <input ref="inputRef" type="file" class="file-input" multiple :accept="accept" :disabled="disabled" @change="onPick" />
      <el-icon class="drop-icon"><UploadFilled /></el-icon>
      <div class="drop-text">拖拽文件到此处，或 <em>点击选择文件</em></div>
      <div class="drop-sub">支持 JPG / PNG / PDF，单个文件不超过 {{ maxSize }}MB，最多 {{ max }} 个</div>
    </div>

    <div v-if="files.length" class="file-grid">
      <div v-for="(file, index) in files" :key="file.uid" class="file-item" :class="file.status">
        <div class="file-thumb" @click="preview(file, index)">
          <img v-if="isImage(file) && file.url" :src="file.url" :alt="file.name" />
          <div v-else class="file-icon">
            <el-icon><Document /></el-icon>
            <span>{{ file.name.split('.').pop()?.toUpperCase() || 'FILE' }}</span>
          </div>
          <div v-if="file.status === 'uploading'" class="uploading-mask">
            <el-progress type="circle" :percentage="file.progress" :width="46" :stroke-width="4" color="#ffffff" />
          </div>
          <div v-if="file.status === 'error'" class="error-mask">
            <el-icon><WarningFilled /></el-icon>
            <span>上传失败</span>
          </div>
          <div v-if="index === 0" class="cover-badge">主图</div>
        </div>

        <div class="file-name" :title="file.name">{{ file.name }}</div>
        <div v-if="file.status === 'error'" class="file-err">{{ file.error || '上传异常' }}</div>

        <div class="file-actions">
          <el-tooltip content="前移" placement="top">
            <el-button link :disabled="index === 0" @click="move(index, -1)"><el-icon><Back /></el-icon></el-button>
          </el-tooltip>
          <el-tooltip content="后移" placement="top">
            <el-button link :disabled="index === files.length - 1" @click="move(index, 1)"><el-icon><Right /></el-icon></el-button>
          </el-tooltip>
          <el-tooltip v-if="file.status === 'error'" content="重试" placement="top">
            <el-button link type="primary" @click="retry(file)"><el-icon><RefreshRight /></el-icon></el-button>
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button link type="danger" @click="remove(index)"><el-icon><Delete /></el-icon></el-button>
          </el-tooltip>
        </div>

        <div v-if="file.status === 'uploading'" class="progress-line">
          <el-progress :percentage="file.progress" :show-text="false" :stroke-width="3" />
        </div>
      </div>
    </div>

    <el-image-viewer
      v-if="viewerVisible"
      :url-list="viewerUrls"
      :initial-index="viewerIndex"
      teleported
      @close="viewerVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

interface UploadFileItem {
  uid: string
  name: string
  url: string
  progress: number
  status: 'uploading' | 'done' | 'error'
  error?: string
  raw?: File
}

const props = withDefaults(
  defineProps<{
    modelValue?: string[]
    title?: string
    tip?: string
    max?: number
    maxSize?: number
    accept?: string
    disabled?: boolean
  }>(),
  {
    modelValue: () => [],
    title: '附件上传',
    tip: '图片或 PDF 文件，可拖拽排序',
    max: 9,
    maxSize: 10,
    accept: 'image/*,.pdf',
    disabled: false
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: string[]): void
  (e: 'change', value: string[]): void
}>()

const inputRef = ref<HTMLInputElement>()
const dragging = ref(false)
const files = ref<UploadFileItem[]>([])
const timers = new Map<string, number>()

watch(
  () => props.modelValue,
  (value) => {
    const incoming = (value || []).join('|')
    const current = files.value.map((f) => f.url || f.name).join('|')
    if (incoming !== current) {
      files.value = (value || []).map((url, i) => ({
        uid: `ext-${i}-${url.slice(-8)}`,
        name: url.split('/').pop()?.split('?')[0] || `附件-${i + 1}`,
        url,
        progress: 100,
        status: 'done'
      }))
    }
  },
  { immediate: true }
)

function sync(): void {
  const value = files.value.filter((f) => f.status === 'done').map((f) => f.url || f.name)
  emit('update:modelValue', value)
  emit('change', value)
}

function openPicker(): void {
  if (props.disabled) return
  inputRef.value?.click()
}

function onPick(event: Event): void {
  const input = event.target as HTMLInputElement
  if (input.files) addFiles(Array.from(input.files))
  input.value = ''
}

function onDrop(event: DragEvent): void {
  dragging.value = false
  if (props.disabled) return
  if (event.dataTransfer?.files) addFiles(Array.from(event.dataTransfer.files))
}

function addFiles(list: File[]): void {
  const remaining = props.max - files.value.length
  if (remaining <= 0) {
    ElMessage.warning(`最多上传 ${props.max} 个文件`)
    return
  }
  list.slice(0, remaining).forEach((file) => {
    const uid = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
    const isImageFile = file.type.startsWith('image/')
    const url = isImageFile ? URL.createObjectURL(file) : ''
    const item: UploadFileItem = { uid, name: file.name, url, progress: 0, status: 'uploading', raw: file }
    if (file.size > props.maxSize * 1024 * 1024) {
      item.status = 'error'
      item.progress = 0
      item.error = `超过 ${props.maxSize}MB 限制`
      ElMessage.error(`${file.name} 超过 ${props.maxSize}MB，无法上传`)
    }
    files.value.push(item)
    if (item.status === 'uploading') startUpload(item)
  })
}

function startUpload(item: UploadFileItem): void {
  const old = timers.get(item.uid)
  if (old) window.clearInterval(old)
  item.status = 'uploading'
  item.error = ''
  item.progress = 0
  const timer = window.setInterval(() => {
    item.progress = Math.min(100, item.progress + Math.round(12 + Math.random() * 18))
    if (item.progress >= 100) {
      window.clearInterval(timer)
      timers.delete(item.uid)
      item.status = 'done'
      sync()
    }
  }, 120)
  timers.set(item.uid, timer)
}

function retry(item: UploadFileItem): void {
  if (item.raw && item.raw.size > props.maxSize * 1024 * 1024) {
    ElMessage.error('文件超过大小限制，请重新选择')
    return
  }
  startUpload(item)
}

function remove(index: number): void {
  const item = files.value[index]
  if (!item) return
  const timer = timers.get(item.uid)
  if (timer) window.clearInterval(timer)
  timers.delete(item.uid)
  if (item.url.startsWith('blob:')) URL.revokeObjectURL(item.url)
  files.value.splice(index, 1)
  sync()
}

function move(index: number, delta: number): void {
  const target = index + delta
  if (target < 0 || target >= files.value.length) return
  const list = files.value
  ;[list[index], list[target]] = [list[target], list[index]]
  files.value = [...list]
  sync()
}

function isImage(file: UploadFileItem): boolean {
  if (file.raw) return file.raw.type.startsWith('image/')
  return /\.(png|jpe?g|gif|webp|bmp|svg)$/i.test(file.name) || file.url.startsWith('blob:') || file.url.startsWith('data:')
}

const viewerVisible = ref(false)
const viewerUrls = ref<string[]>([])
const viewerIndex = ref(0)

function preview(file: UploadFileItem, index: number): void {
  if (!isImage(file) || !file.url) return
  viewerUrls.value = files.value.filter((f) => isImage(f) && f.url).map((f) => f.url)
  viewerIndex.value = Math.max(0, viewerUrls.value.indexOf(file.url))
  viewerVisible.value = true
}

onBeforeUnmount(() => {
  timers.forEach((timer) => window.clearInterval(timer))
  files.value.forEach((f) => {
    if (f.url.startsWith('blob:')) URL.revokeObjectURL(f.url)
  })
})
</script>

<style scoped lang="scss">
.upload-card {
  width: 100%;
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: var(--radius);
  padding: 14px;
}
.upload-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 10px;
}
.head-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.upload-title {
  font-size: 13px;
  font-weight: 600;
}
.upload-tip {
  font-size: 11px;
  color: var(--text-muted);
}
.upload-count {
  font-size: 11px;
  color: var(--text-muted);
  background: #f4f6fa;
  padding: 2px 8px;
  border-radius: 10px;
}
.drop-zone {
  border: 1px dashed #c6d4e6;
  border-radius: 6px;
  background: #fbfcfe;
  padding: 18px;
  text-align: center;
  cursor: pointer;
  transition: all 0.15s;

  &:hover,
  &.dragging {
    border-color: var(--brand);
    background: var(--brand-soft);
  }
  &.disabled {
    cursor: not-allowed;
    opacity: 0.6;
  }
}
.file-input {
  display: none;
}
.drop-icon {
  font-size: 28px;
  color: var(--brand);
  margin-bottom: 4px;
}
.drop-text {
  font-size: 12.5px;
  color: var(--text-secondary);
  em {
    font-style: normal;
    color: var(--brand);
    font-weight: 500;
  }
}
.drop-sub {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 4px;
}
.file-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(132px, 1fr));
  gap: 10px;
}
.file-item {
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 6px;
  position: relative;
  background: #fff;
  &:hover .file-actions {
    opacity: 1;
  }
  &.error {
    border-color: #f3c4c4;
    background: #fffafa;
  }
}
.file-thumb {
  height: 92px;
  border-radius: 4px;
  background: #f5f7fa;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: relative;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }
}
.file-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #8fa3bd;
  font-size: 10px;

  .el-icon {
    font-size: 30px;
  }
}
.uploading-mask {
  position: absolute;
  inset: 0;
  background: rgba(17, 28, 46, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  :deep(.el-progress__text) {
    color: #fff !important;
    font-size: 10px !important;
  }
}
.error-mask {
  position: absolute;
  inset: 0;
  background: rgba(220, 38, 38, 0.08);
  color: #dc2626;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  font-size: 11px;
  .el-icon {
    font-size: 20px;
  }
}
.cover-badge {
  position: absolute;
  left: 4px;
  top: 4px;
  background: var(--brand);
  color: #fff;
  font-size: 9px;
  padding: 1px 5px;
  border-radius: 3px;
}
.file-name {
  font-size: 11px;
  margin-top: 5px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.file-err {
  font-size: 10px;
  color: #dc2626;
  margin-top: 1px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.file-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
  margin-top: 2px;
  opacity: 0.15;
  transition: opacity 0.15s;
  :deep(.el-button) {
    padding: 1px 3px;
    height: auto;
    font-size: 12px;
  }
}
.progress-line {
  margin-top: 4px;
}
</style>
