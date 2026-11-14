<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import {
  clearDriverToken,
  driverLogin,
  getDriverTasks,
  getDriverToken,
  logoutDriver,
  reportTaskNode,
  uploadTaskReceipt
} from '../api/h5'
import { demoMode } from '../api/client'
import { NODE_OPTIONS } from '../demo/data'
import type { DriverTask, DriverUser } from '../types'
import { formatDateTime } from '../utils/format'
import { showToast } from '../composables/useToast'

const USER_KEY = 'chiyuan_h5_driver_user'

function readUser(): DriverUser | null {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? (JSON.parse(raw) as DriverUser) : null
  } catch {
    return null
  }
}

const token = ref(getDriverToken())
const user = ref<DriverUser | null>(readUser())
const loggedIn = computed(() => Boolean(token.value && user.value))

const loginForm = reactive({ phone: '', code: '' })
const loginErrors = reactive<Record<string, string>>({})
const loginLoading = ref(false)
const countdown = ref(0)
let countdownTimer: number | undefined

const tasks = ref<DriverTask[]>([])
const tasksLoading = ref(false)
const tasksError = ref('')
const activeFilter = ref('全部')
const uploadedIds = ref<number[]>([])

const filters = ['全部', '待提货', '运输中', '待回单']

const counts = computed(() => {
  const map: Record<string, number> = { 全部: tasks.value.length }
  for (const filter of filters.slice(1)) {
    map[filter] = tasks.value.filter((task) => task.statusName === filter || task.status === filter).length
  }
  return map
})

const filteredTasks = computed(() =>
  activeFilter.value === '全部'
    ? tasks.value
    : tasks.value.filter(
        (task) => task.statusName === activeFilter.value || task.status === activeFilter.value
      )
)

function statusClass(status: string): string {
  if (status.includes('待提货')) return 'tag--orange'
  if (status.includes('运输') || status.includes('途')) return 'tag--blue'
  if (status.includes('待回单')) return 'tag--orange'
  if (status.includes('完成') || status.includes('签收')) return 'tag'
  return 'tag--gray'
}

async function sendCode() {
  if (!/^1[3-9]\d{9}$/.test(loginForm.phone)) {
    loginErrors.phone = '请先输入正确的手机号'
    return
  }
  delete loginErrors.phone
  countdown.value = 60
  showToast('验证码已发送（演示环境请输入 1234）', 'success', 2800)
  countdownTimer = window.setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0 && countdownTimer) {
      window.clearInterval(countdownTimer)
      countdownTimer = undefined
    }
  }, 1000)
}

function validateLogin() {
  Object.keys(loginErrors).forEach((key) => delete loginErrors[key])
  if (!/^1[3-9]\d{9}$/.test(loginForm.phone.trim())) loginErrors.phone = '请输入正确的 11 位手机号'
  if (!/^\d{4}$/.test(loginForm.code.trim())) loginErrors.code = '请输入 4 位验证码'
  return Object.keys(loginErrors).length === 0
}

async function handleLogin() {
  if (!validateLogin() || loginLoading.value) return
  loginLoading.value = true
  try {
    const result = await driverLogin(loginForm.phone.trim(), loginForm.code.trim())
    token.value = result.token
    user.value = result.user
    localStorage.setItem(USER_KEY, JSON.stringify(result.user))
    showToast('登录成功，欢迎回来', 'success')
    await loadTasks()
  } catch {
    showToast('登录失败，请稍后重试', 'error')
  } finally {
    loginLoading.value = false
  }
}

async function loadTasks() {
  tasksLoading.value = true
  tasksError.value = ''
  try {
    tasks.value = await getDriverTasks()
  } catch {
    tasksError.value = '任务加载失败，请检查网络后重试。'
    tasks.value = []
  } finally {
    tasksLoading.value = false
  }
}

function handleLogout() {
  if (!window.confirm('确定退出登录吗？')) return
  logoutDriver()
  clearDriverToken()
  token.value = ''
  user.value = null
  localStorage.removeItem(USER_KEY)
  tasks.value = []
  showToast('已退出登录')
}

/* ---------- Node report ---------- */
const reportTask = ref<DriverTask | null>(null)
const nodeForm = reactive({ node: '', remark: '' })
const nodeError = ref('')
const nodeSubmitting = ref(false)
const gps = reactive({
  lat: 0,
  lng: 0,
  label: '尚未定位',
  state: 'idle' as 'idle' | 'loading' | 'ok' | 'fallback'
})

