<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

const props = defineProps<{ page: 'about' | 'contact' | 'join' | 'privacy' | 'terms' }>()

const heroMap = {
  about: {
    kicker: 'ABOUT CHIYUAN',
    title: '十二年，只做一件事：把货稳稳送到',
    intro:
      '驰远物流成立于 2012 年，总部位于上海青浦，专注长三角合同物流与零担专线。我们以直营网点、自有运力与数字化系统，为客户提供确定、透明、可追溯的运输服务。'
  },
  contact: {
    kicker: 'CONTACT US',
    title: '有事找我们，随时都在',
    intro:
      '7×24 小时客服热线、商务合作邮箱与各网点联系方式。无论是询价、查件、理赔还是合作洽谈，我们都会在 30 分钟内响应。'
  },
  join: {
    kicker: 'JOIN US',
    title: '和一群靠谱的人，做一件长期的事',
    intro:
      '我们相信物流是实体经济的基础设施。如果你对效率、服务与数字化有兴趣，欢迎加入驰远，一起把每一票货物送好。'
  },
  privacy: {
    kicker: 'PRIVACY POLICY',
    title: '隐私政策',
    intro: '我们重视您的个人信息与隐私保护。本政策说明我们在提供物流服务过程中如何收集、使用、存储和保护您的信息。'
  },
  terms: {
    kicker: 'TERMS OF SERVICE',
    title: '服务条款',
    intro: '本条款适用于您通过驰远物流官网、小程序、H5 或客服渠道使用我们的运输与相关服务。请在托运前仔细阅读。'
  }
} as const

const hero = computed(() => heroMap[props.page])

const stats = [
  { value: '2012', label: '成立年份' },
  { value: '8', label: '直营网点' },
  { value: '300+', label: '自有及签约车辆' },
  { value: '12 万票', label: '日均处理能力' }
]

const values = [
  { title: '准时是底线', desc: '用直营网络与备用运力保障时效，承诺的到达时间就要做到。' },
  { title: '价格写在明处', desc: '首重续重、体积重与附加费全部公开，特殊作业先确认后执行。' },
  { title: '异常主动说', desc: '延误、破损、天气管制第一时间通知，不把问题留到客户来问。' },
  { title: '数据可追溯', desc: '每一票货物都有节点、操作人与时间戳，回单影像随时可查。' }
]

const timeline = [
  { year: '2012', title: '公司成立', desc: '从上海嘉定一条专线起步，服务本地制造企业。' },
  { year: '2016', title: '布局长三角', desc: '开通杭州、苏州、南京专线，网点增至 5 个。' },
  { year: '2020', title: '数字化升级', desc: '上线 TMS 与司机端 App，实现全程节点可视化。' },
  { year: '2023', title: '仓配一体', desc: '上海青浦、杭州余杭标准仓投入运营。' },
  { year: '2026', title: '智能分拨', desc: '华东智能分拨中心启用，日均处理能力达 12 万票。' }
]

const departments = [
  { name: '客户服务', desc: '询价、下单、查件、投诉与建议', phone: '400-880-1688 转 1', email: 'service@chiyuan-logistics.com' },
  { name: '商务合作', desc: '企业月结、合同物流、仓配方案', phone: '400-880-1688 转 2', email: 'sales@chiyuan-logistics.com' },
  { name: '理赔与异常', desc: '货损货差、延误、异常处理', phone: '400-880-1688 转 3', email: 'claims@chiyuan-logistics.com' },
  { name: '人力资源', desc: '社会招聘、校园招聘、司机加盟', phone: '021-5988-1690', email: 'hr@chiyuan-logistics.com' }
]

const jobs = [
  { title: '客户经理（合同物流）', dept: '销售中心', city: '上海 / 杭州', exp: '3 年以上', type: '全职' },
  { title: '调度专员', dept: '运营中心', city: '上海青浦', exp: '1 年以上', type: '全职' },
  { title: '货运司机（B2/A2）', dept: '运力中心', city: '上海 / 苏州', exp: '5 年以上驾龄', type: '全职' },
  { title: '产品经理（物流系统）', dept: '数字化中心', city: '上海', exp: '3 年以上', type: '全职' },
  { title: '运营管培生', dept: '运营中心', city: '上海 / 杭州', exp: '应届生', type: '全职' }
]

