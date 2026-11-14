<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { createQuoteShare } from '../api/h5'
import { DEMO_QUOTE } from '../demo/data'
import type { QuoteLine, SharedQuote } from '../types'
import { formatDateTime, money } from '../utils/format'
import { showToast } from '../composables/useToast'

const route = useRoute()
const officialUrl = import.meta.env.VITE_OFFICIAL_URL || 'http://localhost:5174'

function qs(value: unknown): string {
  if (Array.isArray(value)) return String(value[0] ?? '')
  return value === null || value === undefined ? '' : String(value)
}

function qn(value: unknown, fallback: number): number {
  const parsed = Number(qs(value))
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback
}

function decodePayload(): Record<string, unknown> {
  const raw = qs(route.query.data) || qs(route.query.q)
  if (!raw) return {}
  try {
    const normalized = raw.replace(/-/g, '+').replace(/_/g, '/')
    const text = decodeURIComponent(
      Array.prototype.map
        .call(atob(normalized), (char: string) => `%${`00${char.charCodeAt(0).toString(16)}`.slice(-2)}`)
        .join('')
    )
    const parsed = JSON.parse(text)
    return parsed && typeof parsed === 'object' ? (parsed as Record<string, unknown>) : {}
  } catch {
    return {}
  }
}

const payload = decodePayload()

const feeDefinitions = [
  { key: 'baseFreight', name: '基础运费', formula: '首重 + 续重 + 里程' },
  { key: 'fuelSurcharge', name: '燃油附加', formula: '基础运费 × 10%' },
  { key: 'loadingFee', name: '装卸费', formula: '按需收取' },
  { key: 'upstairsFee', name: '上楼费', formula: '按需收取' },
  { key: 'insuranceFee', name: '保险', formula: '声明价值 × 0.3%' },
  { key: 'codFee', name: '代收手续费', formula: '代收金额 × 0.5%' }
]

function parseLines(): QuoteLine[] {
  const fromQuery = qs(route.query.lines) || qs(route.query.detail)
  const candidates: unknown[] = []
  if (fromQuery) {
    try {
      candidates.push(JSON.parse(fromQuery))
    } catch {
      /* ignore malformed query */
    }
  }
  if (Array.isArray(payload.lines)) candidates.push(payload.lines)
  if (Array.isArray(payload.detail)) candidates.push(payload.detail)

  for (const candidate of candidates) {
    if (Array.isArray(candidate) && candidate.length) {
      const lines = candidate
        .filter((item) => item && typeof item === 'object')
        .map((item) => {
          const row = item as Record<string, unknown>
          return {
            name: String(row.name || '费用'),
            amount: Number(row.amount) || 0,
            formula: row.formula ? String(row.formula) : undefined
          }
        })
      if (lines.length) return lines
    }
  }

  const hasFeeQuery = feeDefinitions.some((fee) => route.query[fee.key] !== undefined)
  if (hasFeeQuery) {
    return feeDefinitions.map((fee) => ({
      name: fee.name,
      amount: Number(qs(route.query[fee.key])) || 0,
      formula: fee.formula
    }))
  }

  return DEMO_QUOTE.lines
}