const DEMO_COORDS = { lat: 31.2304, lng: 121.4737, label: '上海市青浦区华新镇（演示定位）' }

function resetGps() {
  gps.lat = 0
  gps.lng = 0
  gps.label = '尚未定位，提交时将使用演示坐标'
  gps.state = 'idle'
}

function locate() {
  if (gps.state === 'loading') return
  if (!navigator.geolocation) {
    gps.lat = DEMO_COORDS.lat
    gps.lng = DEMO_COORDS.lng
    gps.label = DEMO_COORDS.label
    gps.state = 'fallback'
    showToast('当前浏览器不支持定位，已使用演示坐标', 'error')
    return
  }
  gps.state = 'loading'
  navigator.geolocation.getCurrentPosition(
    (position) => {
      gps.lat = Number(position.coords.latitude.toFixed(6))
      gps.lng = Number(position.coords.longitude.toFixed(6))
      gps.label = '已获取当前位置'
      gps.state = 'ok'
    },
    () => {
      gps.lat = DEMO_COORDS.lat
      gps.lng = DEMO_COORDS.lng
      gps.label = DEMO_COORDS.label
      gps.state = 'fallback'
      showToast('定位失败，已使用演示坐标', 'error')
    },
    { timeout: 7000, enableHighAccuracy: true }
  )
}

function openReport(task: DriverTask) {
  reportTask.value = task
  nodeForm.remark = ''
  nodeForm.node =
    task.statusName === '待提货'
      ? '到达发货地'
      : task.statusName === '运输中'
        ? '到达中转场'
        : '已签收'
  nodeError.value = ''
  resetGps()
}

function closeReport() {
  if (nodeSubmitting.value) return
  reportTask.value = null
}

function updateTaskStatus(task: DriverTask, node: string) {
  if (node === '到达发货地') {
    task.status = '待提货'
    task.statusName = '待提货'
  } else if (node === '已装车' || node === '到达中转场' || node === '到达收货地') {
    task.status = '运输中'
    task.statusName = '运输中'
  } else if (node === '已签收') {
    task.status = '待回单'
    task.statusName = '待回单'
  }
  task.node = node
}

async function submitNode() {
  if (!reportTask.value || nodeSubmitting.value) return
  if (!nodeForm.node) {
    nodeError.value = '请选择要上报的节点'
    return
  }
  nodeSubmitting.value = true
  nodeError.value = ''
  try {
    const lat = gps.lat || DEMO_COORDS.lat
    const lng = gps.lng || DEMO_COORDS.lng
    await reportTaskNode(reportTask.value.id, {
      node: nodeForm.node,
      lat,
      lng,
      remark: nodeForm.remark.trim()
    })
    updateTaskStatus(reportTask.value, nodeForm.node)
    showToast(demoMode.value ? '节点已上报（演示环境）' : '节点上报成功', 'success')
    reportTask.value = null
  } catch {
    nodeError.value = '上报失败，请稍后重试'
    showToast('节点上报失败', 'error')
  } finally {
    nodeSubmitting.value = false
  }
}

/* ---------- Receipt upload ---------- */
const receiptTask = ref<DriverTask | null>(null)
const photos = ref<string[]>([])
const photoLoading = ref(false)
const receiptError = ref('')
const receiptSubmitting = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)

function openReceipt(task: DriverTask) {
  receiptTask.value = task
  photos.value = []
  receiptError.value = ''
}

function closeReceipt() {
  if (receiptSubmitting.value || photoLoading.value) return
  receiptTask.value = null
}

function pickFiles() {
  fileInput.value?.click()
}

function compressImage(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onerror = () => reject(new Error('读取图片失败'))
    reader.onload = () => {
      const image = new Image()
      image.onerror = () => reject(new Error('图片解码失败'))
      image.onload = () => {
        const max = 1280
        const ratio = Math.min(1, max / Math.max(image.width, image.height))
        const canvas = document.createElement('canvas')
        canvas.width = Math.round(image.width * ratio)
        canvas.height = Math.round(image.height * ratio)
        const context = canvas.getContext('2d')
        if (!context) {
          reject(new Error('图片处理失败'))
          return
        }
        context.drawImage(image, 0, 0, canvas.width, canvas.height)
        resolve(canvas.toDataURL('image/jpeg', 0.72))
      }
      image.src = String(reader.result)
    }
    reader.readAsDataURL(file)
  })
}

