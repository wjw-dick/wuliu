<script setup lang="ts">
import { computed, ref } from 'vue'
import { demoMode, demoNotice } from '../api/client'

const dismissed = ref(false)
const visible = computed(() => demoMode.value && !dismissed.value)
</script>

<template>
  <div v-if="visible" class="demo-banner" role="status">
    <div class="demo-banner__inner">
      <span aria-hidden="true">◆</span>
      <span>
        <strong>演示数据</strong>
        — 后端接口暂未连接（{{ demoNotice || '网络异常' }}），当前展示内置示例内容；接口恢复后刷新页面即可加载真实数据。
      </span>
      <button class="demo-banner__close" type="button" aria-label="关闭演示提示" @click="dismissed = true">
        ×
      </button>
    </div>
  </div>
</template>
