<template>
  <div class="page">
    <div class="profile-head page-card">
      <el-avatar :size="64" class="avatar">{{ (auth.displayName || '管').slice(0, 1) }}</el-avatar>
      <div class="profile-meta">
        <div class="profile-name">{{ auth.displayName }}</div>
        <div class="profile-sub">
          <StatusTag :status="auth.scope === 'platform' ? '正常' : '正常'" />
          <span>{{ auth.roles.join('、') || '普通账号' }}</span>
          <span class="muted">·</span>
          <span class="muted">{{ auth.scope === 'platform' ? '平台运营中心' : auth.tenantName }}</span>
        </div>
      </div>
      <div class="profile-actions">
        <el-button @click="router.back()">返回</el-button>
      </div>
    </div>

    <div class="profile-grid">
      <el-card shadow="never" class="profile-card">
        <template #header>账号信息</template>
        <el-form label-width="92px">
          <el-form-item label="登录账号">
            <el-input :model-value="auth.user?.username" disabled />
          </el-form-item>
          <el-form-item label="显示昵称">
            <el-input v-model="nickname" maxlength="20" />
          </el-form-item>
          <el-form-item label="所属租户">
            <el-input :model-value="auth.scope === 'platform' ? '平台运营中心' : auth.tenantName" disabled />
          </el-form-item>
          <el-form-item label="所属组织">
            <el-input :model-value="auth.user?.orgName || '—'" disabled />
          </el-form-item>
          <el-form-item label="角色">
            <el-input :model-value="auth.roles.join('、') || '—'" disabled />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card shadow="never" class="profile-card">
        <template #header>修改密码</template>
        <el-form ref="pwdRef" :model="pwd" :rules="rules" label-width="92px">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="pwd.oldPassword" type="password" show-password placeholder="请输入原密码" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwd.newPassword" type="password" show-password placeholder="至少 6 位，建议字母 + 数字" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="pwd.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="changing" @click="submitPassword">确认修改</el-button>
          </el-form-item>
        </el-form>
        <el-alert
          title="密码修改后需重新登录。若忘记原密码，请联系平台管理员重置。"
          type="info"
          :closable="false"
          show-icon
        />
      </el-card>

      <el-card shadow="never" class="profile-card">
        <template #header>最近登录记录</template>
        <el-timeline>
          <el-timeline-item v-for="(item, i) in logins" :key="i" :timestamp="item.time" placement="top" :type="i === 0 ? 'primary' : ''">
            <div class="login-log">
              <span>{{ item.device }}</span>
              <span class="muted">{{ item.ip }}</span>
              <StatusTag :status="item.result" />
            </div>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <el-card shadow="never" class="profile-card">
        <template #header>安全设置</template>
        <div class="security-item">
          <div>
            <div class="strong">登录保护</div>
            <div class="muted">新设备登录需短信验证</div>
          </div>
          <el-switch v-model="security.sms" />
        </div>
        <div class="security-item">
          <div>
            <div class="strong">操作通知</div>
            <div class="muted">关键财务操作推送到手机</div>
          </div>
          <el-switch v-model="security.notice" />
        </div>
        <div class="security-item">
          <div>
            <div class="strong">会话超时</div>
            <div class="muted">无操作 30 分钟自动退出</div>
          </div>
          <el-switch v-model="security.timeout" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { changePassword } from '@/api/auth'
import StatusTag from '@/components/StatusTag.vue'

const auth = useAuthStore()
const router = useRouter()
const nickname = ref(auth.displayName)
const saving = ref(false)
const changing = ref(false)
const pwdRef = ref<FormInstance>()
const pwd = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const security = reactive({ sms: true, notice: true, timeout: true })

const logins = [
  { time: '2026-11-14 08:30:12', device: 'Chrome 131 · Windows 11', ip: '10.0.0.15', result: '成功' },
  { time: '2026-11-13 17:22:08', device: 'Chrome 131 · Windows 11', ip: '10.0.0.15', result: '成功' },
  { time: '2026-11-12 09:08:44', device: 'Safari 18 · macOS', ip: '116.236.12.8', result: '成功' }
]

const rules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    {
      validator: (_r, value, cb) => {
        if (!value) cb(new Error('请再次输入新密码'))
        else if (value !== pwd.newPassword) cb(new Error('两次输入的密码不一致'))
        else cb()
      },
      trigger: 'blur'
    }
  ]
}

function saveProfile(): void {
  saving.value = true
  setTimeout(() => {
    auth.updateLocalUser({ nickname: nickname.value })
    saving.value = false
    ElMessage.success('资料已保存')
  }, 300)
}

async function submitPassword(): Promise<void> {
  if (!pwdRef.value) return
  const valid = await pwdRef.value.validate().catch(() => false)
  if (!valid) return
  changing.value = true
  try {
    await changePassword({ oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
    ElMessage.success('密码修改成功，下次登录请使用新密码')
    pwd.oldPassword = ''
    pwd.newPassword = ''
    pwd.confirmPassword = ''
  } finally {
    changing.value = false
  }
}
</script>

<style scoped lang="scss">
.profile-head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  margin-bottom: 12px;
}
.avatar {
  background: linear-gradient(135deg, #4b96ff, #1f6feb);
  color: #fff;
  font-size: 24px;
  font-weight: 600;
}
.profile-meta {
  min-width: 0;
}
.profile-name {
  font-size: 18px;
  font-weight: 600;
}
.profile-sub {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 5px;
}
.profile-actions {
  margin-left: auto;
}
.profile-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.profile-card {
  :deep(.el-card__header) {
    font-size: 14px;
  }
}
.login-log {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
}
.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-color);
  font-size: 12px;
  &:last-child {
    border-bottom: none;
  }
  .muted {
    font-size: 11px;
    margin-top: 2px;
  }
}
@media (max-width: 1100px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