async function onFilesChange(event: Event) {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files || [])
  input.value = ''
  if (!files.length) return
  photoLoading.value = true
  try {
    for (const file of files) {
      if (photos.value.length >= 6) {
        showToast('最多上传 6 张照片', 'error')
        break
      }
      if (!file.type.startsWith('image/')) {
        showToast(`${file.name} 不是图片文件`, 'error')
        continue
      }
      if (file.size > 12 * 1024 * 1024) {
        showToast(`${file.name} 超过 12MB，已跳过`, 'error')
        continue
      }
      try {
        const dataUrl = await compressImage(file)
        photos.value.push(dataUrl)
      } catch {
        showToast(`${file.name} 处理失败`, 'error')
      }
    }
  } finally {
    photoLoading.value = false
  }
}

function removePhoto(index: number) {
  photos.value.splice(index, 1)
}

async function submitReceipt() {
  if (!receiptTask.value || receiptSubmitting.value) return
  if (!photos.value.length) {
    receiptError.value = '请至少上传 1 张回单照片'
    return
  }
  receiptSubmitting.value = true
  receiptError.value = ''
  try {
    await uploadTaskReceipt(receiptTask.value.id, photos.value)
    if (!uploadedIds.value.includes(receiptTask.value.id)) {
      uploadedIds.value.push(receiptTask.value.id)
    }
    receiptTask.value.node = '回单已上传'
    showToast(demoMode.value ? '回单已上传（演示环境）' : '回单上传成功', 'success')
    receiptTask.value = null
  } catch {
    receiptError.value = '上传失败，请保留照片后重试'
    showToast('回单上传失败', 'error')
  } finally {
    receiptSubmitting.value = false
  }
}

const showAppSheet = ref(false)

onMounted(() => {
  if (loggedIn.value) void loadTasks()
})

onBeforeUnmount(() => {
  if (countdownTimer) window.clearInterval(countdownTimer)
})
</script>

