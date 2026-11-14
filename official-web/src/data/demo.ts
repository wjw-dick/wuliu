import type {
  ContentItem,
  PriceGuide,
  QuoteInput,
  QuoteResult,
  Site,
  TrackNode,
  TrackResult
} from '../types'

export const DEMO_SITES: Site[] = [
  {
    id: 1,
    name: '驰远物流·上海总部',
    city: '上海',
    address: '上海市青浦区华新镇华隆路 1777 号驰远物流园',
    phone: '021-5988-1688',
    businessHours: '周一至周日 07:00-22:00',
    serviceArea: '上海市全境、长三角次日达',
    lat: 31.2304,
    lng: 121.4737,
    type: '总部/分拨中心'
  },
  {
    id: 2,
    name: '驰远物流·嘉定网点',
    city: '上海',
    address: '上海市嘉定区兴荣路 1288 号 A3 库',
    phone: '021-5992-3301',
    businessHours: '周一至周六 08:00-20:00',
    serviceArea: '嘉定、安亭、外冈、昆山花桥',
    lat: 31.375,
    lng: 121.265,
    type: '营业网点'
  },
  {
    id: 3,
    name: '驰远物流·杭州分拨中心',
    city: '杭州',
    address: '杭州市余杭区仁和街道东风路 66 号',
    phone: '0571-8866-2200',
    businessHours: '周一至周日 06:30-23:00',
    serviceArea: '杭州全域、绍兴、湖州、嘉兴',
    lat: 30.418,
    lng: 120.298,
    type: '分拨中心'
  },
  {
    id: 4,
    name: '驰远物流·苏州网点',
    city: '苏州',
    address: '苏州市工业园区唯亭镇亭荣街 18 号',
    phone: '0512-6288-7710',
    businessHours: '周一至周六 08:00-20:00',
    serviceArea: '苏州工业园区、昆山、太仓、常熟',
    lat: 31.2989,
    lng: 120.5853,
    type: '营业网点'
  },
  {
    id: 5,
    name: '驰远物流·南京网点',
    city: '南京',
    address: '南京市江宁区秣陵街道天元中路 128 号',
    phone: '025-5228-6600',
    businessHours: '周一至周六 08:00-20:00',
    serviceArea: '南京全域、镇江、扬州、马鞍山',
    lat: 31.8206,
    lng: 118.7969,
    type: '营业网点'
  },
  {
    id: 6,
    name: '驰远物流·宁波港网点',
    city: '宁波',
    address: '宁波市北仑区新碶街道明州路 258 号',
    phone: '0574-8688-9900',
    businessHours: '周一至周日 07:00-21:00',
    serviceArea: '宁波、舟山、台州及港口集疏运',
    lat: 29.8683,
    lng: 121.544,
    type: '港口网点'
  },
  {
    id: 7,
    name: '驰远物流·合肥网点',
    city: '合肥',
    address: '合肥市经开区锦绣大道 3155 号',
    phone: '0551-6588-3300',
    businessHours: '周一至周六 08:00-19:00',
    serviceArea: '合肥、芜湖、滁州、六安',
    lat: 31.8206,
    lng: 117.2272,
    type: '营业网点'
  },
  {
    id: 8,
    name: '驰远物流·无锡网点',
    city: '无锡',
    address: '无锡市新吴区硕放街道裕安路 88 号',
    phone: '0510-8528-1177',
    businessHours: '周一至周六 08:00-20:00',
    serviceArea: '无锡全域、江阴、宜兴',
    lat: 31.4912,
    lng: 120.3119,
    type: '营业网点'
  }
]

const DEMO_ROUTE_KM: Record<string, number> = {
  '上海-杭州': 180,
  '杭州-上海': 180,
  '上海-苏州': 105,
  '苏州-上海': 105,
  '上海-南京': 300,
  '南京-上海': 300,
  '上海-宁波': 225,
  '宁波-上海': 225,
  '杭州-南京': 280,
  '南京-杭州': 280,
  '上海-合肥': 470,
  '合肥-上海': 470,
  '苏州-杭州': 160,
  '杭州-苏州': 160
}

