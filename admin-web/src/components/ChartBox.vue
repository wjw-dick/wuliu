<template>
  <div class="chart-box" :style="{ height: height + 'px' }">
    <div v-if="loading" v-loading="true" class="chart-loading" element-loading-text="图表加载中" />
    <div ref="chartRef" class="chart-canvas" />
    <div v-if="empty && !loading" class="chart-empty">暂无图表数据</div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = withDefaults(
  defineProps<{
    option: any
    height?: number
    loading?: boolean
    empty?: boolean
  }>(),
  { height: 300, loading: false, empty: false }
)

const chartRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null
let observer: ResizeObserver | null = null

function render(): void {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  if (props.option) chart.setOption(props.option, true)
}

onMounted(async () => {
  await nextTick()
  render()
  if (chartRef.value) {
    observer = new ResizeObserver(() => chart?.resize())
    observer.observe(chartRef.value)
  }
})

watch(
  () => props.option,
  () => nextTick(render),
  { deep: true }
)

onBeforeUnmount(() => {
  observer?.disconnect()
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.chart-box {
  position: relative;
  width: 100%;
}
.chart-canvas {
  width: 100%;
  height: 100%;
}
.chart-loading {
  position: absolute;
  inset: 0;
  z-index: 2;
}
.chart-empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 12px;
}
</style>