<template>
  <div class="page page--no-bar driver-page">
    <!-- Login -->
    <template v-if="!loggedIn">
      <section class="login-hero">
        <span class="login-hero__mark" aria-hidden="true"><i></i><i></i><i></i></span>
        <h1>驰远司机 · 免安装端</h1>
        <p>无需下载 App，登录后即可上报运输节点、上传回单照片。</p>
      </section>

      <div class="restricted-banner" role="note">
        <span class="restricted-banner__icon" aria-hidden="true">!</span>
        <div>
          <strong>功能受限</strong>
          <p>仅支持节点上报与回单上传，请下载 App 使用完整功能（导航、称重、电子签名、离线队列等）。</p>
          <button type="button" @click="showAppSheet = true">如何下载 App →</button>
        </div>
      </div>

      <section class="card login-card">
        <h2>手机号登录</h2>
        <p class="login-card__sub">使用与 App 一致的司机手机号登录</p>
        <form novalidate @submit.prevent="handleLogin">
          <div class="field">
            <label for="driver-phone">手机号</label>
            <input
              id="driver-phone"
              v-model="loginForm.phone"
              class="input"
              type="tel"
              inputmode="numeric"
              maxlength="11"
              placeholder="请输入 11 位手机号"
              :aria-invalid="Boolean(loginErrors.phone)"
            />
            <span v-if="loginErrors.phone" class="form-error">{{ loginErrors.phone }}</span>
          </div>
          <div class="field">
            <label for="driver-code">验证码</label>
            <div class="code-row">
              <input
                id="driver-code"
                v-model="loginForm.code"
                class="input"
                type="text"
                inputmode="numeric"
                maxlength="4"
                placeholder="4 位验证码"
                :aria-invalid="Boolean(loginErrors.code)"
              />
              <button class="btn btn--outline code-btn" type="button" :disabled="countdown > 0" @click="sendCode">
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </button>
            </div>
            <span v-if="loginErrors.code" class="form-error">{{ loginErrors.code }}</span>
          </div>
          <button class="btn btn--primary btn--block login-submit" type="submit" :disabled="loginLoading">
            <span v-if="loginLoading" class="spinner"></span>
            {{ loginLoading ? '正在登录…' : '登录并查看任务' }}
          </button>
        </form>
        <p class="login-card__demo">演示环境验证码：<strong>1234</strong>，任意手机号均可登录。</p>
        <p class="login-card__agree">登录即表示同意《司机服务协议》与《隐私政策》</p>
      </section>

      <div class="login-help">
        <h3>登录遇到问题？</h3>
        <p>请联系车队调度或客服热线 <a href="tel:4008801688">400-880-1688</a> 协助处理。</p>
      </div>
    </template>

    <!-- Tasks -->
    <template v-else>
      <section class="driver-head card">
        <span class="driver-avatar" aria-hidden="true">{{ user?.name?.slice(0, 1) || '司' }}</span>
        <div class="driver-head__info">
          <strong>{{ user?.name || '司机师傅' }}，辛苦了</strong>
          <p>{{ user?.phone }}<span v-if="user?.vehicleNo"> · {{ user.vehicleNo }}</span></p>
        </div>
        <button class="driver-head__logout" type="button" @click="handleLogout">退出</button>
      </section>

      <div class="restricted-banner" role="note">
        <span class="restricted-banner__icon" aria-hidden="true">!</span>
        <div>
          <strong>功能受限：仅支持节点上报与回单上传</strong>
          <p>导航、称重、电子签名、异常上报等完整功能请下载「驰远司机」App。</p>
          <button type="button" @click="showAppSheet = true">查看下载方式 →</button>
        </div>
      </div>

      <div class="driver-toolbar">
        <div class="driver-filters" role="tablist" aria-label="任务筛选">
          <button
            v-for="filter in filters"
            :key="filter"
            type="button"
            role="tab"
            :aria-selected="activeFilter === filter"
            :class="{ 'is-active': activeFilter === filter }"
            @click="activeFilter = filter"
          >
            {{ filter }}<span>{{ counts[filter] || 0 }}</span>
          </button>
        </div>
        <button class="driver-refresh" type="button" aria-label="刷新任务" @click="loadTasks">↻</button>
      </div>

      <div v-if="tasksLoading" class="task-skeletons">
        <div v-for="n in 3" :key="n" class="card task-skeleton">
          <div class="skeleton" style="height: 14px; width: 45%"></div>
          <div class="skeleton" style="height: 22px; width: 70%; margin-top: 10px"></div>
          <div class="skeleton" style="height: 12px; width: 90%; margin-top: 10px"></div>
          <div class="skeleton" style="height: 36px; width: 100%; margin-top: 14px"></div>
        </div>
      </div>

      <div v-else-if="tasksError" class="state-block">
        <div class="state-block__icon" aria-hidden="true">!</div>
        <h3>任务加载失败</h3>
        <p>{{ tasksError }}</p>
        <button class="btn btn--primary btn--sm" type="button" @click="loadTasks">重新加载</button>
      </div>

      <div v-else-if="!filteredTasks.length" class="state-block">
        <div class="state-block__icon" aria-hidden="true">☰</div>
        <h3>{{ activeFilter === '全部' ? '暂无运输任务' : `暂无${activeFilter}任务` }}</h3>
        <p>新任务由调度派发后会出现在这里。下拉或点击刷新试试。</p>
        <button class="btn btn--outline btn--sm" type="button" @click="loadTasks">刷新任务</button>
      </div>

      <div v-else class="task-list">
        <article v-for="task in filteredTasks" :key="task.id" class="card task-card">
          <div class="task-card__top">
            <span class="task-card__no mono">{{ task.waybillNo }}</span>
            <span class="tag" :class="statusClass(task.statusName)">{{ task.statusName }}</span>
          </div>

          <div class="task-route">
            <div class="task-route__point">
              <span class="task-route__dot task-route__dot--send"></span>
              <strong>{{ task.origin }}</strong>
              <small>{{ task.sender.name }}</small>
            </div>
            <span class="task-route__line" aria-hidden="true"></span>
            <div class="task-route__point task-route__point--right">
              <span class="task-route__dot task-route__dot--recv"></span>
              <strong>{{ task.destination }}</strong>
              <small>{{ task.receiver.name }}</small>
            </div>
          </div>

          <div class="task-address">
            <p><span>提</span>{{ task.sender.address }}</p>
            <p><span>送</span>{{ task.receiver.address }}</p>
          </div>

          <dl class="task-meta">
            <div><dt>货物</dt><dd>{{ task.cargoName }}</dd></div>
            <div><dt>件数</dt><dd>{{ task.pieces }} 件</dd></div>
            <div><dt>重量</dt><dd>{{ task.weight }} kg</dd></div>
            <div><dt>提货时间</dt><dd>{{ formatDateTime(task.pickupTime) }}</dd></div>
          </dl>

          <div v-if="task.node || uploadedIds.includes(task.id)" class="task-node">
            <span>最近节点</span>
            <strong>{{ uploadedIds.includes(task.id) && task.node !== '回单已上传' ? '回单已上传' : task.node }}</strong>
          </div>

          <div class="task-actions">
            <a class="task-action task-action--call" :href="`tel:${task.sender.phone}`">联系发货人</a>
            <a class="task-action task-action--call" :href="`tel:${task.receiver.phone}`">联系收货人</a>
          </div>
          <div class="task-actions task-actions--primary">
            <button class="btn btn--outline task-btn" type="button" @click="openReport(task)">节点上报</button>
            <button class="btn btn--primary task-btn" type="button" @click="openReceipt(task)">回单上传</button>
          </div>
        </article>
        <p class="driver-footnote">
          任务数据支持离线查看，节点上报与回单上传需网络。演示环境数据不会写入真实业务系统。
        </p>
      </div>
    </template>

    <!-- Node report sheet -->
    <Teleport to="body">
      <div v-if="reportTask" class="sheet-mask" @click.self="closeReport">
        <div class="sheet" role="dialog" aria-modal="true" aria-label="节点上报">
          <div class="sheet__handle"></div>
          <div class="sheet__head">
            <h2>节点上报</h2>
            <button class="sheet__close" type="button" aria-label="关闭" @click="closeReport">×</button>
          </div>
          <div class="sheet__body">
            <p class="sheet-task-no mono">{{ reportTask.waybillNo }}</p>

            <div class="node-options">
              <button
                v-for="option in NODE_OPTIONS"
                :key="option.value"
                type="button"
                class="node-option"
                :class="{ 'is-active': nodeForm.node === option.value }"
                @click="nodeForm.node = option.value"
              >
                <span class="node-option__radio" aria-hidden="true"></span>
                <span>
                  <strong>{{ option.value }}</strong>
                  <small>{{ option.desc }}</small>
                </span>
              </button>
            </div>
            <p v-if="nodeError" class="form-error">{{ nodeError }}</p>

            <div class="field" style="margin-top: 14px">
              <label for="node-remark">备注（选填）</label>
              <textarea id="node-remark" v-model="nodeForm.remark" class="textarea" placeholder="如：货物外包装完好，装车 10 件"></textarea>
            </div>

            <div class="gps-row">
              <div>
                <strong>当前位置</strong>
                <p>{{ gps.label }}</p>
                <small v-if="gps.lat" class="mono">{{ gps.lat }}, {{ gps.lng }}</small>
              </div>
              <button class="btn btn--outline btn--sm gps-btn" type="button" :disabled="gps.state === 'loading'" @click="locate">
                <span v-if="gps.state === 'loading'" class="spinner spinner--dark"></span>
                {{ gps.state === 'loading' ? '定位中' : '获取定位' }}
              </button>
            </div>

            <button class="btn btn--primary btn--block sheet-submit" type="button" :disabled="nodeSubmitting" @click="submitNode">
              <span v-if="nodeSubmitting" class="spinner"></span>
              {{ nodeSubmitting ? '正在上报…' : '提报节点' }}
            </button>
            <p class="sheet-tip">提交后将同步至调度与客户端，请确认节点无误后操作。</p>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Receipt upload sheet -->
    <Teleport to="body">
      <div v-if="receiptTask" class="sheet-mask" @click.self="closeReceipt">
        <div class="sheet" role="dialog" aria-modal="true" aria-label="回单上传">
          <div class="sheet__handle"></div>
          <div class="sheet__head">
            <h2>回单上传</h2>
            <button class="sheet__close" type="button" aria-label="关闭" @click="closeReceipt">×</button>
          </div>
          <div class="sheet__body">
            <p class="sheet-task-no mono">{{ receiptTask.waybillNo }}</p>
            <p class="upload-hint">请上传清晰完整的签收回单，最多 6 张。照片将自动压缩至 1280px 后上传。</p>

            <div class="upload-grid">
              <div v-for="(photo, index) in photos" :key="index" class="upload-item">
                <img :src="photo" :alt="`回单照片 ${index + 1}`" />
                <button type="button" class="upload-remove" :aria-label="`删除第 ${index + 1} 张`" @click="removePhoto(index)">×</button>
              </div>
              <button v-if="photos.length < 6" class="upload-add" type="button" @click="pickFiles">
                <span aria-hidden="true">＋</span>
                <small>{{ photoLoading ? '处理中…' : '拍照/选择' }}</small>
              </button>
            </div>
            <input ref="fileInput" class="visually-hidden" type="file" accept="image/*" multiple @change="onFilesChange" />
            <p v-if="receiptError" class="form-error">{{ receiptError }}</p>

            <div class="notice notice--warn upload-notice">
              演示/受限环境：仅上传照片，不收取代收货款与电子签名。完整作业请使用 App。
            </div>

            <button class="btn btn--primary btn--block sheet-submit" type="button" :disabled="receiptSubmitting || photoLoading" @click="submitReceipt">
              <span v-if="receiptSubmitting" class="spinner"></span>
              {{ receiptSubmitting ? '正在上传…' : `上传回单（${photos.length} 张）` }}
            </button>
            <p class="sheet-tip">上传成功后回单将进入审核队列，审核结果可在 App 查看。</p>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- App download sheet -->
    <Teleport to="body">
      <div v-if="showAppSheet" class="sheet-mask" @click.self="showAppSheet = false">
        <div class="sheet" role="dialog" aria-modal="true" aria-label="下载驰远司机 App">
          <div class="sheet__handle"></div>
          <div class="sheet__head">
            <h2>下载「驰远司机」App</h2>
            <button class="sheet__close" type="button" aria-label="关闭" @click="showAppSheet = false">×</button>
          </div>
          <div class="sheet__body">
            <div class="app-steps">
              <div><span>1</span><p>在 App Store / 华为应用市场 / 小米应用商店搜索「驰远司机」。</p></div>
              <div><span>2</span><p>使用当前手机号登录，完成实名与车辆绑定。</p></div>
              <div><span>3</span><p>开启定位权限，即可使用导航、称重、电子签名与离线补传。</p></div>
            </div>
            <div class="notice notice--warn">演示环境无真实下载二维码。正式环境由平台配置应用下载地址与小程序码。</div>
            <button class="btn btn--primary btn--block" type="button" style="margin-top: 14px" @click="showAppSheet = false">我知道了</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.driver-page {
  padding-top: 12px;
}