function hash(text: string): number {
  let value = 0
  for (let i = 0; i < text.length; i += 1) {
    value = (value * 31 + text.charCodeAt(i)) % 100000
  }
  return value
}

function routeKm(origin: string, destination: string): number {
  const key = `${origin.trim()}-${destination.trim()}`
  if (DEMO_ROUTE_KM[key]) return DEMO_ROUTE_KM[key]
  return 260 + (hash(key) % 700)
}

function money(value: number): number {
  return Math.round(value * 100) / 100
}

export function demoQuote(input: QuoteInput): QuoteResult {
  const km = routeKm(input.origin, input.destination)
  const weight = Math.max(1, Number(input.weight) || 1)
  const firstWeight = 100
  const baseFreight = money(120 + km * 1.1 + Math.max(0, weight - firstWeight) * 0.85)
  const fuelSurcharge = money(baseFreight * 0.1)
  const loadingFee = input.needLoading ? 50 : 0
  const upstairsFee = input.needUpstairs ? 80 : 0
  const insuranceFee = input.value > 0 ? money(Math.max(10, input.value * 0.003)) : 0
  const codFee = input.codAmount > 0 ? money(input.codAmount * 0.005) : 0
  const remoteFee = 0
  const holidayFee = 0
  const totalAmount = money(
    baseFreight + fuelSurcharge + loadingFee + upstairsFee + insuranceFee + codFee + remoteFee + holidayFee
  )
  const seq = String((hash(`${input.origin}${input.destination}${weight}`) % 9000) + 1000)
  const today = new Date()
  const datePart = `${today.getFullYear()}${String(today.getMonth() + 1).padStart(2, '0')}${String(
    today.getDate()
  ).padStart(2, '0')}`
  const detail = [
    {
      name: '基础运费',
      amount: baseFreight,
      formula: `首重 ${firstWeight}kg/120 元 + 续重 ${Math.max(0, weight - firstWeight)}kg × 0.85 元/kg + 里程 ${km}km × 1.1 元/km`
    },
    {
      name: '燃油附加',
      amount: fuelSurcharge,
      formula: '基础运费 × 10%（随油价月度浮动）'
    }
  ]
  if (loadingFee > 0) {
    detail.push({ name: '装卸费', amount: loadingFee, formula: '上门提货装卸 50 元/票' })
  }
  if (upstairsFee > 0) {
    detail.push({ name: '上楼费', amount: upstairsFee, formula: '无电梯楼层搬运 80 元/票' })
  }
  if (insuranceFee > 0) {
    detail.push({ name: '保险', amount: insuranceFee, formula: `声明价值 ${input.value} 元 × 0.3%，最低 10 元` })
  }
  if (codFee > 0) {
    detail.push({ name: '代收手续费', amount: codFee, formula: `代收金额 ${input.codAmount} 元 × 0.5%` })
  }
  detail.push({ name: '远程/节假日附加', amount: remoteFee + holidayFee, formula: '本线路暂不收取' })

  return {
    quoteNo: `BJ${datePart}${seq}`,
    baseFreight,
    fuelSurcharge,
    loadingFee,
    upstairsFee,
    insuranceFee,
    codFee,
    remoteFee,
    holidayFee,
    totalAmount,
    ruleVersion: 'V1（演示价格）',
    priceSource: '线路标准价',
    detail,
    validUntil: '报价有效期 24 小时，实际以客服确认为准'
  }
}

