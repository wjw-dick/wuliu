import type { RouteRecordRaw } from 'vue-router'

const Layout = () => import('@/layout/index.vue')

export const layoutRoutes: RouteRecordRaw[] = [
  /* ================= 平台运营端 ================= */
  {
    path: '/platform/dashboard',
    name: 'PlatformDashboard',
    component: () => import('@/views/platform/PlatformDashboard.vue'),
    meta: { title: '工作台', icon: 'Odometer', group: 'plat-overview', scope: 'platform', perm: 'platform:dashboard', affix: true, order: 1 }
  },
  {
    path: '/platform/tenants',
    name: 'Tenants',
    component: () => import('@/views/platform/Tenants.vue'),
    meta: { title: '租户管理', icon: 'OfficeBuilding', group: 'plat-tenant', scope: 'platform', perm: 'tenant:list', order: 1 }
  },
  {
    path: '/platform/licenses',
    name: 'Licenses',
    component: () => import('@/views/platform/Licenses.vue'),
    meta: { title: '资质审核', icon: 'DocumentChecked', group: 'plat-tenant', scope: 'platform', perm: 'license:list', order: 2 }
  },
  {
    path: '/platform/plans',
    name: 'Plans',
    component: () => import('@/views/platform/Plans.vue'),
    meta: { title: '套餐管理', icon: 'Tickets', group: 'plat-commerce', scope: 'platform', perm: 'plan:list', order: 1 }
  },
  {
    path: '/platform/bills',
    name: 'Bills',
    component: () => import('@/views/platform/Bills.vue'),
    meta: { title: '账单收款', icon: 'Wallet', group: 'plat-commerce', scope: 'platform', perm: 'bill:list', order: 2 }
  },
  {
    path: '/platform/users',
    name: 'PlatformUsers',
    component: () => import('@/views/platform/PlatformUsers.vue'),
    meta: { title: '平台账号', icon: 'User', group: 'plat-system', scope: 'platform', perm: 'platform-user:list', order: 1 }
  },
  {
    path: '/platform/roles',
    name: 'PlatformRoles',
    component: () => import('@/views/platform/PlatformRoles.vue'),
    meta: { title: '平台角色', icon: 'Lock', group: 'plat-system', scope: 'platform', perm: 'platform-role:list', order: 2 }
  },
  {
    path: '/platform/audits',
    name: 'Audits',
    component: () => import('@/views/platform/Audits.vue'),
    meta: { title: '审计日志', icon: 'List', group: 'plat-system', scope: 'platform', perm: 'audit:list', order: 3 }
  },
  {
    path: '/platform/announcements',
    name: 'PlatformAnnouncements',
    component: () => import('@/views/platform/PlatformAnnouncements.vue'),
    meta: { title: '平台公告', icon: 'Bell', group: 'plat-system', scope: 'platform', perm: 'platform-announcement:list', order: 4 }
  },
  {
    path: '/platform/dicts',
    name: 'PlatformDicts',
    component: () => import('@/views/platform/PlatformDicts.vue'),
    meta: { title: '平台字典', icon: 'Collection', group: 'plat-system', scope: 'platform', perm: 'platform-dict:list', order: 5 }
  },

  /* ================= 租户后台 ================= */
  {
    path: '/tenant/dashboard',
    name: 'TenantDashboard',
    component: () => import('@/views/tenant/TenantDashboard.vue'),
    meta: { title: '工作台', icon: 'Odometer', group: 'tenant-overview', scope: 'tenant', perm: 'dashboard:view', affix: true, order: 1 }
  },
  {
    path: '/tenant/customers',
    name: 'Customers',
    component: () => import('@/views/tenant/Customers.vue'),
    meta: { title: '客户档案', icon: 'User', group: 'tenant-customer', scope: 'tenant', perm: 'customer:list', order: 1 }
  },
  {
    path: '/tenant/agreements',
    name: 'Agreements',
    component: () => import('@/views/tenant/Agreements.vue'),
    meta: { title: '协议价', icon: 'Document', group: 'tenant-customer', scope: 'tenant', perm: 'agreement:list', order: 2 }
  },
  {
    path: '/tenant/leads',
    name: 'Leads',
    component: () => import('@/views/tenant/Leads.vue'),
    meta: { title: '线索管理', icon: 'Promotion', group: 'tenant-customer', scope: 'tenant', perm: 'lead:list', order: 3 }
  },
  {
    path: '/tenant/quotes',
    name: 'Quotes',
    component: () => import('@/views/tenant/Quotes.vue'),
    meta: { title: '报价单', icon: 'PriceTag', group: 'tenant-business', scope: 'tenant', perm: 'quote:list', order: 1 }
  },
  {
    path: '/tenant/waybills',
    name: 'Waybills',
    component: () => import('@/views/tenant/Waybills.vue'),
    meta: { title: '运单管理', icon: 'Tickets', group: 'tenant-business', scope: 'tenant', perm: 'waybill:list', order: 2 }
  },
  {
    path: '/tenant/waybills/create',
    name: 'WaybillCreate',
    component: () => import('@/views/tenant/WaybillCreate.vue'),
    meta: { title: '新建运单', group: 'tenant-business', scope: 'tenant', perm: 'waybill:create', hidden: true, activeMenu: '/tenant/waybills', parentTitle: '运单管理' }
  },
  {
    path: '/tenant/waybills/:id',
    name: 'WaybillDetail',
    component: () => import('@/views/tenant/WaybillDetail.vue'),
    meta: { title: '运单详情', group: 'tenant-business', scope: 'tenant', perm: 'waybill:list', hidden: true, activeMenu: '/tenant/waybills', parentTitle: '运单管理' }
  },
  {
    path: '/tenant/dispatch/board',
    name: 'DispatchBoard',
    component: () => import('@/views/tenant/DispatchBoard.vue'),
    meta: { title: '调度看板', icon: 'Van', group: 'tenant-business', scope: 'tenant', perm: 'dispatch:board', order: 3 }
  },
  {
    path: '/tenant/vehicles',
    name: 'Vehicles',
    component: () => import('@/views/tenant/Vehicles.vue'),
    meta: { title: '车辆管理', icon: 'Van', group: 'tenant-capacity', scope: 'tenant', perm: 'vehicle:list', order: 1 }
  },
  {
    path: '/tenant/drivers',
    name: 'Drivers',
    component: () => import('@/views/tenant/Drivers.vue'),
    meta: { title: '司机管理', icon: 'Avatar', group: 'tenant-capacity', scope: 'tenant', perm: 'driver:list', order: 2 }
  },
  {
    path: '/tenant/carriers',
    name: 'Carriers',
    component: () => import('@/views/tenant/Carriers.vue'),
    meta: { title: '承运商', icon: 'OfficeBuilding', group: 'tenant-capacity', scope: 'tenant', perm: 'carrier:list', order: 3 }
  },
  {
    path: '/tenant/qualifications',
    name: 'Qualifications',
    component: () => import('@/views/tenant/Qualifications.vue'),
    meta: { title: '资质到期', icon: 'Warning', group: 'tenant-capacity', scope: 'tenant', perm: 'qualification:list', order: 4 }
  },
  {
    path: '/tenant/tracks',
    name: 'Tracks',
    component: () => import('@/views/tenant/Tracks.vue'),
    meta: { title: '轨迹跟踪', icon: 'MapLocation', group: 'tenant-transit', scope: 'tenant', perm: 'track:list', order: 1 }
  },
  {
    path: '/tenant/exceptions',
    name: 'Exceptions',
    component: () => import('@/views/tenant/Exceptions.vue'),
    meta: { title: '异常处理', icon: 'Warning', group: 'tenant-transit', scope: 'tenant', perm: 'exception:list', order: 2 }
  },
  {
    path: '/tenant/receipts',
    name: 'Receipts',
    component: () => import('@/views/tenant/Receipts.vue'),
    meta: { title: '签收回单', icon: 'DocumentChecked', group: 'tenant-transit', scope: 'tenant', perm: 'receipt:list', order: 3 }
  },
  {
    path: '/tenant/returns',
    name: 'Returns',
    component: () => import('@/views/tenant/Returns.vue'),
    meta: { title: '退回任务', icon: 'RefreshLeft', group: 'tenant-transit', scope: 'tenant', perm: 'return:list', order: 4 }
  },
  {
    path: '/tenant/receivables',
    name: 'Receivables',
    component: () => import('@/views/tenant/Receivables.vue'),
    meta: { title: '应收管理', icon: 'Money', group: 'tenant-finance', scope: 'tenant', perm: 'receivable:list', order: 1 }
  },
  {
    path: '/tenant/payables',
    name: 'Payables',
    component: () => import('@/views/tenant/Payables.vue'),
    meta: { title: '应付管理', icon: 'Wallet', group: 'tenant-finance', scope: 'tenant', perm: 'payable:list', order: 2 }
  },
  {
    path: '/tenant/statements',
    name: 'Statements',
    component: () => import('@/views/tenant/Statements.vue'),
    meta: { title: '对账单', icon: 'Document', group: 'tenant-finance', scope: 'tenant', perm: 'statement:list', order: 3 }
  },
  {
    path: '/tenant/statements/:id',
    name: 'StatementDetail',
    component: () => import('@/views/tenant/StatementDetail.vue'),
    meta: { title: '对账明细', group: 'tenant-finance', scope: 'tenant', perm: 'statement:list', hidden: true, activeMenu: '/tenant/statements', parentTitle: '对账单' }
  },
  {
    path: '/tenant/invoices',
    name: 'Invoices',
    component: () => import('@/views/tenant/Invoices.vue'),
    meta: { title: '发票管理', icon: 'Tickets', group: 'tenant-finance', scope: 'tenant', perm: 'invoice:list', order: 4 }
  },
  {
    path: '/tenant/payments',
    name: 'Payments',
    component: () => import('@/views/tenant/Payments.vue'),
    meta: { title: '收款登记', icon: 'CreditCard', group: 'tenant-finance', scope: 'tenant', perm: 'payment:list', order: 5 }
  },
  {
    path: '/tenant/aging',
    name: 'Aging',
    component: () => import('@/views/tenant/Aging.vue'),
    meta: { title: '账龄分析', icon: 'TrendCharts', group: 'tenant-finance', scope: 'tenant', perm: 'aging:view', order: 6 }
  },
  {
    path: '/tenant/cod',
    name: 'Cod',
    component: () => import('@/views/tenant/Cod.vue'),
    meta: { title: '代收货款', icon: 'Coin', group: 'tenant-finance', scope: 'tenant', perm: 'cod:list', order: 7 }
  },
  {
    path: '/tenant/claims',
    name: 'Claims',
    component: () => import('@/views/tenant/Claims.vue'),
    meta: { title: '理赔管理', icon: 'FirstAidKit', group: 'tenant-service', scope: 'tenant', perm: 'claim:list', order: 1 }
  },
  {
    path: '/tenant/claims/:id',
    name: 'ClaimDetail',
    component: () => import('@/views/tenant/ClaimDetail.vue'),
    meta: { title: '理赔详情', group: 'tenant-service', scope: 'tenant', perm: 'claim:list', hidden: true, activeMenu: '/tenant/claims', parentTitle: '理赔管理' }
  },
  {
    path: '/tenant/reports',
    name: 'Reports',
    component: () => import('@/views/tenant/Reports.vue'),
    meta: { title: '报表中心', icon: 'DataAnalysis', group: 'tenant-service', scope: 'tenant', perm: 'report:view', order: 2 }
  },
  {
    path: '/tenant/orgs',
    name: 'Orgs',
    component: () => import('@/views/tenant/Orgs.vue'),
    meta: { title: '网点组织', icon: 'OfficeBuilding', group: 'tenant-org', scope: 'tenant', perm: 'org:list', order: 1 }
  },
  {
    path: '/tenant/users',
    name: 'TenantUsers',
    component: () => import('@/views/tenant/TenantUsers.vue'),
    meta: { title: '员工账号', icon: 'User', group: 'tenant-org', scope: 'tenant', perm: 'user:list', order: 2 }
  },
  {
    path: '/tenant/roles',
    name: 'TenantRoles',
    component: () => import('@/views/tenant/TenantRoles.vue'),
    meta: { title: '角色权限', icon: 'Lock', group: 'tenant-org', scope: 'tenant', perm: 'role:list', order: 3 }
  },
  {
    path: '/tenant/system/config',
    name: 'SystemConfig',
    component: () => import('@/views/tenant/SystemConfig.vue'),
    meta: { title: '系统参数', icon: 'Setting', group: 'tenant-settings', scope: 'tenant', perm: 'config:view', order: 1 }
  },
  {
    path: '/tenant/system/dicts',
    name: 'SystemDicts',
    component: () => import('@/views/tenant/SystemDicts.vue'),
    meta: { title: '数据字典', icon: 'Collection', group: 'tenant-settings', scope: 'tenant', perm: 'dict:list', order: 2 }
  },
  {
    path: '/tenant/system/menus',
    name: 'SystemMenus',
    component: () => import('@/views/tenant/SystemMenus.vue'),
    meta: { title: '菜单配置', icon: 'Menu', group: 'tenant-settings', scope: 'tenant', perm: 'menu:manage', order: 3 }
  },
  {
    path: '/tenant/system/messages',
    name: 'SystemMessages',
    component: () => import('@/views/tenant/SystemMessages.vue'),
    meta: { title: '消息配置', icon: 'ChatDotRound', group: 'tenant-settings', scope: 'tenant', perm: 'message:config', order: 4 }
  },
  {
    path: '/tenant/system/sso',
    name: 'SystemSso',
    component: () => import('@/views/tenant/SystemSso.vue'),
    meta: { title: 'SSO 配置', icon: 'Key', group: 'tenant-settings', scope: 'tenant', perm: 'sso:config', order: 5 }
  },
  {
    path: '/tenant/system/storage',
    name: 'SystemStorage',
    component: () => import('@/views/tenant/SystemStorage.vue'),
    meta: { title: '对象存储', icon: 'UploadFilled', group: 'tenant-settings', scope: 'tenant', perm: 'storage:config', order: 6 }
  },
  {
    path: '/tenant/system/wechat',
    name: 'SystemWechat',
    component: () => import('@/views/tenant/SystemWechat.vue'),
    meta: { title: '微信账号', icon: 'ChatLineSquare', group: 'tenant-settings', scope: 'tenant', perm: 'wechat:config', order: 7 }
  },
  {
    path: '/tenant/system/i18n',
    name: 'SystemI18n',
    component: () => import('@/views/tenant/SystemI18n.vue'),
    meta: { title: '多语言', icon: 'Guide', group: 'tenant-settings', scope: 'tenant', perm: 'i18n:list', order: 8 }
  },
  {
    path: '/tenant/cms',
    name: 'Cms',
    component: () => import('@/views/tenant/Cms.vue'),
    meta: { title: 'CMS 内容', icon: 'Document', group: 'tenant-content', scope: 'tenant', perm: 'cms:list', order: 1 }
  },
  {
    path: '/tenant/announcements',
    name: 'TenantAnnouncements',
    component: () => import('@/views/tenant/TenantAnnouncements.vue'),
    meta: { title: '公告中心', icon: 'Bell', group: 'tenant-content', scope: 'tenant', perm: 'announcement:list', order: 2 }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '个人中心', hidden: true }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/Forbidden.vue'),
    meta: { title: '无权限', hidden: true }
  }
]

export const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    children: layoutRoutes
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在', hidden: true }
  }
]
