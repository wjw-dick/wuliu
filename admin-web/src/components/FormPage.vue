<template>
  <div class="form-page page">
    <div class="page-title-row">
      <div class="flex-center gap-8">
        <el-button v-if="back" circle size="small" @click="$emit('cancel')">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <div>
          <div class="page-title">{{ title }}</div>
          <div v-if="subtitle" class="page-subtitle">{{ subtitle }}</div>
        </div>
      </div>
      <slot name="header-extra" />
    </div>

    <div class="form-layout">
      <section class="form-main page-card">
        <div class="form-body">
          <slot />
        </div>
        <div v-if="showFooter" class="form-footer">
          <slot name="footer">
            <el-button @click="$emit('cancel')">{{ cancelText }}</el-button>
            <el-button type="primary" :loading="loading" @click="$emit('submit')">{{ submitText }}</el-button>
          </slot>
        </div>
      </section>

      <aside class="form-aside">
        <div v-if="$slots.aside" class="aside-card">
          <div class="aside-title">
            <el-icon><InfoFilled /></el-icon>
            <span>{{ asideTitle }}</span>
          </div>
          <slot name="aside" />
        </div>
        <slot name="aside-extra" />
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string
    subtitle?: string
    submitText?: string
    cancelText?: string
    loading?: boolean
    showFooter?: boolean
    back?: boolean
    asideTitle?: string
  }>(),
  {
    submitText: '保存',
    cancelText: '取消',
    loading: false,
    showFooter: true,
    back: false,
    asideTitle: '辅助信息'
  }
)

defineEmits<{
  (e: 'submit'): void
  (e: 'cancel'): void
}>()
</script>

<style scoped lang="scss">
.form-layout {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(260px, 1fr);
  gap: 12px;
  align-items: start;
}
.form-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.form-body {
  padding: 18px;
  min-height: 320px;
}
.form-footer {
  border-top: 1px solid var(--border-color);
  padding: 12px 18px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  background: #fcfdff;
  border-radius: 0 0 var(--radius) var(--radius);
  position: sticky;
  bottom: 0;
  z-index: 2;
}
.form-aside {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}
.aside-card {
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: var(--radius);
  box-shadow: var(--shadow-card);
  padding: 14px;
}
.aside-title {
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
  padding-bottom: 10px;
  margin-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
  color: var(--text-main);
}
@media (max-width: 1100px) {
  .form-layout {
    grid-template-columns: 1fr;
  }
}
</style>
