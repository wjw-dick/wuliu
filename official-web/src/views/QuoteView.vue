<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import QuoteBreakdown from '../components/QuoteBreakdown.vue'
import { demoMode } from '../api/client'
import { queryQuote, submitLead } from '../api/public'
import type { QuoteInput, QuoteResult } from '../types'
import { money } from '../utils/format'

const route = useRoute()

const form = reactive<QuoteInput>({
  origin: '上海',
  destination: '杭州',
  cargoType: '普货',
  weight: 500,
  volume: 2.5,
  pieces: 10,
  vehicleType: '6.8 米厢式',
  value: 10000,
  needLoading: true,
  needUpstairs: false,
  codAmount: 0,
  ladder: false
})

const errors = reactive<Record<string, string>>({})
const loading = ref(false)
const quote = ref<QuoteResult | null>(null)
const stale = ref(false)
const resultRef = ref<HTMLElement | null>(null)
const leadRef = ref<HTMLElement | null>(null)

const lead = reactive({
  name: '',
  phone: '',
  company: '',
  remark: ''
})
const leadErrors = reactive<Record<string, string>>({})
const leadSubmitting = ref(false)
const leadSuccess = ref(false)

const cities = ['上海', '杭州', '苏州', '南京', '宁波', '合肥', '无锡', '常州', '嘉兴', '绍兴', '湖州', '南通']
const cargoTypes = ['普货', '大件设备', '易碎品', '服装鞋帽', '食品饮料', '冷链货物']
const vehicleTypes = ['不限', '4.2 米厢式', '6.8 米厢式', '9.6 米厢式', '13 米平板', '17.5 米挂车']
const hotRoutes = [
  { from: '上海', to: '杭州', km: 180, price: '658 元起' },
  { from: '上海', to: '苏州', km: 105, price: '486 元起' },
  { from: '上海', to: '南京', km: 300, price: '834 元起' },
  { from: '杭州', to: '宁波', km: 155, price: '602 元起' }
]

function validate() {
  Object.keys(errors).forEach((key) => delete errors[key])
  if (!form.origin.trim()) errors.origin = '请选择出发地'
  if (!form.destination.trim()) errors.destination = '请选择目的地'
  if (!form.weight || Number(form.weight) <= 0) errors.weight = '请输入有效重量'
  if (Number(form.volume) < 0) errors.volume = '体积不能为负数'
  if (!form.pieces || Number(form.pieces) <= 0) errors.pieces = '请输入件数'
  if (Number(form.value) < 0) errors.value = '货值不能为负数'
  if (Number(form.codAmount) < 0) errors.codAmount = '代收金额不能为负数'
  return Object.keys(errors).length === 0
}

