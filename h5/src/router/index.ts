import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/quote-share'
    },
    {
      path: '/quote-share',
      name: 'quote-share',
      component: () => import('../views/QuoteShareView.vue'),
      meta: { title: '报价分享' }
    },
    {
      path: '/receipt/:waybillNo',
      name: 'receipt',
      component: () => import('../views/ReceiptView.vue'),
      meta: { title: '电子回单' }
    },
    {
      path: '/driver',
      name: 'driver',
      component: () => import('../views/DriverView.vue'),
      meta: { title: '司机免安装端' }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/NotFoundView.vue'),
      meta: { title: '页面不存在' }
    }
  ],
  scrollBehavior(_to, _from, savedPosition) {
    return savedPosition || { top: 0 }
  }
})

router.afterEach((to) => {
  const title = (to.meta.title as string) || '移动服务'
  document.title = `${title} · 驰远物流`
})

export default router
