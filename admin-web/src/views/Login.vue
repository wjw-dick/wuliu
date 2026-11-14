<template>
  <div class="login-page">
    <section class="brand-panel">
      <div class="brand-grid"></div>
      <div class="brand-inner">
        <div class="brand-top">
          <div class="brand-logo">全</div>
          <div class="brand-name">
            <strong>全达物流</strong>
            <span>QUANDA LOGISTICS SaaS</span>
          </div>
        </div>

        <div class="brand-copy">
          <div class="eyebrow">一站式物流数字化平台</div>
          <h1>让每一票货物<br />都有迹可循</h1>
          <p>从询价、调度、在途到结算对账，一套系统贯通九端业务，多租户隔离与全链路审计为规模化经营保驾护航。</p>
          <div class="feature-chips">
            <span>多租户隔离</span>
            <span>调度看板</span>
            <span>轨迹回放</span>
            <span>财务对账</span>
            <span>开放接口</span>
          </div>
        </div>

        <div class="route-art">
          <svg viewBox="0 0 560 190" fill="none" aria-hidden="true">
            <path d="M18 146C90 146 106 48 186 48s100 94 178 94 104-74 166-74" stroke="url(#lineGrad)" stroke-width="2.5" stroke-linecap="round" stroke-dasharray="7 7" />
            <path d="M18 168C94 168 118 104 188 104s108 52 176 52 92-38 166-38" stroke="rgba(255,255,255,.14)" stroke-width="1.5" stroke-linecap="round" />
            <circle cx="18" cy="146" r="6" fill="#7fb0ff" />
            <circle cx="186" cy="48" r="6" fill="#7fb0ff" />
            <circle cx="364" cy="142" r="6" fill="#7fb0ff" />
            <circle cx="530" cy="68" r="8" fill="#fff" />
            <circle cx="530" cy="68" r="15" stroke="rgba(255,255,255,.4)" stroke-width="1.5" />
            <defs>
              <linearGradient id="lineGrad" x1="0" y1="0" x2="560" y2="0" gradientUnits="userSpaceOnUse">
                <stop stop-color="#1f6feb" stop-opacity="0" />
                <stop offset=".35" stop-color="#5a93f1" />
                <stop offset="1" stop-color="#bcd7ff" />
              </linearGradient>
            </defs>
          </svg>
          <div class="art-labels">
            <span>上海始发</span>
            <span>杭州中转</span>
            <span>广州签收</span>
          </div>
        </div>

        <div class="brand-stats">
          <div><strong>12</strong><span>平台租户</span></div>
          <div><strong>18,620</strong><span>累计运单</span></div>
          <div><strong>99.2%</strong><span>轨迹完整率</span></div>
        </div>
      </div>
    </section>

    <section class="form-panel">
      <div class="form-inner">
        <div class="login-title">欢迎登录</div>
        <div class="login-sub">请选择控制台并输入账号密码</div>

        <div class="scope-tabs">
          <button :class="{ active: scope === 'platform' }" @click="scope = 'platform'">
            <el-icon><Monitor /></el-icon>
            平台运营端
          </button>
          <button :class="{ active: scope === 'tenant' }" @click="scope = 'tenant'">
            <el-icon><OfficeBuilding /></el-icon>
            租户后台
          </button>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="submit">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入账号" autocomplete="username">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="请输入密码"
              autocomplete="current-password"
              @keyup.enter="submit"
            >
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>

          <div class="login-options">
            <el-checkbox v-model="remember">记住账号</el-checkbox>
            <span class="link-text" @click="forgot">忘记密码？</span>
          </div>

          <el-button type="primary" class="login-btn" size="large" :loading="loading" @click="submit">
            {{ loading ? '正在登录…' : '登 录' }}
          </el-button>
        </el-form>

        <div class="demo-box">
          <div class="demo-title"><el-icon><InfoFilled /></el-icon> 演示账号</div>
          <div class="demo-row" @click="fillDemo">
            <span>平台超管</span>
            <code>admin / admin123</code>
            <el-icon><CopyDocument /></el-icon>
          </div>
          <div class="demo-row" @click="fillDemo">
            <span>租户管理员</span>
            <code>admin / admin123</code>
            <el-icon><CopyDocument /></el-icon>
          </div>
          <div class="demo-note">选择上方的控制台后使用同一组账号登录</div>
        </div>

        <div class="login-footer">© 2026 全达物流 SaaS · 沪ICP备2026012345号</div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useTabsStore } from '@/stores/tabs'

const auth = useAuthStore()
const tabs = useTabsStore()
const router = useRouter()
const route = useRoute()

const scope = ref<'platform' | 'tenant'>('tenant')
const loading = ref(false)
const remember = ref(true)
const formRef = ref<FormInstance>()
const form = reactive({ username: 'admin', password: 'admin123' })

const rules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ]
}

onMounted(() => {
  const saved = localStorage.getItem('wuliu_last_username')
  if (saved) form.username = saved
  const s = route.query.scope
  if (s === 'platform' || s === 'tenant') scope.value = s
})

function fillDemo(): void {
  form.username = 'admin'
  form.password = 'admin123'
  ElMessage.success('已填入演示账号，点击登录即可')
}

function forgot(): void {
  ElMessage.info('请联系平台管理员重置密码，客服电话 400-800-9000')
}

