<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import SiteMap from '../components/SiteMap.vue'
import { getSites } from '../api/public'
import type { Site } from '../types'

const router = useRouter()

const quickQuote = reactive({
  origin: '上海',
  destination: '杭州',
  weight: 500
})

const quickTrack = reactive({
  waybillNo: '',
  phone: ''
})

const sites = ref<Site[]>([])
const mapLoading = ref(true)
const selectedSiteId = ref<number | null>(null)

const services = [
  {
    key: '零',
    title: '零担专线',
    subtitle: 'LTL · 天天发车',
    description: '长三角核心城市今发明至，拼载降本，适合多批次、小批量货物。',
    features: ['最低 30kg 起运，按需拼载', '首重续重透明计价', '全程节点短信推送'],
    price: '0.85 元/kg 起',
    to: '/services#ltl'
  },
  {
    key: '整',
    title: '整车包车',
    subtitle: 'FTL · 专车直达',
    description: '4.2 米至 17.5 米全车型覆盖，门到门直达，不中转、不分拨。',
    features: ['专属车辆，全程不换装', '支持温控、气垫、尾板车', 'GPS 实时轨迹与电子围栏'],
    price: '900 元/趟 起',
    to: '/services#ftl'
  },
  {
    key: '城',
    title: '同城配送',
    subtitle: 'CITY · 当日达',
    description: '上海、杭州主城区当日达，多点配送与预约时段，服务门店与仓配客户。',
    features: ['3 公里内 120 元起步', '多点配送、上楼入仓', '签收拍照，回单当日回传'],
    price: '120 元/单 起',
    to: '/services#city'
  }
]

const cases = [
  {
    industry: '精密制造',
    company: '某汽车零部件（上海）有限公司',
    quote: '“每周 12 车零担发往长三角 6 个工厂，节点延误率从 8% 降到 1.2%，产线不再为等料停摆。”',
    metrics: [
      { value: '99.4%', label: '准时交付' },
      { value: '-18%', label: '综合运费' }
    ]
  },
  {
    industry: '电商零售',
    company: '某家居电商品牌',
    quote: '“大促期间日单量增长 3 倍，驰远提前锁定运力并开通夜班分拨，48 小时发出率保持 100%。”',
    metrics: [
      { value: '3×', label: '峰值承载' },
      { value: '48h', label: '全部发出' }
    ]
  },
  {
    industry: '医疗器械',
    company: '某医疗器械经销集团',
    quote: '“高值耗材需要温控与保险托底，每票都有独立轨迹与回单影像，对账周期从 15 天缩短到 3 天。”',
    metrics: [
      { value: '0', label: '货损投诉' },
      { value: '3天', label: '对账周期' }
    ]
  }
]

const qualifications = [
  { title: '营业执照', code: '统一社会信用代码 91310118MA1JL8XX6K', year: '2012 年注册' },
  { title: '道路运输经营许可证', code: '沪交运管许可青字 310118008866 号', year: '有效期至 2029 年' },
  { title: 'ISO 9001 质量管理体系', code: '认证范围：道路货物运输与仓储配送', year: '2026 年复审通过' },
  { title: 'ISO 14001 环境管理体系', code: '认证范围：道路货物运输与仓储配送', year: '2026 年复审通过' }
]

const faqs = [
  {
    q: '下单后多久可以上门提货？',
    a: '上海、杭州、苏州主城区支持当日 2 小时内上门，其他区域最晚次日 12:00 前。您也可以在小程序或官网预约具体时间窗，客服会在提货前 30 分钟电话确认。'
  },
  {
    q: '报价包含哪些费用？会产生额外收费吗？',
    a: '在线报价已包含基础运费、燃油附加，并按您勾选的项目计算装卸、上楼、保险与代收手续费。如遇超区、等待、特殊装卸等情况，司机会先联系确认并经您同意后作业，不会先斩后奏。'
  },
  {
    q: '货物损坏或丢失如何理赔？',
    a: '未投保货物按运单约定限额赔付；建议按声明价值投保（费率 0.3%，最低 10 元）。出险后在小程序提交报案与照片，理赔专员 1 个工作日内响应，资料齐全后 7 个工作日内完成核定。'
  },
  {
    q: '企业月结需要满足什么条件？',
    a: '持有有效营业执照、月均运费 5000 元以上即可申请，标准账期月结 30 天，优质客户最长 45 天。提交申请后客户经理 1 个工作日内联系，签约后即可享受协议价与月度对账。'
  }
]

const openFaq = ref<number | null>(0)
const showQr = ref(false)

function goQuote() {
  router.push({
    path: '/quote',
    query: {
      origin: quickQuote.origin,
      destination: quickQuote.destination,
      weight: String(quickQuote.weight || '')
    }
  })
}

function goTrack() {
  router.push({
    path: '/track',
    query: {
      waybillNo: quickTrack.waybillNo.trim(),
      phone: quickTrack.phone.trim()
    }
  })
}

