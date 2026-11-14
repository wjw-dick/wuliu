import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getMe, logout as logoutApi, type AuthUser } from '@/api/auth'
import { TOKEN_KEY, USER_KEY, SCOPE_KEY } from '@/api/request'

function readUser(): AuthUser | null {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function collectPaths(items: any[], result: string[] = []): string[] {
  if (!Array.isArray(items)) return result
  for (const item of items) {
    if (!item) continue
    if (typeof item === 'string') {
      result.push(item)
      continue
    }
    if (item.path) result.push(item.path)
    if (item.url) result.push(item.url)
    if (item.children) collectPaths(item.children, result)
  }
  return result
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref<AuthUser | null>(readUser())
  const roles = ref<string[]>([])
  const buttons = ref<string[]>([])
  const menuPaths = ref<string[]>([])
  const loaded = ref(false)
  const scope = ref<'platform' | 'tenant'>((localStorage.getItem(SCOPE_KEY) as any) || 'tenant')

  const isLoggedIn = computed(() => !!token.value)
  const displayName = computed(() => user.value?.nickname || user.value?.username || '未登录')
  const tenantName = computed(() => user.value?.tenantName || '示例物流有限公司')

  function persist(): void {
    localStorage.setItem(TOKEN_KEY, token.value)
    localStorage.setItem(SCOPE_KEY, scope.value)
    if (user.value) localStorage.setItem(USER_KEY, JSON.stringify(user.value))
  }

  async function login(payload: { username: string; password: string; scope: 'platform' | 'tenant' }): Promise<void> {
    const res = await loginApi(payload)
    token.value = res.token
    user.value = res.user
    scope.value = res.user?.scope || payload.scope
    roles.value = res.roles || []
    buttons.value = res.buttons || []
    menuPaths.value = collectPaths(res.menus || [])
    loaded.value = !!res.buttons?.length
    persist()
  }

  async function fetchMe(): Promise<void> {
    if (!token.value) return
    try {
      const res = await getMe()
      if (res.user) {
        user.value = { ...user.value, ...res.user } as AuthUser
        scope.value = res.user.scope || scope.value
      }
      roles.value = res.roles || []
      buttons.value = res.buttons || []
      menuPaths.value = collectPaths(res.menus || [])
      loaded.value = true
      persist()
    } catch {
      // 后端不可用时保留本地演示会话
    }
  }

  function hasPerm(perm?: string): boolean {
    if (!perm) return true
    if (!loaded.value) return true
    if (!buttons.value.length && !menuPaths.value.length) return true
    if (buttons.value.includes('*') || buttons.value.includes(perm)) return true
    return menuPaths.value.some((p) => perm.startsWith(p))
  }

  function hasScope(target: 'platform' | 'tenant'): boolean {
    return scope.value === target
  }

  async function logout(): Promise<void> {
    try {
      if (token.value) await logoutApi()
    } catch {
      /* ignore */
    }
    token.value = ''
    user.value = null
    roles.value = []
    buttons.value = []
    menuPaths.value = []
    loaded.value = false
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
    localStorage.removeItem(SCOPE_KEY)
    localStorage.removeItem('wuliu_tabs')
  }

  function updateLocalUser(data: Partial<AuthUser>): void {
    if (user.value) {
      user.value = { ...user.value, ...data }
      localStorage.setItem(USER_KEY, JSON.stringify(user.value))
    }
  }

  return {
    token,
    user,
    roles,
    buttons,
    menuPaths,
    loaded,
    scope,
    isLoggedIn,
    displayName,
    tenantName,
    login,
    fetchMe,
    hasPerm,
    hasScope,
    logout,
    updateLocalUser
  }
})
