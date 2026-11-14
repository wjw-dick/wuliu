import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '../store/auth'
import AdminLayout from '../layout/AdminLayout.vue'

const Login = () => import('../pages/Login.vue')
const NotFound = () => import('../pages/NotFound.vue')

// 平台端菜单（scope=platform）
const platformRoutes = [
  { path: '/platform/dashboard', name: 'p-dashboard', component: () => import('../pages/platform/Dashboard.vue'), meta: { title: '平台工作台', icon: 'DataBoard' } },
  { path: '/platform/tenants', name: 'p-tenants', component: () => import('../pages/platform/Tenants.vue'), meta: { title: '租户管理', icon: 'OfficeBuilding' } },
  { path: '/platform/licenses', name: 'p-licenses', component: () => import('../pages/platform/Licenses.vue'), meta: { title: '资质审核', icon: 'Stamp' } },
  { path: '/platform/plans', name: 'p-plans', component: () => import('../pages/CrudPage.vue'), props: { configKey: 'plans' }, meta: { title: '套餐管理', icon: 'Goods' } },
  { path: '/platform/bills', name: 'p-bills', component: () => import('../pages/platform/Bills.vue'), meta: { title: '账单与收款', icon: 'Money' } },
  { path: '/platform/users', name: 'p-users', component: () => import('../pages/CrudPage.vue'), props: { configKey: 'platformUsers' }, meta: { title: '平台账号', icon: 'User' } },
  { path: '/platform/audits', name: 'p-audits', component: () => import('../pages/platform/Audits.vue'), meta: { title: '审计日志', icon: 'Document' } },
  { path: '/platform/announcements', name: 'p-announcements', component: () => import('../pages/CrudPage.vue'), props: { configKey: 'platformAnnouncements' }, meta: { title: '平台公告', icon: 'Bell' } },
  { path: '/platform/dicts', name: 'p-dicts', component: () => import('../pages/Dicts.vue'), meta: { title: '平台字典', icon: 'Collection' } },
  { path: '/platform/configs', name: 'p-configs', component: () => import('../pages/SystemConfig.vue'), props: { platform: true }, meta: { title: '平台参数', icon: 'Setting' } }
]