async function submit(): Promise<void> {
  if (!formRef.value || loading.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await auth.login({ username: form.username, password: form.password, scope: scope.value })
    if (remember.value) localStorage.setItem('wuliu_last_username', form.username)
    else localStorage.removeItem('wuliu_last_username')
    tabs.removeAll()
    const fallback = scope.value === 'platform' ? '/platform/dashboard' : '/tenant/dashboard'
    const redirect = route.query.redirect as string
    const target = redirect && redirect.startsWith(scope.value === 'platform' ? '/platform' : '/tenant') ? redirect : fallback
    ElMessage.success(`登录成功，欢迎回来 ${auth.displayName}`)
    router.replace(target)
  } catch {
    /* 错误提示由请求拦截器统一处理 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  height: 100vh;
  width: 100vw;
  display: flex;
  background: #fff;
  overflow: hidden;
}
.brand-panel {
  flex: 1.1;
  min-width: 0;
  background: radial-gradient(1200px 600px at 10% 0%, #1c3d75 0%, #0f1d33 45%, #0b1524 100%);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-grid {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(255, 255, 255, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.035) 1px, transparent 1px);
  background-size: 42px 42px;
  mask-image: radial-gradient(circle at 40% 30%, black, transparent 75%);
}
.brand-inner {
  position: relative;
  z-index: 1;
  width: 78%;
  max-width: 560px;
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 36px 0 30px;
}
.brand-top {
  display: flex;
  align-items: center;
  gap: 12px;
}
.brand-logo {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #4b96ff, #1f6feb);
  color: #fff;
  font-weight: 700;
  font-size: 19px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 18px rgba(31, 111, 235, 0.5);
}
.brand-name {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
  strong {
    color: #fff;
    font-size: 17px;
    letter-spacing: 2px;
  }
  span {
    color: #6e86a6;
    font-size: 9px;
    letter-spacing: 1.6px;
    margin-top: 4px;
  }
}
.brand-copy {
  margin-top: auto;
  margin-bottom: 22px;
}
.eyebrow {
  color: #7fb0ff;
  font-size: 12px;
  letter-spacing: 3px;
  margin-bottom: 14px;
}
.brand-copy h1 {
  color: #fff;
  font-size: 38px;
  line-height: 1.35;
  font-weight: 700;
  margin: 0 0 18px;
  letter-spacing: 1px;
}
.brand-copy p {
  color: #9fb1c9;
  font-size: 13px;
  line-height: 1.9;
  max-width: 440px;
  margin: 0 0 20px;
}
.feature-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  span {
    color: #b8c9df;
    font-size: 11px;
    border: 1px solid rgba(255, 255, 255, 0.14);
    background: rgba(255, 255, 255, 0.04);
    padding: 4px 10px;
    border-radius: 12px;
  }
}
.route-art {
  margin: 6px 0 24px;
  svg {
    width: 100%;
    height: auto;
    display: block;
  }
}
.art-labels {
  display: flex;
  justify-content: space-between;
  color: #6f86a5;
  font-size: 10px;
  margin-top: -6px;
  padding: 0 4px;
}
.brand-stats {
  display: flex;
  gap: 44px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  padding-top: 20px;
  div {
    display: flex;
    flex-direction: column;
    gap: 3px;
  }
  strong {
    color: #fff;
    font-size: 22px;
    font-family: 'DIN Alternate', Consolas, sans-serif;
  }
  span {
    color: #7488a3;
    font-size: 11px;
  }
}

.form-panel {
  width: 480px;
  flex: 0 0 480px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 48px;
  overflow-y: auto;
}
.form-inner {
  width: 100%;
  max-width: 360px;
}
.login-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-main);
}
.login-sub {
  font-size: 12.5px;
  color: var(--text-muted);
  margin-top: 6px;
  margin-bottom: 22px;
}
.scope-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  background: #f4f6fa;
  border-radius: 8px;
  padding: 3px;
  gap: 3px;
  margin-bottom: 20px;

  button {
    border: none;
    background: transparent;
    height: 36px;
    border-radius: 6px;
    color: var(--text-secondary);
    font-size: 13px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 5px;
    transition: all 0.15s;

    &.active {
      background: #fff;
      color: var(--brand);
      font-weight: 600;
      box-shadow: 0 1px 4px rgba(16, 35, 60, 0.1);
    }
  }
}
.login-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 2px 0 18px;
  font-size: 12px;
}
.login-btn {
  width: 100%;
  height: 42px;
  font-size: 15px;
  letter-spacing: 4px;
  border-radius: 6px;
  box-shadow: 0 6px 16px rgba(31, 111, 235, 0.28);
}
.demo-box {
  margin-top: 22px;
  border: 1px dashed #d8e2f2;
  background: #f9fbff;
  border-radius: 8px;
  padding: 10px 12px;
}
.demo-title {
  font-size: 11.5px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 6px;
}
.demo-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 11.5px;
  padding: 5px 6px;
  border-radius: 4px;
  cursor: pointer;
  color: var(--text-secondary);

  code {
    font-family: 'SFMono-Regular', Consolas, monospace;
    color: var(--brand);
    background: rgba(31, 111, 235, 0.08);
    padding: 1px 6px;
    border-radius: 3px;
  }
  .el-icon {
    color: var(--text-muted);
  }
  &:hover {
    background: #eef4ff;
  }
}
.demo-note {
  font-size: 10.5px;
  color: var(--text-muted);
  margin-top: 4px;
}
.login-footer {
  margin-top: 26px;
  text-align: center;
  font-size: 10.5px;
  color: #b2bdcb;
}
@media (max-width: 1100px) {
  .brand-panel {
    display: none;
  }
  .form-panel {
    width: 100%;
    flex: 1;
  }
}
</style>