/* Login */
.login-hero {
  padding: 10px 4px 20px;
  text-align: center;
}

.login-hero__mark {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  margin: 0 auto 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
  background: linear-gradient(145deg, #1f6feb, #0b3d91);
  box-shadow: 0 10px 24px rgba(31, 111, 235, 0.3);
}

.login-hero__mark i {
  display: block;
  width: 4px;
  border-radius: 3px;
  background: #fff;
}

.login-hero__mark i:nth-child(1) {
  height: 14px;
  opacity: 0.65;
}

.login-hero__mark i:nth-child(2) {
  height: 24px;
}

.login-hero__mark i:nth-child(3) {
  height: 17px;
  opacity: 0.82;
}

.login-hero h1 {
  font-size: 22px;
  letter-spacing: 0.02em;
}

.login-hero p {
  font-size: 12px;
  color: var(--muted);
  margin-top: 6px;
}

.restricted-banner {
  display: flex;
  gap: 9px;
  padding: 11px 13px;
  border-radius: 11px;
  background: #fff8e8;
  border: 1px solid #f5dda6;
  color: #8a5300;
  margin-bottom: 12px;
}

.restricted-banner__icon {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: #f59e0b;
  color: #fff;
  font-size: 12px;
  font-weight: 800;
  margin-top: 1px;
}

.restricted-banner strong {
  display: block;
  font-size: 12px;
}

.restricted-banner p {
  font-size: 11px;
  line-height: 1.55;
  margin-top: 2px;
}

.restricted-banner button {
  font-size: 11px;
  font-weight: 600;
  color: #8a5300;
  text-decoration: underline;
  padding: 5px 0 2px;
  min-height: 30px;
}

.login-card {
  padding: 20px 18px;
}

.login-card h2 {
  font-size: 17px;
}

.login-card__sub {
  font-size: 11.5px;
  color: var(--muted);
  margin: 3px 0 16px;
}

.login-card form {
  display: grid;
  gap: 14px;
}

.code-row {
  display: flex;
  gap: 9px;
}

.code-row .input {
  flex: 1;
  min-width: 0;
}

.code-btn {
  flex-shrink: 0;
  min-height: 48px;
  font-size: 12.5px;
  padding: 0 12px;
}

.login-submit {
  margin-top: 6px;
}

.login-card__demo {
  margin-top: 13px;
  padding: 8px 10px;
  border-radius: 7px;
  background: #f6faff;
  font-size: 11px;
  color: var(--muted);
  text-align: center;
}

.login-card__demo strong {
  color: var(--primary);
}

.login-card__agree {
  margin-top: 10px;
  font-size: 10px;
  color: var(--muted-light);
  text-align: center;
}

.login-help {
  margin-top: 18px;
  padding: 14px 16px;
  border-radius: 10px;
  background: #edf2f9;
}

.login-help h3 {
  font-size: 12px;
  margin-bottom: 3px;
}

.login-help p {
  font-size: 11px;
  color: var(--muted);
}

.login-help a {
  color: var(--primary);
  font-weight: 600;
}

/* Driver head */
.driver-head {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 13px 14px;
}

.driver-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: linear-gradient(145deg, #e8f2ff, #cfe4ff);
  color: var(--primary);
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
}

.driver-head__info {
  flex: 1;
  min-width: 0;
}

.driver-head__info strong {
  font-size: 14px;
}

.driver-head__info p {
  font-size: 11px;
  color: var(--muted-light);
  margin-top: 1px;
}

.driver-head__logout {
  flex-shrink: 0;
  min-height: 34px;
  padding: 0 10px;
  border: 1px solid var(--line);
  border-radius: 7px;
  font-size: 11.5px;
  color: var(--muted);
  background: #fff;
}

/* Toolbar */
.driver-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 14px 0 10px;
}