const qrModules: Array<{ x: number; y: number }> = (() => {
  const size = 25
  const cells: Array<{ x: number; y: number }> = []
  let seed = 20261114
  const random = () => {
    seed = (seed * 1103515245 + 12345) % 2147483648
    return seed / 2147483648
  }
  const inFinder = (x: number, y: number) => {
    const zones = [
      [0, 0],
      [size - 7, 0],
      [0, size - 7]
    ]
    return zones.some(([zx, zy]) => x >= zx && x < zx + 7 && y >= zy && y < zy + 7)
  }
  for (let y = 0; y < size; y += 1) {
    for (let x = 0; x < size; x += 1) {
      if (inFinder(x, y)) continue
      if (random() > 0.52) cells.push({ x, y })
    }
  }
  return cells
})()

let faqScript: HTMLScriptElement | null = null

onMounted(async () => {
  faqScript = document.createElement('script')
  faqScript.type = 'application/ld+json'
  faqScript.id = 'home-faq-jsonld'
  faqScript.textContent = JSON.stringify({
    '@context': 'https://schema.org',
    '@type': 'FAQPage',
    mainEntity: faqs.map((item) => ({
      '@type': 'Question',
      name: item.q,
      acceptedAnswer: { '@type': 'Answer', text: item.a }
    }))
  })
  document.head.appendChild(faqScript)

  sites.value = await getSites()
  selectedSiteId.value = sites.value[0]?.id ?? null
  mapLoading.value = false
})

onBeforeUnmount(() => {
  faqScript?.remove()
})
</script>