const benefits = ['五险一金 + 补充商业保险', '带薪年假与司龄假', '餐补、交通补与通讯补', '年度体检与节日福利', '司机安全里程奖', '内部晋升与轮岗机会']

const privacySections = [
  {
    title: '一、我们收集的信息',
    paragraphs: [
      '为完成运输服务，我们可能收集寄件人与收件人的姓名、手机号码、地址、公司名称、货物信息、运单与轨迹信息、签收与回单影像。',
      '当您使用官网询价、月结申请或在线留言时，我们会收集您主动填写的姓名、电话、公司名称与需求描述。您不提供这些信息时，仍可浏览大部分页面，但可能无法完成询价留资或月结申请。'
    ]
  },
  {
    title: '二、我们如何使用信息',
    paragraphs: [
      '我们仅在以下目的范围内使用您的信息：提供报价、下单、提货、运输、配送、签收与回单服务；进行身份核验与安全校验；处理查询、投诉、理赔与售后；进行对账、开票与结算；遵守法律法规要求。',
      '未经您的单独同意，我们不会将您的个人信息用于营销推送，也不会向无关第三方出售或出租。'
    ]
  },
  {
    title: '三、信息的共享与委托处理',
    paragraphs: [
      '为完成运输，我们可能将必要的收发货信息提供给实际承运司机、合作网点或末端配送人员。我们通过协议要求其仅用于本次运输服务，并采取保密与安全措施。',
      '在法律法规要求、司法机关或监管机构依法调取时，我们可能依法提供相关信息。'
    ]
  },
  {
    title: '四、信息的存储与保护',
    paragraphs: [
      '您的信息存储于中华人民共和国境内的服务器，保存期限为实现服务目的所必需的最短时间，法律法规另有规定的除外。',
      '我们采用访问控制、传输加密、脱敏展示、操作审计等措施保护信息安全。发生个人信息安全事件时，我们将按法律要求及时告知您并向监管部门报告。'
    ]
  },
  {
    title: '五、您的权利',
    paragraphs: [
      '您有权查询、更正、补充或删除您的个人信息，也可以撤回同意或注销账户。您可通过客服热线 400-880-1688 或邮箱 privacy@chiyuan-logistics.com 提出请求。',
      '为保障安全，我们可能需要先验证您的身份，并在 15 个工作日内予以答复。'
    ]
  },
  {
    title: '六、未成年人保护与政策更新',
    paragraphs: [
      '我们的服务面向企业与成年个人用户，不主动收集未满 14 周岁未成年人的个人信息。如发现相关情况，我们将及时删除。',
      '本政策可能因业务或法律变化而更新，更新后将在本页面公示并标注生效日期。'
    ]
  }
]

const termsSections = [
  {
    title: '一、服务范围',
    paragraphs: [
      '驰远物流提供道路货物运输、仓储、装卸、配送及相关增值服务。具体服务内容、时效与价格以报价单、电子运单或双方签署的运输服务协议为准。',
      '危险品、冷链、超限超重、贵重物品等特殊货物，须在下单前如实告知并经我方书面确认后方可承运。'
    ]
  },
  {
    title: '二、托运人义务',
    paragraphs: [
      '托运人应如实提供货物名称、性质、重量、体积、件数、包装方式及收发货信息，并保证货物包装符合运输要求。因申报不实或包装不当造成的损失，由托运人承担相应责任。',
      '托运人应确保货物不属于法律法规禁止运输的物品，并配合完成必要的安全检查与单据签署。'
    ]
  },
  {
    title: '三、费用与结算',
    paragraphs: [
      '运费按报价单、协议价或官网公示的计费口径计算。报价通常不含税，开票税率按国家规定执行。',
      '现结客户应在签收前或约定时间内完成支付；月结客户应按协议约定账期结算。对账单生成后，客户应在 7 个工作日内确认或提出异议。'
    ]
  },
  {
    title: '四、提货、运输与签收',
    paragraphs: [
      '我方按约定时间窗上门提货。因客户原因导致等待、返空或二次上门的，可能产生额外费用，司机会提前告知并确认。',
      '收货人应在签收时检查外包装与件数。发现异常应当场拍照并在签收单上注明；无异议签收后，视为货物外观与件数完好。'
    ]
  },
  {
    title: '五、保险与责任限制',
    paragraphs: [
      '建议托运人按货物声明价值投保。未投保货物的赔偿限额以电子运单约定为准；投保货物的赔付以保险公司核定为准。',
      '因不可抗力、货物自身性质、包装不当或收货人原因造成的损失，我方不承担或依法减轻责任。'
    ]
  },
  {
    title: '六、异常、理赔与争议解决',
    paragraphs: [
      '发生货损、货差或延误时，托运人应在签收后 7 日内通过客服或小程序提交理赔申请，并提供运单、价值证明、照片等材料。我们将在 1 个工作日内响应，资料齐全后 7 个工作日内完成核定。',
      '因本服务产生的争议，双方应友好协商；协商不成的，可向承运人所在地有管辖权的人民法院提起诉讼。'
    ]
  }
]
</script>

