<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import TrackTimeline from '../components/TrackTimeline.vue'
import { queryTrack } from '../api/public'
import type { TrackResult } from '../types'

const route = useRoute()

const mode = ref<'waybill' | 'phone'>('waybill')
const form = reactive({ waybillNo: '', phone: '' })
const error = ref('')
const loading = ref(false)
const track = ref<TrackResult | null>(null)
const copied = ref(false)
const searched = ref(false)

async function search(scroll = true) {
  error.value = ''
  const waybillNo = form.waybillNo.trim()
  const phone = form.phone.replace(/\D/g, '')
  if (mode.value === 'waybill' && !waybillNo) {
    error.value = '请输入运单号'
    return
  }
  if (mode.value === 'phone' && phone.length < 4) {
    error.value = '请输入手机号后四位'
    return
  }
  loading.value = true
  searched.value = true
  try {
    track.value = await queryTrack(
      mode.value === 'waybill' ? waybillNo : '',
      mode.value === 'phone' ? phone.slice(-4) : form.phone.replace(/\D/g, '')
    )
    if (scroll) {
      window.setTimeout(() => {
        document.getElementById('track-result')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
      }, 60)
    }
  } catch {
    error.value = '查询失败，请稍后重试或联系客服 400-880-1688'
    track.value = null
  } finally {
    loading.value = false
  }
}

async function copyNo() {
  if (!track.value) return
  try {
    await navigator.clipboard.writeText(track.value.waybillNo)
    copied.value = true
    window.setTimeout(() => {
      copied.value = false
    }, 1800)
  } catch {
    copied.value = false
  }
}

onMounted(() => {
  const waybillNo = typeof route.query.waybillNo === 'string' ? route.query.waybillNo : ''
  const phone = typeof route.query.phone === 'string' ? route.query.phone : ''
  form.waybillNo = waybillNo
  form.phone = phone
  if (waybillNo) {
    mode.value = 'waybill'
    void search(false)
  } else if (phone.length >= 4) {
    mode.value = 'phone'
    void search(false)
  }
})
</script>

<template>
  <div class="track-page">
    <section class="page-hero">
      <div class="container page-hero__inner">
        <span class="eyebrow">TRACK &amp; TRACE</span>
        <h1>运单到哪了，随时查得到</h1>
        <p>输入运单号，或使用收发货人手机号后四位查询。全程节点、司机车辆与预计到达时间一屏掌握。</p>
      </div>
    </section>

    <section class="section section--tight track-main">
      <div class="container">
        <div class="card track-search">
          <div class="track-search__head">
            <div class="track-search__tabs" role="tablist" aria-label="查询方式">
              <button
                type="button"
                role="tab"
                :aria-selected="mode === 'waybill'"
                :class="{ 'is-active': mode === 'waybill' }"
                @click="mode = 'waybill'; error = ''"
              >
                按运单号查询
              </button>
              <button
                type="button"
                role="tab"
                :aria-selected="mode === 'phone'"
                :class="{ 'is-active': mode === 'phone' }"
                @click="mode = 'phone'; error = ''"
              >
                按手机后四位
              </button>
            </div>
            <span class="small muted">查询结果仅供收发货人参考</span>
          </div>

          <form class="track-search__form" novalidate @submit.prevent="search()">
            <template v-if="mode === 'waybill'">
              <div class="track-search__field">
                <label for="waybill-no" class="sr-only">运单号</label>
                <input
                  id="waybill-no"
                  v-model="form.waybillNo"
                  class="input"
                  type="text"
                  autocomplete="off"
                  placeholder="请输入运单号，如 YH0001202611140001"
                  :aria-invalid="Boolean(error)"
                />
              </div>
              <div class="track-search__field track-search__field--phone">
                <label for="phone-code" class="sr-only">手机号后四位（选填）</label>
                <input id="phone-code" v-model="form.phone" class="input" type="text" inputmode="numeric" maxlength="11" placeholder="手机后四位（选填）" />
              </div>
            </template>
            <template v-else>
              <div class="track-search__field">
                <label for="phone-only" class="sr-only">手机号后四位</label>
                <input
                  id="phone-only"
                  v-model="form.phone"
                  class="input"
                  type="text"
                  inputmode="numeric"
                  maxlength="11"
                  placeholder="请输入收/发货人手机号后四位"
                  :aria-invalid="Boolean(error)"
                />
              </div>
            </template>
            <button class="btn btn--primary" type="submit" :disabled="loading">
              {{ loading ? '查询中…' : '查询轨迹' }}
            </button>
          </form>

          <p v-if="error" class="form-error track-search__error" role="alert">{{ error }}</p>
          <p v-else class="track-search__hint">
            运单号可在托运单、短信通知或小程序「我的运单」中查看；如忘记运单号，可联系客服协助查询。
          </p>
        </div>

        <div id="track-result" class="track-result">
          <div v-if="loading" class="card track-loading" aria-live="polite" aria-label="轨迹加载中">
            <div class="skeleton track-loading__summary"></div>
            <div class="track-loading__lines">
              <div class="skeleton"></div>
              <div class="skeleton"></div>
              <div class="skeleton"></div>
            </div>
          </div>

          <template v-else-if="track">
            <div class="track-result__bar">
              <span class="pill">查询成功</span>
              <div class="track-result__actions">
                <button class="btn btn--outline btn--sm" type="button" @click="copyNo">
                  {{ copied ? '已复制' : '复制运单号' }}
                </button>
                <a class="btn btn--outline btn--sm" href="tel:4008801688">联系客服</a>
              </div>
            </div>
            <TrackTimeline :track="track" />
          </template>

          <div v-else-if="searched" class="state-block">
            <div class="state-block__icon" aria-hidden="true">?</div>
            <h3>未查询到该运单</h3>
            <p>请核对运单号或手机号后四位。若刚下单，系统可能需要几分钟同步；仍有问题请联系客服 400-880-1688。</p>
            <button class="btn btn--outline btn--sm" type="button" @click="search(false)">重新查询</button>
          </div>

          <div v-else class="track-guide">
            <div class="track-guide__item">
              <span>01</span>
              <h3>找到运单号</h3>
              <p>托运单右上角、受理短信或小程序「我的运单」均可查看。</p>
            </div>
            <div class="track-guide__item">
              <span>02</span>
              <h3>输入并查询</h3>
              <p>支持运单号精确查询，也可用收发货人手机号后四位。</p>
            </div>
            <div class="track-guide__item">
              <span>03</span>
              <h3>查看全程节点</h3>
              <p>已受理、已提货、运输中、已签收，异常会同步提示。</p>
            </div>
          </div>
        </div>

        <div class="track-faq">
          <div>
            <h2>查询不到轨迹？</h2>
            <p>
              新运单通常在受理后 10 分钟内可查。若长时间无轨迹，可能是分拨扫描延迟或运单号输入有误，请致电
              <a href="tel:4008801688">400-880-1688</a>，客服可凭手机号为您核实。
            </p>
          </div>
          <RouterLink class="btn btn--outline btn--sm" to="/contact">联系在线客服</RouterLink>
        </div>
      </div>
    </section>
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