async function handleQuote(scroll = true) {
  if (!validate()) return
  loading.value = true
  stale.value = false
  try {
    quote.value = await queryQuote({ ...form })
    if (scroll) {
      await nextTick()
      resultRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  } finally {
    loading.value = false
  }
}

function onFormChange() {
  if (quote.value) stale.value = true
}

function validateLead() {
  Object.keys(leadErrors).forEach((key) => delete leadErrors[key])
  if (!lead.name.trim()) leadErrors.name = '请填写您的称呼'
  const phoneOk = /^(1[3-9]\d{9}|0\d{2,3}-?\d{7,8})$/.test(lead.phone.trim())
  if (!lead.phone.trim()) leadErrors.phone = '请填写联系电话'
  else if (!phoneOk) leadErrors.phone = '请填写有效的手机号或座机号'
  if (!lead.company.trim()) leadErrors.company = '请填写公司名称'
  return Object.keys(leadErrors).length === 0
}

async function handleLead() {
  if (!validateLead()) return
  leadSubmitting.value = true
  try {
    const remark = [
      quote.value ? `报价单号：${quote.value.quoteNo}` : '',
      `${form.origin} → ${form.destination}`,
      `${form.weight}kg / ${form.volume}m³ / ${form.pieces}件`,
      lead.remark.trim()
    ]
      .filter(Boolean)
      .join('；')
    await submitLead({
      name: lead.name.trim(),
      phone: lead.phone.trim(),
      company: lead.company.trim(),
      source: '官网-在线询价',
      remark
    })
    leadSuccess.value = true
  } finally {
    leadSubmitting.value = false
  }
}

function scrollToLead() {
  leadRef.value?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

function useRoutePreset() {
  const { origin, destination, weight } = route.query
  if (typeof origin === 'string' && origin) form.origin = origin
  if (typeof destination === 'string' && destination) form.destination = destination
  if (typeof weight === 'string' && Number(weight) > 0) form.weight = Number(weight)
  if (origin || destination || weight) {
    void handleQuote(false)
  }
}

onMounted(useRoutePreset)
</script>

<template>
  <div class="quote-page">
    <section class="page-hero">
      <div class="container page-hero__inner">
        <span class="eyebrow">INSTANT QUOTATION</span>
        <h1>在线询价，明细一目了然</h1>
        <p>
          填写线路与货物信息，立即获得基础运费、燃油附加、装卸、上楼、保险与代收手续费明细。无需注册，不留手机号也能先看价格。
        </p>
      </div>
    </section>

    <section class="section section--tight quote-main">
      <div class="container">
        <ol class="steps" aria-label="询价流程">
          <li class="is-active"><span>1</span>填写货物信息</li>
          <li :class="{ 'is-active': quote }"><span>2</span>查看明细报价</li>
          <li :class="{ 'is-active': leadSuccess }"><span>3</span>提交需求获方案</li>
        </ol>

        <div class="quote-layout">
          <form class="card quote-form" novalidate @submit.prevent="handleQuote()" @change="onFormChange">
            <div class="quote-form__head">
              <h2>货物与线路信息</h2>
              <span class="small muted">带 <em>*</em> 为必填项</span>
            </div>

            <fieldset class="form-section">
              <legend>线路信息</legend>
              <div class="form-grid">
                <div class="field">
                  <label for="origin">出发地 <em>*</em></label>
                  <input id="origin" v-model="form.origin" class="input" list="city-list" placeholder="如：上海" :aria-invalid="Boolean(errors.origin)" />
                  <span v-if="errors.origin" class="form-error">{{ errors.origin }}</span>
                </div>
                <div class="field">
                  <label for="destination">目的地 <em>*</em></label>
                  <input id="destination" v-model="form.destination" class="input" list="city-list" placeholder="如：杭州" :aria-invalid="Boolean(errors.destination)" />
                  <span v-if="errors.destination" class="form-error">{{ errors.destination }}</span>
                </div>
                <datalist id="city-list">
                  <option v-for="city in cities" :key="city" :value="city" />
                </datalist>
              </div>
            </fieldset>

            <fieldset class="form-section">
              <legend>货物信息</legend>
              <div class="form-grid">
                <div class="field">
                  <label for="cargoType">货物类型</label>
                  <select id="cargoType" v-model="form.cargoType" class="select">
                    <option v-for="type in cargoTypes" :key="type">{{ type }}</option>
                  </select>
                </div>
                <div class="field">
                  <label for="vehicleType">期望车型</label>
                  <select id="vehicleType" v-model="form.vehicleType" class="select">
                    <option v-for="type in vehicleTypes" :key="type">{{ type }}</option>
                  </select>
                </div>
                <div class="field">
                  <label for="weight">重量（kg） <em>*</em></label>
                  <input id="weight" v-model.number="form.weight" class="input" type="number" min="1" step="1" :aria-invalid="Boolean(errors.weight)" />
                  <span v-if="errors.weight" class="form-error">{{ errors.weight }}</span>
                </div>
                <div class="field">
                  <label for="volume">体积（m³）</label>
                  <input id="volume" v-model.number="form.volume" class="input" type="number" min="0" step="0.1" :aria-invalid="Boolean(errors.volume)" />
                  <span v-if="errors.volume" class="form-error">{{ errors.volume }}</span>
                </div>
                <div class="field">
                  <label for="pieces">件数 <em>*</em></label>
                  <input id="pieces" v-model.number="form.pieces" class="input" type="number" min="1" step="1" :aria-invalid="Boolean(errors.pieces)" />
                  <span v-if="errors.pieces" class="form-error">{{ errors.pieces }}</span>
                </div>
                <div class="field">
                  <label for="value">货物声明价值（元）</label>
                  <input id="value" v-model.number="form.value" class="input" type="number" min="0" step="100" />
                  <span class="form-help">用于计算保险费用，未投保可填 0</span>
                </div>
              </div>
            </fieldset>

            <fieldset class="form-section">
              <legend>增值服务</legend>
              <div class="form-row">
                <label class="checkbox"><input v-model="form.needLoading" type="checkbox" /> 需要上门装卸（50 元/票）</label>
                <label class="checkbox"><input v-model="form.needUpstairs" type="checkbox" /> 需要上楼搬运（80 元/票）</label>
                <label class="checkbox"><input v-model="form.ladder" type="checkbox" /> 需要尾板/叉车</label>
              </div>
              <div class="form-grid cod-row">
                <div class="field">
                  <label for="codAmount">代收货款金额（元）</label>
                  <input id="codAmount" v-model.number="form.codAmount" class="input" type="number" min="0" step="100" />
                  <span class="form-help">手续费为代收金额的 0.5%，T+1 结算</span>
                </div>
              </div>
            </fieldset>

            <div class="quote-form__foot">
              <p class="small muted">报价为不含税参考价，有效期 24 小时。</p>
              <button class="btn btn--primary" type="submit" :disabled="loading">
                {{ loading ? '正在计算…' : quote ? '重新计算报价' : '立即计算报价' }}
              </button>
            </div>
          </form>

          <div class="quote-side">
            <div ref="resultRef" class="quote-side__anchor"></div>

            <div v-if="loading" class="card result-card" aria-live="polite">
              <div class="skeleton result-skeleton__bar"></div>
              <div class="skeleton result-skeleton__line"></div>
              <div class="skeleton result-skeleton__line"></div>
              <div class="skeleton result-skeleton__line"></div>
              <div class="skeleton result-skeleton__line result-skeleton__line--short"></div>
            </div>

            <template v-else-if="quote">
              <div class="card result-card" aria-live="polite">
                <div class="result-card__top">
                  <div>
                    <span class="small muted">预估总价（不含税）</span>
                    <p class="result-total">¥ <strong>{{ money(quote.totalAmount) }}</strong></p>
                    <p class="result-route">{{ form.origin }} → {{ form.destination }}</p>
                  </div>
                  <div class="result-badge">
                    <span>{{ quote.priceSource || '线路标准价' }}</span>
                    <small>{{ quote.ruleVersion }}</small>
                  </div>
                </div>

                <div v-if="stale" class="notice notice--warn result-stale">
                  货物信息已修改，当前金额为上次计算结果，请点击“重新计算报价”更新。
                </div>

                <QuoteBreakdown :quote="quote" />

                <div class="result-card__actions">
                  <button class="btn btn--primary" type="button" @click="scrollToLead">
                    提交需求，锁定此价格
                  </button>
                  <RouterLink class="btn btn--outline" :to="{ path: '/track' }">查询运单</RouterLink>
                </div>
                <p class="result-disclaimer">
                  实际运费以客服确认与电子运单为准。危险品、冷链、超限设备需人工复核。
                </p>
              </div>

              <div ref="leadRef" class="card lead-card">
                <div class="lead-card__head">
                  <div>
                    <h2>获取专属方案与协议价</h2>
                    <p class="small muted">月发货 10 票以上可申请企业协议价，客户经理 30 分钟内联系。</p>
                  </div>
                  <span class="pill">免费咨询</span>
                </div>

                <div v-if="leadSuccess" class="notice notice--success lead-success" role="status">
                  <span>
                    提交成功！我们已收到您的需求，客户经理将在 30 分钟内联系您。
                    <template v-if="demoMode">（演示环境：数据未实际提交至后端）</template>
                  </span>
                </div>

                <form v-else class="lead-form" novalidate @submit.prevent="handleLead">
                  <div class="form-grid">
                    <div class="field">
                      <label for="lead-name">您的称呼 <em>*</em></label>
                      <input id="lead-name" v-model="lead.name" class="input" placeholder="如：张经理" :aria-invalid="Boolean(leadErrors.name)" />
                      <span v-if="leadErrors.name" class="form-error">{{ leadErrors.name }}</span>
                    </div>
                    <div class="field">
                      <label for="lead-phone">联系电话 <em>*</em></label>
                      <input id="lead-phone" v-model="lead.phone" class="input" type="tel" inputmode="tel" placeholder="手机或座机号" :aria-invalid="Boolean(leadErrors.phone)" />
                      <span v-if="leadErrors.phone" class="form-error">{{ leadErrors.phone }}</span>
                    </div>
                    <div class="field field--full">
                      <label for="lead-company">公司名称 <em>*</em></label>
                      <input id="lead-company" v-model="lead.company" class="input" placeholder="请输入公司全称" :aria-invalid="Boolean(leadErrors.company)" />
                      <span v-if="leadErrors.company" class="form-error">{{ leadErrors.company }}</span>
                    </div>
                    <div class="field field--full">
                      <label for="lead-remark">补充说明（选填）</label>
                      <textarea id="lead-remark" v-model="lead.remark" class="textarea" placeholder="如常发线路、月发货量、时效要求等"></textarea>
                    </div>
                  </div>
                  <button class="btn btn--primary btn--block" type="submit" :disabled="leadSubmitting">
                    {{ leadSubmitting ? '正在提交…' : '提交需求' }}
                  </button>
                  <p class="form-help lead-privacy">提交即表示同意《隐私政策》，我们仅将信息用于本次业务联系，不会对外提供。</p>
                </form>
              </div>
            </template>

            <div v-else class="card empty-result">
              <div class="empty-result__icon" aria-hidden="true">¥</div>
              <h2>先看看这票货要多少钱</h2>
              <p>填写左侧出发地、目的地与重量，点击“立即计算报价”，即可查看含全部附加费的明细。</p>
              <ul>
                <li><span>基础运费</span><span>按首重 + 续重 + 里程</span></li>
                <li><span>燃油附加</span><span>基础运费 × 10%</span></li>
                <li><span>装卸 / 上楼</span><span>按需勾选，明码标价</span></li>
                <li><span>保险 / 代收</span><span>按声明价值与代收额计算</span></li>
              </ul>
              <div class="hot-routes">
                <h3>热门线路参考</h3>
                <div class="hot-routes__grid">
                  <button v-for="item in hotRoutes" :key="`${item.from}-${item.to}`" type="button" @click="form.origin = item.from; form.destination = item.to; handleQuote()">
                    <span>{{ item.from }} → {{ item.to }}</span>
                    <strong>{{ item.price }}</strong>
                  </button>
                </div>
              </div>
            </div>

            <aside class="quote-help">
              <div>
                <strong>需要人工报价？</strong>
                <p>大件、冷链、超限或长期合作，请直接联系客户经理。</p>
              </div>
              <a class="btn btn--outline btn--sm" href="tel:4008801688">400-880-1688</a>
            </aside>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.quote-main {
  background: var(--soft);
}

.steps {
  display: flex;
  align-items: center;
  gap: 12px;
  list-style: none;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.steps li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--muted-light);
  padding: 8px 16px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid var(--line);
}

.steps li span {
  display: grid;
  place-items: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #edf2f9;
  color: var(--muted);
  font-size: 11px;
  font-weight: 700;
}

.steps li.is-active {
  color: var(--primary);
  border-color: #c9dcf7;
  background: #f5f9ff;
}

.steps li.is-active span {
  background: var(--primary);
  color: #fff;
}

.quote-layout {
  display: grid;
  grid-template-columns: 1fr 1.02fr;
  gap: 24px;
  align-items: start;
}

.quote-form {
  padding: 26px;
}

.quote-form__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--line);
}

