import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: {
        title: '零担专线·整车包车·同城配送',
        description:
          '驰远物流官网：提供上海、杭州、苏州、南京、宁波等长三角城市的零担专线、整车包车与同城配送服务，在线询价 30 秒出明细，运单全程可查。',
        keywords: '上海物流公司,零担专线,整车运输,同城配送,运单查询,长三角物流,驰远物流'
      }
    },
    {
      path: '/quote',
      name: 'quote',
      component: () => import('../views/QuoteView.vue'),
      meta: {
        title: '在线询价与报价明细',
        description:
          '输入出发地、目的地与货物信息，立即获取基础运费、燃油附加、装卸、上楼、保险、代收手续费等明细报价，并可提交企业需求获取专属方案。',
        keywords: '物流在线报价,运费计算,零担价格,整车报价,物流询价'
      }
    },
    {
      path: '/track',
      name: 'track',
      component: () => import('../views/TrackView.vue'),
      meta: {
        title: '运单轨迹查询',
        description:
          '输入运单号或收发货人手机号后四位，实时查询驰远物流运单状态、全程节点、司机车辆与预计到达时间。',
        keywords: '运单查询,物流轨迹,快递查询,货运跟踪'
      }
    },
    {
      path: '/services',
      name: 'services',
      component: () => import('../views/ServicesView.vue'),
      meta: {
        title: '服务矩阵与计费标准',
        description:
          '零担专线、整车包车、同城配送、仓储装卸四大服务，公开首重续重、体积重换算与附加费口径，价格透明可核。',
        keywords: '零担专线,整车包车,同城配送,物流价格表,计费标准'
      }
    },
    {
      path: '/sites',
      name: 'sites',
      component: () => import('../views/SitesView.vue'),
      meta: {
        title: '网点查询与地址电话',
        description:
          '查看驰远物流长三角网点分布、详细地址、联系电话、营业时间与服务范围，支持地图与列表切换、一键拨号。',
        keywords: '物流网点,上海物流网点,杭州物流网点,网点查询'
      }
    },
    {
      path: '/monthly',
      name: 'monthly',
      component: () => import('../views/MonthlyView.vue'),
      meta: {
        title: '企业月结申请',
        description:
          '申请驰远物流企业月结：专属协议价、最长 45 天账期、月度对账与在线开票。线上提交资料，客户经理 1 个工作日内对接。',
        keywords: '物流月结,企业月结,运费账期,物流协议价'
      }
    },
    {
      path: '/news',
      name: 'news',
      component: () => import('../views/NewsListView.vue'),
      meta: {
        title: '资讯中心与物流百科',
        description:
          '驰远物流公司动态、服务公告与物流百科，了解旺季保障、月结流程、包装与运输知识。',
        keywords: '物流资讯,物流百科,公司公告'
      }
    },
    {
      path: '/news/:id',
      name: 'news-detail',
      component: () => import('../views/NewsDetailView.vue'),
      meta: {
        title: '资讯详情',
        description: '驰远物流资讯详情，分享公司动态、服务公告与物流行业知识。',
        keywords: '物流资讯,公司动态,服务公告'
      }
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/StaticPageView.vue'),
      props: { page: 'about' },
      meta: {
        title: '关于我们',
        description:
          '驰远物流成立于 2012 年，专注长三角合同物流与零担专线，拥有 8 个分拨网点、300 余台运力与全程可视化系统。',
        keywords: '驰远物流,物流公司简介,上海物流企业'
      }
    },
    {
      path: '/contact',
      name: 'contact',
      component: () => import('../views/StaticPageView.vue'),
      props: { page: 'contact' },
      meta: {
        title: '联系我们',
        description:
          '驰远物流客服热线 400-880-1688，上海总部地址、商务合作邮箱与各网点联系方式。',
        keywords: '物流客服电话,驰远物流联系方式'
      }
    },
    {
      path: '/join',
      name: 'join',
      component: () => import('../views/StaticPageView.vue'),
      props: { page: 'join' },
      meta: {
        title: '加入我们',
        description:
          '驰远物流招聘：客户经理、调度专员、司机、运营管培生等岗位，加入我们共建高效供应链。',
        keywords: '物流招聘,上海物流公司招聘,司机招聘'
      }
    },
    {
      path: '/privacy',
      name: 'privacy',
      component: () => import('../views/StaticPageView.vue'),
      props: { page: 'privacy' },
      meta: {
        title: '隐私政策',
        description: '驰远物流隐私政策：说明个人信息的收集、使用、存储与保护方式，以及您的权利。',
        keywords: '隐私政策,个人信息保护'
      }
    },
    {
      path: '/terms',
      name: 'terms',
      component: () => import('../views/StaticPageView.vue'),
      props: { page: 'terms' },
      meta: {
        title: '服务条款',
        description: '驰远物流服务条款：运输服务范围、下单与承运、费用结算、货物保险、责任限制与争议解决。',
        keywords: '服务条款,运输协议'
      }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/NotFoundView.vue'),
      meta: {
        title: '页面不存在',
        description: '您访问的页面不存在，请返回驰远物流首页或使用在线询价、运单查询功能。'
      }
    }
  ],
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0 }
  }
})

export default router