<template>
  <div class="home">
    <section class="hero">
      <div class="hero__glow" aria-hidden="true"></div>
      <div class="container hero__inner">
        <div class="hero__copy">
          <span class="hero__badge">
            <i></i>
            长三角合同物流服务商 · 12 年稳定运营
          </span>
          <h1>
            让每一票货物<br />
            <span>准时、可视、可预期</span>
          </h1>
          <p class="hero__lead">
            零担专线、整车包车、同城配送一体化。30 秒获取明细报价，从提货到签收全程节点透明，企业月结灵活结算。
          </p>
          <div class="hero__actions">
            <RouterLink class="btn btn--primary hero__cta" to="/quote">
              立即获取报价
              <span aria-hidden="true">→</span>
            </RouterLink>
            <RouterLink class="btn btn--ghost" to="/track">查询运单轨迹</RouterLink>
          </div>
          <dl class="hero__stats">
            <div><dt>99.2%</dt><dd>准时交付率</dd></div>
            <div><dt>12 城</dt><dd>直营网点</dd></div>
            <div><dt>300+</dt><dd>自有及签约运力</dd></div>
            <div><dt>7×24h</dt><dd>客服响应</dd></div>
          </dl>
        </div>

        <div class="hero__visual" aria-hidden="true">
          <div class="hv-card">
            <div class="hv-card__top">
              <span class="hv-live"><i></i>在途运单</span>
              <span class="hv-status">运输中</span>
            </div>
            <p class="hv-no">YH0001202611140001</p>
            <div class="hv-route">
              <div class="hv-route__point">
                <strong>上海</strong>
                <small>11-14 05:40 已发车</small>
              </div>
              <div class="hv-route__track">
                <i></i>
                <span class="hv-truck">▮</span>
              </div>
              <div class="hv-route__point hv-route__point--right">
                <strong>杭州</strong>
                <small>预计 18:00 送达</small>
              </div>
            </div>
            <div class="hv-map">
              <span class="hv-map__grid"></span>
              <svg viewBox="0 0 300 110" preserveAspectRatio="none">
                <path d="M18 84 C 76 26, 118 96, 170 48 S 252 22, 284 36" />
              </svg>
              <span class="hv-map__pin hv-map__pin--a"><i></i>上海</span>
              <span class="hv-map__pin hv-map__pin--b"><i></i>杭州</span>
              <span class="hv-map__truck">🚚</span>
            </div>
            <div class="hv-steps">
              <span class="is-done">已受理</span>
              <span class="is-done">已提货</span>
              <span class="is-current">运输中</span>
              <span>已签收</span>
            </div>
          </div>

          <div class="hv-float hv-float--quote">
            <span class="hv-float__label">本单明细报价</span>
            <strong>¥ 658.00</strong>
            <span class="hv-float__hint">基础运费 + 燃油附加 10%</span>
          </div>

          <div class="hv-float hv-float--eta">
            <span class="hv-float__ring">99.2<small>%</small></span>
            <span class="hv-float__text">近 30 天<br />准时交付率</span>
          </div>
        </div>
      </div>
    </section>

    <div class="container quick-wrap">
      <div class="quick card">
        <form class="quick__panel" @submit.prevent="goQuote">
          <div class="quick__head">
            <span class="quick__icon">价</span>
            <div>
              <h2>在线询价</h2>
              <p>30 秒生成费用明细，不留手机号也能看</p>
            </div>
          </div>
          <div class="quick__form">
            <label class="sr-only" for="quick-origin">出发地</label>
            <select id="quick-origin" v-model="quickQuote.origin" class="select">
              <option>上海</option>
              <option>杭州</option>
              <option>苏州</option>
              <option>南京</option>
              <option>宁波</option>
              <option>合肥</option>
              <option>无锡</option>
            </select>
            <span class="quick__arrow" aria-hidden="true">→</span>
            <label class="sr-only" for="quick-destination">目的地</label>
            <select id="quick-destination" v-model="quickQuote.destination" class="select">
              <option>杭州</option>
              <option>上海</option>
              <option>苏州</option>
              <option>南京</option>
              <option>宁波</option>
              <option>合肥</option>
              <option>无锡</option>
            </select>
            <label class="sr-only" for="quick-weight">重量</label>
            <input id="quick-weight" v-model.number="quickQuote.weight" class="input quick__weight" type="number" min="1" placeholder="重量 kg" />
            <button class="btn btn--primary" type="submit">去报价</button>
          </div>
        </form>

        <form class="quick__panel quick__panel--track" @submit.prevent="goTrack">
          <div class="quick__head">
            <span class="quick__icon quick__icon--green">踪</span>
            <div>
              <h2>运单查询</h2>
              <p>运单号或手机后四位，实时看节点</p>
            </div>
          </div>
          <div class="quick__form">
            <label class="sr-only" for="quick-waybill">运单号</label>
            <input id="quick-waybill" v-model="quickTrack.waybillNo" class="input" type="text" placeholder="运单号，如 YH0001202611140001" />
            <label class="sr-only" for="quick-phone">手机后四位</label>
            <input id="quick-phone" v-model="quickTrack.phone" class="input quick__phone" type="text" inputmode="numeric" maxlength="11" placeholder="手机后四位" />
            <button class="btn btn--outline" type="submit">立即查询</button>
          </div>
        </form>
      </div>
    </div>

    <section class="section services">
      <div class="container">
        <div class="section-head">
          <div class="section-head__text">
            <span class="eyebrow">SERVICE MATRIX</span>
            <h2>三条产品线，覆盖从一票到一车</h2>
            <p>标准产品明码标价，复杂需求由客户经理 1 对 1 出方案。所有服务均含全程轨迹与电子回单。</p>
          </div>
          <RouterLink class="text-link" to="/services">查看计费标准 →</RouterLink>
        </div>

        <div class="grid grid--3 service-grid">
          <article v-for="service in services" :key="service.title" class="service-card card">
            <div class="service-card__top">
              <span class="service-card__icon">{{ service.key }}</span>
              <span class="service-card__subtitle">{{ service.subtitle }}</span>
            </div>
            <h3>{{ service.title }}</h3>
            <p class="service-card__desc">{{ service.description }}</p>
            <ul>
              <li v-for="feature in service.features" :key="feature">{{ feature }}</li>
            </ul>
            <div class="service-card__foot">
              <span class="service-card__price">{{ service.price }}</span>
              <RouterLink :to="service.to">了解详情 →</RouterLink>
            </div>
          </article>
        </div>
      </div>
    </section>

    <section class="section section--soft network">
      <div class="container">
        <div class="section-head">
          <div class="section-head__text">
            <span class="eyebrow">SERVICE NETWORK</span>
            <h2>扎根长三角，网点就在您身边</h2>
            <p>上海、杭州、苏州、南京、宁波、合肥、无锡等地设直营网点与分拨中心，干线直连、末端自营。</p>
          </div>
          <RouterLink class="text-link" to="/sites">全部网点 →</RouterLink>
        </div>

        <div class="network__layout">
          <div v-if="mapLoading" class="skeleton network__skeleton" aria-label="网点地图加载中"></div>
          <SiteMap v-else :sites="sites" :selected-id="selectedSiteId" @select="selectedSiteId = $event.id" />
          <aside class="network__aside">
            <div class="network__stat">
              <strong>8</strong>
              <span>直营网点 / 分拨中心</span>
            </div>
            <div class="network__stat">
              <strong>12</strong>
              <span>覆盖城市</span>
            </div>
            <div class="network__stat">
              <strong>2h</strong>
              <span>主城区最快上门</span>
            </div>
            <div class="network__cities">
              <RouterLink v-for="city in ['上海', '杭州', '苏州', '南京', '宁波', '合肥', '无锡']" :key="city" :to="{ path: '/sites', query: { city } }">
                {{ city }}
              </RouterLink>
            </div>
            <a class="btn btn--outline btn--block btn--sm" href="tel:4008801688">拨打 400-880-1688 找网点</a>
          </aside>
        </div>
      </div>
    </section>

    <section class="section cases">
      <div class="container">
        <div class="section-head">
          <div class="section-head__text">
            <span class="eyebrow">CUSTOMER STORIES</span>
            <h2>被制造、电商与医疗客户长期选择</h2>
            <p>从单条线路试发到全国合同物流，我们用可量化的交付结果赢得续约。</p>
          </div>
          <div class="cases__logos" aria-label="部分合作客户">
            <span>汽车零部件</span>
            <span>家居电商</span>
            <span>医疗器械</span>
            <span>精密电子</span>
          </div>
        </div>

        <div class="grid grid--3">
          <article v-for="item in cases" :key="item.company" class="case-card card">
            <span class="case-card__industry">{{ item.industry }}</span>
            <blockquote>{{ item.quote }}</blockquote>
            <p class="case-card__company">{{ item.company }}</p>
            <div class="case-card__metrics">
              <div v-for="metric in item.metrics" :key="metric.label">
                <strong>{{ metric.value }}</strong>
                <span>{{ metric.label }}</span>
              </div>
            </div>
          </article>
        </div>
      </div>
    </section>

    <section class="section section--soft qualifications">
      <div class="container">
        <div class="section-head">
          <div class="section-head__text">
            <span class="eyebrow">COMPLIANCE</span>
            <h2>资质齐全，合作更放心</h2>
            <p>证照与体系认证可在签约前核验原件，运输全程可投保货运险。</p>
          </div>
          <RouterLink class="text-link" to="/contact">索取资质副本 →</RouterLink>
        </div>
        <div class="grid grid--4">
          <div v-for="item in qualifications" :key="item.title" class="qual-card card">
            <span class="qual-card__badge" aria-hidden="true">✓</span>
            <h3>{{ item.title }}</h3>
            <p>{{ item.code }}</p>
            <span class="qual-card__year">{{ item.year }}</span>
          </div>
        </div>
      </div>
    </section>

    <section class="section faq">
      <div class="container faq__layout">
        <div class="faq__intro">
          <span class="eyebrow">FAQ</span>
          <h2>发货前，您可能想确认这些</h2>
          <p>没有找到答案？客服热线 400-880-1688，或在线提交需求，我们会在 30 分钟内回复。</p>
          <RouterLink class="btn btn--outline btn--sm" to="/contact">联系客户经理</RouterLink>
        </div>
        <div class="faq__list">
          <div v-for="(item, index) in faqs" :key="item.q" class="faq__item" :class="{ 'faq__item--open': openFaq === index }">
            <button type="button" :aria-expanded="openFaq === index" @click="openFaq = openFaq === index ? null : index">
              <span>{{ item.q }}</span>
              <i aria-hidden="true">{{ openFaq === index ? '−' : '+' }}</i>
            </button>
            <p v-show="openFaq === index">{{ item.a }}</p>
          </div>
        </div>
      </div>
    </section>

    <section class="section cta-section">
      <div class="container">
        <div class="cta-card">
          <div class="cta-card__copy">
            <span class="eyebrow">MINI PROGRAM</span>
            <h2>微信小程序下单，更快一步</h2>
            <p>扫码进入驰远物流小程序：在线下单、实时轨迹、电子回单、对账开票，一个入口全部搞定。</p>
            <div class="cta-card__actions">
              <button class="btn btn--primary" type="button" @click="showQr = true">打开小程序</button>
              <RouterLink class="btn btn--ghost" to="/monthly">申请企业月结</RouterLink>
            </div>
            <p class="cta-card__hint">已有小程序？在微信中搜索「驰远物流」即可。</p>
          </div>
          <button class="cta-card__qr" type="button" aria-label="查看小程序码" @click="showQr = true">
            <svg viewBox="0 0 25 25" role="img" aria-label="驰远物流小程序码示意">
              <rect width="25" height="25" fill="#fff" />
              <g fill="#071a33">
                <rect v-for="(cell, index) in qrModules" :key="index" :x="cell.x" :y="cell.y" width="1" height="1" />
                <path d="M0 0h7v7H0zM1 1v5h5V1zM2 2h3v3H2z" />
                <path d="M18 0h7v7h-7zM19 1v5h5V1zM20 2h3v3h-3z" />
                <path d="M0 18h7v7H0zM1 19v5h5v-5zM2 20h3v3H2z" />
              </g>
            </svg>
            <span>微信扫一扫</span>
          </button>
        </div>
      </div>
    </section>

    <Teleport to="body">
      <div v-if="showQr" class="qr-modal" role="dialog" aria-modal="true" aria-label="打开驰远物流小程序" @click.self="showQr = false">
        <div class="qr-modal__card">
          <button class="qr-modal__close" type="button" aria-label="关闭" @click="showQr = false">×</button>
          <span class="eyebrow">MINI PROGRAM</span>
          <h3>微信扫码进入小程序</h3>
          <div class="qr-modal__code">
            <svg viewBox="0 0 25 25" role="img" aria-label="小程序码示意">
              <rect width="25" height="25" fill="#fff" />
              <g fill="#071a33">
                <rect v-for="(cell, index) in qrModules" :key="index" :x="cell.x" :y="cell.y" width="1" height="1" />
                <path d="M0 0h7v7H0zM1 1v5h5V1zM2 2h3v3H2z" />
                <path d="M18 0h7v7h-7zM19 1v5h5V1zM20 2h3v3h-3z" />
                <path d="M0 18h7v7H0zM1 19v5h5v-5zM2 20h3v3H2z" />
              </g>
            </svg>
          </div>
          <p>在线下单 · 查轨迹 · 电子回单 · 对账开票</p>
          <p class="small muted">演示环境为示意二维码，正式环境由租户后台配置真实小程序码。</p>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

