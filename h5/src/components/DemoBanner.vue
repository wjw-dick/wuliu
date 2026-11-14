<script setup lang="ts">
import { computed, ref } from 'vue'
import { demoMode, demoNotice } from '../api/client'

const dismissed = ref(false)
const visible = computed(() => demoMode.value && !dismissed.value)
</script>

<template>
  <div v-if="visible" class="demo-banner" role="status">
    <span aria-hidden="true">◆</span>
    <span>
      <strong>演示数据</strong> · 后端未连接（{{ demoNotice || '网络异常' }}），当前为内置示例内容，接口恢复后刷新即可加载真实数据。
    </span>
    <button class="demo-banner__close" type="button" aria-label="关闭演示提示" @click="dismissed = true">×</button>
  </div>
</template>
