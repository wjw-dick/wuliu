<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { getReceipt } from '../api/h5'
import type { ReceiptData } from '../types'
import { formatDateTime } from '../utils/format'
import { showToast } from '../composables/useToast'

const route = useRoute()
const receipt = ref<ReceiptData | null>(null)
const loading = ref(true)
const error = ref('')
const viewerIndex = ref<number | null>(null)
const copied = ref(false)

const waybillNo = computed(() => String(route.params.waybillNo || ''))
const sign = computed(() => (typeof route.query.sign === 'string' ? route.query.sign : ''))

const mapLink = computed(() => {
  if (!receipt.value) return '#'
  const { lng, lat, address } = receipt.value.coordinates
  return `https://uri.amap.com/marker?position=${lng},${lat}&name=${encodeURIComponent(address)}`
})

const viewerImage = computed(() => {
  if (viewerIndex.value === null || !receipt.value) return null
  return receipt.value.images[viewerIndex.value] || null
})

async function load() {
  loading.value = true
  error.value = ''
  viewerIndex.value = null
  try {
    receipt.value = await getReceipt(waybillNo.value, sign.value)
    document.title = `电子回单 ${receipt.value.waybillNo} · 驰远物流`
  } catch {
    error.value = '回单加载失败，请检查网络后重试。'
    receipt.value = null
  } finally {
    loading.value = false
  }
}

function openViewer(index: number) {
  viewerIndex.value = index
}

function closeViewer() {
  viewerIndex.value = null
}

function stepViewer(step: number) {
  if (viewerIndex.value === null || !receipt.value) return
  const total = receipt.value.images.length
  viewerIndex.value = (viewerIndex.value + step + total) % total
}

let touchStartX = 0

function onTouchStart(event: TouchEvent) {
  touchStartX = event.changedTouches[0]?.clientX || 0
}

function onTouchEnd(event: TouchEvent) {
  const endX = event.changedTouches[0]?.clientX || 0
  const delta = endX - touchStartX
  if (Math.abs(delta) < 40) return
  stepViewer(delta > 0 ? -1 : 1)
}

function onKeydown(event: KeyboardEvent) {
  if (viewerIndex.value === null) return
  if (event.key === 'Escape') closeViewer()
  if (event.key === 'ArrowLeft') stepViewer(-1)
  if (event.key === 'ArrowRight') stepViewer(1)
}

async function shareReceipt() {
  const url = window.location.href
  try {
    if (navigator.share) {
      await navigator.share({
        title: `驰远物流电子回单 ${waybillNo.value}`,
        text: '电子回单已签收，点击查看签收信息与回单照片。',
        url
      })
      return
    }
    await navigator.clipboard.writeText(url)
    copied.value = true
    showToast('回单链接已复制', 'success')
    window.setTimeout(() => {
      copied.value = false
    }, 1800)
  } catch {
    showToast('分享失败，请长按地址栏复制链接', 'error')
  }
}

function downloadPdf() {
  viewerIndex.value = null
  if (receipt.value?.pdfUrl) {
    window.open(receipt.value.pdfUrl, '_blank', 'noopener')
    return
  }
  showToast('演示环境：调起浏览器打印，选择“另存为 PDF”即可', 'info', 3200)
  window.setTimeout(() => window.print(), 350)
}

watch(
  () => route.fullPath,
  () => {
    void load()
  },
  { immediate: true }
)

watch(viewerIndex, (value) => {
  document.body.style.overflow = value === null ? '' : 'hidden'
})

onMounted(() => {
  window.addEventListener('keydown', onKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKeydown)
  document.body.style.overflow = ''
})
</script>

