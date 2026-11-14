<template>
  <header class="navbar">
    <div class="nav-left">
      <button class="icon-btn" title="切换菜单" @click="app.toggleSidebar()">
        <el-icon><component :is="app.sidebarCollapsed ? 'Expand' : 'Fold'" /></el-icon>
      </button>
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item v-if="groupTitle">{{ groupTitle }}</el-breadcrumb-item>
        <el-breadcrumb-item v-if="parentTitle">{{ parentTitle }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ route.meta.title || '页面' }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="nav-right">
      <el-autocomplete
        v-model="searchText"
        class="global-search"
        :fetch-suggestions="fetchSuggestions"
        placeholder="搜索菜单、运单、客户..."
        clearable
        @select="handleSelect"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #default="{ item }">
          <div class="search-item">
            <span>{{ item.value }}</span>
            <span class="search-group">{{ item.group }}</span>
          </div>
        </template>
      </el-autocomplete>

      <el-popover placement="bottom-end" :width="330" trigger="click">
        <template #reference>
          <button class="icon-btn" title="通知">
            <el-badge :value="notifications.length" :max="9" class="bell-badge">
              <el-icon><Bell /></el-icon>
            </el-badge>
          </button>
        </template>
        <div class="notice-panel">
          <div class="notice-head">
            <strong>待办与通知</strong>
            <span class="link-text" @click="goAnnouncements">查看公告</span>
          </div>
          <div v-for="(item, idx) in notifications" :key="idx" class="notice-item" @click="goAnnouncements">
            <span class="dot" :class="item.level"></span>
            <div class="notice-body">
              <div class="notice-title">{{ item.title }}</div>
              <div class="notice-time">{{ item.time }}</div>
            </div>
          </div>
        </div>
      </el-popover>

      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-chip">
          <el-avatar :size="28" class="avatar">{{ avatarChar }}</el-avatar>
          <div class="user-meta">
            <span class="user-name">{{ auth.displayName }}</span>
            <span class="user-role">{{ auth.roles[0] || (isPlatform ? '平台账号' : '租户账号') }}</span>
          </div>
          <el-icon class="caret"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon> 个人中心
            </el-dropdown-item>
            <el-dropdown-item command="password">
              <el-icon><Key /></el-icon> 修改密码
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon> 退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>

  <el-dialog v-model="passwordVisible" title="修改密码" width="420px" append-to-body>
    <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="88px">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少 6 位" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="passwordVisible = false">取消</el-button>
      <el-button type="primary" :loading="pwdLoading" @click="submitPassword">确认修改</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { useTabsStore } from '@/stores/tabs'
import { buildMenus, GROUP_META } from '@/router/menu'
import { layoutRoutes } from '@/router/routes'
import { changePassword } from '@/api/auth'

const app = useAppStore()
const auth = useAuthStore()
const tabs = useTabsStore()
const route = useRoute()
const router = useRouter()

const isPlatform = computed(() => auth.scope === 'platform')
const groupTitle = computed(() => (route.meta.group ? GROUP_META[route.meta.group as string]?.title : ''))
const parentTitle = computed(() => route.meta.parentTitle || '')
const avatarChar = computed(() => (auth.displayName || '管').slice(0, 1))

const searchText = ref('')
const menuIndex = computed(() => {
  const groups = buildMenus(layoutRoutes, auth.scope, (p) => auth.hasPerm(p))
  return groups.flatMap((g) => g.children.map((c) => ({ value: c.title, group: g.title, path: c.path })))
})

function fetchSuggestions(query: string, cb: (arg: any[]) => void): void {
  const q = (query || '').trim()
  const list = q ? menuIndex.value.filter((m) => m.value.includes(q) || m.group.includes(q)) : menuIndex.value
  cb(list.slice(0, 10))
}
function handleSelect(item: any): void {
  if (item?.path) router.push(item.path)
  searchText.value = ''
}