const quote = computed<SharedQuote>(() => {
  const lines = parseLines()
  const sum = lines.reduce((total, line) => total + Number(line.amount || 0), 0)
  const totalFromQuery =
    Number(qs(route.query.totalAmount)) || Number(qs(route.query.total)) || Number(payload.totalAmount) || 0
  return {
    quoteNo: qs(route.query.quoteNo) || String(payload.quoteNo || '') || DEMO_QUOTE.quoteNo,
    origin: qs(route.query.origin) || String(payload.origin || '') || DEMO_QUOTE.origin,
    destination:
      qs(route.query.destination) || String(payload.destination || '') || DEMO_QUOTE.destination,
    cargoType: qs(route.query.cargoType) || String(payload.cargoType || '') || DEMO_QUOTE.cargoType,
    weight: qn(route.query.weight, Number(payload.weight) || DEMO_QUOTE.weight),
    volume: qn(route.query.volume, Number(payload.volume) || DEMO_QUOTE.volume),
    pieces: qn(route.query.pieces, Number(payload.pieces) || DEMO_QUOTE.pieces),
    vehicleType:
      qs(route.query.vehicleType) || String(payload.vehicleType || '') || DEMO_QUOTE.vehicleType,
    customerName:
      qs(route.query.customerName) || String(payload.customerName || '') || DEMO_QUOTE.customerName,
    createdAt: qs(route.query.createdAt) || DEMO_QUOTE.createdAt,
    validUntil: qs(route.query.validUntil) || DEMO_QUOTE.validUntil,
    lines,
    totalAmount: totalFromQuery || sum || DEMO_QUOTE.totalAmount,
    remark: qs(route.query.remark) || String(payload.remark || '') || DEMO_QUOTE.remark
  }
})

const shareLoading = ref(false)
const showShare = ref(false)
const shareUrl = ref('')

const orderUrl = computed(() => {
  const params = new URLSearchParams({
    origin: quote.value.origin,
    destination: quote.value.destination,
    weight: String(quote.value.weight),
    from: 'h5-quote-share'
  })
  return `${officialUrl}/quote?${params.toString()}`
})

async function createShare() {
  if (shareLoading.value) return
  shareLoading.value = true
  try {
    shareUrl.value = await createQuoteShare(quote.value, window.location.href)
    showShare.value = true
  } finally {
    shareLoading.value = false
  }
}

async function copyLink() {
  try {
    await navigator.clipboard.writeText(shareUrl.value || window.location.href)
    showToast('链接已复制，可发送给同事或老板', 'success')
  } catch {
    showToast('复制失败，请长按选择链接复制', 'error')
  }
}

async function systemShare() {
  const url = shareUrl.value || window.location.href
  if (navigator.share) {
    try {
      await navigator.share({
        title: `驰远物流报价 ${quote.value.origin} → ${quote.value.destination}`,
        text: `${quote.value.origin} → ${quote.value.destination}，预估 ¥${money(quote.value.totalAmount)}`,
        url
      })
    } catch {
      /* 用户取消分享 */
    }
    return
  }
  await copyLink()
}
</script>

