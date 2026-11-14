<template>
  <span class="confirm-danger">
    <el-button v-if="showButton" type="danger" :plain="plain" :disabled="disabled" @click="open">
      <el-icon><Delete /></el-icon>
      <span>{{ buttonText }}</span>
    </el-button>
    <el-dialog
      v-model="visible"
      :title="title"
      width="440px"
      append-to-body
      :close-on-click-modal="false"
      @closed="reason = ''"
    >
      <el-alert :title="content" type="warning" :closable="false" show-icon class="mb-12" />
      <div v-if="requireReason" class="reason-label">
        {{ reasonLabel }} <span class="required">*</span>
      </div>
      <el-input
        v-model="reason"
        type="textarea"
        :rows="3"
        maxlength="200"
        show-word-limit
        :placeholder="reasonPlaceholder"
      />
      <div v-if="presets.length" class="preset-list">
        <span v-for="p in presets" :key="p" class="preset" @click="reason = p">{{ p }}</span>
      </div>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="danger" :loading="loading" :disabled="requireReason && !reason.trim()" @click="confirm">
          {{ confirmText }}
        </el-button>
      </template>
    </el-dialog>
  </span>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    modelValue?: boolean
    title?: string
    content?: string
    buttonText?: string
    confirmText?: string
    reasonLabel?: string
    reasonPlaceholder?: string
    requireReason?: boolean
    loading?: boolean
    showButton?: boolean
    plain?: boolean
    disabled?: boolean
    presets?: string[]
  }>(),
  {
    modelValue: false,
    title: '危险操作确认',
    content: '该操作不可撤销，请确认后继续。',
    buttonText: '删除',
    confirmText: '确认操作',
    reasonLabel: '操作原因',
    reasonPlaceholder: '请输入操作原因，便于后续审计追溯',
    requireReason: true,
    loading: false,
    showButton: true,
    plain: false,
    disabled: false,
    presets: () => []
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm', reason: string): void
}>()

const visible = ref(props.modelValue || false)
const reason = ref('')

watch(
  () => props.modelValue,
  (value) => {
    if (value !== visible.value) visible.value = value
  }
)
watch(visible, (value) => emit('update:modelValue', value))

function open(): void {
  visible.value = true
  emit('update:modelValue', true)
}

function confirm(): void {
  if (props.requireReason && !reason.value.trim()) return
  emit('confirm', reason.value.trim())
}

defineExpose({ open, close: () => (visible.value = false) })
</script>

<style scoped lang="scss">
.reason-label {
  font-size: 12.5px;
  margin-bottom: 6px;
  color: var(--text-secondary);
}
.required {
  color: #dc2626;
}
.preset-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}
.preset {
  font-size: 11px;
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
</style>
