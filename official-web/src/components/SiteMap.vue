<script setup lang="ts">
import { computed, ref } from 'vue'
import type { Site } from '../types'

const props = defineProps<{
  sites: Site[]
  selectedId?: number | null
}>()

const emit = defineEmits<{ (e: 'select', site: Site): void }>()

const hoveredId = ref<number | null>(null)

const points = computed(() => {
  if (!props.sites.length) return []
  const lats = props.sites.map((s) => Number(s.lat) || 0)
  const lngs = props.sites.map((s) => Number(s.lng) || 0)
  const minLat = Math.min(...lats)
  const maxLat = Math.max(...lats)
  const minLng = Math.min(...lngs)
  const maxLng = Math.max(...lngs)
  return props.sites.map((site) => {
    const latRatio = maxLat === minLat ? 0.5 : (Number(site.lat) - minLat) / (maxLat - minLat)
    const lngRatio = maxLng === minLng ? 0.5 : (Number(site.lng) - minLng) / (maxLng - minLng)
    return {
      site,
      x: 9 + lngRatio * 82,
      y: 85 - latRatio * 70
    }
  })
})

const pathData = computed(() => {
  if (points.value.length < 2) return ''
  return points.value
    .map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x.toFixed(2)} ${point.y.toFixed(2)}`)
    .join(' ')
})

const activeSite = computed(
  () =>
    props.sites.find((site) => site.id === props.selectedId) ||
    props.sites.find((site) => site.id === hoveredId.value) ||
    props.sites[0] ||
    null
)

function select(site: Site) {
  hoveredId.value = site.id
  emit('select', site)
}
</script>

<template>
  <div class="site-map" role="img" aria-label="驰远物流长三角网点分布示意图">
    <div class="site-map__bg" aria-hidden="true">
      <span class="blob blob--1"></span>
      <span class="blob blob--2"></span>
      <span class="blob blob--3"></span>
      <span class="river"></span>
    </div>

    <svg class="site-map__routes" viewBox="0 0 100 100" preserveAspectRatio="none" aria-hidden="true">
      <path :d="pathData" />
    </svg>

    <span class="site-map__label site-map__label--a" aria-hidden="true">长江三角洲</span>
    <span class="site-map__label site-map__label--b" aria-hidden="true">G60 科创走廊</span>
    <span class="site-map__compass" aria-hidden="true">N</span>

    <button
      v-for="point in points"
      :key="point.site.id"
      class="pin"
      :class="{ 'pin--active': activeSite?.id === point.site.id }"
      type="button"
      :style="{ left: `${point.x}%`, top: `${point.y}%` }"
      :aria-label="`${point.site.name}，${point.site.address}`"
      @mouseenter="hoveredId = point.site.id"
      @mouseleave="hoveredId = null"
      @focus="hoveredId = point.site.id"
      @blur="hoveredId = null"
      @click="select(point.site)"
    >
      <span class="pin__pulse"></span>
      <span class="pin__dot"></span>
      <span class="pin__label">{{ point.site.city }}</span>
    </button>

    <div v-if="activeSite" class="site-map__card">
      <span class="site-map__card-tag">{{ activeSite.type || '营业网点' }}</span>
      <strong>{{ activeSite.name }}</strong>
      <p>{{ activeSite.address }}</p>
      <div class="site-map__card-row">
        <a :href="`tel:${activeSite.phone}`" @click.stop>{{ activeSite.phone }}</a>
        <span>{{ activeSite.businessHours }}</span>
      </div>
    </div>

    <div class="site-map__legend">
      <span><i></i> 网点 / 分拨中心</span>
      <span class="site-map__legend-line"></span>
      <span>干线直连</span>
    </div>
  </div>
</template>

<style scoped>
.site-map {
  position: relative;
  min-height: 420px;
  border-radius: 18px;
  overflow: hidden;
  background: linear-gradient(140deg, #eef6ff 0%, #e3efff 48%, #f4f9ff 100%);
  border: 1px solid #d6e6fb;
  isolation: isolate;
}

.site-map::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(31, 111, 235, 0.07) 1px, transparent 1px),
    linear-gradient(90deg, rgba(31, 111, 235, 0.07) 1px, transparent 1px);
  background-size: 44px 44px;
  mask-image: radial-gradient(circle at 55% 55%, #000 30%, transparent 85%);
  z-index: 0;
}

.site-map__bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.blob {
  position: absolute;
  border-radius: 46% 54% 62% 38% / 44% 40% 60% 56%;
  background: rgba(142, 190, 255, 0.3);
  filter: blur(2px);
}

.blob--1 {
  width: 48%;
  height: 54%;
  left: 12%;
  top: 16%;
  transform: rotate(-8deg);
}

.blob--2 {
  width: 42%;
  height: 46%;
  right: 8%;
  top: 24%;
  background: rgba(137, 214, 192, 0.22);
  transform: rotate(14deg);
}

.blob--3 {
  width: 26%;
  height: 30%;
  left: 48%;
  bottom: 6%;
  background: rgba(167, 199, 255, 0.35);
}

.river {
  position: absolute;
  left: -5%;
  right: -5%;
  top: 40%;
  height: 22px;
  border-top: 5px solid rgba(93, 160, 245, 0.3);
  border-radius: 50%;
  transform: rotate(-9deg);
}

.river::after {
  content: '';
  position: absolute;
  left: 18%;
  top: 10px;
  width: 64%;
  border-top: 3px solid rgba(93, 160, 245, 0.18);
  border-radius: 50%;
}

.site-map__routes {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
}

.site-map__routes path {
  fill: none;
  stroke: #1f6feb;
  stroke-width: 1.3;
  stroke-dasharray: 2.4 1.8;
  stroke-linecap: round;
  opacity: 0.45;
  vector-effect: non-scaling-stroke;
}

.site-map__label {
  position: absolute;
  z-index: 1;
  font-size: 11px;
  letter-spacing: 0.18em;
  color: rgba(23, 78, 156, 0.32);
  font-weight: 600;
}

.site-map__label--a {
  left: 7%;
  top: 12%;
}

.site-map__label--b {
  right: 7%;
  bottom: 26%;
}

.site-map__compass {
  position: absolute;
  right: 16px;
  top: 14px;
  z-index: 2;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid rgba(31, 111, 235, 0.24);
  background: rgba(255, 255, 255, 0.7);
  display: grid;
  place-items: center;
  font-size: 11px;
  font-weight: 700;
  color: #1f6feb;
}

.pin {
  position: absolute;
  z-index: 3;
  transform: translate(-50%, -50%);
  border: 0;
  background: transparent;
  padding: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.pin__dot {
  width: 13px;
  height: 13px;
  border-radius: 50%;
  background: #1f6feb;
  border: 3px solid #fff;
  box-shadow: 0 4px 10px rgba(31, 111, 235, 0.45);
  transition: transform 0.18s ease;
}

.pin__pulse {
  position: absolute;
  top: -3px;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: rgba(31, 111, 235, 0.16);
  animation: pinPulse 2.6s ease-out infinite;
}

.pin__label {
  font-size: 11px;
  font-weight: 600;
  color: #174e9c;
  background: rgba(255, 255, 255, 0.82);
  border-radius: 5px;
  padding: 1px 5px;
  white-space: nowrap;
  opacity: 0;
  transform: translateY(-3px);
  transition: opacity 0.16s ease, transform 0.16s ease;
}

.pin:hover .pin__label,
.pin:focus-visible .pin__label,
.pin--active .pin__label {
  opacity: 1;
  transform: translateY(0);
}

.pin--active .pin__dot {
  transform: scale(1.22);
  background: #0a2b5e;
}

.pin:hover .pin__dot {
  transform: scale(1.18);
}

@keyframes pinPulse {
  0% {
    transform: scale(0.6);
    opacity: 0.7;
  }
  70% {
    transform: scale(1.5);
    opacity: 0;
  }
  100% {
    opacity: 0;
  }
}

.site-map__card {
  position: absolute;
  left: 16px;
  bottom: 16px;
  z-index: 4;
  width: min(320px, calc(100% - 32px));
  padding: 14px 16px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(214, 230, 251, 0.9);
  box-shadow: 0 14px 32px rgba(10, 43, 94, 0.12);
}

.site-map__card-tag {
  display: inline-block;
  font-size: 10px;
  font-weight: 700;
  color: #1f6feb;
  background: #eef5ff;
  padding: 2px 7px;
  border-radius: 4px;
  margin-bottom: 6px;
}

.site-map__card strong {
  display: block;
  font-size: 14px;
  color: #0e1b2c;
}

.site-map__card p {
  font-size: 12px;
  color: #5c6c80;
  line-height: 1.5;
  margin: 4px 0 6px;
}

.site-map__card-row {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
  font-size: 11.5px;
  color: #8494a8;
}

.site-map__card-row a {
  color: #1f6feb;
  font-weight: 600;
}

.site-map__legend {
  position: absolute;
  right: 14px;
  bottom: 14px;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border-radius: 7px;
  background: rgba(255, 255, 255, 0.72);
  font-size: 10.5px;
  color: #5c6c80;
}

.site-map__legend i {
  display: inline-block;
  width: 8px;
  height: 8px;
  background: #1f6feb;
  border-radius: 50%;
  margin-right: 4px;
}

.site-map__legend-line {
  width: 18px;
  border-top: 1px dashed #1f6feb;
  opacity: 0.7;
}

@media (max-width: 720px) {
  .site-map {
    min-height: 340px;
  }

  .site-map__legend {
    display: none;
  }

  .site-map__card {
    left: 12px;
    right: 12px;
    bottom: 12px;
    width: auto;
  }
}

@media (prefers-reduced-motion: reduce) {
  .pin__pulse {
    display: none;
  }
}
</style>