/* ---------------- Hero ---------------- */
.hero {
  position: relative;
  overflow: hidden;
  color: #fff;
  background: radial-gradient(900px 500px at 88% 10%, rgba(31, 111, 235, 0.5), transparent 62%),
    radial-gradient(700px 400px at 8% 90%, rgba(20, 82, 176, 0.45), transparent 65%),
    linear-gradient(140deg, #061a33 0%, #082b58 52%, #0b3d84 100%);
}

.hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(255, 255, 255, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.045) 1px, transparent 1px);
  background-size: 64px 64px;
  mask-image: radial-gradient(circle at 60% 40%, #000 20%, transparent 80%);
  pointer-events: none;
}

.hero__glow {
  position: absolute;
  width: 520px;
  height: 520px;
  border-radius: 50%;
  right: -140px;
  top: -180px;
  background: radial-gradient(circle, rgba(90, 168, 255, 0.35), transparent 66%);
  filter: blur(10px);
}

.hero__inner {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 54px;
  align-items: center;
  padding-top: 82px;
  padding-bottom: 112px;
}

.hero__badge {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  padding: 7px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.14);
  font-size: 12.5px;
  color: rgba(255, 255, 255, 0.82);
}

.hero__badge i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #4ade80;
  box-shadow: 0 0 0 4px rgba(74, 222, 128, 0.18);
}