.quote-form__head h2 {
  font-size: 20px;
}

.quote-form__head em {
  color: var(--red);
  font-style: normal;
}

.form-section {
  border: 0;
  padding: 22px 0 4px;
  margin: 0;
  border-bottom: 1px solid var(--line);
}

.form-section legend {
  font-size: 13px;
  font-weight: 700;
  color: var(--primary);
  letter-spacing: 0.04em;
  margin-bottom: 14px;
}

.cod-row {
  margin-top: 16px;
}

.quote-form__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-top: 22px;
}

.quote-side {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.quote-side__anchor {
  scroll-margin-top: 130px;
}

.result-card {
  overflow: hidden;
  scroll-margin-top: 130px;
}

.result-card__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 24px 24px 18px;
  background: linear-gradient(145deg, #f7fbff, #fff);
  border-bottom: 1px solid var(--line);
}

.result-total {
  font-size: 15px;
  color: var(--primary);
  line-height: 1.2;
}

.result-total strong {
  font-size: 38px;
  letter-spacing: -0.03em;
}

.result-route {
  font-size: 12px;
  color: var(--muted-light);
  margin-top: 4px;
}

.result-badge {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.result-badge span {
  font-size: 11px;
  font-weight: 700;
  color: var(--primary);
  background: #eaf3ff;
  border-radius: 6px;
  padding: 4px 9px;
}

.result-badge small {
  font-size: 10px;
  color: var(--muted-light);
}

.result-stale {
  margin: 16px 20px 0;
}

.result-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 20px 24px 8px;
}

.result-card__actions .btn {
  flex: 1;
  min-width: 150px;
}

.result-disclaimer {
  padding: 0 24px 20px;
  font-size: 11px;
  color: var(--muted-light);
}

.result-skeleton__bar {
  height: 76px;
  border-radius: 0;
}

.result-skeleton__line {
  height: 14px;
  margin: 18px 24px;
}

.result-skeleton__line--short {
  width: 50%;
}

.lead-card {
  padding: 24px;
  scroll-margin-top: 130px;
}

.lead-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 18px;
}