export function demoTrack(waybillNo: string, phone = ''): TrackResult {
  const no = waybillNo.trim() || 'YH0001202611140001'
  const tail = phone.replace(/\D/g, '').slice(-4) || '0001'
  const nodes: TrackNode[] = [
    {
      time: '2026-11-14 09:20:00',
      status: '运输中',
      description: '车辆已驶入杭州转运中心，即将安排末端配送',
      location: '杭州市余杭区'
    },
    {
      time: '2026-11-14 05:40:00',
      status: '运输中',
      description: '干线车辆离开上海分拨中心，全程高速直达',
      location: 'G60 沪昆高速'
    },
    {
      time: '2026-11-13 21:10:00',
      status: '已装车',
      description: '货物完成装车与封签，车牌 沪B·8F216',
      location: '上海青浦分拨中心'
    },
    {
      time: '2026-11-13 18:30:00',
      status: '已提货',
      description: '提货完成，货物外包装完好，共 10 件',
      location: '上海市嘉定区'
    },
    {
      time: '2026-11-13 18:02:00',
      status: '已受理',
      description: `运单创建成功，客户手机尾号 ${tail}`,
      location: '线上下单'
    }
  ]
  return {
    waybillNo: no,
    status: '运输中',
    statusName: '运输中',
    origin: '上海',
    destination: '杭州',
    cargoName: '机械配件',
    pieces: 10,
    weight: 500,
    volume: 2.5,
    senderName: '张经理',
    receiverName: '李经理',
    createdAt: '2026-11-13 18:02:00',
    estimatedArrival: '2026-11-14 18:00:00',
    driverName: '王师傅',
    vehicleNo: '沪B·8F216',
    nodes
  }
}

export const DEMO_PRICE_GUIDE: PriceGuide = {
  updatedAt: '2026-11-01',
  intro:
    '驰远物流坚持明码标价。以下为官网公开的计费口径，适用于零担专线、整车包车与同城配送标准产品；协议客户按合同价执行。',
  items: [
    {
      title: '零担专线计费',
      rule: '首重 100kg 内按起步价计费，超出部分按续重单价计费，同时叠加线路里程系数。轻抛货按体积重折算。',
      example: '上海 → 杭州，500kg 普货：120 元（首重）+ 400kg × 0.85 元 + 180km × 1.1 元 ≈ 658 元'
    },
    {
      title: '体积重换算',
      rule: '体积重（kg）= 长 × 宽 × 高（cm）÷ 6000。实际重量与体积重取大者计费。',
      example: '一票货物体积 2.5m³，体积重约 417kg；若实重 500kg，则按 500kg 计费'
    },
    {
      title: '整车包车',
      rule: '按车型 + 里程计价，含基础里程与等候时长。4.2 米 / 6.8 米 / 9.6 米 / 13 米车型可选。',
      example: '6.8 米厢式，上海 → 杭州 180km：起步 900 元 + 超里程 6 元/km'
    },
    {
      title: '同城配送',
      rule: '按车型与配送点位计价，3 公里内起步价，超出按公里计费；多点配送每增加一点加收 30 元。',
      example: '上海内环 12km，2 个点位：120 元 + 9km × 4 元 + 30 元 = 186 元'
    }
  ],
  surcharges: [
    { name: '燃油附加费', standard: '基础运费 × 8%~12%', remark: '随成品油价格每月调整，官网公示' },
    { name: '上门提货/装卸', standard: '50 元/票', remark: '需要叉车、吊装等特殊作业另行报价' },
    { name: '上楼费', standard: '80 元/票/层段', remark: '无电梯且单件超过 30kg 时收取' },
    { name: '货物保险', standard: '声明价值 × 0.3%', remark: '最低 10 元，最高赔付以保额为限' },
    { name: '代收货款手续费', standard: '代收金额 × 0.5%', remark: 'T+1 结算至指定账户' },
    { name: '远程/超区附加', standard: '50~200 元/票', remark: '乡镇、园区限行等特殊区域按实际报价' },
    { name: '节假日附加', standard: '基础运费 × 10%', remark: '法定节假日期间作业收取' }
  ],
  notes: [
    '以上价格为不含税参考价，开票税率按国家规定执行。',
    '危险品、冷链、大件设备等特殊货物需单独询价。',
    '报价有效期 24 小时，最终以客服确认及电子运单为准。',
    '企业月结客户可享专属协议价与账期，提交月结申请后由客户经理对接。'
  ]
}

