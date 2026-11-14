<template>
  <el-tag :type="type" :effect="effect" size="small" round class="status-tag">
    <span class="dot" :class="`dot-${type}`"></span>{{ label }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { statusType, translateCode } from '@/utils/dict'

const props = withDefaults(
  defineProps<{
    status?: string | null
    effect?: 'dark' | 'light' | 'plain'
  }>(),
  { effect: 'light' }
)

const label = computed(() => translateCode(props.status))
const type = computed(() => statusType(props.status))
</script>

<style scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  font-size: 11.5px;
  height: 21px;
  padding: 0 8px;
}
.dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  margin-right: 4px;
  background: currentColor;
  opacity: 0.85;
}
</style>
