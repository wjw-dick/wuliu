<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { getPriceGuide } from '../api/public'
import type { PriceGuide } from '../types'

const guide = ref<PriceGuide | null>(null)
const loading = ref(true)
const error = ref('')

const serviceDetails = [
  {
    id: 'ltl',
    tag: 'LTL · 零担专线',
    title: '小批量、多批次，拼载更省',
    description:
      '面向月发货 10~200 票的企业客户，依托上海、杭州、苏州、南京、宁波等直营网点，天天发车、当日分拨，核心城市今发明至。',
    features: [
      '30kg 起运，1 件也接，按实际重量与体积重取大计费',
      '天天发车，华东核心线路 18:00 前截单当日发出',
      '全程节点扫描，提货、装车、到达、签收短信推送',
      '电子回单与纸质回单可选，回单影像 24 小时内上传'
    ],
    suitable: '汽配、电子元器件、服装鞋帽、食品饮料、日常百货',
    lead: '最低 0.85 元/kg 起'
  },
  {
    id: 'ftl',
    tag: 'FTL · 整车包车',
    title: '一车一票，门到门直达',
    description:
      '4.2 米至 17.5 米全车型，专车专用不中转。支持厢式、高栏、平板、冷藏与气垫车，适配设备、原材料与大促备货。',
    features: [
      '专属车辆，装车即走，全程不换装不分拨',
      'GPS 轨迹与电子围栏，偏离路线自动预警',
      '可选尾板、叉车、吊装与温控增值服务',
      '按趟计价，含基础里程与等候时长，超时提前确认'
    ],
    suitable: '大型设备、原材料、展会物资、电商大促、整车调拨',
    lead: '900 元/趟 起'
  },
  {
    id: 'city',
    tag: 'CITY · 同城配送',
    title: '主城区当日达，多点配送',
    description:
      '上海、杭州主城区当日达，支持预约时段、多点配送与上楼入仓。为门店补货、仓间调拨与 O2O 订单提供稳定运力。',
    features: [
      '3 公里内 120 元起步，超出按公里透明计费',
      '多点配送每增加一点仅加收 30 元',
      '签收拍照、电子签名，回单当日回传',
      '支持固定司机、固定线路的长期城配方案'
    ],
    suitable: '连锁门店、商超补货、仓间调拨、O2O 即时配送',
    lead: '120 元/单 起'
  },
  {
    id: 'warehouse',
    tag: 'VAS · 仓储与增值',
    title: '仓配一体，按需扩展',
    description:
      '上海青浦、杭州余杭设标准仓，提供短期周转、贴标换标、分拣打包、越库中转与库存可视化服务。',
    features: [
      '标准托盘位与货架位灵活租用，支持短租',
      '贴标、换包装、组套、质检等增值作业',
      'WMS 库存同步，出入库实时可查',
      '仓配一体报价，减少中间环节'
    ],
    suitable: '电商备货、跨境集货、区域分仓、项目临时周转',
    lead: '按需报价'
  }
]

const flow = [
  { step: '01', title: '提交需求', desc: '官网/小程序/电话，1 分钟完成询价' },
  { step: '02', title: '确认报价', desc: '客服复核货物与线路，锁定价格' },
  { step: '03', title: '上门提货', desc: '按预约时间提货，现场验货拍照' },
  { step: '04', title: '干线运输', desc: '全程轨迹可查，异常主动预警' },
  { step: '05', title: '签收回单', desc: '电子签收，回单影像当日回传' }
]