.driver-filters {
  display: flex;
  gap: 6px;
  overflow-x: auto;
  flex: 1;
  scrollbar-width: none;
  padding-bottom: 2px;
}

.driver-filters::-webkit-scrollbar {
  display: none;
}

.driver-filters button {
  flex-shrink: 0;
  min-height: 44px;
  padding: 0 12px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: #fff;
  font-size: 12px;
  color: var(--muted);
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.driver-filters button span {
  font-size: 10px;
  background: #f1f5fa;
  border-radius: 999px;
  padding: 1px 6px;
  color: var(--muted-light);
}

.driver-filters button.is-active {
  background: var(--primary);
  border-color: var(--primary);
  color: #fff;
  font-weight: 600;
}

.driver-filters button.is-active span {
  background: rgba(255, 255, 255, 0.22);
  color: #fff;
}

.driver-refresh {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid var(--line);
  color: var(--primary);
  font-size: 18px;
}

.task-skeletons {
  display: grid;
  gap: 10px;
}

.task-skeleton {
  padding: 15px;
}

.task-list {
  display: grid;
  gap: 11px;
}

.task-card {
  padding: 15px;
}

.task-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.task-card__no {
  font-size: 11px;
  color: var(--muted-light);
}

.task-route {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 10px 0 9px;
}

.task-route__point {
  display: flex;
  align-items: center;
  gap: 5px;
  min-width: 0;
}

.task-route__point strong {
  font-size: 18px;
}

.task-route__point small {
  font-size: 10px;
  color: var(--muted-light);
  margin-left: 1px;
}

.task-route__point--right {
  text-align: right;
}

.task-route__dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.task-route__dot--send {
  background: var(--primary);
}

.task-route__dot--recv {
  background: var(--green);
}

.task-route__line {
  flex: 1;
  min-width: 20px;
  height: 1px;
  background: repeating-linear-gradient(90deg, #bad3f2 0 5px, transparent 5px 9px);
}

.task-address {
  padding: 9px 11px;
  border-radius: 9px;
  background: #f7faff;
  display: grid;
  gap: 6px;
}

.task-address p {
  display: flex;
  gap: 7px;
  font-size: 11px;
  line-height: 1.5;
  color: #4a5d73;
}

.task-address p span {
  flex-shrink: 0;
  width: 17px;
  height: 17px;
  border-radius: 4px;
  background: #e6f1ff;
  color: var(--primary);
  font-size: 9.5px;
  font-weight: 700;
  display: grid;
  place-items: center;
  margin-top: 1px;
}

.task-address p + p span {
  background: #eaf8f0;
  color: var(--green);
}

.task-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px 12px;
  margin: 11px 0 0;
}

.task-meta div {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 11px;
  padding-bottom: 5px;
  border-bottom: 1px dashed #eef2f8;
}

.task-meta dt {
  color: var(--muted-light);
}

.task-meta dd {
  margin: 0;
  color: #34475e;
  text-align: right;
}

.task-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 9px;
  padding: 6px 10px;
  border-radius: 7px;
  background: #fff9ec;
  font-size: 10.5px;
  color: #8a5300;
}