<template>
  <div class="page quote-share">
    <section class="quote-hero">
      <div class="quote-hero__top">
        <span class="quote-hero__badge">报价单</span>
        <span class="quote-hero__valid">有效期至 {{ formatDateTime(quote.validUntil) }}</span>
      </div>
      <div class="quote-route">
        <span>{{ quote.origin }}</span>
        <i aria-hidden="true">
          <b></b>
        </i>
        <span>{{ quote.destination }}</span>
      </div>
      <p class="quote-total"><small>预估总价</small> ¥ {{ money(quote.totalAmount) }}</p>
      <p class="quote-meta">
        {{ quote.weight }}kg · {{ quote.volume }}m³ · {{ quote.pieces }}件 · {{ quote.vehicleType }}
      </p>
      <div class="quote-hero__no">
        <span>报价单号</span>
        <strong class="mono">{{ quote.quoteNo }}</strong>
      </div>
    </section>

    <section class="card price-card">
      <header class="price-card__head">
        <h2>费用明细</h2>
        <span>含附加费 · 不含税</span>
      </header>
      <ul class="price-list">
        <li v-for="line in quote.lines" :key="line.name" :class="{ 'is-zero': Number(line.amount) === 0 }">
          <div>
            <span class="price-list__name">{{ line.name }}</span>
            <small v-if="line.formula">{{ line.formula }}</small>
          </div>
          <strong class="mono">¥ {{ money(line.amount) }}</strong>
        </li>
      </ul>
      <div class="price-total">
        <span>合计</span>
        <strong class="mono">¥ {{ money(quote.totalAmount) }}</strong>
      </div>
      <p class="price-note">报价为不含税参考价，实际运费以客服确认与电子运单为准。</p>
    </section>

    <section class="card detail-card">
      <h2>货物信息</h2>
      <dl>
        <div class="info-row">
          <dt>货物类型</dt>
          <dd>{{ quote.cargoType }}</dd>
        </div>
        <div class="info-row">
          <dt>重量 / 体积 / 件数</dt>
          <dd>{{ quote.weight }}kg / {{ quote.volume }}m³ / {{ quote.pieces }}件</dd>
        </div>
        <div class="info-row">
          <dt>期望车型</dt>
          <dd>{{ quote.vehicleType }}</dd>
        </div>
        <div v-if="quote.customerName" class="info-row">
          <dt>客户</dt>
          <dd>{{ quote.customerName }}</dd>
        </div>
        <div class="info-row">
          <dt>报价时间</dt>
          <dd>{{ formatDateTime(quote.createdAt) }}</dd>
        </div>
      </dl>
    </section>

    <section class="card share-card">
      <div class="share-card__text">
        <h2>分享给同事或老板</h2>
        <p>生成专属分享链接，对方打开即可看到同一份报价并直接下单。</p>
      </div>
      <button class="btn btn--outline btn--block" type="button" :disabled="shareLoading" @click="createShare">
        <span v-if="shareLoading" class="spinner spinner--dark"></span>
        {{ shareLoading ? '正在生成…' : '生成分享链接' }}
      </button>
    </section>

    <div class="quote-tips">
      <p>· 报价有效期 24 小时，超时请重新询价。</p>
      <p>· 大件、冷链、危险品等特殊货物需人工复核。</p>
      <p>· 企业月结客户可享专属协议价，最长 45 天账期。</p>
    </div>

    <div class="bottom-bar">
      <a class="btn btn--outline bottom-bar__ghost" href="tel:4008801688">联系客服</a>
      <a class="btn btn--primary" :href="orderUrl">立即下单</a>
    </div>

    <Teleport to="body">
      <div v-if="showShare" class="sheet-mask" @click.self="showShare = false">
        <div class="sheet" role="dialog" aria-modal="true" aria-label="分享报价链接">
          <div class="sheet__handle"></div>
          <div class="sheet__head">
            <h2>分享链接已生成</h2>
            <button class="sheet__close" type="button" aria-label="关闭" @click="showShare = false">×</button>
          </div>
          <div class="sheet__body">
            <p class="share-sheet__hint">发送给同事或老板，对方无需登录即可查看报价。</p>
            <div class="share-sheet__url mono">{{ shareUrl }}</div>
            <div class="share-sheet__actions">
              <button class="btn btn--primary" type="button" @click="copyLink">复制链接</button>
              <button class="btn btn--outline" type="button" @click="systemShare">系统分享</button>
            </div>
            <p class="share-sheet__tip">链接长期有效，若报价已过期，打开后会提示重新询价。</p>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.quote-share {
  padding-top: 12px;
}