<template>
  <div class="static-page">
    <section class="page-hero">
      <div class="container page-hero__inner">
        <span class="eyebrow">{{ hero.kicker }}</span>
        <h1>{{ hero.title }}</h1>
        <p>{{ hero.intro }}</p>
      </div>
    </section>

    <!-- About -->
    <template v-if="page === 'about'">
      <section class="section">
        <div class="container about-intro">
          <div class="about-intro__text">
            <span class="eyebrow">WHO WE ARE</span>
            <h2>用直营网络，做确定的交付</h2>
            <p>
              驰远物流是一家以长三角为核心的区域合同物流服务商。我们不追求网点数量的快速扩张，而是把每一个直营网点做深：稳定的自有运力、训练有素的现场团队、可追溯的作业流程。
            </p>
            <p>
              从 2012 年的一条上海—嘉定专线，到今天覆盖上海、杭州、苏州、南京、宁波、合肥、无锡的 8 个网点与 12 万票/日的分拨能力，我们服务的客户从本地制造企业扩展到汽车零部件、医疗器械、精密电子与电商零售品牌。
            </p>
            <p>
              我们相信，物流的价值不只是把货从 A 运到 B，而是让客户的产线不停、门店不断货、承诺能兑现。这也是我们持续投入数字化与直营网络的原因。
            </p>
            <div class="about-actions">
              <RouterLink class="btn btn--primary btn--sm" to="/contact">联系我们</RouterLink>
              <RouterLink class="btn btn--outline btn--sm" to="/join">加入我们</RouterLink>
            </div>
          </div>
          <div class="about-visual" aria-hidden="true">
            <div class="about-visual__card">
              <span>华东运营网络</span>
              <div class="about-visual__map">
                <i style="left: 62%; top: 38%"></i>
                <i style="left: 48%; top: 52%"></i>
                <i style="left: 30%; top: 46%"></i>
                <i style="left: 40%; top: 68%"></i>
                <i style="left: 70%; top: 62%"></i>
              </div>
              <div class="about-visual__foot">
                <strong>8</strong>
                <span>直营网点 / 分拨中心</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="section section--tight section--soft">
        <div class="container">
          <div class="stats-grid">
            <div v-for="item in stats" :key="item.label">
              <strong>{{ item.value }}</strong>
              <span>{{ item.label }}</span>
            </div>
          </div>
        </div>
      </section>

      <section class="section">
        <div class="container">
          <div class="section-head">
            <div class="section-head__text">
              <span class="eyebrow">OUR VALUES</span>
              <h2>我们坚持的四件事</h2>
            </div>
          </div>
          <div class="grid grid--4">
            <div v-for="item in values" :key="item.title" class="value-card card">
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
            </div>
          </div>
        </div>
      </section>

      <section class="section section--soft">
        <div class="container">
          <div class="section-head">
            <div class="section-head__text">
              <span class="eyebrow">MILESTONES</span>
              <h2>一路走来</h2>
            </div>
          </div>
          <ol class="timeline-list">
            <li v-for="item in timeline" :key="item.year">
              <span class="timeline-list__year">{{ item.year }}</span>
              <div>
                <h3>{{ item.title }}</h3>
                <p>{{ item.desc }}</p>
              </div>
            </li>
          </ol>
        </div>
      </section>

      <section class="section">
        <div class="container">
          <div class="static-cta">
            <div>
              <h2>想了解我们的服务能力？</h2>
              <p>获取线路价格、时效承诺与资质文件，或预约到仓参观。</p>
            </div>
            <div class="static-cta__actions">
              <RouterLink class="btn btn--primary" to="/quote">在线询价</RouterLink>
              <RouterLink class="btn btn--outline" to="/contact">预约参观</RouterLink>
            </div>
          </div>
        </div>
      </section>
    </template>

    <!-- Contact -->
    <template v-else-if="page === 'contact'">
      <section class="section">
        <div class="container">
          <div class="contact-top">
            <a class="contact-card contact-card--primary" href="tel:4008801688">
              <span>7×24 小时客服热线</span>
              <strong>400-880-1688</strong>
              <small>询价 · 查件 · 投诉 · 理赔</small>
            </a>
            <div class="contact-card">
              <span>总部地址</span>
              <strong>上海市青浦区华新镇华隆路 1777 号</strong>
              <small>驰远物流园 · 来访请提前预约</small>
            </div>
            <a class="contact-card" href="mailto:service@chiyuan-logistics.com">
              <span>客服邮箱</span>
              <strong>service@chiyuan-logistics.com</strong>
              <small>我们将在 1 个工作日内回复</small>
            </a>
          </div>

          <div class="contact-layout">
            <div>
              <h2 class="block-title">按业务联系</h2>
              <div class="dept-list">
                <div v-for="dept in departments" :key="dept.name" class="dept card">
                  <div class="dept__head">
                    <h3>{{ dept.name }}</h3>
                    <span class="small muted">{{ dept.desc }}</span>
                  </div>
                  <div class="dept__contacts">
                    <a :href="`tel:${dept.phone.replace(/[^\d]/g, '')}`">☎ {{ dept.phone }}</a>
                    <a :href="`mailto:${dept.email}`">✉ {{ dept.email }}</a>
                  </div>
                </div>
              </div>
            </div>

            <aside class="contact-aside">
              <div class="contact-hours card">
                <h3>服务时间</h3>
                <dl>
                  <div><dt>客服热线</dt><dd>7×24 小时</dd></div>
                  <div><dt>网点营业</dt><dd>周一至周日 07:00-22:00</dd></div>
                  <div><dt>提货预约</dt><dd>当日 22:00 前可约次日</dd></div>
                  <div><dt>商务洽谈</dt><dd>工作日 09:00-18:00</dd></div>
                </dl>
              </div>
              <div class="contact-address card">
                <h3>上海总部</h3>
                <p>上海市青浦区华新镇华隆路 1777 号驰远物流园 A 栋</p>
                <p class="small muted">自驾：G15 沈海高速华新出口 3 公里；地铁 17 号线嘉松中路站转公交 2 站。</p>
                <RouterLink class="text-link" to="/sites">查看全部网点 →</RouterLink>
              </div>
              <div class="contact-cta">
                <h3>需要报价？</h3>
                <p>在线填写货物信息，30 秒出明细。</p>
                <RouterLink class="btn btn--primary btn--block btn--sm" to="/quote">在线询价</RouterLink>
              </div>
            </aside>
          </div>
        </div>
      </section>
    </template>

    <!-- Join -->
    <template v-else-if="page === 'join'">
      <section class="section">
        <div class="container join-intro">
          <div>
            <span class="eyebrow">WHY CHIYUAN</span>
            <h2>把复杂留给系统，把简单留给客户</h2>
            <p>
              物流是一个需要耐心、细节与协同的行业。我们鼓励一线员工提出问题、改进流程，也用数字化工具减少重复劳动。这里没有复杂的层级，只有把事情做好的共同目标。
            </p>
            <p>
              无论你是司机、调度、销售还是产品技术，只要认同“准时、透明、负责”的价值观，我们都欢迎你。
            </p>
          </div>
          <div class="join-benefits">
            <h3>我们提供</h3>
            <ul>
              <li v-for="item in benefits" :key="item">{{ item }}</li>
            </ul>
          </div>
        </div>
      </section>

      <section class="section section--soft">
        <div class="container">
          <div class="section-head">
            <div class="section-head__text">
              <span class="eyebrow">OPEN POSITIONS</span>
              <h2>在招岗位</h2>
              <p>简历请发送至 <a class="text-link" href="mailto:hr@chiyuan-logistics.com">hr@chiyuan-logistics.com</a>，邮件标题请注明“姓名 + 应聘岗位”。</p>
            </div>
          </div>
          <div class="job-list">
            <div v-for="job in jobs" :key="job.title" class="job card">
              <div class="job__main">
                <h3>{{ job.title }}</h3>
                <div class="job__tags">
                  <span>{{ job.dept }}</span>
                  <span>{{ job.city }}</span>
                  <span>{{ job.exp }}</span>
                  <span>{{ job.type }}</span>
                </div>
              </div>
              <a class="btn btn--outline btn--sm" :href="`mailto:hr@chiyuan-logistics.com?subject=${encodeURIComponent('应聘：' + job.title)}`">投递简历</a>
            </div>
          </div>
        </div>
      </section>

      <section class="section">
        <div class="container">
          <div class="static-cta">
            <div>
              <h2>没有合适的岗位？</h2>
              <p>欢迎投递简历至人才库，有匹配岗位时我们会第一时间联系你。</p>
            </div>
            <a class="btn btn--primary" href="mailto:hr@chiyuan-logistics.com">投递人才库</a>
          </div>
        </div>
      </section>
    </template>

    <!-- Privacy / Terms -->
    <template v-else>
      <section class="section">
        <div class="container legal-layout">
          <article class="legal card">
            <p class="legal__updated">生效日期：2026 年 11 月 1 日 · 最近更新：2026 年 11 月 1 日</p>
            <section v-for="section in page === 'privacy' ? privacySections : termsSections" :key="section.title">
              <h2>{{ section.title }}</h2>
              <p v-for="paragraph in section.paragraphs" :key="paragraph">{{ paragraph }}</p>
            </section>
            <div class="legal__contact">
              <h2>联系我们</h2>
              <p>
                如对本政策或条款有任何疑问，请致电 <a href="tel:4008801688">400-880-1688</a>，或发送邮件至
                <a href="mailto:privacy@chiyuan-logistics.com">privacy@chiyuan-logistics.com</a>。
              </p>
            </div>
          </article>
          <aside class="legal-aside">
            <div class="card legal-aside__card">
              <h3>相关页面</h3>
              <RouterLink to="/privacy">隐私政策</RouterLink>
              <RouterLink to="/terms">服务条款</RouterLink>
              <RouterLink to="/about">关于我们</RouterLink>
              <RouterLink to="/contact">联系我们</RouterLink>
            </div>
            <div class="legal-aside__tip">
              <strong>需要帮助？</strong>
              <p>客服 7×24 小时在线</p>
              <a href="tel:4008801688">400-880-1688</a>
            </div>
          </aside>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
