import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './routes'
import { useAuthStore } from '@/stores/auth'
import { useTabsStore } from '@/stores/tabs'
import { GROUP_META } from './menu'

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

const PUBLIC_PATHS = ['/login', '/403']

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (to.path === '/login') {
    if (auth.isLoggedIn) {
      return auth.scope === 'platform' ? '/platform/dashboard' : '/tenant/dashboard'
    }
    return true
  }

  if (!auth.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (!auth.loaded) {
    await auth.fetchMe()
  }

  if (to.meta.scope && to.meta.scope !== auth.scope) {
    return auth.scope === 'platform' ? '/platform/dashboard' : '/tenant/dashboard'
  }

  if (to.meta.perm && !auth.hasPerm(to.meta.perm)) {
    return '/403'
  }

  const groupTitle = to.meta.group ? GROUP_META[to.meta.group]?.title : ''
  document.title = to.meta.title ? `${to.meta.title} · 全达物流` : '全达物流 · 管理后台'
  return true
})

router.afterEach((to) => {
  if (!to.name || PUBLIC_PATHS.includes(to.path) || to.path.startsWith('/403')) return
  const tabs = useTabsStore()
  const title = (to.meta.title as string) || '页面'
  tabs.addTab({
    path: to.path,
    fullPath: to.fullPath,
    name: String(to.name),
    title,
    affix: !!to.meta.affix
  })
})

export default router
