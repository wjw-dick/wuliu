<template>
  <div class="rich-editor" :class="{ 'is-disabled': disabled }">
    <Toolbar :editor="editorRef" :default-config="toolbarConfig" mode="default" class="editor-toolbar" />
    <Editor
      v-model="valueHtml"
      :default-config="editorConfig"
      mode="default"
      :style="{ height: height + 'px', overflowY: 'hidden' }"
      @on-created="handleCreated"
    />
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref, shallowRef, watch } from 'vue'
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const props = withDefaults(
  defineProps<{
    modelValue?: string
    height?: number
    placeholder?: string
    disabled?: boolean
  }>(),
  {
    modelValue: '',
    height: 320,
    placeholder: '请输入内容，支持表格、图片、视频与源码模式…',
    disabled: false
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const editorRef = shallowRef<any>()
const valueHtml = ref(props.modelValue || '')

watch(
  () => props.modelValue,
  (val) => {
    if (val !== valueHtml.value) valueHtml.value = val || ''
  }
)
watch(valueHtml, (val) => {
  emit('update:modelValue', val)
})

function handleCreated(editor: any): void {
  editorRef.value = editor
}

const toolbarConfig = {
  excludeKeys: ['fullScreen', 'group-video', 'insertVideo', 'uploadVideo']
}

const editorConfig: any = {
  placeholder: props.placeholder,
  readOnly: props.disabled,
  MENU_CONF: {
    uploadImage: {
      customUpload(file: File, insertFn: (url: string, alt: string, href: string) => void) {
        const reader = new FileReader()
        reader.onload = () => insertFn(String(reader.result || ''), file.name, '')
        reader.readAsDataURL(file)
      }
    }
  }
}

onBeforeUnmount(() => {
  editorRef.value?.destroy()
})
</script>

<style scoped lang="scss">
.rich-editor {
  border: 1px solid var(--border-color);
  border-radius: 6px;
  overflow: hidden;
  background: #fff;
  width: 100%;
  z-index: 1;

  &.is-disabled {
    opacity: 0.65;
    pointer-events: none;
  }
}
.editor-toolbar {
  border-bottom: 1px solid var(--border-color);
  background: #fbfcfe;
}
:deep(.w-e-text-container) {
  background: #fff;
}
:deep(.w-e-text-container [data-slate-editor]) {
  padding: 12px 16px;
  min-height: 200px;
  font-size: 13px;
  line-height: 1.75;
}
:deep(.w-e-bar-item button) {
  height: 26px;
}
</style>