.lead-card__head h2 {
  font-size: 19px;
  margin-bottom: 4px;
}

.lead-form .btn {
  margin-top: 18px;
}

.lead-privacy {
  margin-top: 10px;
  text-align: center;
}

.lead-success {
  font-size: 13.5px;
}

.empty-result {
  padding: 30px 26px;
}

.empty-result__icon {
  width: 48px;
  height: 48px;
  border-radius: 13px;
  display: grid;
  place-items: center;
  background: #eaf3ff;
  color: var(--primary);
  font-size: 22px;
  font-weight: 800;
  margin-bottom: 16px;
}

.empty-result h2 {
  font-size: 20px;
  margin-bottom: 8px;
}

.empty-result > p {
  font-size: 13.5px;
  color: var(--muted);
  line-height: 1.75;
}

.empty-result ul {
  list-style: none;
  margin: 20px 0;
  border-top: 1px solid var(--line);
}

.empty-result li {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--line);
  font-size: 12.5px;
}

.empty-result li span:first-child {
  font-weight: 600;
  color: #32465e;
}

.empty-result li span:last-child {
  color: var(--muted-light);
  text-align: right;
}

.hot-routes {
  margin-top: 22px;
  padding-top: 18px;
  border-top: 1px solid var(--line);
}

.hot-routes h3 {
  font-size: 13px;
  margin-bottom: 12px;
}