.quote-hero {
  position: relative;
  overflow: hidden;
  padding: 20px;
  border-radius: 16px;
  color: #fff;
  background: radial-gradient(320px 200px at 90% 0%, rgba(76, 154, 255, 0.6), transparent 65%),
    linear-gradient(145deg, #082446, #0b3d84 65%, #1557c0);
  box-shadow: 0 12px 30px rgba(10, 43, 94, 0.24);
}

.quote-hero::after {
  content: '';
  position: absolute;
  right: -36px;
  bottom: -60px;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  border: 22px solid rgba(255, 255, 255, 0.07);
}

.quote-hero__top {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.quote-hero__badge {
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: 0.12em;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 5px;
  padding: 2px 8px;
}

.quote-hero__valid {
  font-size: 10.5px;
  color: rgba(255, 255, 255, 0.62);
}

.quote-route {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 18px;
  font-size: 25px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.quote-route i {
  flex: 1;
  height: 2px;
  position: relative;
  background: repeating-linear-gradient(90deg, rgba(255, 255, 255, 0.5) 0 5px, transparent 5px 10px);
}

.quote-route i::before,
.quote-route i::after {
  content: '';
  position: absolute;
  top: -3px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #fff;
}

.quote-route i::before {
  left: 0;
}

.quote-route i::after {
  right: 0;
  background: rgba(255, 255, 255, 0.5);
}

.quote-route i b {
  position: absolute;
  right: 46%;
  top: -8px;
  width: 14px;
  height: 17px;
  border-radius: 3px;
  background: #8dc0ff;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
}

.quote-total {
  position: relative;
  z-index: 1;
  margin-top: 16px;
  font-size: 34px;
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.15;
}

.quote-total small {
  font-size: 11px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.6);
  margin-right: 5px;
  letter-spacing: 0;
}

.quote-meta {
  position: relative;
  z-index: 1;
  font-size: 11.5px;
  color: rgba(255, 255, 255, 0.66);
  margin-top: 5px;
}

.quote-hero__no {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.14);
  font-size: 10.5px;
  color: rgba(255, 255, 255, 0.55);
}

.quote-hero__no strong {
  font-size: 11.5px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.price-card {
  margin-top: 12px;
  overflow: hidden;
}

.price-card__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
  padding: 16px 16px 12px;
  border-bottom: 1px solid var(--line);
}

.price-card__head h2 {
  font-size: 15px;
}

.price-card__head span {
  font-size: 10.5px;
  color: var(--muted-light);
}

.price-list {
  padding: 4px 16px;
}

.price-list li {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  padding: 11px 0;
  border-bottom: 1px dashed #eef2f8;
}

.price-list li:last-child {
  border-bottom: 0;
}

.price-list li > div {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.price-list__name {
  font-size: 13px;
  font-weight: 600;
  color: #2b3c52;
}

.price-list small {
  font-size: 10.5px;
  color: var(--muted-light);
  line-height: 1.5;
  margin-top: 1px;
}

.price-list strong {
  font-size: 13.5px;
  color: var(--ink);
  white-space: nowrap;
}

.price-list li.is-zero .price-list__name,
.price-list li.is-zero strong {
  color: #a5b1c1;
  font-weight: 500;
}

.price-total {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding: 13px 16px;
  background: #f7fbff;
  border-top: 1px solid var(--line);
  border-bottom: 1px solid var(--line);
  font-size: 13px;
  color: var(--muted);
}

.price-total strong {
  font-size: 23px;
  color: var(--primary);
  font-weight: 800;
  letter-spacing: -0.02em;
}

.price-note {
  padding: 10px 16px 13px;
  font-size: 10.5px;
  color: var(--muted-light);
  line-height: 1.55;
}

.detail-card,
.share-card {
  margin-top: 12px;
  padding: 16px;
}

.detail-card h2,
.share-card h2 {
  font-size: 14px;
  margin-bottom: 6px;
}

.detail-card dl {
  margin: 0;
}

.share-card__text p {
  font-size: 11.5px;
  color: var(--muted);
  line-height: 1.6;
  margin: 3px 0 12px;
}

.quote-tips {
  margin: 16px 4px 0;
  font-size: 11px;
  color: var(--muted-light);
  line-height: 1.9;
}

.share-sheet__hint {
  font-size: 12px;
  color: var(--muted);
  margin-bottom: 10px;
}

.share-sheet__url {
  padding: 12px;
  border-radius: 9px;
  background: var(--soft);
  border: 1px solid var(--line);
  font-size: 11.5px;
  color: #34475e;
  word-break: break-all;
  line-height: 1.6;
  max-height: 84px;
  overflow-y: auto;
}

.share-sheet__actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 14px;
}

.share-sheet__tip {
  margin-top: 12px;
  font-size: 10.5px;
  color: var(--muted-light);
  text-align: center;
}
</style>