.task-node strong {
  font-size: 11px;
}

.task-actions {
  display: flex;
  gap: 8px;
  margin-top: 9px;
}

.task-action {
  flex: 1;
  min-height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 7px;
  font-size: 11px;
  background: #f4f7fb;
  color: #4a5d73;
}

.task-action--call::before {
  content: '☎';
  margin-right: 4px;
  color: var(--primary);
}

.task-actions--primary {
  margin-top: 7px;
}

.task-btn {
  flex: 1;
  min-height: 42px;
  font-size: 13px;
}

.driver-footnote {
  margin: 14px 4px 0;
  font-size: 10px;
  color: var(--muted-light);
  line-height: 1.6;
  text-align: center;
}

/* Sheets */
.sheet-task-no {
  font-size: 11px;
  color: var(--muted-light);
  margin-bottom: 12px;
}

.node-options {
  display: grid;
  gap: 7px;
}

.node-option {
  display: flex;
  align-items: flex-start;
  gap: 9px;
  min-height: 52px;
  padding: 9px 11px;
  border: 1px solid var(--line);
  border-radius: 9px;
  background: #fbfdff;
  text-align: left;
}

.node-option__radio {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid #c7d4e4;
  margin-top: 1px;
  position: relative;
}

.node-option.is-active {
  border-color: var(--primary);
  background: #f3f9ff;
}