onMounted(async () => {
  try {
    guide.value = await getPriceGuide()
  } catch {
    error.value = '计费说明加载失败，请稍后重试或致电 400-880-1688。'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="services-page">
    <section class="page-hero">
      <div class="container page-hero__inner">
        <span class="eyebrow">SERVICES &amp; PRICING</span>
        <h1>服务与价格，都写在明面上</h1>
        <p>
          从一票零担到整车包车，从同城配送到仓配一体，我们提供标准化产品与可核验的计费口径。没有隐藏费用，特殊作业先确认后执行。
        </p>
        <div class="hero-actions">
          <RouterLink class="btn btn--primary" to="/quote">立即在线询价</RouterLink>
          <a class="btn btn--ghost" href="#price-guide">查看计费标准</a>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="container services-list">
        <article v-for="(service, index) in serviceDetails" :id="service.id" :key="service.id" class="service-block" :class="{ 'service-block--reverse': index % 2 === 1 }">
          <div class="service-block__body">
            <span class="eyebrow">{{ service.tag }}</span>
            <h2>{{ service.title }}</h2>
            <p class="service-block__desc">{{ service.description }}</p>
            <ul class="service-block__features">
              <li v-for="feature in service.features" :key="feature">{{ feature }}</li>
            </ul>
            <div class="service-block__meta">
              <div>
                <span>适合货物</span>
                <p>{{ service.suitable }}</p>
              </div>
              <div>
                <span>参考价格</span>
                <p class="service-block__price">{{ service.lead }}</p>
              </div>
            </div>
            <div class="service-block__actions">
              <RouterLink class="btn btn--primary btn--sm" to="/quote">获取此服务报价</RouterLink>
              <a class="btn btn--outline btn--sm" href="tel:4008801688">电话咨询</a>
            </div>
          </div>
          <div class="service-block__visual" aria-hidden="true">
            <div class="visual-card">
              <span class="visual-card__tag">{{ service.tag.split(' · ')[0] }}</span>
              <div class="visual-card__lines">
                <i></i><i></i><i></i>
              </div>
              <div class="visual-card__route">
                <span>上海</span>
                <div><b></b></div>
                <span>杭州</span>
              </div>
              <div class="visual-card__foot">
                <span>{{ service.lead }}</span>
                <span>全程可查</span>
              </div>
            </div>
            <span class="visual-badge">24h 响应</span>
            <span class="visual-badge visual-badge--bottom">电子回单</span>
          </div>
        </article>
      </div>
    </section>

    <section class="section section--soft process">
      <div class="container">
        <div class="section-head">
          <div class="section-head__text">
            <span class="eyebrow">WORKFLOW</span>
            <h2>从询价到回单，五步完成</h2>
            <p>每个节点都有责任人和时间戳，异常在 30 分钟内进入处理流程。</p>
          </div>
        </div>
        <ol class="process__flow">
          <li v-for="item in flow" :key="item.step">
            <span class="process__num">{{ item.step }}</span>
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
          </li>
        </ol>
      </div>
    </section>

    <section id="price-guide" class="section price-guide">
      <div class="container">
        <div class="section-head">
          <div class="section-head__text">
            <span class="eyebrow">PRICE GUIDE</span>
            <h2>计费口径公开</h2>
            <p v-if="guide">{{ guide.intro }}</p>
            <p v-else>首重续重、体积重换算、附加费说明，公开可核。</p>
          </div>
          <span v-if="guide" class="pill">更新于 {{ guide.updatedAt }}</span>
        </div>

        <div v-if="loading" class="price-skeleton">
          <div class="skeleton"></div>
          <div class="skeleton"></div>
          <div class="skeleton"></div>
        </div>

        <div v-else-if="error" class="state-block">
          <div class="state-block__icon" aria-hidden="true">!</div>
          <h3>计费说明暂时无法加载</h3>
          <p>{{ error }}</p>
          <button class="btn btn--outline btn--sm" type="button" @click="$router.go(0)">重新加载</button>
        </div>

        <template v-else-if="guide">
          <div class="price-grid">
            <div v-for="item in guide.items" :key="item.title" class="price-item card">
              <h3>{{ item.title }}</h3>
              <p class="price-item__rule">{{ item.rule }}</p>
              <p class="price-item__example"><span>示例</span>{{ item.example }}</p>
            </div>
          </div>

          <div class="surcharge card">
            <div class="surcharge__head">
              <h3>附加费一览</h3>
              <span class="small muted">以下为官网公示标准，特殊场景单独报价</span>
            </div>
            <div class="table-scroll">
              <table class="data-table">
                <thead>
                  <tr>
                    <th scope="col">费用名称</th>
                    <th scope="col">收费标准</th>
                    <th scope="col">说明</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="row in guide.surcharges" :key="row.name">
                    <td>{{ row.name }}</td>
                    <td>{{ row.standard }}</td>
                    <td class="surcharge__remark">{{ row.remark || '—' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="price-notes">
            <h3>补充说明</h3>
            <ul>
              <li v-for="note in guide.notes" :key="note">{{ note }}</li>
            </ul>
          </div>
        </template>

        <div class="price-cta">
          <div>
            <h3>想知道这票货具体多少钱？</h3>
            <p>输入线路与货物信息，30 秒查看完整费用明细。</p>
          </div>
          <RouterLink class="btn btn--primary" to="/quote">开始在线询价</RouterLink>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 24px;
}

.services-list {
  display: flex;
  flex-direction: column;
  gap: 64px;
}

.service-block {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 56px;
  align-items: center;
  scroll-margin-top: 120px;
}

.service-block--reverse .service-block__body {
  order: 2;
}

.service-block--reverse .service-block__visual {
  order: 1;
}

.service-block__body h2 {
  font-size: clamp(26px, 3vw, 34px);
  line-height: 1.3;
  margin: 10px 0 12px;
  letter-spacing: -0.02em;
}

.service-block__desc {
  font-size: 14.5px;
  line-height: 1.85;
  color: var(--muted);
}

.service-block__features {
  list-style: none;
  margin: 20px 0;
  display: grid;
  gap: 9px;
}

.service-block__features li {
  position: relative;
  padding-left: 20px;
  font-size: 13.5px;
  color: #32465e;
}

.service-block__features li::before {
  content: '✓';
  position: absolute;
  left: 0;
  top: 0;
  color: var(--green);
  font-weight: 700;
  font-size: 12px;
}

.service-block__meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
  padding: 16px 0;
  border-top: 1px solid var(--line);
  border-bottom: 1px solid var(--line);
}

.service-block__meta span {
  font-size: 11px;
  color: var(--muted-light);
}

.service-block__meta p {
  font-size: 13px;
  color: #32465e;
  margin-top: 2px;
  line-height: 1.6;
}

.service-block__meta .service-block__price {
  color: var(--primary);
  font-weight: 700;
  font-size: 15px;
}

.service-block__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 20px;
}

.service-block__visual {
  position: relative;
  min-height: 330px;
  border-radius: 20px;
  background: linear-gradient(145deg, #eef6ff, #dcecff);
  border: 1px solid #d3e4fa;
  display: grid;
  place-items: center;
  overflow: hidden;
}

.service-block__visual::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(31, 111, 235, 0.07) 1px, transparent 1px),
    linear-gradient(90deg, rgba(31, 111, 235, 0.07) 1px, transparent 1px);
  background-size: 34px 34px;
}

.visual-card {
  position: relative;
  z-index: 1;
  width: min(300px, 80%);
  padding: 20px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 24px 50px rgba(10, 43, 94, 0.16);
  transform: rotate(-2deg);
}

.visual-card__tag {
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.16em;
  color: var(--primary);
  background: #eff5ff;
  border-radius: 5px;
  padding: 3px 8px;
}

.visual-card__lines {
  display: grid;
  gap: 7px;
  margin: 16px 0;
}

.visual-card__lines i {
  display: block;
  height: 8px;
  border-radius: 4px;
  background: #eef3fa;
}

.visual-card__lines i:nth-child(1) {
  width: 88%;
}

.visual-card__lines i:nth-child(2) {
  width: 66%;
}

.visual-card__lines i:nth-child(3) {
  width: 74%;
}

.visual-card__route {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  border-top: 1px solid var(--line);
  border-bottom: 1px solid var(--line);
  font-size: 17px;
  font-weight: 700;
}

.visual-card__route div {
  flex: 1;
  position: relative;
  height: 2px;
  background: repeating-linear-gradient(90deg, #9cc4f7 0 5px, transparent 5px 10px);
}

.visual-card__route b {
  position: absolute;
  left: 40%;
  top: -9px;
  width: 14px;
  height: 18px;
  border-radius: 4px;
  background: var(--primary);
}

.visual-card__foot {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 11px;
  color: var(--muted);
}

.visual-card__foot span:first-child {
  color: var(--primary);
  font-weight: 700;
}

.visual-badge {
  position: absolute;
  z-index: 2;
  top: 26px;
  right: 22px;
  padding: 6px 12px;
  border-radius: 999px;
  background: #071a33;
  color: #fff;
  font-size: 10.5px;
  font-weight: 600;
  box-shadow: 0 8px 20px rgba(7, 26, 51, 0.25);
}

.visual-badge--bottom {
  top: auto;
  right: auto;
  bottom: 28px;
  left: 24px;
  background: #fff;
  color: var(--primary);
}

/* Process */
.process__flow {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  list-style: none;
  counter-reset: step;
}

.process__flow li {
  padding: 22px 18px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 14px;
  position: relative;
}

.process__flow li:not(:last-child)::after {
  content: '→';
  position: absolute;
  right: -13px;
  top: 42px;
  color: #b9cde7;
  font-size: 14px;
  z-index: 1;
}

.process__num {
  font-size: 22px;
  font-weight: 800;
  color: #d5e5fa;
  letter-spacing: -0.02em;
}

.process__flow h3 {
  font-size: 15px;
  margin: 8px 0 4px;
}

.process__flow p {
  font-size: 12px;
  color: var(--muted);
  line-height: 1.65;
}

/* Price guide */
.price-guide {
  scroll-margin-top: 100px;
}

.price-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px;
}

.price-item {
  padding: 24px;
}

.price-item h3 {
  font-size: 17px;
  margin-bottom: 10px;
}

.price-item__rule {
  font-size: 13.5px;
  color: var(--muted);
  line-height: 1.8;
}

.price-item__example {
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f7fbff;
  border: 1px solid #e2eefb;
  font-size: 12.5px;
  color: #32465e;
  line-height: 1.65;
}

.price-item__example span {
  color: var(--primary);
  font-weight: 700;
  margin-right: 6px;
}

.surcharge {
  margin-top: 22px;
  overflow: hidden;
}

.surcharge__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 14px;
  padding: 20px 24px;
  border-bottom: 1px solid var(--line);
  flex-wrap: wrap;
}