.hot-routes__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 9px;
}

.hot-routes__grid button {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 12px;
  border: 1px solid var(--line);
  border-radius: 9px;
  background: #fbfdff;
  text-align: left;
  transition: border-color 0.15s ease, background 0.15s ease;
}

.hot-routes__grid button:hover {
  border-color: #bcd7f8;
  background: #f5f9ff;
}

.hot-routes__grid span {
  font-size: 12px;
  color: #32465e;
}

.hot-routes__grid strong {
  font-size: 12px;
  color: var(--primary);
}

.quote-help {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 12px;
  background: #071a33;
  color: rgba(255, 255, 255, 0.7);
}

.quote-help strong {
  color: #fff;
  font-size: 14px;
}

.quote-help p {
  font-size: 11.5px;
  margin-top: 2px;
}

.quote-help .btn {
  flex-shrink: 0;
  background: transparent;
  border-color: rgba(255, 255, 255, 0.25);
  color: #fff;
}

@media (max-width: 1000px) {
  .quote-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 620px) {
  .quote-form {
    padding: 20px 18px;
  }

  .quote-form__head {
    flex-direction: column;
    gap: 4px;
  }

  .result-card__top {
    padding: 20px 18px 16px;
  }

  .result-total strong {
    font-size: 32px;
  }

  .result-card__actions,
  .result-disclaimer {
    padding-left: 18px;
    padding-right: 18px;
  }

  .quote-form__foot {
    flex-direction: column;
    align-items: stretch;
  }

  .quote-form__foot .btn {
    width: 100%;
  }

  .hot-routes__grid {
    grid-template-columns: 1fr;
  }

  .lead-card {
    padding: 20px 18px;
  }

  .quote-help {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
