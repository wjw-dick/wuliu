<template>
  <div class="kpi-card" :class="{ clickable }" @click="$emit('click')">
    <div class="kpi-top">
      <span class="kpi-title">{{ title }}</span>
      <span class="kpi-icon" :style="{ background: softColor, color: color }">
        <el-icon><component :is="icon" /></el-icon>
      </span>
    </div>
    <div class="kpi-value" :style="{ color: valueColor || 'var(--text-main)' }">
      <span v-if="prefix" class="kpi-prefix">{{ prefix }}</span>{{ value }}<span v-if="unit" class="kpi-unit">{{ unit }}</span>
    </div>
    <div class="kpi-bottom">
      <span v-if="trend !== undefined" class="kpi-trend" :class="Number(trend) >= 0 ? 'up' : 'down'">
        <el-icon><component :is="Number(trend) >= 0 ? 'Top' : 'Bottom'" /></el-icon>
        {{ Math.abs(Number(trend)) }}%
      </span>
      <span class="kpi-label">{{ trendLabel }}</span>
      <span v-if="extra" class="kpi-extra">{{ extra }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    title: string
    value: string | number
    unit?: string
    prefix?: string
    icon?: string
    color?: string
    trend?: number | string
    trendLabel?: string
    extra?: string
    valueColor?: string
    clickable?: boolean
  }>(),
  {
    icon: 'DataLine',
    color: '#1f6feb',
    trendLabel: '较昨日',
    clickable: false
  }
)

const softColor = computed(() => `${props.color}14`)
</script>

<style scoped lang="scss">
.kpi-card {
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: var(--radius);
  box-shadow: var(--shadow-card);
  padding: 14px 16px;
  min-width: 0;
  position: relative;
  overflow: hidden;
  transition: transform 0.15s, box-shadow 0.15s;

  &.clickable {
    cursor: pointer;
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 6px 18px rgba(31, 111, 235, 0.12);
    }
  }
}
.kpi-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.kpi-title {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.kpi-icon {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex: 0 0 30px;
}
.kpi-value {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.25;
  margin-top: 6px;
  letter-spacing: -0.5px;
  font-family: 'DIN Alternate', 'SFMono-Regular', Consolas, sans-serif;
  white-space: nowrap;
}
.kpi-prefix {
  font-size: 14px;
  margin-right: 2px;
  font-weight: 500;
  opacity: 0.8;
}
.kpi-unit {
  font-size: 12px;
  font-weight: 400;
  margin-left: 3px;
  color: var(--text-muted);
}
.kpi-bottom {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 4px;
  font-size: 11px;
  color: var(--text-muted);
  min-height: 16px;
}
.kpi-trend {
  display: inline-flex;
  align-items: center;
  gap: 1px;
  font-weight: 600;
  &.up {
    color: #16a34a;
  }
  &.down {
    color: #dc2626;
  }
}
.kpi-extra {
  margin-left: auto;
  color: var(--text-secondary);
}
</style>
