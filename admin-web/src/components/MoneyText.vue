<template>
  <span class="money-text" :class="classes">
    <span v-if="symbol" class="symbol">¥</span>{{ formatted }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatMoney } from '@/utils/format'

const props = withDefaults(
  defineProps<{
    value?: number | string | null
    symbol?: boolean
    strong?: boolean
    colored?: boolean
    size?: 'small' | 'normal' | 'large'
  }>(),
  { symbol: true, strong: false, colored: false, size: 'normal' }
)

const formatted = computed(() => formatMoney(props.value, false))
const classes = computed(() => [
  { strong: props.strong },
  props.size,
  props.colored ? (Number(props.value || 0) < 0 ? 'negative' : Number(props.value || 0) > 0 ? 'positive' : '') : ''
])
</script>

<style scoped>
.money-text {
  font-family: 'DIN Alternate', 'SFMono-Regular', Consolas, monospace;
  font-size: 12.5px;
  white-space: nowrap;
}
.money-text.strong {
  font-weight: 600;
}
.money-text.large {
  font-size: 17px;
  font-weight: 700;
}
.money-text.small {
  font-size: 11.5px;
}
.symbol {
  font-size: 0.9em;
  margin-right: 1px;
  opacity: 0.8;
}
.positive {
  color: #16a34a;
}
.negative {
  color: #dc2626;
}
</style>