// 租户端菜单（scope=tenant）
const tenantRoutes = [
  { path: '/tenant/dashboard', name: 't-dashboard', component: () => import('../pages/tenant/Dashboard.vue'), meta: { title: '工作台', icon: 'DataBoard' } },
  { path: '/tenant/customers', name: 't-customers', component: () => import('../pages/tenant/Customers.vue'), meta: { title: '客户与协议', icon: 'UserFilled' } },
  { path: '/tenant/quotes', name: 't-quotes', component: () => import('../pages/tenant/Quotes.vue'), meta: { title: '询价与报价', icon: 'PriceTag' } },
  { path: '/tenant/waybills', name: 't-waybills', component: () => import('../pages/tenant/Waybills.vue'), meta: { title: '运单管理', icon: 'Document' } },
  { path: '/tenant/waybill/:id', name: 't-waybill-detail', component: () => import('../pages/tenant/WaybillDetail.vue'), meta: { title: '运单详情', hidden: true } },
  { path: '/tenant/board', name: 't-board', component: () => import('../pages/tenant/Board.vue'), meta: { title: '调度看板', icon: 'MapLocation' } },
  { path: '/tenant/vehicles', name: 't-vehicles', component: () => import('../pages/CrudPage.vue'), props: { configKey: 'vehicles' }, meta: { title: '车辆档案', icon: 'Van' } },
  { path: '/tenant/drivers', name: 't-drivers', component: () => import('../pages/CrudPage.vue'), props: { configKey: 'drivers' }, meta: { title: '司机档案', icon: 'User' } },
  { path: '/tenant/carriers', name: 't-carriers', component: () => import('../pages/CrudPage.vue'), props: { configKey: 'carriers' }, meta: { title: '承运商档案', icon: 'OfficeBuilding' } },
  { path: '/tenant/qualifications', name: 't-qualifications', component: () => import('../pages/tenant/Qualifications.vue'), meta: { title: '资质到期', icon: 'AlarmClock' } },
  { path: '/tenant/tracks', name: 't-tracks', component: () => import('../pages/tenant/Tracks.vue'), meta: { title: '在途跟踪', icon: 'Guide' } },
  { path: '/tenant/exceptions', name: 't-exceptions', component: () => import('../pages/tenant/Exceptions.vue'), meta: { title: '异常处理', icon: 'Warning' } },
  { path: '/tenant/receipts', name: 't-receipts', component: () => import('../pages/tenant/Receipts.vue'), meta: { title: '签收与回单', icon: 'Checked' } },
  { path: '/tenant/receivables', name: 't-receivables', component: () => import('../pages/CrudPage.vue'), props: { configKey: 'receivables' }, meta: { title: '应收结算', icon: 'Wallet' } },
  { path: '/tenant/payables', name: 't-payables', component: () => import('../pages/CrudPage.vue'), props: { configKey: 'payables' }, meta: { title: '应付结算', icon: 'Wallet' } },
  { path: '/tenant/statements', name: 't-statements', component: () => import('../pages/tenant/Statements.vue'), meta: { title: '对账单', icon: 'Tickets' } },
  { path: '/tenant/statement/:id', name: 't-statement-detail', component: () => import('../pages/tenant/StatementDetail.vue'), meta: { title: '对账详情', hidden: true } },
  { path: '/tenant/invoices', name: 't-invoices', component: () => import('../pages/CrudPage.vue'), props: { configKey: 'invoices' }, meta: { title: '发票管理', icon: 'Notebook' } },
  { path: '/tenant/payments', name: 't-payments', component: () => import('../pages/CrudPage.vue'), props: { configKey: 'payments' }, meta: { title: '收款登记', icon: 'Coin' } },
  { path: '/tenant/aging', name: 't-aging', component: () => import('../pages/tenant/Aging.vue'), meta: { title: '账龄催收', icon: 'TrendCharts' } },
  { path: '/tenant/cod', name: 't-cod', component: () => import('../pages/tenant/Cod.vue'), meta: { title: '代收货款', icon: 'Money' } },
  { path: '/tenant/claims', name: 't-claims', component: () => import('../pages/tenant/Claims.vue'), meta: { title: '理赔售后', icon: 'Umbrella' } },
  { path: '/tenant/reports', name: 't-reports', component: () => import('../pages/tenant/Reports.vue'), meta: { title: '报表中心', icon: 'PieChart' } },
  { path: '/tenant/orgs', name: 't-orgs', component: () => import('../pages/CrudPage.vue'), props: { configKey: 'orgs' }, meta: { title: '网点与组织', icon: 'Share' } },
  { path: '/tenant/users', name: 't-users', component: () => import('../pages/CrudPage.vue'), props: { configKey: 'tenantUsers' }, meta: { title: '账号管理', icon: 'User' } },
  { path: '/tenant/roles', name: 't-roles', component: () => import('../pages/CrudPage.vue'), props: { configKey: 'tenantRoles' }, meta: { title: '角色权限', icon: 'Lock' } },
  { path: '/tenant/system/config', name: 't-config', component: () => import('../pages/SystemConfig.vue'), meta: { title: '系统参数', icon: 'Setting' } },
  { path: '/tenant/system/dicts', name: 't-dicts', component: () => import('../pages/Dicts.vue'), meta: { title: '数据字典', icon: 'Collection' } },
  { path: '/tenant/system/menus', name: 't-menus', component: () => import('../pages/tenant/Menus.vue'), meta: { title: '菜单可视化', icon: 'Menu' } },
  { path: '/tenant/system/messages', name: 't-messages', component: () => import('../pages/tenant/Messages.vue'), meta: { title: '消息配置', icon: 'ChatDotRound' } },
  { path: '/tenant/system/sso', name: 't-sso', component: () => import('../pages/tenant/Sso.vue'), meta: { title: 'SSO 配置', icon: 'Link' } },
  { path: '/tenant/system/storage', name: 't-storage', component: () => import('../pages/tenant/Storage.vue'), meta: { title: '对象存储', icon: 'FolderOpened' } },
  { path: '/tenant/system/wechat', name: 't-wechat', component: () => import('../pages/tenant/Wechat.vue'), meta: { title: '公众号账号', icon: 'ChatLineRound' } },
  { path: '/tenant/system/i18n', name: 't-i18n', component: () => import('../pages/CrudPage.vue'), props: { configKey: 'i18n' }, meta: { title: '多语言', icon: 'Connection' } },
  { path: '/tenant/cms', name: 't-cms', component: () => import('../pages/tenant/Cms.vue'), meta: { title: '内容 CMS', icon: 'Picture' } },
  { path: '/tenant/announcements', name: 't-announcements', component: () => import('../pages/CrudPage.vue'), props: { configKey: 'tenantAnnouncements' }, meta: { title: '租户公告', icon: 'Bell' } }
]

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login, meta: { title: '登录' } },
  {
    path: '/platform',
    component: AdminLayout,
    children: platformRoutes
  },
  {
    path: '/tenant',
    component: AdminLayout,
    children: tenantRoutes
  },
  { path: '/:pathMatch(.*)*', component: NotFound }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach(async (to) => {
  const auth = useAuth()
  if (to.path === '/login') return true
  if (!auth.isLogin) return { path: '/login' }
  const targetScope = to.path.startsWith('/platform') ? 'platform' : 'tenant'
  if (!auth.scope) await auth.refreshMe()
  if (auth.scope && auth.scope !== targetScope) {
    return { path: `/${auth.scope}/dashboard` }
  }
  return true
})

export default router
export { platformRoutes, tenantRoutes }