const notifications = [
  { title: '7 张运单待调度，请及时派车', time: '12 分钟前', level: 'warning' },
  { title: '5 张异常单待处理，其中 2 张已升级', time: '30 分钟前', level: 'danger' },
  { title: '3 份对账单待客户确认', time: '1 小时前', level: 'primary' }
]

function goAnnouncements(): void {
  router.push(isPlatform.value ? '/platform/announcements' : '/tenant/announcements')
}

function handleCommand(command: string): void {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'password') {
    passwordVisible.value = true
  } else if (command === 'logout') {
    ElMessageBox.confirm('退出后需要重新登录，是否继续？', '退出登录', {
      confirmButtonText: '退出登录',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        await auth.logout()
        tabs.removeAll()
        router.replace('/login')
      })
      .catch(() => undefined)
  }
}

const passwordVisible = ref(false)
const pwdLoading = ref(false)
const pwdFormRef = ref<FormInstance>()
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== pwdForm.newPassword) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

async function submitPassword(): Promise<void> {
  if (!pwdFormRef.value) return
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  pwdLoading.value = true
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功，请使用新密码登录')
    passwordVisible.value = false
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } finally {
    pwdLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.navbar {
  height: 52px;
  flex: 0 0 52px;
  background: #fff;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px 0 6px;
  gap: 12px;
  position: relative;
  z-index: 10;
}
.nav-left {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
}
.icon-btn {
  border: none;
  background: transparent;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  color: var(--text-secondary);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  &:hover {
    background: #f2f5fa;
    color: var(--brand);
  }
}
.breadcrumb {
  margin-left: 4px;
  :deep(.el-breadcrumb__inner) {
    color: var(--text-muted);
    font-weight: 400;
  }
  :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
    color: var(--text-main);
    font-weight: 500;
  }
}
.nav-right {
  display: flex;
  align-items: center;
  gap: 6px;
}
.global-search {
  width: 230px;
  :deep(.el-input__wrapper) {
    background: #f5f7fb;
    box-shadow: none;
    border: 1px solid transparent;
    border-radius: 6px;
  }
  :deep(.el-input__wrapper.is-focus) {
    background: #fff;
    border-color: var(--brand);
  }
}
.search-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 12.5px;
}
.search-group {
  color: var(--text-muted);
  font-size: 11px;
}
.bell-badge {
  display: inline-flex;
  :deep(.el-badge__content) {
    font-size: 9px;
    height: 14px;
    line-height: 14px;
    padding: 0 4px;
  }
}
.notice-panel {
  margin: -6px;
}
.notice-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 6px 10px;
  border-bottom: 1px solid var(--border-color);
  font-size: 13px;
}
.notice-item {
  display: flex;
  gap: 8px;
  padding: 10px 6px;
  border-bottom: 1px solid #f2f4f8;
  cursor: pointer;
  &:hover {
    background: #f7f9fc;
  }
  &:last-child {
    border-bottom: none;
  }
}
.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-top: 6px;
  flex: 0 0 6px;
  background: var(--brand);
  &.warning {
    background: #f59e0b;
  }
  &.danger {
    background: #ef4444;
  }
}
.notice-title {
  font-size: 12.5px;
  line-height: 18px;
}
.notice-time {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 2px;
}
.user-chip {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 3px 6px;
  border-radius: 6px;
  cursor: pointer;
  &:hover {
    background: #f2f5fa;
  }
}
.avatar {
  background: linear-gradient(135deg, #3b8af7, #1f6feb);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}
.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}
.user-name {
  font-size: 12.5px;
  font-weight: 500;
}
.user-role {
  font-size: 10px;
  color: var(--text-muted);
}
.caret {
  color: var(--text-muted);
  font-size: 12px;
}
@media (max-width: 1100px) {
  .global-search {
    width: 160px;
  }
  .user-meta {
    display: none;
  }
}
</style>
