<script setup lang="ts">
import { reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { demoMode } from '../api/client'
import { applyMonthly } from '../api/public'
import type { MonthlyPayload } from '../types'

const form = reactive<MonthlyPayload>({
  company: '',
  contact: '',
  phone: '',
  monthlyVolume: '5000-20000 元',
  routes: '',
  settleCycle: '月结 30 天',
  remark: ''
})

const errors = reactive<Record<string, string>>({})
const submitting = ref(false)
const success = ref(false)

const benefits = [
  { title: '专属协议价', desc: '按线路与货量核定，较标准价再降 5%~12%，价格锁定 12 个月。' },
  { title: '灵活账期', desc: '标准月结 30 天，优质客户最长 45 天，缓解现金流压力。' },
  { title: '月度对账', desc: '每月 3 日前生成对账单，在线确认与异议申诉，明细可导出。' },
  { title: '在线开票', desc: '对账确认后一键申请增值税专票/普票，电子发票 2 个工作日送达。' }
]

const steps = [
  { no: '01', title: '提交申请', desc: '填写公司与联系人信息，1 分钟完成。' },
  { no: '02', title: '资质审核', desc: '客户经理 1 个工作日内联系，核验营业执照等资料。' },
  { no: '03', title: '签订协议', desc: '确认线路、价格、账期与结算方式，签署运输服务协议。' },
  { no: '04', title: '开通月结', desc: '账号开通，下单即享协议价，次月起按月对账。' }
]

function validate() {
  Object.keys(errors).forEach((key) => delete errors[key])
  if (!form.company.trim()) errors.company = '请填写公司全称'
  if (!form.contact.trim()) errors.contact = '请填写联系人姓名'
  const phoneOk = /^(1[3-9]\d{9}|0\d{2,3}-?\d{7,8})$/.test(form.phone.trim())
  if (!form.phone.trim()) errors.phone = '请填写联系电话'
  else if (!phoneOk) errors.phone = '请填写有效的手机号或座机号'
  if (!form.monthlyVolume) errors.monthlyVolume = '请选择月均运费'
  if (!form.routes.trim()) errors.routes = '请填写常发线路'
  return Object.keys(errors).length === 0
}

async function submit() {
  if (!validate()) return
  submitting.value = true
  try {
    await applyMonthly({ ...form })
    success.value = true
  } finally {
    submitting.value = false
  }
}

function again() {
  success.value = false
  form.company = ''
  form.contact = ''
  form.phone = ''
  form.routes = ''
  form.remark = ''
}
</script>

<template>
  <div class="monthly-page">
    <section class="page-hero">
      <div class="container page-hero__inner">
        <span class="eyebrow">ENTERPRISE ACCOUNT</span>
        <h1>企业月结，让运费管理更简单</h1>
        <p>
          专属协议价、最长 45 天账期、月度对账与在线开票。已有 300+ 制造、商贸与电商企业选择驰远月结服务。
        </p>
      </div>
    </section>

    <section class="section section--tight monthly-main">
      <div class="container monthly-layout">
        <div class="monthly-info">
          <div class="benefit-grid">
            <div v-for="item in benefits" :key="item.title" class="benefit card">
              <span class="benefit__check" aria-hidden="true">✓</span>
              <h2>{{ item.title }}</h2>
              <p>{{ item.desc }}</p>
            </div>
          </div>

          <div class="monthly-flow card">
            <h2>申请流程</h2>
            <ol>
              <li v-for="step in steps" :key="step.no">
                <span>{{ step.no }}</span>
                <div>
                  <strong>{{ step.title }}</strong>
                  <p>{{ step.desc }}</p>
                </div>
              </li>
            </ol>
          </div>

          <div class="monthly-docs">
            <h2>需要准备的材料</h2>
            <ul>
              <li>营业执照复印件（加盖公章）</li>
              <li>法定代表人身份证明或授权委托书</li>
              <li>开票信息（名称、税号、地址电话、开户行账号）</li>
              <li>常用线路与预估月发货量说明</li>
            </ul>
            <p class="small muted">线上提交后，客户经理会发送资料清单与协议模板，无需现场办理。</p>
          </div>

          <div class="monthly-quote">
            <div>
              <strong>已有明确线路和货量？</strong>
              <p>直接询价并备注“申请月结”，我们会一并给出协议价方案。</p>
            </div>
            <RouterLink class="btn btn--outline btn--sm" to="/quote">先算一票价格</RouterLink>
          </div>
        </div>

        <div class="monthly-form-wrap">
          <div class="card monthly-form">
            <template v-if="success">
              <div class="monthly-success">
                <div class="monthly-success__icon" aria-hidden="true">✓</div>
                <h2>申请已提交</h2>
                <p>
                  感谢您的信任！客户经理将在 1 个工作日内与您联系，核验资料并沟通协议价与账期方案。
                </p>
                <p v-if="demoMode" class="notice notice--warn">
                  当前为演示环境，申请数据未实际提交至后端。接口恢复后可正常受理。
                </p>
                <div class="monthly-success__actions">
                  <a class="btn btn--primary btn--block" href="tel:4008801688">加急咨询 400-880-1688</a>
                  <button class="btn btn--outline btn--block" type="button" @click="again">再提交一份申请</button>
                </div>
              </div>
            </template>

            <template v-else>
              <div class="monthly-form__head">
                <span class="eyebrow">APPLY NOW</span>
                <h2>提交月结申请</h2>
                <p>填写信息后，客户经理将在 1 个工作日内联系您。</p>
              </div>

              <form novalidate @submit.prevent="submit">
                <div class="form-grid">
                  <div class="field field--full">
                    <label for="company">公司全称 <em>*</em></label>
                    <input id="company" v-model="form.company" class="input" placeholder="请填写营业执照上的公司名称" :aria-invalid="Boolean(errors.company)" />
                    <span v-if="errors.company" class="form-error">{{ errors.company }}</span>
                  </div>
                  <div class="field">
                    <label for="contact">联系人 <em>*</em></label>
                    <input id="contact" v-model="form.contact" class="input" placeholder="您的姓名" :aria-invalid="Boolean(errors.contact)" />
                    <span v-if="errors.contact" class="form-error">{{ errors.contact }}</span>
                  </div>
                  <div class="field">
                    <label for="monthly-phone">联系电话 <em>*</em></label>
                    <input id="monthly-phone" v-model="form.phone" class="input" type="tel" inputmode="tel" placeholder="手机或座机号" :aria-invalid="Boolean(errors.phone)" />
                    <span v-if="errors.phone" class="form-error">{{ errors.phone }}</span>
                  </div>
                  <div class="field">
                    <label for="volume">月均运费 <em>*</em></label>
                    <select id="volume" v-model="form.monthlyVolume" class="select">
                      <option>5000-20000 元</option>
                      <option>20000-50000 元</option>
                      <option>50000-100000 元</option>
                      <option>100000 元以上</option>
                      <option>暂不确定</option>
                    </select>
                  </div>
                  <div class="field">
                    <label for="cycle">期望账期</label>
                    <select id="cycle" v-model="form.settleCycle" class="select">
                      <option>月结 30 天</option>
                      <option>月结 45 天</option>
                      <option>半月结</option>
                      <option>按合同约定</option>
                    </select>
                  </div>
                  <div class="field field--full">
                    <label for="routes">常发线路 <em>*</em></label>
                    <input id="routes" v-model="form.routes" class="input" placeholder="如：上海 → 杭州，每周 3 车" :aria-invalid="Boolean(errors.routes)" />
                    <span v-if="errors.routes" class="form-error">{{ errors.routes }}</span>
                  </div>
                  <div class="field field--full">
                    <label for="monthly-remark">其他需求（选填）</label>
                    <textarea id="monthly-remark" v-model="form.remark" class="textarea" placeholder="如货物类型、时效要求、开票类型、是否需要仓配等"></textarea>
                  </div>
                </div>

                <button class="btn btn--primary btn--block monthly-submit" type="submit" :disabled="submitting">
                  {{ submitting ? '正在提交…' : '提交月结申请' }}
                </button>
                <p class="form-help monthly-privacy">
                  提交即表示同意《隐私政策》。我们仅将信息用于月结审核与业务联系。
                </p>
              </form>
            </template>
          </div>

          <div class="monthly-contact">
            <div>
              <strong>想先聊聊？</strong>
              <p>工作日 09:00-18:00，客户经理在线。</p>
            </div>
            <a class="btn btn--outline btn--sm" href="tel:4008801688">400-880-1688</a>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.monthly-main {
  background: var(--soft);
}

.monthly-layout {
  display: grid;
  grid-template-columns: 1fr 0.86fr;
  gap: 28px;
  align-items: start;
}

.monthly-info {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.benefit-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.benefit {
  padding: 20px;
}

.benefit__check {
  display: grid;
  place-items: center;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: #eaf8f0;
  color: var(--green);
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 10px;
}

.benefit h2 {
  font-size: 15px;
  margin-bottom: 5px;
}

.benefit p {
  font-size: 12.5px;
  color: var(--muted);
  line-height: 1.7;
}

.monthly-flow {
  padding: 24px;
}

.monthly-flow h2 {
  font-size: 17px;
  margin-bottom: 16px;
}

.monthly-flow ol {
  list-style: none;
  display: grid;
  gap: 16px;
}

.monthly-flow li {
  display: flex;
  gap: 12px;
}

.monthly-flow li > span {
  flex: 0 0 28px;
  height: 28px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: #eef5ff;
  color: var(--primary);
  font-size: 10px;
  font-weight: 800;
}

.monthly-flow strong {
  font-size: 13.5px;
}

.monthly-flow p {
  font-size: 12px;
  color: var(--muted);
  margin-top: 2px;
}

.monthly-docs {
  padding: 4px 2px;
}

.monthly-docs h2 {
  font-size: 16px;
  margin-bottom: 10px;
}

.monthly-docs ul {
  padding-left: 20px;
  display: grid;
  gap: 5px;
  font-size: 13px;
  color: #32465e;
  margin-bottom: 8px;
}

.monthly-quote {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid var(--line);
}

.monthly-quote strong {
  font-size: 13.5px;
}

.monthly-quote p {
  font-size: 12px;
  color: var(--muted);
  margin-top: 2px;
}

.monthly-quote .btn {
  flex-shrink: 0;
}

.monthly-form-wrap {
  position: sticky;
  top: 110px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.monthly-form {
  padding: 28px;
}

.monthly-form__head {
  margin-bottom: 20px;
}

.monthly-form__head h2 {
  font-size: 22px;
  margin: 8px 0 5px;
}

.monthly-form__head p {
  font-size: 13px;
  color: var(--muted);
}

.monthly-form__head em,
.monthly-form label em {
  color: var(--red);
  font-style: normal;
}

.monthly-submit {
  margin-top: 20px;
}

.monthly-privacy {
  margin-top: 10px;
  text-align: center;
}

.monthly-success {
  text-align: center;
  padding: 16px 0;
}

.monthly-success__icon {
  width: 54px;
  height: 54px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  margin: 0 auto 16px;
  background: #eaf8f0;
  color: var(--green);
  font-size: 24px;
  font-weight: 700;
}

.monthly-success h2 {
  font-size: 22px;
  margin-bottom: 10px;
}

.monthly-success > p {
  font-size: 13.5px;
  color: var(--muted);
  line-height: 1.8;
  margin-bottom: 14px;
}

.monthly-success .notice {
  text-align: left;
  margin-bottom: 18px;
}

.monthly-success__actions {
  display: grid;
  gap: 10px;
}

.monthly-contact {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 16px 18px;
  border-radius: 12px;
  background: #071a33;
  color: rgba(255, 255, 255, 0.62);
}

.monthly-contact strong {
  color: #fff;
  font-size: 13.5px;
}

.monthly-contact p {
  font-size: 11.5px;
  margin-top: 2px;
}

.monthly-contact .btn {
  background: transparent;
  border-color: rgba(255, 255, 255, 0.26);
  color: #fff;
  flex-shrink: 0;
}

@media (max-width: 1000px) {
  .monthly-layout {
    grid-template-columns: 1fr;
  }

  .monthly-form-wrap {
    position: static;
  }
}

@media (max-width: 620px) {
  .benefit-grid {
    grid-template-columns: 1fr;
  }

  .monthly-form {
    padding: 22px 18px;
  }

  .monthly-quote {
    flex-direction: column;
    align-items: flex-start;
  }

  .monthly-quote .btn {
    width: 100%;
  }
}
</style>