<template>
  <div class="page receipt-page receipt-print">
    <template v-if="loading">
      <div class="card hero-skeleton">
        <div class="skeleton" style="height: 18px; width: 40%"></div>
        <div class="skeleton" style="height: 34px; width: 72%; margin-top: 14px"></div>
        <div class="skeleton" style="height: 12px; width: 54%; margin-top: 10px"></div>
      </div>
      <div class="card block-skeleton">
        <div v-for="n in 5" :key="n" class="skeleton" style="height: 16px"></div>
      </div>
      <div class="card block-skeleton">
        <div class="skeleton" style="height: 90px"></div>
      </div>
    </template>

    <div v-else-if="error || !receipt" class="state-block">
      <div class="state-block__icon" aria-hidden="true">!</div>
      <h3>回单暂时无法打开</h3>
      <p>{{ error || '未找到该运单的电子回单，请确认链接是否完整或联系客服。' }}</p>
      <button class="btn btn--primary btn--sm" type="button" @click="load">重新加载</button>
      <RouterLink class="state-link" to="/quote-share">返回报价分享</RouterLink>
    </div>

    <template v-else>
      <section class="receipt-hero">
        <div class="receipt-hero__top">
          <span class="receipt-hero__label">电子回单</span>
          <span class="receipt-stamp">{{ receipt.statusName || receipt.status }}</span>
        </div>
        <p class="receipt-hero__no mono">{{ receipt.waybillNo }}</p>
        <p class="receipt-hero__time">签收时间 {{ formatDateTime(receipt.signedAt) }}</p>
        <div class="receipt-hero__foot">
          <span>签收人 {{ receipt.signerName }}</span>
          <span>{{ receipt.signerType }}</span>
        </div>
      </section>

      <section class="card block">
        <div class="block__head">
          <h2>运单信息</h2>
          <span class="tag tag--blue">已签收</span>
        </div>
        <div class="party">
          <span class="party__dot party__dot--send">发</span>
          <div>
            <strong>{{ receipt.sender.name }} <small>{{ receipt.sender.phone }}</small></strong>
            <p>{{ receipt.sender.address }}</p>
          </div>
        </div>
        <div class="party-line" aria-hidden="true"></div>
        <div class="party">
          <span class="party__dot party__dot--recv">收</span>
          <div>
            <strong>{{ receipt.receiver.name }} <small>{{ receipt.receiver.phone }}</small></strong>
            <p>{{ receipt.receiver.address }}</p>
          </div>
        </div>
        <dl class="receipt-info">
          <div class="info-row">
            <dt>货物</dt>
            <dd>{{ receipt.cargo.name }}</dd>
          </div>
          <div class="info-row">
            <dt>件数 / 重量 / 体积</dt>
            <dd>{{ receipt.cargo.pieces }} 件 / {{ receipt.cargo.weight }} kg / {{ receipt.cargo.volume }} m³</dd>
          </div>
          <div class="info-row">
            <dt>承运司机</dt>
            <dd>
              {{ receipt.driver.name }} ·
              <a :href="`tel:${receipt.driver.phone}`">{{ receipt.driver.phone }}</a>
            </dd>
          </div>
          <div class="info-row">
            <dt>车牌号</dt>
            <dd class="mono">{{ receipt.driver.vehicleNo }}</dd>
          </div>
          <div class="info-row">
            <dt>承运方</dt>
            <dd>{{ receipt.company.name }}</dd>
          </div>
        </dl>
        <p v-if="receipt.remark" class="receipt-remark">{{ receipt.remark }}</p>
      </section>

      <section class="card block signature-block">
        <div class="block__head">
          <h2>签收信息</h2>
          <span class="verify-pill">已存证</span>
        </div>
        <div class="signature-area">
          <img v-if="receipt.signatureImage" :src="receipt.signatureImage" alt="收货人签名" />
          <span v-else class="signature-fallback">{{ receipt.signerName }}</span>
          <small>收货人签名</small>
        </div>
        <dl class="receipt-info">
          <div class="info-row">
            <dt>签收人</dt>
            <dd>{{ receipt.signerName }}（{{ receipt.signerType }}）</dd>
          </div>
          <div class="info-row">
            <dt>联系电话</dt>
            <dd>{{ receipt.signerPhone }}</dd>
          </div>
          <div class="info-row">
            <dt>签收时间</dt>
            <dd>{{ formatDateTime(receipt.signedAt) }}</dd>
          </div>
        </dl>
        <a class="location-card" :href="mapLink" target="_blank" rel="noopener">
          <span class="location-card__pin" aria-hidden="true"></span>
          <div>
            <strong>签收位置</strong>
            <p>{{ receipt.coordinates.address }}</p>
            <small class="mono">{{ receipt.coordinates.lat }}, {{ receipt.coordinates.lng }}</small>
          </div>
          <span class="location-card__arrow" aria-hidden="true">›</span>
        </a>
      </section>

      <section class="card block gallery-block">
        <div class="block__head">
          <h2>回单照片</h2>
          <span class="small muted">{{ receipt.images.length }} 张</span>
        </div>
        <div class="gallery">
          <button
            v-for="(image, index) in receipt.images"
            :key="image.url"
            class="gallery__item"
            type="button"
            :aria-label="`查看${image.caption}`"
            @click="openViewer(index)"
          >
            <img :src="image.url" :alt="image.caption" loading="lazy" />
            <span class="gallery__caption">{{ image.caption }}</span>
            <span class="gallery__zoom" aria-hidden="true">⤢</span>
          </button>
        </div>
      </section>

      <p class="verify-note">
        本回单由驰远物流系统生成，签收时间、位置与影像已加密存证。如有疑问请致电
        <a href="tel:4008801688">400-880-1688</a>。
      </p>
    </template>

    <div v-if="receipt" class="bottom-bar no-print">
      <button class="btn btn--outline bottom-bar__ghost" type="button" @click="shareReceipt">
        {{ copied ? '已复制' : '分享回单' }}
      </button>
      <button class="btn btn--primary" type="button" @click="downloadPdf">下载 PDF</button>
    </div>

    <Teleport to="body">
      <div
        v-if="viewerImage"
        class="viewer"
        role="dialog"
        aria-modal="true"
        :aria-label="viewerImage.caption"
        @click.self="closeViewer"
        @touchstart.passive="onTouchStart"
        @touchend.passive="onTouchEnd"
      >
        <div class="viewer__top">
          <span>{{ viewerIndex! + 1 }} / {{ receipt?.images.length }}</span>
          <button type="button" aria-label="关闭预览" @click="closeViewer">×</button>
        </div>
        <div class="viewer__stage" @click.self="closeViewer">
          <img :src="viewerImage.url" :alt="viewerImage.caption" />
        </div>
        <div class="viewer__bottom">
          <p>{{ viewerImage.caption }}</p>
          <div class="viewer__nav">
            <button type="button" aria-label="上一张" @click="stepViewer(-1)">‹</button>
            <button type="button" aria-label="下一张" @click="stepViewer(1)">›</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.receipt-page {
  padding-top: 12px;
}