.hero h1 {
  font-size: clamp(38px, 4.5vw, 58px);
  line-height: 1.18;
  letter-spacing: -0.03em;
  margin: 22px 0 20px;
  font-weight: 750;
}

.hero h1 span {
  background: linear-gradient(92deg, #8cc4ff, #4f9bff 55%, #b3d7ff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.hero__lead {
  max-width: 520px;
  font-size: 16px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.7);
}

.hero__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 30px;
}

.hero__cta {
  min-height: 50px;
  padding: 0 28px;
  font-size: 15.5px;
}

.hero__stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin: 42px 0 0;
  padding-top: 26px;
  border-top: 1px solid rgba(255, 255, 255, 0.12);
}

.hero__stats dt {
  font-size: clamp(20px, 2.2vw, 26px);
  font-weight: 700;
  letter-spacing: -0.01em;
}

.hero__stats dd {
  margin: 2px 0 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

/* Hero visual */
.hero__visual {
  position: relative;
  min-height: 440px;
}

.hv-card {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 440px;
  margin: 0 auto;
  padding: 20px;
  border-radius: 20px;
  background: linear-gradient(165deg, rgba(255, 255, 255, 0.98), rgba(240, 247, 255, 0.94));
  color: var(--ink);
  box-shadow: 0 34px 80px rgba(3, 14, 30, 0.45);
  transform: rotate(-1.4deg);
}

.hv-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hv-live {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-weight: 700;
  color: var(--muted);
  letter-spacing: 0.04em;
}

.hv-live i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #16a34a;
  animation: blink 1.8s infinite;
}

.hv-status {
  font-size: 11px;
  font-weight: 700;
  color: var(--primary);
  background: #e8f2ff;
  border-radius: 999px;
  padding: 4px 10px;
}

.hv-no {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 12px;
  color: var(--muted-light);
  margin: 5px 0 16px;
  letter-spacing: 0.02em;
}

.hv-route {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hv-route__point {
  display: flex;
  flex-direction: column;
  min-width: 72px;
}

.hv-route__point--right {
  text-align: right;
}

.hv-route__point strong {
  font-size: 20px;
}

.hv-route__point small {
  font-size: 9.5px;
  color: var(--muted-light);
  white-space: nowrap;
}

.hv-route__track {
  position: relative;
  flex: 1;
  height: 2px;
  background: repeating-linear-gradient(90deg, #a9cdf7 0 5px, transparent 5px 10px);
}

.hv-route__track i {
  position: absolute;
  left: 0;
  top: -3px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary);
}

.hv-truck {
  position: absolute;
  left: 46%;
  top: -11px;
  font-size: 12px;
  color: var(--primary);
}

.hv-map {
  position: relative;
  height: 118px;
  margin: 16px 0 14px;
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(140deg, #e8f2ff, #f4f9ff);
  border: 1px solid #dbe8f8;
}

.hv-map__grid {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(31, 111, 235, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(31, 111, 235, 0.08) 1px, transparent 1px);
  background-size: 26px 26px;
}

.hv-map svg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.hv-map svg path {
  fill: none;
  stroke: #1f6feb;
  stroke-width: 1.6;
  stroke-dasharray: 4 3;
  opacity: 0.65;
}

.hv-map__pin {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 10px;
  font-weight: 700;
  color: #174e9c;
  background: rgba(255, 255, 255, 0.88);
  border-radius: 5px;
  padding: 2px 6px;
  box-shadow: 0 3px 8px rgba(10, 43, 94, 0.12);
}

.hv-map__pin i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--primary);
}

.hv-map__pin--a {
  left: 8%;
  top: 66%;
}

