import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', component: () => import('../pages/Home.vue'), meta: { title: '首页' } },
  { path: '/quote', component: () => import('../pages/Quote.vue'), meta: { title: '询价' } },
  { path: '/track', component: () => import('../pages/Track.vue'), meta: { title: '跟踪' } },
  { path: '/receipt/:waybillNo', component: () => import('../pages/Receipt.vue'), meta: { title: '电子回单' } }
]

const router = createRouter({ history: createWebHistory(), routes })
router.afterEach((to) => { document.title = `${to.meta.title || ''} · 示例物流` })
export default router