.hero-skeleton {
  padding: 20px;
}

.block-skeleton {
  margin-top: 12px;
  padding: 18px 16px;
  display: grid;
  gap: 12px;
}

.receipt-hero {
  position: relative;
  overflow: hidden;
  padding: 20px;
  border-radius: 16px;
  color: #fff;
  background: radial-gradient(280px 180px at 90% 0%, rgba(76, 154, 255, 0.55), transparent 65%),
    linear-gradient(145deg, #082446, #0b3d84 65%, #1557c0);
  box-shadow: 0 12px 30px rgba(10, 43, 94, 0.22);
}

.receipt-hero::after {
  content: '';
  position: absolute;
  right: -30px;
  bottom: -50px;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  border: 20px solid rgba(255, 255, 255, 0.07);
}

.receipt-hero__top {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.receipt-hero__label {
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: 0.14em;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 5px;
  padding: 2px 8px;
}

.receipt-stamp {
  position: relative;
  width: 58px;
  height: 58px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.5);
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
  color: #dcecff;
  transform: rotate(-12deg);
  letter-spacing: 0.06em;
}

.receipt-stamp::before {
  content: '';
  position: absolute;
  inset: 4px;
  border-radius: 50%;
  border: 1px dashed rgba(255, 255, 255, 0.32);
}

.receipt-hero__no {
  position: relative;
  z-index: 1;
  margin-top: 16px;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.receipt-hero__time {
  position: relative;
  z-index: 1;
  margin-top: 4px;
  font-size: 11.5px;
  color: rgba(255, 255, 255, 0.65);
}

.receipt-hero__foot {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.14);
  font-size: 11.5px;
  color: rgba(255, 255, 255, 0.72);
}

.block {
  margin-top: 12px;
  padding: 16px;
}

.block__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.block__head h2 {
  font-size: 14.5px;
}

.party {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.party__dot {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  border-radius: 6px;
  display: grid;
  place-items: center;
  font-size: 10px;
  font-weight: 700;
  margin-top: 1px;
}

.party__dot--send {
  background: #eef5ff;
  color: var(--primary);
}

.party__dot--recv {
  background: #eef8f2;
  color: var(--green);
}

.party strong {
  font-size: 13px;
  display: block;
  line-height: 1.4;
}

.party strong small {
  font-size: 11px;
  color: var(--muted-light);
  font-weight: 400;
  margin-left: 4px;
}

.party p {
  font-size: 11.5px;
  color: var(--muted);
  line-height: 1.55;
  margin-top: 1px;
}

.party-line {
  width: 1px;
  height: 16px;
  margin: 3px 0 3px 10.5px;
  border-left: 1px dashed #cfdaea;
}

.receipt-info {
  margin: 12px 0 0;
  border-top: 1px solid var(--line);
  padding-top: 4px;
}

.receipt-info a {
  color: var(--primary);
}

.receipt-remark {
  margin-top: 10px;
  padding: 9px 11px;
  border-radius: 8px;
  background: #f7faff;
  font-size: 11px;
  color: var(--muted);
  line-height: 1.55;
}

.signature-area {
  position: relative;
  height: 118px;
  border-radius: 9px;
  background: #fdfcf9;
  border: 1px solid #ece6da;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  overflow: hidden;
}

.signature-area img {
  width: 78%;
  max-height: 94%;
  object-fit: contain;
}

.signature-fallback {
  font-family: 'KaiTi', 'STKaiti', serif;
  font-size: 42px;
  color: #1b3a6b;
  transform: rotate(-4deg);
}

.signature-area small {
  position: absolute;
  right: 10px;
  bottom: 6px;
  font-size: 9.5px;
  color: #b3a99a;
}

.verify-pill {
  font-size: 10px;
  color: var(--green);
  background: #eef8f2;
  border-radius: 999px;
  padding: 2px 8px;
}

.location-card {
  display: flex;
  align-items: center;
  gap: 11px;
  margin-top: 12px;
  padding: 11px 12px;
  border-radius: 10px;
  background: #f6faff;
  border: 1px solid #e2eefb;
}

.location-card__pin {
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  border-radius: 50%;
  background: #e6f1ff;
  position: relative;
}

.location-card__pin::before {
  content: '';
  position: absolute;
  left: 10px;
  top: 8px;
  width: 10px;
  height: 10px;
  border-radius: 50% 50% 50% 0;
  background: var(--primary);
  transform: rotate(-45deg);
}

.location-card > div {
  flex: 1;
  min-width: 0;
}

.location-card strong {
  font-size: 11.5px;
  color: #32465e;
}

.location-card p {
  font-size: 11px;
  color: var(--muted);
  line-height: 1.5;
  margin-top: 1px;
}

.location-card small {
  font-size: 9.5px;
  color: var(--muted-light);
}

.location-card__arrow {
  color: #b9c4d2;
  font-size: 20px;
}

.gallery {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.gallery__item {
  position: relative;
  aspect-ratio: 4 / 5;
  overflow: hidden;
  border-radius: 9px;
  padding: 0;
  background: #eef2f7;
}

.gallery__item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.2s ease;
}

.gallery__item:active img {
  transform: scale(1.02);
}

.gallery__caption {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 18px 9px 7px;
  font-size: 10.5px;
  color: #fff;
  text-align: left;
  background: linear-gradient(transparent, rgba(7, 26, 51, 0.72));
}

.gallery__zoom {
  position: absolute;
  right: 6px;
  top: 6px;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: grid;
  place-items: center;
  background: rgba(7, 26, 51, 0.5);
  color: #fff;
  font-size: 12px;
}

.verify-note {
  margin: 16px 4px 0;
  font-size: 10.5px;
  color: var(--muted-light);
  line-height: 1.7;
  text-align: center;
}

.verify-note a {
  color: var(--primary);
}

.state-link {
  display: block;
  margin-top: 14px;
  font-size: 12.5px;
  color: var(--primary);
}

/* Fullscreen viewer */
.viewer {
  position: fixed;
  inset: 0;
  z-index: 500;
  background: #050b14;
  display: flex;
  flex-direction: column;
  animation: fadeIn 0.16s ease;
}

.viewer__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  padding-top: calc(12px + env(safe-area-inset-top));
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
}

.viewer__top button {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  color: #fff;
  font-size: 28px;
  line-height: 1;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.08);
}

.viewer__stage {
  flex: 1;
  min-height: 0;
  display: grid;
  place-items: center;
  padding: 8px 12px;
}

.viewer__stage img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 4px;
}

.viewer__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 18px calc(22px + env(safe-area-inset-bottom));
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
}

.viewer__nav {
  display: flex;
  gap: 8px;
}

.viewer__nav button {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  font-size: 26px;
  line-height: 1;
  display: grid;
  place-items: center;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
}

@media print {
  :global(body) {
    background: #fff;
  }

  .receipt-page {
    padding: 0 !important;
  }

  .receipt-hero {
    color: #0e1b2c !important;
    background: #fff !important;
    box-shadow: none !important;
    border: 1px solid #ccc;
  }

  .receipt-hero__no,
  .receipt-hero__time,
  .receipt-hero__foot,
  .receipt-hero__label,
  .receipt-stamp {
    color: #0e1b2c !important;
    border-color: #666 !important;
  }

  .receipt-stamp {
    transform: none;
  }

  .gallery-block {
    display: none;
  }

  .block {
    break-inside: avoid;
    border: 1px solid #ddd;
    box-shadow: none;
  }
}
</style>
