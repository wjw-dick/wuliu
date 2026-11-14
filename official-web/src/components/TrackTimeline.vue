<script setup lang="ts">
import type { TrackResult } from '../types'
import { formatDate, money } from '../utils/format'

defineProps<{ track: TrackResult }>()
</script>

<template>
  <div class="track-detail">
    <div class="track-summary">
      <div class="track-summary__top">
        <div>
          <span class="small muted">运单号</span>
          <p class="mono track-summary__no">{{ track.waybillNo }}</p>
        </div>
        <span class="track-status">{{ track.statusName || track.status }}</span>
      </div>
      <div class="track-route">
        <div>
          <span>{{ track.origin }}</span>
          <small>始发</small>
        </div>
        <div class="track-route__line" aria-hidden="true"><i></i></div>
        <div>
          <span>{{ track.destination }}</span>
          <small>目的</small>
        </div>
      </div>
      <dl class="track-meta">
        <div><dt>货物</dt><dd>{{ track.cargoName || '—' }}</dd></div>
        <div><dt>件数 / 重量</dt><dd>{{ track.pieces || 0 }} 件 / {{ money(track.weight) }} kg</dd></div>
        <div><dt>司机 / 车牌</dt><dd>{{ track.driverName || '—' }} · {{ track.vehicleNo || '—' }}</dd></div>
        <div><dt>预计到达</dt><dd>{{ formatDate(track.estimatedArrival) }}</dd></div>
      </dl>
    </div>

    <div class="track-timeline">
      <div class="track-timeline__head">
        <h2>全程轨迹</h2>
        <span class="small muted">共 {{ track.nodes?.length || 0 }} 个节点</span>
      </div>
      <ol v-if="track.nodes?.length" class="timeline">
        <li
          v-for="(node, index) in track.nodes"
          :key="`${node.time}-${index}`"
          class="timeline__item"
          :class="{ 'timeline__item--active': index === 0 }"
        >
          <span class="timeline__dot" aria-hidden="true"></span>
          <div class="timeline__time">{{ node.time }}</div>
          <div class="timeline__status">{{ node.statusName || node.status }}</div>
          <p class="timeline__desc">{{ node.description || '货物状态已更新' }}</p>
          <p v-if="node.location" class="timeline__loc">📍 {{ node.location }}</p>
        </li>
      </ol>
      <p v-else class="small muted">暂无轨迹节点，请稍后再试或联系客服。</p>
    </div>
  </div>
</template>

<style scoped>
.track-detail {
  display: grid;
  grid-template-columns: 0.9fr 1.1fr;
  gap: 28px;
  align-items: start;
}

.track-summary {
  padding: 22px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: linear-gradient(160deg, #f7fbff, #fff);
}

.track-summary__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.track-summary__no {
  font-size: 17px;
  font-weight: 700;
  margin-top: 2px;
}

.track-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: #eaf3ff;
  color: var(--primary);
  font-size: 12px;
  font-weight: 700;
  padding: 5px 11px;
  border-radius: 999px;
}

.track-status::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.track-route {
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 22px 0;
}

.track-route > div:not(.track-route__line) {
  display: flex;
  flex-direction: column;
}

.track-route span {
  font-size: 20px;
  font-weight: 700;
}

.track-route small {
  font-size: 11px;
  color: var(--muted-light);
}

.track-route__line {
  flex: 1;
  position: relative;
  height: 2px;
  background: repeating-linear-gradient(90deg, #b9d4f7 0 6px, transparent 6px 11px);
}

.track-route__line::before,
.track-route__line::after {
  content: '';
  position: absolute;
  top: -3px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #1f6feb;
}

.track-route__line::before {
  left: 0;
}

.track-route__line::after {
  right: 0;
  background: #fff;
  border: 2px solid #1f6feb;
}

.track-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin: 0;
  padding-top: 18px;
  border-top: 1px solid var(--line);
}

.track-meta dt {
  font-size: 11px;
  color: var(--muted-light);
}

.track-meta dd {
  margin: 3px 0 0;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.4;
}

.track-timeline {
  padding: 22px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: #fff;
  box-shadow: var(--shadow-sm);
}

.track-timeline__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 20px;
}

.track-timeline__head h2 {
  font-size: 17px;
}

.timeline {
  max-height: 540px;
  overflow-y: auto;
  padding-right: 6px;
}

@media (max-width: 900px) {
  .track-detail {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 520px) {
  .track-meta {
    grid-template-columns: 1fr;
  }
}
</style>