.node-option.is-active .node-option__radio {
  border-color: var(--primary);
}

.node-option.is-active .node-option__radio::after {
  content: '';
  position: absolute;
  inset: 3px;
  border-radius: 50%;
  background: var(--primary);
}

.node-option strong {
  display: block;
  font-size: 12.5px;
}

.node-option small {
  display: block;
  font-size: 10.5px;
  color: var(--muted-light);
  margin-top: 1px;
}

.gps-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 9px;
  background: var(--soft);
}

.gps-row strong {
  font-size: 11.5px;
}

.gps-row p {
  font-size: 10.5px;
  color: var(--muted);
  margin-top: 1px;
}

.gps-row small {
  font-size: 9.5px;
  color: var(--muted-light);
}

.gps-btn {
  flex-shrink: 0;
  gap: 5px;
}

.sheet-submit {
  margin-top: 16px;
}

.sheet-tip {
  margin-top: 9px;
  font-size: 10px;
  color: var(--muted-light);
  text-align: center;
  line-height: 1.55;
}

.upload-hint {
  font-size: 11px;
  color: var(--muted);
  line-height: 1.6;
  margin-bottom: 10px;
}

.upload-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.upload-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  background: var(--soft);
}

.upload-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-remove {
  position: absolute;
  right: 4px;
  top: 4px;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: rgba(7, 26, 51, 0.65);
  color: #fff;
  font-size: 16px;
  line-height: 1;
  display: grid;
  place-items: center;
}

.upload-add {
  aspect-ratio: 1;
  border: 1px dashed #cfdbea;
  border-radius: 8px;
  background: #fbfdff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  color: var(--muted-light);
}

.upload-add span {
  font-size: 22px;
  line-height: 1;
  color: var(--primary);
}

.upload-add small {
  font-size: 10px;
}

.upload-notice {
  margin-top: 12px;
}

.app-steps {
  display: grid;
  gap: 12px;
}

.app-steps div {
  display: flex;
  gap: 10px;
}

.app-steps span {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: #eef5ff;
  color: var(--primary);
  font-size: 11px;
  font-weight: 700;
}

.app-steps p {
  font-size: 12px;
  color: #3a4d64;
  line-height: 1.6;
}
</style>
