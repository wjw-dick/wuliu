import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', component: () => import('../pages/Home.vue'), meta: { title: '首页' } },
  { path: '/quote', component: () => import('../pages/Quote.vue'), meta: { title: '在线询价' } },
  { path: '/track', component: () => import('../pages/Track.vue'), meta: { title: '运单跟踪' } },
  { path: '/services', component: () => import('../pages/Services.vue'), meta: { title: '服务网络' } },
  { path: '/about', component: () => import('../pages/About.vue'), meta: { title: '关于我们' } }
]

const router = createRouter({ history: createWebHistory(), routes })

router.afterEach((to) => { document.title = `${to.meta.title || ''} · 示例物流` })
export default router