.hv-map__pin--b {
  right: 8%;
  top: 22%;
}

.hv-map__truck {
  position: absolute;
  left: 50%;
  top: 38%;
  font-size: 15px;
  filter: drop-shadow(0 3px 4px rgba(10, 43, 94, 0.18));
}

.hv-steps {
  display: flex;
  justify-content: space-between;
  gap: 4px;
  font-size: 10px;
  color: var(--muted-light);
}

.hv-steps span {
  position: relative;
  padding-top: 12px;
  flex: 1;
  text-align: center;
}

.hv-steps span::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  width: 7px;
  height: 7px;
  margin-left: -3.5px;
  border-radius: 50%;
  background: #dce6f3;
}

.hv-steps .is-done,
.hv-steps .is-current {
  color: var(--primary);
  font-weight: 700;
}

.hv-steps .is-done::before,
.hv-steps .is-current::before {
  background: var(--primary);
}

.hv-steps .is-current::before {
  box-shadow: 0 0 0 4px rgba(31, 111, 235, 0.15);
}

.hv-float {
  position: absolute;
  z-index: 3;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 20px 50px rgba(3, 14, 30, 0.35);
  color: var(--ink);
}

.hv-float--quote {
  left: -14px;
  bottom: 26px;
  padding: 13px 16px;
  display: flex;
  flex-direction: column;
  transform: rotate(1.6deg);
}

.hv-float__label {
  font-size: 10px;
  color: var(--muted-light);
}

.hv-float--quote strong {
  font-size: 24px;
  color: var(--primary);
  letter-spacing: -0.02em;
  line-height: 1.25;
}

.hv-float__hint {
  font-size: 9.5px;
  color: var(--muted-light);
}

.hv-float--eta {
  right: -8px;
  top: 34px;
  padding: 11px 14px;
  display: flex;
  align-items: center;
  gap: 9px;
  transform: rotate(2deg);
}

.hv-float__ring {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 13px;
  font-weight: 800;
  color: var(--primary);
  background: conic-gradient(var(--primary) 0 92%, #e8eef7 92% 100%);
  position: relative;
}

.hv-float__ring::before {
  content: '';
  position: absolute;
  inset: 4px;
  border-radius: 50%;
  background: #fff;
}

.hv-float__ring > *,
.hv-float__ring {
  z-index: 1;
}

.hv-float__ring small {
  font-size: 8px;
}

.hv-float__text {
  font-size: 9.5px;
  color: var(--muted);
  line-height: 1.4;
}

@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.25;
  }
}

/* ---------------- Quick tools ---------------- */
.quick-wrap {
  position: relative;
  z-index: 3;
  margin-top: -54px;
}

.quick {
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 0;
  overflow: hidden;
  box-shadow: var(--shadow);
}

.quick__panel {
  padding: 24px 26px;
}

.quick__panel--track {
  background: #f8fbff;
  border-left: 1px solid var(--line);
}

.quick__head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.quick__icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  background: #eaf3ff;
  color: var(--primary);
  font-weight: 800;
  font-size: 15px;
}

.quick__icon--green {
  background: #eaf8f0;
  color: var(--green);
}

.quick__head h2 {
  font-size: 16px;
}

.quick__head p {
  font-size: 12px;
  color: var(--muted-light);
}

.quick__form {
  display: flex;
  gap: 9px;
  align-items: center;
}

.quick__form .select,
.quick__form .input {
  min-height: 44px;
  font-size: 13.5px;
}

.quick__form .select {
  width: auto;
  min-width: 92px;
  padding-right: 30px;
}

.quick__weight {
  width: 106px;
  flex-shrink: 0;
}

.quick__phone {
  width: 124px;
  flex-shrink: 0;
}

.quick__form .btn {
  min-height: 44px;
  padding: 0 16px;
  font-size: 13.5px;
  flex-shrink: 0;
}

.quick__arrow {
  color: var(--muted-light);
  font-size: 13px;
}

/* ---------------- Services ---------------- */
.service-card {
  padding: 26px;
  display: flex;
  flex-direction: column;
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}

.service-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow);
  border-color: #c9dcf7;
}

.service-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.service-card__icon {
  width: 50px;
  height: 50px;
  border-radius: 13px;
  display: grid;
  place-items: center;
  background: linear-gradient(145deg, #eaf3ff, #d9e9ff);
  color: var(--primary);
  font-size: 22px;
  font-weight: 800;
}

.service-card__subtitle {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.1em;
  color: var(--muted-light);
}

.service-card h3 {
  font-size: 21px;
  margin-bottom: 8px;
}

.service-card__desc {
  font-size: 13.5px;
  color: var(--muted);
  line-height: 1.7;
  min-height: 46px;
}

.service-card ul {
  list-style: none;
  margin: 16px 0 20px;
  display: grid;
  gap: 8px;
}

.service-card li {
  position: relative;
  padding-left: 17px;
  font-size: 13px;
  color: #33465d;
}

.service-card li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #9cc4f7;
}