.track-main {
  background: var(--soft);
  min-height: 560px;
}

.track-search {
  padding: 24px 26px;
}

.track-search__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.track-search__tabs {
  display: inline-flex;
  padding: 4px;
  border-radius: 10px;
  background: #f1f5fb;
  gap: 3px;
}

.track-search__tabs button {
  border: 0;
  background: transparent;
  padding: 8px 18px;
  min-height: 38px;
  border-radius: 8px;
  font-size: 13.5px;
  color: var(--muted);
  font-weight: 600;
}

.track-search__tabs button.is-active {
  background: #fff;
  color: var(--primary);
  box-shadow: 0 2px 8px rgba(15, 39, 71, 0.08);
}

.track-search__form {
  display: flex;
  gap: 10px;
}

.track-search__field {
  flex: 1;
}

.track-search__field--phone {
  flex: 0 0 200px;
}

.track-search__form .input {
  min-height: 50px;
  font-size: 15px;
}

.track-search__form .btn {
  min-height: 50px;
  padding: 0 30px;
  flex-shrink: 0;
}

.track-search__error {
  margin-top: 10px;
}

.track-search__hint {
  margin-top: 10px;
  font-size: 12px;
  color: var(--muted-light);
}

.track-result {
  margin-top: 24px;
  scroll-margin-top: 130px;
}

.track-result__bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.track-result__actions {
  display: flex;
  gap: 8px;
}

.track-loading {
  padding: 22px;
}

.track-loading__summary {
  height: 210px;
}

.track-loading__lines {
  display: grid;
  gap: 14px;
  margin-top: 20px;
}

.track-loading__lines .skeleton {
  height: 50px;
}

.track-guide {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}

.track-guide__item {
  padding: 26px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: #fff;
}

.track-guide__item span {
  font-size: 26px;
  font-weight: 800;
  color: #d6e6fb;
  letter-spacing: -0.02em;
}

.track-guide__item h3 {
  font-size: 16px;
  margin: 10px 0 6px;
}

.track-guide__item p {
  font-size: 13px;
  color: var(--muted);
  line-height: 1.7;
}

.track-faq {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-top: 30px;
  padding-top: 26px;
  border-top: 1px solid var(--line);
}

.track-faq h2 {
  font-size: 16px;
  margin-bottom: 6px;
}

.track-faq p {
  font-size: 13px;
  color: var(--muted);
  max-width: 760px;
  line-height: 1.75;
}

.track-faq a {
  color: var(--primary);
  font-weight: 600;
}

.track-faq .btn {
  flex-shrink: 0;
}

@media (max-width: 760px) {
  .track-search {
    padding: 18px;
  }

  .track-search__form {
    flex-direction: column;
  }

  .track-search__field--phone {
    flex: 1;
  }

  .track-search__form .btn {
    width: 100%;
  }

  .track-guide {
    grid-template-columns: 1fr;
  }

  .track-faq {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