export const DEMO_CONTENTS: ContentItem[] = [
  {
    id: 1,
    title: '驰远物流华东智能分拨中心正式启用，日均处理能力提升至 12 万票',
    summary:
      '11 月 8 日，位于上海青浦的驰远物流华东智能分拨中心正式投入运营，自动化分拣线、数字月台与全程可视化系统同步上线。',
    category: '公司动态',
    author: '品牌中心',
    publishedAt: '2026-11-08 10:30:00',
    views: 2381,
    content: `
      <p>11 月 8 日上午，驰远物流华东智能分拨中心启用仪式在上海青浦举行。该中心占地 4.2 万平方米，配置 6 条自动化分拣线、120 个数字月台与 8000 个智能库位，日均处理能力由 6 万票提升至 12 万票。</p>
      <h2>自动化与数字化双轮驱动</h2>
      <p>新分拨中心引入 DWS 体积重量采集、自动称重扫码与 AI 装载算法，货物从入场到装车平均耗时缩短 38%。同时，全部月台接入 TMS 调度系统，车辆到场后可自动分配泊位，减少排队等待。</p>
      <ul>
        <li>自动分拣线 6 条，分拣准确率 99.97%</li>
        <li>数字月台 120 个，车辆平均等待时间下降 45%</li>
        <li>全场 5G + 物联网温湿度监控，异常自动预警</li>
      </ul>
      <blockquote>“我们希望通过稳定的时效与透明的轨迹，让每一票货物的交付都可预期。”驰远物流运营负责人表示。</blockquote>
      <h2>服务承诺再升级</h2>
      <p>依托新中心，上海至杭州、苏州、宁波等核心线路将实现“当日提货、次日送达”，并新增夜间提货窗口，最晚可预约至 22:00。企业客户可通过官网或小程序实时查询运单轨迹与预计到达时间。</p>
      <p>未来三年，驰远物流计划在长三角、粤港澳与成渝地区持续投入智能仓配网络，为客户提供更确定的供应链服务。</p>
    `
  },
  {
    id: 2,
    title: '双十一旺季运输保障方案发布：新增 200 台运力，承诺不涨价',
    summary:
      '面对双十一旺季，驰远物流提前锁定 200 台备用运力，开通电商专线与夜间分拨，并承诺标准产品价格不上浮。',
    category: '服务公告',
    author: '运营中心',
    publishedAt: '2026-10-28 14:00:00',
    views: 1560,
    content: `
      <p>为保障双十一期间客户货物及时发出，驰远物流发布旺季运输保障方案，从运力、时效、客服三方面升级服务。</p>
      <h2>运力保障</h2>
      <p>公司已提前锁定 200 台备用车辆，其中 4.2 米厢式 80 台、6.8 米厢式 70 台、9.6 米厢式 50 台，重点投放上海、杭州、苏州、南京等电商仓集中区域。</p>
      <h2>时效保障</h2>
      <ul>
        <li>华东核心线路保持“今发明至”，截单时间延长至 21:00</li>
        <li>开通电商专线，优先配载、优先中转</li>
        <li>分拨中心实行两班制，夜间持续作业</li>
      </ul>
      <h2>价格承诺</h2>
      <p>旺季期间（11 月 1 日至 11 月 20 日），标准零担与整车产品价格不上浮，燃油附加费按官网公示标准执行。协议客户按合同约定价格结算。</p>
      <p>客户可通过 400-880-1688 客服热线或官网在线询价获取最新报价。</p>
    `
  },
  {
    id: 3,
    title: '企业月结怎么申请？一篇看懂账期、对账与开票流程',
    summary:
      '月结客户可享专属协议价、最长 45 天账期与月度对账单。本文梳理申请条件、所需资料及线上操作流程。',
    category: '物流百科',
    author: '客户成功部',
    publishedAt: '2026-10-16 09:20:00',
    views: 3204,
    content: `
      <p>对于发货频次稳定的企业客户，月结不仅能简化付款流程，还能获得更优的协议价格。下面是驰远物流企业月结的完整指南。</p>
      <h2>一、申请条件</h2>
      <ul>
        <li>持有有效营业执照，经营状态正常</li>
        <li>月均运费达到 5000 元以上（或承诺年度合作量）</li>
        <li>同意按合同约定账期结算，并提供开票资料</li>
      </ul>
      <h2>二、所需资料</h2>
      <p>营业执照复印件（加盖公章）、法定代表人身份证明、开票信息、常用线路与预估货量。线上提交后，客户经理将在 1 个工作日内联系。</p>
      <h2>三、账期与对账</h2>
      <p>标准账期为月结 30 天，优质客户可申请最长 45 天。每月 3 日前系统生成上月对账单，客户可在小程序或企业后台在线确认，如有异议可发起差异申诉。</p>
      <h2>四、开票流程</h2>
      <p>对账确认后即可申请开票，支持增值税专用发票与普通发票。电子发票通常在 2 个工作日内开具并推送至预留邮箱。</p>
      <p>如需申请，请访问官网“企业月结”页面提交资料，或致电 400-880-1688 转 2。</p>
    `
  },
  {
    id: 4,
    title: '驰远物流通过 ISO 9001 与 ISO 14001 双体系认证',
    summary:
      '公司质量管理与环境管理体系通过第三方审核，标志着服务标准化与绿色运营能力再上新台阶。',
    category: '公司动态',
    author: '品质管理部',
    publishedAt: '2026-09-30 16:10:00',
    views: 986,
    content: `
      <p>近日，驰远物流顺利通过 ISO 9001:2015 质量管理体系与 ISO 14001:2015 环境管理体系认证，认证范围覆盖道路货物运输、仓储与配送服务。</p>
      <h2>标准化服务流程</h2>
      <p>公司围绕提货、装卸、干线运输、末端配送与回单管理建立 36 项标准作业程序（SOP），并通过数字化系统固化关键节点，确保服务一致性。</p>
      <h2>绿色运营</h2>
      <p>通过线路优化、新能源车辆替换与包装循环利用，公司单位运输周转量碳排放较去年同期下降 11.6%。未来将继续扩大新能源车队规模。</p>
    `
  },
  {
    id: 5,
    title: '冬季雨雪天气货物运输注意事项',
    summary:
      '低温雨雪天气易导致道路结冰与延误。驰远物流提醒客户提前备货，并做好防潮、防冻包装。',
    category: '物流百科',
    author: '安全委员会',
    publishedAt: '2026-11-12 08:45:00',
    views: 742,
    content: `
      <p>进入冬季，雨雪、大雾天气增多，对公路运输时效与货物安全提出更高要求。为确保货物顺利送达，请注意以下事项：</p>
      <h2>提前规划发货</h2>
      <p>建议预留 1~2 天缓冲时间，尤其是发往北方及高海拔地区的货物。可联系客户经理了解线路实时通行情况。</p>
      <h2>做好包装防护</h2>
      <ul>
        <li>易受潮货物增加防潮膜与干燥剂</li>
        <li>液体类货物注意防冻，必要时使用保温包装</li>
        <li>精密设备加固木箱，避免颠簸损伤</li>
      </ul>
      <h2>关注轨迹与异常提醒</h2>
      <p>雨雪天气期间，驰远物流将通过短信与小程序推送延误预警。客户也可在官网“运单查询”页面实时查看车辆位置与节点状态。</p>
    `
  }
]

export function findDemoContent(id: number): ContentItem | undefined {
  return DEMO_CONTENTS.find((item) => item.id === id)
}