.service-card__foot {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid var(--line);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.service-card__price {
  font-size: 13px;
  font-weight: 700;
  color: var(--primary);
}

.service-card__foot a {
  font-size: 12px;
  color: var(--muted);
}

.service-card__foot a:hover {
  color: var(--primary);
}

/* ---------------- Network ---------------- */
.network__layout {
  display: grid;
  grid-template-columns: 1fr 268px;
  gap: 24px;
  align-items: stretch;
}

.network__skeleton {
  min-height: 420px;
  border-radius: 18px;
}

.network__aside {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 22px;
  border-radius: 18px;
  background: #fff;
  border: 1px solid var(--line);
}

.network__stat {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding-bottom: 12px;
  border-bottom: 1px dashed var(--line);
}

.network__stat strong {
  font-size: 26px;
  color: var(--primary);
  letter-spacing: -0.02em;
}

.network__stat span {
  font-size: 12px;
  color: var(--muted);
}

.network__cities {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin: 4px 0 auto;
}

.network__cities a {
  font-size: 12px;
  padding: 5px 10px;
  border-radius: 6px;
  background: var(--soft);
  color: #32465e;
}

.network__cities a:hover {
  background: var(--soft-blue);
  color: var(--primary);
}

/* ---------------- Cases ---------------- */
.cases__logos {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.cases__logos span {
  font-size: 11.5px;
  color: var(--muted-light);
  border: 1px solid var(--line);
  border-radius: 6px;
  padding: 5px 10px;
}

.case-card {
  padding: 24px;
  display: flex;
  flex-direction: column;
}

.case-card__industry {
  align-self: flex-start;
  font-size: 11px;
  font-weight: 700;
  color: var(--primary);
  background: #eef5ff;
  padding: 3px 9px;
  border-radius: 5px;
  margin-bottom: 14px;
}

.case-card blockquote {
  font-size: 14px;
  line-height: 1.8;
  color: #2b3c52;
  flex: 1;
}

.case-card__company {
  margin-top: 14px;
  font-size: 12px;
  color: var(--muted-light);
}

.case-card__metrics {
  display: flex;
  gap: 26px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--line);
}

.case-card__metrics div {
  display: flex;
  flex-direction: column;
}

.case-card__metrics strong {
  font-size: 21px;
  color: var(--primary);
  line-height: 1.2;
}

.case-card__metrics span {
  font-size: 11px;
  color: var(--muted-light);
}

/* ---------------- Qualifications ---------------- */
.qual-card {
  padding: 22px;
}

.qual-card__badge {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 9px;
  background: #eaf8f0;
  color: var(--green);
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 14px;
}

.qual-card h3 {
  font-size: 15px;
  margin-bottom: 6px;
}

.qual-card p {
  font-size: 12px;
  color: var(--muted);
  line-height: 1.6;
  min-height: 38px;
}

.qual-card__year {
  display: inline-block;
  margin-top: 10px;
  font-size: 11px;
  color: var(--muted-light);
  border-top: 1px solid var(--line);
  padding-top: 9px;
  width: 100%;
}

/* ---------------- FAQ ---------------- */
.faq__layout {
  display: grid;
  grid-template-columns: 0.75fr 1.25fr;
  gap: 56px;
  align-items: start;
}

.faq__intro h2 {
  font-size: clamp(26px, 3vw, 34px);
  line-height: 1.28;
  margin: 10px 0 14px;
}

.faq__intro p {
  font-size: 14px;
  color: var(--muted);
  line-height: 1.8;
  margin-bottom: 20px;
}

.faq__list {
  border-top: 1px solid var(--line);
}

.faq__item {
  border-bottom: 1px solid var(--line);
}

.faq__item button {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 18px 2px;
  background: transparent;
  border: 0;
  text-align: left;
  font-size: 15px;
  font-weight: 600;
  color: var(--ink);
}

.faq__item button i {
  font-style: normal;
  color: var(--primary);
  font-size: 18px;
  flex-shrink: 0;
}

.faq__item p {
  padding: 0 36px 18px 2px;
  font-size: 13.5px;
  color: var(--muted);
  line-height: 1.85;
}

.faq__item--open button {
  color: var(--primary);
}

/* ---------------- CTA ---------------- */
.cta-card {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40px;
  padding: 52px 56px;
  border-radius: 22px;
  color: #fff;
  background: radial-gradient(600px 300px at 85% 20%, rgba(64, 150, 255, 0.5), transparent 60%),
    linear-gradient(125deg, #071a33, #0b3d84 70%, #1557c0);
}

.cta-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 46px 46px;
  mask-image: radial-gradient(circle at 80% 30%, #000, transparent 75%);
  pointer-events: none;
}

.cta-card__copy {
  position: relative;
  z-index: 1;
  max-width: 590px;
}

.cta-card .eyebrow {
  color: #8dc0ff;
}

.cta-card .eyebrow::before {
  background: #8dc0ff;
}

.cta-card h2 {
  font-size: clamp(26px, 3vw, 36px);
  margin: 12px 0 12px;
  letter-spacing: -0.02em;
}

.cta-card p {
  color: rgba(255, 255, 255, 0.68);
  font-size: 14.5px;
  line-height: 1.8;
}

.cta-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 24px;
}

.cta-card__hint {
  margin-top: 14px;
  font-size: 12px !important;
  color: rgba(255, 255, 255, 0.45) !important;
}

.cta-card__qr {
  position: relative;
  z-index: 1;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 14px 14px 10px;
  border: 0;
  border-radius: 16px;
  background: #fff;
  color: #33465d;
  font-size: 11px;
  font-weight: 600;
  box-shadow: 0 24px 50px rgba(0, 0, 0, 0.3);
}

.cta-card__qr svg {
  width: 132px;
  height: 132px;
}

/* ---------------- QR modal ---------------- */
.qr-modal {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: grid;
  place-items: center;
  padding: 20px;
  background: rgba(4, 13, 26, 0.65);
  backdrop-filter: blur(4px);
}

.qr-modal__card {
  position: relative;
  width: min(360px, 100%);
  padding: 32px 28px 26px;
  border-radius: 18px;
  background: #fff;
  text-align: center;
  box-shadow: var(--shadow-lg);
}

.qr-modal__close {
  position: absolute;
  right: 12px;
  top: 10px;
  width: 36px;
  height: 36px;
  border: 0;
  background: transparent;
  color: var(--muted);
  font-size: 24px;
  line-height: 1;
  border-radius: 8px;
}

.qr-modal__close:hover {
  background: var(--soft);
}

.qr-modal__card h3 {
  font-size: 20px;
  margin: 10px 0 20px;
}

.qr-modal__code {
  width: 190px;
  height: 190px;
  margin: 0 auto 18px;
  padding: 10px;
  border-radius: 12px;
  border: 1px solid var(--line);
}

.qr-modal__code svg {
  width: 100%;
  height: 100%;
}

.qr-modal__card p {
  font-size: 13px;
  color: var(--muted);
}

.qr-modal__card p.small {
  margin-top: 10px;
  font-size: 11px;
  color: var(--muted-light);
}

/* ---------------- Responsive ---------------- */
@media (max-width: 1080px) {
  .hero__inner {
    gap: 30px;
  }

  .hv-float--quote {
    left: 0;
  }

  .hv-float--eta {
    right: 0;
  }

  .quick__form {
    flex-wrap: wrap;
  }

  .quick__form .select,
  .quick__form .input,
  .quick__form .btn {
    flex: 1;
  }

  .quick__weight,
  .quick__phone {
    width: auto;
  }

  .quick__arrow {
    display: none;
  }
}

@media (max-width: 900px) {
  .hero__inner {
    grid-template-columns: 1fr;
    padding-top: 54px;
    padding-bottom: 118px;
  }

  .hero__visual {
    min-height: 380px;
    max-width: 480px;
    margin: 0 auto;
    width: 100%;
  }

  .hero__lead {
    max-width: none;
  }

  .quick {
    grid-template-columns: 1fr;
  }

  .quick__panel--track {
    border-left: 0;
    border-top: 1px solid var(--line);
  }

  .network__layout {
    grid-template-columns: 1fr;
  }

  .network__aside {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 14px;
  }

  .network__cities,
  .network__aside .btn {
    grid-column: 1 / -1;
  }

  .network__stat {
    border-bottom: 0;
    padding-bottom: 0;
  }

  .faq__layout {
    grid-template-columns: 1fr;
    gap: 28px;
  }

  .cta-card {
    padding: 40px 32px;
  }
}

@media (max-width: 720px) {
  .hero__inner {
    padding-top: 42px;
    padding-bottom: 100px;
  }

  .hero h1 {
    font-size: 34px;
  }

  .hero__stats {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    margin-top: 30px;
  }

  .hero__stats dt {
    font-size: 22px;
  }

  .hero__visual {
    min-height: 350px;
  }

  .hv-card {
    transform: none;
    padding: 16px;
  }

  .hv-float--quote {
    bottom: -8px;
    left: -4px;
    padding: 10px 13px;
  }

  .hv-float--quote strong {
    font-size: 20px;
  }

  .hv-float--eta {
    top: -12px;
    right: -4px;
  }

  .quick-wrap {
    margin-top: -40px;
  }

  .quick__panel {
    padding: 20px;
  }

  .quick__form .select,
  .quick__form .input {
    min-width: 0;
    flex: 1 1 40%;
  }

  .quick__form .btn {
    flex: 1 1 100%;
  }

  .network__aside {
    grid-template-columns: 1fr 1fr;
  }

  .network__aside .network__stat:last-of-type {
    grid-column: 1 / -1;
  }

  .cases__logos {
    display: none;
  }

  .cta-card {
    flex-direction: column;
    align-items: flex-start;
    padding: 34px 24px;
  }

  .cta-card__qr {
    align-self: center;
  }
}

@media (max-width: 460px) {
  .hero__actions .btn {
    width: 100%;
  }

  .hv-float--eta {
    display: none;
  }

  .hv-route__point small {
    font-size: 8.5px;
  }
}
</style>