/* About */
.about-intro {
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 56px;
  align-items: center;
}

.about-intro__text h2 {
  font-size: clamp(27px, 3vw, 36px);
  margin: 10px 0 16px;
  letter-spacing: -0.02em;
}

.about-intro__text p {
  font-size: 14.5px;
  line-height: 1.9;
  color: var(--muted);
  margin-bottom: 14px;
}

.about-actions {
  display: flex;
  gap: 10px;
  margin-top: 22px;
}

.about-visual {
  display: grid;
  place-items: center;
}

.about-visual__card {
  width: min(360px, 100%);
  padding: 22px;
  border-radius: 18px;
  color: #fff;
  background: radial-gradient(300px 220px at 80% 10%, rgba(64, 150, 255, 0.5), transparent 60%),
    linear-gradient(145deg, #071a33, #0b3d84);
  box-shadow: var(--shadow-lg);
}

.about-visual__card > span {
  font-size: 11px;
  letter-spacing: 0.16em;
  color: rgba(255, 255, 255, 0.55);
}

.about-visual__map {
  position: relative;
  height: 200px;
  margin: 14px 0;
  border-radius: 10px;
  background-image: linear-gradient(rgba(255, 255, 255, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.06) 1px, transparent 1px);
  background-size: 28px 28px;
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.about-visual__map i {
  position: absolute;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #69a9ff;
  border: 3px solid #fff;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 0 6px rgba(105, 169, 255, 0.2);
}

.about-visual__foot {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.about-visual__foot strong {
  font-size: 30px;
  color: #8dc0ff;
}

.about-visual__foot span {
  font-size: 11.5px;
  color: rgba(255, 255, 255, 0.6);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  text-align: center;
}

.stats-grid strong {
  display: block;
  font-size: clamp(28px, 3.4vw, 40px);
  color: var(--primary);
  letter-spacing: -0.03em;
}

.stats-grid span {
  font-size: 12px;
  color: var(--muted);
}

.value-card {
  padding: 24px;
}

.value-card h3 {
  font-size: 16px;
  margin-bottom: 8px;
}

.value-card p {
  font-size: 13px;
  color: var(--muted);
  line-height: 1.75;
}

.timeline-list {
  list-style: none;
  display: grid;
  gap: 0;
  border-left: 2px solid #dbe8f8;
  margin-left: 8px;
  padding-left: 26px;
}

.timeline-list li {
  position: relative;
  padding-bottom: 24px;
}

.timeline-list li::before {
  content: '';
  position: absolute;
  left: -33px;
  top: 5px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #fff;
  border: 3px solid var(--primary);
}

.timeline-list__year {
  font-size: 12px;
  font-weight: 700;
  color: var(--primary);
  letter-spacing: 0.08em;
}

.timeline-list h3 {
  font-size: 16px;
  margin: 3px 0 3px;
}

.timeline-list p {
  font-size: 13px;
  color: var(--muted);
}

.static-cta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 32px 36px;
  border-radius: var(--radius);
  background: linear-gradient(120deg, #0a2b5e, #1557c0);
  color: rgba(255, 255, 255, 0.66);
}

.static-cta h2 {
  color: #fff;
  font-size: 22px;
  margin-bottom: 5px;
}

.static-cta p {
  font-size: 13px;
}

.static-cta__actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.static-cta .btn--outline {
  background: transparent;
  border-color: rgba(255, 255, 255, 0.28);
  color: #fff;
}

/* Contact */
.contact-top {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.contact-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 22px;
  border-radius: var(--radius);
  border: 1px solid var(--line);
  background: #fff;
  transition: border-color 0.16s ease, box-shadow 0.16s ease;
}

.contact-card:hover {
  border-color: #bcd7f8;
  box-shadow: var(--shadow-sm);
}

.contact-card--primary {
  background: linear-gradient(145deg, #0a2b5e, #1557c0);
  border-color: transparent;
  color: rgba(255, 255, 255, 0.65);
}

.contact-card span {
  font-size: 11px;
  color: var(--muted-light);
}

.contact-card--primary span {
  color: rgba(255, 255, 255, 0.6);
}

.contact-card strong {
  font-size: 16px;
  color: var(--ink);
  line-height: 1.4;
}

.contact-card--primary strong {
  font-size: 28px;
  color: #fff;
  letter-spacing: 0.02em;
}

.contact-card small {
  font-size: 11.5px;
  color: var(--muted-light);
}

.contact-card--primary small {
  color: rgba(255, 255, 255, 0.5);
}

.contact-layout {
  display: grid;
  grid-template-columns: 1fr 330px;
  gap: 28px;
  margin-top: 40px;
}

.block-title {
  font-size: 20px;
  margin-bottom: 16px;
}

.dept-list {
  display: grid;
  gap: 12px;
}

.dept {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 18px 20px;
  flex-wrap: wrap;
}

.dept__head h3 {
  font-size: 15px;
  margin-bottom: 2px;
}

.dept__contacts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  font-size: 12.5px;
}

.dept__contacts a {
  color: var(--primary);
  font-weight: 600;
}

.contact-aside {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.contact-hours,
.contact-address {
  padding: 20px;
}

.contact-hours h3,
.contact-address h3 {
  font-size: 15px;
  margin-bottom: 10px;
}

.contact-hours dl {
  display: grid;
  gap: 8px;
  margin: 0;
}

.contact-hours dl div {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 12px;
  padding-bottom: 8px;
  border-bottom: 1px dashed var(--line);
}

.contact-hours dt {
  color: var(--muted-light);
}

.contact-hours dd {
  margin: 0;
  color: #32465e;
  text-align: right;
}

.contact-address p {
  font-size: 13px;
  color: #32465e;
  line-height: 1.7;
  margin-bottom: 8px;
}

.contact-address .text-link {
  font-size: 12.5px;
}

.contact-cta {
  padding: 20px;
  border-radius: var(--radius);
  background: #071a33;
  color: rgba(255, 255, 255, 0.6);
}

.contact-cta h3 {
  color: #fff;
  font-size: 15px;
  margin-bottom: 3px;
}

.contact-cta p {
  font-size: 12px;
  margin-bottom: 12px;
}

/* Join */
.join-intro {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 56px;
  align-items: start;
}

.join-intro h2 {
  font-size: clamp(27px, 3vw, 36px);
  margin: 10px 0 16px;
}

.join-intro > div > p {
  font-size: 14.5px;
  line-height: 1.9;
  color: var(--muted);
  margin-bottom: 14px;
}

.join-benefits {
  padding: 24px;
  border-radius: var(--radius);
  background: var(--soft);
  border: 1px solid var(--line);
}

.join-benefits h3 {
  font-size: 15px;
  margin-bottom: 12px;
}

.join-benefits ul {
  list-style: none;
  display: grid;
  gap: 9px;
}

.join-benefits li {
  position: relative;
  padding-left: 18px;
  font-size: 13px;
  color: #32465e;
}

.join-benefits li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: var(--green);
  font-weight: 700;
  font-size: 11px;
}

.job-list {
  display: grid;
  gap: 12px;
}

.job {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 20px 22px;
  flex-wrap: wrap;
}

.job__main h3 {
  font-size: 16px;
  margin-bottom: 6px;
}

.job__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.job__tags span {
  font-size: 11px;
  color: var(--muted);
  background: var(--soft);
  padding: 2px 8px;
  border-radius: 4px;
}

/* Legal */
.legal-layout {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 26px;
  align-items: start;
}

.legal {
  padding: 38px 42px;
}

.legal__updated {
  font-size: 12px;
  color: var(--muted-light);
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line);
  margin-bottom: 10px;
}

.legal section {
  padding-top: 18px;
}

.legal h2 {
  font-size: 18px;
  margin-bottom: 10px;
  color: var(--ink);
}

.legal section p {
  font-size: 14px;
  line-height: 1.9;
  color: #33465d;
  margin-bottom: 12px;
}

.legal__contact {
  margin-top: 26px;
  padding: 20px 22px;
  border-radius: 10px;
  background: var(--soft);
}

.legal__contact p {
  font-size: 13.5px;
  line-height: 1.8;
}

.legal a {
  color: var(--primary);
  font-weight: 600;
}

.legal-aside {
  position: sticky;
  top: 110px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.legal-aside__card {
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.legal-aside__card h3 {
  font-size: 14px;
  margin-bottom: 10px;
}

.legal-aside__card a {
  font-size: 13px;
  color: var(--muted);
  padding: 9px 0;
  border-top: 1px solid var(--line);
}

.legal-aside__card a:hover {
  color: var(--primary);
}

.legal-aside__tip {
  padding: 20px;
  border-radius: var(--radius);
  background: #071a33;
  color: rgba(255, 255, 255, 0.6);
}

.legal-aside__tip strong {
  color: #fff;
  font-size: 14px;
}

.legal-aside__tip p {
  font-size: 11.5px;
  margin: 3px 0 6px;
}

.legal-aside__tip a {
  color: #8dc0ff;
  font-size: 19px;
  font-weight: 700;
}

@media (max-width: 980px) {
  .about-intro,
  .join-intro,
  .contact-layout,
  .legal-layout {
    grid-template-columns: 1fr;
    gap: 32px;
  }

  .contact-top {
    grid-template-columns: 1fr 1fr;
  }

  .contact-top .contact-card--primary {
    grid-column: 1 / -1;
  }

  .legal-aside {
    position: static;
    display: grid;
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 760px) {
  .stats-grid {
    grid-template-columns: 1fr 1fr;
    gap: 22px;
  }

  .contact-top {
    grid-template-columns: 1fr;
  }

  .static-cta {
    flex-direction: column;
    align-items: flex-start;
    padding: 26px 22px;
  }

  .static-cta__actions {
    width: 100%;
  }

  .static-cta__actions .btn {
    flex: 1;
  }

  .legal {
    padding: 26px 22px;
  }

  .legal-aside {
    grid-template-columns: 1fr;
  }
}
</style>