.surcharge__head h3 {
  font-size: 17px;
}

.surcharge__remark {
  color: var(--muted) !important;
  font-size: 12.5px;
}

.price-notes {
  margin-top: 22px;
  padding: 22px 24px;
  border-radius: var(--radius);
  background: #f7faff;
  border: 1px solid #e4eefb;
}

.price-notes h3 {
  font-size: 14px;
  margin-bottom: 8px;
}

.price-notes ul {
  padding-left: 20px;
  display: grid;
  gap: 5px;
  font-size: 12.5px;
  color: var(--muted);
}

.price-skeleton {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.price-skeleton .skeleton {
  height: 150px;
}

.price-cta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-top: 32px;
  padding: 26px 30px;
  border-radius: var(--radius);
  background: linear-gradient(120deg, #0a2b5e, #1557c0);
  color: #fff;
}

.price-cta h3 {
  font-size: 19px;
  margin-bottom: 4px;
}

.price-cta p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.65);
}

.price-cta .btn {
  flex-shrink: 0;
}

.table-scroll {
  overflow-x: auto;
}

@media (max-width: 960px) {
  .service-block {
    grid-template-columns: 1fr;
    gap: 28px;
  }

  .service-block--reverse .service-block__body,
  .service-block--reverse .service-block__visual {
    order: unset;
  }

  .service-block__visual {
    min-height: 280px;
  }

  .process__flow {
    grid-template-columns: repeat(2, 1fr);
  }

  .process__flow li::after {
    display: none;
  }
}

@media (max-width: 700px) {
  .services-list {
    gap: 44px;
  }

  .price-grid {
    grid-template-columns: 1fr;
  }

  .price-skeleton {
    grid-template-columns: 1fr;
  }

  .process__flow {
    grid-template-columns: 1fr;
  }

  .service-block__meta {
    grid-template-columns: 1fr;
  }

  .price-cta {
    flex-direction: column;
    align-items: flex-start;
    padding: 24px;
  }

  .price-cta .btn {
    width: 100%;
  }
}
</style>
