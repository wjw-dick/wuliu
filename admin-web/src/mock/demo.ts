/* eslint-disable */
/**
 * 集中式演示数据引擎：仅在 /api 请求不可达（无后端）时启用。
 * 所有数据均为中文业务数据，结构和 API.md 保持一致。
 */
const BASE = new Date('2026-11-14T10:00:00')
const pad = (n: number) => String(n).padStart(2, '0')
function dt(offsetDays = 0, hour = 10, minute = 0, second = 0): string {
  const d = new Date(BASE)
  d.setDate(d.getDate() + offsetDays)
  d.setHours(hour, minute, second, 0)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}
function dateOnly(offsetDays = 0): string {
  return dt(offsetDays).slice(0, 10)
}
let seq = 8000
const nextId = () => ++seq

type Row = Record<string, any>
const db = new Map<string, Row[]>()

const PLATFORM_TENANT_NAMES = [
  '上海示例制造有限公司', '杭州云仓供应链有限公司', '苏州恒达精密机械有限公司', '宁波甬通货运代理有限公司',
  '广州南方家居建材有限公司', '深圳鲜达冷链物流有限公司', '南京中恒医药器械有限公司', '合肥新程电商仓储有限公司',
  '厦门海川商贸有限公司', '北京京联物流有限公司', '无锡捷运工业品有限公司', '东莞优品家居有限公司'
]

function tenants(): Row[] {
  const plans = ['标准版', '专业版', '旗舰版', '基础版']
  const statuses = ['正常', '正常', '试用中', '欠费', '正常', '正常', '已到期', '正常', '已停用', '正常', '试用中', '正常']
  return PLATFORM_TENANT_NAMES.map((name, i) => ({
    id: 1001 + i,
    tenantNo: `T2026${String(1001 + i)}`,
    name,
    shortName: name.slice(0, 4),
    industry: ['制造业', '商贸批发', '家居建材', '冷链生鲜', '电商零售', '医药器械'][i % 6],
    region: ['上海市', '浙江省', '江苏省', '浙江省', '广东省', '广东省', '江苏省', '安徽省', '福建省', '北京市', '江苏省', '广东省'][i],
    contact: ['张经理', '王经理', '李总', '陈经理', '刘经理', '赵经理'][i % 6],
    phone: `138${String(10000000 + i * 12345).slice(0, 8)}`,
    status: statuses[i],
    statusName: statuses[i],
    planName: plans[i % 4],
    seats: [10, 20, 30, 15][i % 4],
    usedSeats: [6, 12, 18, 9][i % 4],
    expireAt: dateOnly([120, 300, 15, -3, 180, 90, -15, 240, 60, 365, 25, 200][i]),
    createdAt: dt(-380 + i * 20).slice(0, 10),
    creditCode: `91310000MA1FL${String(100 + i)}X`,
    address: ['上海市嘉定区叶城路 1 号', '杭州市余杭区文一西路 18 号', '苏州市工业园区星湖街 88 号'][i % 3],
    balance: [0, 1200, 0, -860, 0, 2400, -1200, 0, 0, 3600, 0, 800][i],
    waybillCount: 120 + i * 67,
    licenseStatus: i % 4 === 0 ? '待审核' : '已通过'
  }))
}

function plans(): Row[] {
  return [
    { id: 1, name: '基础版', type: '基础版', price: 2999, months: 12, seatLimit: 10, storageGb: 20, smsCount: 2000, status: '正常', sort: 1, features: ['运单管理', '客户档案', '基础报表'], remark: '适合初创物流团队' },
    { id: 2, name: '标准版', type: '标准版', price: 6999, months: 12, seatLimit: 30, storageGb: 100, smsCount: 10000, status: '正常', sort: 2, features: ['运单管理', '调度看板', '财务对账', '客户门户'], remark: '成长型物流企业首选' },
    { id: 3, name: '专业版', type: '专业版', price: 12999, months: 12, seatLimit: 80, storageGb: 500, smsCount: 50000, status: '正常', sort: 3, features: ['全部标准版功能', '轨迹回放', '理赔管理', '开放接口'], remark: '多网点协同' },
    { id: 4, name: '旗舰版', type: '旗舰版', price: 24999, months: 12, seatLimit: 200, storageGb: 2048, smsCount: 200000, status: '正常', sort: 4, features: ['全部专业版功能', '专属部署', 'SSO 单点登录', '专属客户成功'], remark: '集团型客户' }
  ]
}

function bills(): Row[] {
  return tenants().slice(0, 9).map((t, i) => {
    const amount = [6999, 12999, 2999, 24999, 6999, 12999, 6999, 2999, 12999][i]
    const paid = [6999, 12999, 0, 10000, 6999, 0, 3000, 0, 12999][i]
    const status = paid >= amount ? '已结清' : paid > 0 ? '部分收款' : (i === 3 ? '已逾期' : '待支付')
    return {
      id: 3001 + i,
      billNo: `B2026${String(11001 + i)}`,
      tenantId: t.id,
      tenantName: t.name,
      type: i % 3 === 0 ? '套餐续费' : i % 3 === 1 ? '席位增购' : '短信包',
      amount,
      paidAmount: paid,
      status,
      statusName: status,
      dueDate: dateOnly(10 + i * 2),
      createdAt: dt(-30 + i * 2).slice(0, 10),
      method: paid > 0 ? '银行转账' : '',
      remark: i === 3 ? '已发送两次催缴通知' : ''
    }
  })
}

function platformUsers(): Row[] {
  return [
    { id: 1, username: 'admin', nickname: '平台超管', roleName: '平台超管', mobile: '13800000001', email: 'admin@quantaida.cloud', status: '正常', lastLoginAt: dt(-1, 9, 20) },
    { id: 2, username: 'operation', nickname: '运营小周', roleName: '平台运营', mobile: '13800000002', email: 'ops@quantaida.cloud', status: '正常', lastLoginAt: dt(0, 8, 45) },
    { id: 3, username: 'finance', nickname: '财务小李', roleName: '平台财务', mobile: '13800000003', email: 'fin@quantaida.cloud', status: '正常', lastLoginAt: dt(-2, 16, 10) },
    { id: 4, username: 'service01', nickname: '客服小陈', roleName: '平台运营', mobile: '13800000004', email: 'cs@quantaida.cloud', status: '停用', lastLoginAt: dt(-20, 11, 0) }
  ]
}

function platformRoles(): Row[] {
  return [
    { id: 1, name: '平台超管', code: 'PLATFORM_ADMIN', description: '拥有平台全部权限', userCount: 1, status: '正常', permissions: ['*'] },
    { id: 2, name: '平台运营', code: 'PLATFORM_OPS', description: '租户开通、资质审核、公告维护', userCount: 2, status: '正常', permissions: ['tenant:list', 'tenant:create', 'license:review', 'announcement:manage'] },
    { id: 3, name: '平台财务', code: 'PLATFORM_FIN', description: '账单、收款、套餐管理', userCount: 1, status: '正常', permissions: ['bill:list', 'bill:pay', 'plan:manage'] }
  ]
}

function quotes(): Row[] {
  const statuses = ['已报价', '已确认', '待报价', '已失效', '已转单', '已报价', '已报价', '已确认']
  return Array.from({ length: 8 }).map((_, i) => ({
    id: 6001 + i,
    quoteNo: `BJ20261114${String(1001 + i)}`,
    customerName: ['上海示例制造有限公司', '杭州云仓供应链有限公司', '苏州恒达精密机械有限公司', '宁波甬通货运代理有限公司'][i % 4],
    origin: ['上海', '杭州', '苏州', '宁波'][i % 4],
    destination: ['杭州', '广州', '北京', '厦门'][i % 4],
    cargoType: ['普货', '易碎品', '精密设备', '普货'][i % 4],
    vehicleType: ['6.8 米厢式', '9.6 米厢式', '4.2 米厢式'][i % 3],
    weight: 300 + i * 120,
    volume: +(2 + i * 0.6).toFixed(1),
    pieces: 8 + i,
    totalAmount: 480 + i * 260,
    status: statuses[i],
    statusName: statuses[i],
    validUntil: dateOnly(3 + i),
    createdAt: dt(-i, 9 + i, 15),
    salesName: ['王销售', '李销售', '陈销售'][i % 3]
  }))
}

const WAYBILL_STATUS = ['已受理', '待调度', '已派车', '已提货', '运输中', '中转中', '到达待派送', '派送中', '已签收', '待对账', '已结算', '已完结', '异常处理中', '待报价', '待客户确认', '已取消', '退回中', '理赔中']

function waybills(): Row[] {
  return Array.from({ length: 18 }).map((_, i) => {
    const status = WAYBILL_STATUS[i % WAYBILL_STATUS.length]
    const receivable = 480 + i * 160
    const payable = Math.round(receivable * 0.68)
    return {
      id: 7001 + i,
      waybillNo: `YH20261114${String(10001 + i)}`,
      customerId: 2001 + (i % 8),
      customerName: ['上海示例制造有限公司', '杭州云仓供应链有限公司', '苏州恒达精密机械有限公司', '宁波甬通货运代理有限公司', '广州南方家居建材有限公司', '深圳鲜达冷链物流有限公司'][i % 6],
      senderName: ['张经理', '王经理', '李经理', '陈经理'][i % 4],
      senderPhone: `1380000${String(1001 + i)}`,
      senderAddress: ['上海市嘉定区叶城路 1 号', '杭州市余杭区文一西路 18 号', '苏州市工业园区星湖街 88 号', '宁波市鄞州区诚信路 9 号'][i % 4],
      receiverName: ['刘经理', '赵经理', '周经理', '吴经理'][i % 4],
      receiverPhone: `1390000${String(2001 + i)}`,
      receiverAddress: ['杭州市余杭区良渚街道 2 号', '广州市白云区机场路 66 号', '北京市朝阳区建国路 8 号', '厦门市思明区湖滨路 5 号'][i % 4],
      origin: ['上海', '杭州', '苏州', '宁波'][i % 4],
      destination: ['杭州', '广州', '北京', '厦门'][i % 4],
      cargoName: ['机械配件', '服装百货', '精密仪器', '生鲜冷链', '家居板材'][i % 5],
      cargoType: ['普货', '易碎品', '精密设备', '需冷藏', '大件'][i % 5],
      weight: 300 + i * 85,
      volume: +(2.2 + i * 0.35).toFixed(1),
      pieces: 6 + i,
      vehicleType: ['6.8 米厢式', '9.6 米厢式', '4.2 米厢式', '13 米高栏'][i % 4],
      pickupType: i % 3 === 0 ? '客户自送到站' : '上门提货',
      deliveryType: i % 4 === 0 ? '收货人自提' : '送货上门',
      settleType: ['月结', '现结', '到付', '月结'][i % 4],
      pickupTime: dt(-i + 2, 9, 0),
      deliveryTime: dt(-i + 3, 18, 0),
      status,
      statusName: status,
      totalAmount: receivable,
      receivableAmount: receivable,
      payableAmount: payable,
      grossProfit: receivable - payable,
      insured: i % 3 === 0,
      codAmount: i % 5 === 0 ? 2000 : 0,
      driverName: status === '待调度' || status === '待报价' ? '' : ['张师傅', '李师傅', '王师傅', '赵师傅'][i % 4],
      driverPhone: status === '待调度' || status === '待报价' ? '' : `1370000${String(3001 + i)}`,
      plateNo: status === '待调度' || status === '待报价' ? '' : ['沪A·8F236', '浙B·5K129', '苏E·9M668', '粤A·3Q771'][i % 4],
      orgName: ['上海总部', '杭州网点', '苏州网点'][i % 3],
      remark: '',
      createdAt: dt(-i, 8 + (i % 6), 20)
    }
  })
}

function customers(): Row[] {
  return Array.from({ length: 9 }).map((_, i) => ({
    id: 2001 + i,
    customerNo: `KH2026${String(1001 + i)}`,
    name: ['上海示例制造有限公司', '杭州云仓供应链有限公司', '苏州恒达精密机械有限公司', '宁波甬通货运代理有限公司', '广州南方家居建材有限公司', '深圳鲜达冷链物流有限公司', '南京中恒医药器械有限公司', '合肥新程电商仓储有限公司', '厦门海川商贸有限公司'][i],
    type: ['企业客户', '月结客户', '企业客户', '个体客户', '企业客户', '月结客户', '企业客户', '平台会员', '企业客户'][i],
    level: ['战略客户', '重点客户', '普通客户', '潜在客户'][i % 4],
    contact: ['张经理', '王经理', '李总', '陈经理'][i % 4],
    phone: `1380000${String(1101 + i)}`,
    settleType: ['月结', '月结', '现结', '到付'][i % 4],
    creditLimit: [200000, 120000, 50000, 0][i % 4],
    receivable: [48200, 32600, 0, 8900][i % 4],
    status: i === 7 ? '停用' : '正常',
    source: ['官网询价', '老客推荐', '销售开发', '小程序'][i % 4],
    salesName: ['王销售', '李销售', '陈销售'][i % 3],
    orgName: ['上海总部', '杭州网点', '苏州网点'][i % 3],
    createdAt: dt(-300 + i * 21).slice(0, 10),
    address: ['上海市嘉定区叶城路 1 号', '杭州市余杭区文一西路 18 号', '苏州市工业园区星湖街 88 号'][i % 3],
    taxNo: `91310000MA1FL${String(200 + i)}X`,
    remark: i === 3 ? '价格敏感，需主管审批折扣' : ''
  }))
}

function agreements(): Row[] {
  return Array.from({ length: 9 }).map((_, i) => ({
    id: 4001 + i,
    customerId: 2001 + (i % 9),
    customerName: customers()[i % 9].name,
    origin: ['上海', '杭州', '苏州', '宁波', '广州'][i % 5],
    destination: ['杭州', '广州', '北京', '厦门', '深圳'][i % 5],
    vehicleType: ['6.8 米厢式', '9.6 米厢式', '4.2 米厢式'][i % 3],
    price: 500 + i * 120,
    settleType: ['月结', '现结', '月结'][i % 3],
    effectiveFrom: dateOnly(-180 + i * 10),
    effectiveTo: dateOnly(180 + i * 10),
    status: i === 8 ? '已失效' : '有效',
    statusName: i === 8 ? '已失效' : '有效',
    remark: '含燃油联动，超 5% 另行议价'
  }))
}

function leads(): Row[] {
  return Array.from({ length: 8 }).map((_, i) => ({
    id: 5001 + i,
    name: ['周先生', '吴女士', '郑经理', '孙经理', '钱总', '冯经理', '陈女士', '褚先生'][i],
    company: ['嘉兴恒信机电有限公司', '湖州织里童装城', '绍兴纺织品公司', '金华五金城', '台州模具厂', '温州鞋业集团', '常州新能源公司', '南通家纺城'][i],
    phone: `1350000${String(1201 + i)}`,
    source: ['官网询价', '公众号', '小程序', '电话', '销售开发'][i % 5],
    status: ['待跟进', '跟进中', '已转客户', '待跟进', '跟进中', '已失效', '待跟进', '跟进中'][i],
    statusName: ['待跟进', '跟进中', '已转客户', '待跟进', '跟进中', '已失效', '待跟进', '跟进中'][i],
    owner: ['王销售', '李销售', '陈销售'][i % 3],
    nextFollowAt: dateOnly(1 + i),
    remark: ['询价上海→杭州 6.8 米', '需要月结与回单服务', '价格对比中', '预计下周签约'][i % 4],
    createdAt: dt(-12 + i, 9, 30)
  }))
}

function vehicles(): Row[] {
  return Array.from({ length: 10 }).map((_, i) => ({
    id: 9001 + i,
    plateNo: ['沪A·8F236', '浙B·5K129', '苏E·9M668', '粤A·3Q771', '沪B·7H552', '浙A·2N883', '苏A·6P901', '沪C·4R118'][i % 8],
    type: ['6.8 米厢式', '9.6 米厢式', '4.2 米厢式', '13 米高栏', '冷藏车'][i % 5],
    loadWeight: [8, 18, 3, 32, 10][i % 5],
    volume: [40, 60, 18, 80, 45][i % 5],
    driverName: ['张师傅', '李师傅', '王师傅', '赵师傅'][i % 4],
    driverPhone: `1370000${String(3001 + i)}`,
    status: ['在途', '空闲', '在途', '维修', '空闲'][i % 5],
    orgName: ['上海总部', '杭州网点', '苏州网点'][i % 3],
    transportCertExpire: dateOnly([180, 30, 8, 240, 90][i % 5]),
    insuranceExpire: dateOnly([90, 60, 15, 300, 120][i % 5]),
    createdAt: dt(-500 + i * 30).slice(0, 10),
    remark: i === 3 ? '例行保养中' : ''
  }))
}

function drivers(): Row[] {
  return Array.from({ length: 10 }).map((_, i) => ({
    id: 9501 + i,
    name: ['张师傅', '李师傅', '王师傅', '赵师傅', '刘师傅', '陈师傅'][i % 6],
    phone: `1370000${String(3101 + i)}`,
    licenseNo: `3101${String(19850101 + i)}`,
    licenseExpire: dateOnly([400, 800, 60, 1200, 200][i % 5]),
    licenseType: ['A2', 'B2', 'A2', 'C1'][i % 4],
    qualificationNo: `CY2026${String(3001 + i)}`,
    qualificationExpire: dateOnly([25, 300, 9, 600, 120][i % 5]),
    vehiclePlate: ['沪A·8F236', '浙B·5K129', '苏E·9M668', '', '沪B·7H552'][i % 5],
    status: ['任务中', '空闲', '任务中', '请假', '空闲'][i % 5],
    orgName: ['上海总部', '杭州网点', '苏州网点'][i % 3],
    joinDate: dt(-600 + i * 40).slice(0, 10),
    score: (4.2 + (i % 8) * 0.1).toFixed(1)
  }))
}

function carriers(): Row[] {
  return Array.from({ length: 7 }).map((_, i) => ({
    id: 9801 + i,
    name: ['安达运输有限公司', '顺捷物流有限公司', '中通供应链有限公司', '联运天下运输队', '恒通冷链运输有限公司', '快捷城配服务部', '远航大件运输有限公司'][i],
    contact: ['孙经理', '周经理', '吴经理', '郑经理'][i % 4],
    phone: `1360000${String(3301 + i)}`,
    settleType: ['月结', '单结'][i % 2],
    score: (3.8 + (i % 5) * 0.24).toFixed(2),
    status: ['合作中', '合作中', '待审核', '合作中', '已暂停', '合作中', '已终止'][i],
    licenseExpire: dateOnly([200, 40, 400, 15, 300][i % 5]),
    routes: ['上海→杭州', '苏州→北京', '广州→深圳', '宁波→厦门'][i % 4],
    bankAccount: `6222021001${String(100000 + i)}`,
    createdAt: dt(-700 + i * 50).slice(0, 10)
  }))
}

function qualifications(): Row[] {
  return Array.from({ length: 8 }).map((_, i) => ({
    id: 9901 + i,
    ownerName: ['沪A·8F236', '浙B·5K129', '张师傅', '李师傅', '安达运输有限公司', '苏E·9M668'][i % 6],
    ownerType: ['车辆', '车辆', '司机', '司机', '承运商', '车辆'][i % 6],
    certName: ['道路运输证', '交强险保单', '从业资格证', '驾驶证', '道路运输经营许可证'][i % 5],
    certNo: `ZZ2026${String(5001 + i)}`,
    expireAt: dateOnly([8, 25, 5, 60, 12, 300, 28, 18][i]),
    daysLeft: [8, 25, 5, 60, 12, 300, 28, 18][i],
    status: [8, 25, 5].includes([8, 25, 5, 60, 12, 300, 28, 18][i]) ? '即将到期' : '正常',
    orgName: ['上海总部', '杭州网点'][i % 2]
  }))
}

function exceptions(): Row[] {
  const types = ['延误', '破损', '拒收', '改址', '无人收货', '车辆故障', '短少']
  const statuses = ['待处理', '处理中', '已解决', '已升级', '待处理', '处理中', '已解决', '待处理']
  return Array.from({ length: 8 }).map((_, i) => ({
    id: 11001 + i,
    exceptionNo: `YC20261114${String(2001 + i)}`,
    waybillNo: `YH20261114${String(10001 + i)}`,
    customerName: customers()[i % 8].name,
    type: types[i % types.length],
    typeName: types[i % types.length],
    description: ['收货人临时变更地址', '外包装挤压破损 2 件', '收货人拒收，要求退回', '高速封路导致延误 4 小时', '多次联系收货人未果'][i % 5],
    status: statuses[i],
    statusName: statuses[i],
    reporter: ['张师傅', '李师傅', '客服小周'][i % 3],
    assignee: ['调度小陈', '客服小周', ''][i % 3],
    images: [],
    createdAt: dt(-i, 8 + i, 10)
  }))
}

function receipts(): Row[] {
  return Array.from({ length: 8 }).map((_, i) => ({
    id: 12001 + i,
    waybillNo: `YH20261114${String(10001 + i)}`,
    customerName: customers()[i % 8].name,
    driverName: ['张师傅', '李师傅', '王师傅'][i % 3],
    signer: ['刘经理', '赵经理', '周经理'][i % 3],
    signAt: dt(-i, 15, 20),
    images: ['回单-1.jpg', '回单-2.jpg'],
    status: ['待审核', '已通过', '已驳回', '待审核', '已通过', '待审核', '已通过', '待审核'][i],
    statusName: ['待审核', '已通过', '已驳回', '待审核', '已通过', '待审核', '已通过', '待审核'][i],
    reviewRemark: i === 2 ? '回单照片模糊，无法辨认签收人' : '',
    receivedAt: dt(-i, 16, 0)
  }))
}

function returns(): Row[] {
  return Array.from({ length: 5 }).map((_, i) => ({
    id: 12501 + i,
    returnNo: `TH20261114${String(6001 + i)}`,
    waybillNo: `YH20261114${String(10005 + i)}`,
    customerName: customers()[i % 5].name,
    reason: ['收货人拒收', '地址错误', '货物破损', '客户取消', '无人收货'][i],
    status: ['待处理', '退回中', '已退回', '待处理', '已退回'][i],
    statusName: ['待处理', '退回中', '已退回', '待处理', '已退回'][i],
    fee: [180, 260, 120, 0, 300][i],
    applyAt: dt(-i - 1, 10, 30),
    completeAt: i === 2 || i === 4 ? dt(-i, 16, 0) : ''
  }))
}

function receivables(): Row[] {
  return Array.from({ length: 10 }).map((_, i) => {
    const amount = 1200 + i * 480
    const received = i % 3 === 0 ? amount : i % 3 === 1 ? 600 : 0
    const status = received >= amount ? '已结清' : received > 0 ? '部分收款' : i === 9 ? '已挂账' : '待对账'
    return {
      id: 13001 + i,
      receivableNo: `YS20261114${String(3001 + i)}`,
      customerName: customers()[i % 8].name,
      waybillNo: `YH20261114${String(10001 + i)}`,
      amount,
      receivedAmount: received,
      settleType: ['月结', '现结', '到付'][i % 3],
      status,
      statusName: status,
      dueDate: dateOnly(5 + i * 3),
      agingDays: [0, 15, 32, 63, 91, 8, 45, 120, 20, 180][i],
      createdAt: dt(-40 + i * 2, 11, 0)
    }
  })
}

function payables(): Row[] {
  return Array.from({ length: 9 }).map((_, i) => {
    const amount = 800 + i * 320
    const paid = i % 3 === 0 ? amount : 0
    return {
      id: 13501 + i,
      payableNo: `YF20261114${String(4001 + i)}`,
      carrierName: ['安达运输有限公司', '顺捷物流有限公司', '联运天下运输队', '恒通冷链运输有限公司'][i % 4],
      waybillNo: `YH20261114${String(10001 + i)}`,
      amount,
      paidAmount: paid,
      status: paid >= amount ? '已结清' : '待付款',
      statusName: paid >= amount ? '已结清' : '待付款',
      dueDate: dateOnly(8 + i * 2),
      createdAt: dt(-30 + i, 14, 20)
    }
  })
}

function statementItems(statementId = 1): Row[] {
  return Array.from({ length: 12 }).map((_, i) => {
    const amount = 480 + i * 126
    return {
      id: statementId * 100 + i,
      waybillNo: `YH20261114${String(10001 + i)}`,
      deliveryAt: dt(-20 + i, 17, 0),
      origin: ['上海', '杭州', '苏州'][i % 3],
      destination: ['杭州', '广州', '北京'][i % 3],
      customerName: customers()[i % 8].name,
      weight: 200 + i * 70,
      amount,
      payable: Math.round(amount * 0.7),
      variance: i === 4 ? 120 : 0,
      remark: i === 4 ? '客户主张燃油费重复计算' : ''
    }
  })
}

function statements(): Row[] {
  return Array.from({ length: 7 }).map((_, i) => {
    const total = 12600 + i * 3200
    const confirmed = i === 3 ? total - 120 : total
    return {
      id: 14001 + i,
      statementNo: `DZ202611${String(1001 + i)}`,
      customerName: customers()[i % 8].name,
      type: '客户',
      periodStart: dateOnly(-30 + i),
      periodEnd: dateOnly(-1 + i),
      totalAmount: total,
      confirmedAmount: i === 0 ? 0 : confirmed,
      variance: i === 0 ? 0 : total - confirmed,
      status: ['待对账', '客户已确认', '待对账', '有异议', '已开票', '已结清', '部分收款'][i],
      statusName: ['待对账', '客户已确认', '待对账', '有异议', '已开票', '已结清', '部分收款'][i],
      createdAt: dt(-15 + i, 10, 0),
      confirmedAt: i === 0 || i === 2 ? '' : dt(-10 + i, 14, 30),
      billCount: 12 + i
    }
  })
}

function invoices(): Row[] {
  return Array.from({ length: 7 }).map((_, i) => ({
    id: 14501 + i,
    invoiceNo: i < 4 ? `FP202611${String(7001 + i)}` : '',
    customerName: customers()[i % 8].name,
    statementNo: `DZ202611${String(1001 + i)}`,
    type: ['增值税专用发票', '增值税普通发票', '电子普通发票'][i % 3],
    amount: 12600 + i * 3200,
    taxRate: 9,
    status: ['已开票', '已开票', '待开票', '待开票', '已开票', '待开票', '已作废'][i],
    statusName: ['已开票', '已开票', '待开票', '待开票', '已开票', '待开票', '已作废'][i],
    applyAt: dt(-8 + i, 9, 20),
    issuedAt: i < 2 || i === 4 ? dt(-5 + i, 15, 0) : ''
  }))
}

function payments(): Row[] {
  return Array.from({ length: 8 }).map((_, i) => ({
    id: 15001 + i,
    paymentNo: `SK20261114${String(5001 + i)}`,
    customerName: customers()[i % 8].name,
    statementNo: `DZ202611${String(1001 + i)}`,
    amount: 3000 + i * 1200,
    method: ['银行转账', '微信支付', '现金'][i % 3],
    status: '已收款',
    statusName: '已收款',
    paidAt: dt(-6 + i, 14, 0),
    operator: ['财务小李', '财务小王'][i % 2],
    remark: i === 3 ? '差额 120 元客户下期补付' : ''
  }))
}

function cods(): Row[] {
  return Array.from({ length: 6 }).map((_, i) => ({
    id: 15501 + i,
    waybillNo: `YH20261114${String(10003 + i)}`,
    customerName: customers()[i % 6].name,
    amount: 1000 + i * 500,
    channel: ['微信支付', '银行代收', '现金'][i % 3],
    status: ['已结算', '待结算', '待结算', '已结算', '待结算', '已退回'][i],
    statusName: ['已结算', '待结算', '待结算', '已结算', '待结算', '已退回'][i],
    fee: Math.round((1000 + i * 500) * 0.006),
    settledAt: i % 3 === 0 ? dt(-2 + i, 10, 0) : '',
    createdAt: dt(-10 + i, 11, 0)
  }))
}

function claims(): Row[] {
  return Array.from({ length: 6 }).map((_, i) => ({
    id: 16001 + i,
    claimNo: `LP20261114${String(8001 + i)}`,
    waybillNo: `YH20261114${String(10001 + i)}`,
    customerName: customers()[i % 6].name,
    type: ['货损', '货差', '丢失', '延误'][i % 4],
    amount: 1200 + i * 800,
    compensation: i >= 4 ? 1200 + i * 700 : 0,
    liability: ['承运商', '自有司机', '装卸方', '待定'][i % 4],
    status: ['待立案', '已立案', '定责中', '待审批', '已赔付', '已结案'][i],
    statusName: ['待立案', '已立案', '定责中', '待审批', '已赔付', '已结案'][i],
    description: ['外包装挤压导致设备面板破损', '签收时发现短少 1 件', '中转场站丢失一票货物', '延误 2 天客户索赔'][i % 4],
    createdAt: dt(-12 + i * 2, 9, 30),
    reporter: '客服小周'
  }))
}

function orgs(): Row[] {
  return [
    { id: 1, parentId: 0, name: '上海总部', type: '公司', manager: '张总', phone: '021-68880001', address: '上海市嘉定区叶城路 1 号', status: '正常', sort: 1 },
    { id: 2, parentId: 1, name: '运营中心', type: '部门', manager: '王经理', phone: '021-68880002', address: '总部 3 楼', status: '正常', sort: 1 },
    { id: 3, parentId: 1, name: '财务部', type: '部门', manager: '李会计', phone: '021-68880003', address: '总部 2 楼', status: '正常', sort: 2 },
    { id: 4, parentId: 1, name: '杭州网点', type: '网点', manager: '陈经理', phone: '0571-88880001', address: '杭州市余杭区文一西路 18 号', status: '正常', sort: 3 },
    { id: 5, parentId: 1, name: '苏州网点', type: '网点', manager: '赵经理', phone: '0512-66660001', address: '苏州市工业园区星湖街 88 号', status: '正常', sort: 4 },
    { id: 6, parentId: 1, name: '自有车队', type: '车队', manager: '刘队长', phone: '13800009999', address: '上海市嘉定区停车场', status: '正常', sort: 5 },
    { id: 7, parentId: 4, name: '杭州城配组', type: '车队', manager: '周组长', phone: '13800008888', address: '杭州市余杭区', status: '停用', sort: 1 }
  ]
}

function tenantUsers(): Row[] {
  return [
    { id: 1, username: 'admin', nickname: '企业管理员', roleName: '企业管理员', orgName: '上海总部', mobile: '13800000001', status: '正常', lastLoginAt: dt(0, 8, 30) },
    { id: 2, username: 'dispatch01', nickname: '调度小陈', roleName: '调度主管', orgName: '运营中心', mobile: '13800000011', status: '正常', lastLoginAt: dt(0, 8, 50) },
    { id: 3, username: 'finance01', nickname: '财务小李', roleName: '财务专员', orgName: '财务部', mobile: '13800000012', status: '正常', lastLoginAt: dt(-1, 17, 10) },
    { id: 4, username: 'sales01', nickname: '销售小王', roleName: '销售专员', orgName: '运营中心', mobile: '13800000013', status: '正常', lastLoginAt: dt(-1, 16, 20) },
    { id: 5, username: 'service01', nickname: '客服小周', roleName: '客服专员', orgName: '运营中心', mobile: '13800000014', status: '正常', lastLoginAt: dt(-2, 9, 0) },
    { id: 6, username: 'hzadmin', nickname: '杭州网点管理员', roleName: '网点管理员', orgName: '杭州网点', mobile: '13800000015', status: '停用', lastLoginAt: dt(-30, 10, 0) }
  ]
}

function tenantRoles(): Row[] {
  return [
    { id: 1, name: '企业管理员', code: 'TENANT_ADMIN', dataScope: '公司级', description: '租户全部功能', userCount: 1, status: '正常', permissions: ['*'] },
    { id: 2, name: '调度主管', code: 'DISPATCH_MANAGER', dataScope: '公司级', description: '运单调度、车辆司机管理', userCount: 1, status: '正常', permissions: ['waybill:list', 'dispatch:assign', 'vehicle:list', 'driver:list'] },
    { id: 3, name: '财务专员', code: 'FINANCE', dataScope: '公司级', description: '应收应付、对账开票', userCount: 1, status: '正常', permissions: ['receivable:list', 'payable:list', 'statement:confirm', 'invoice:list'] },
    { id: 4, name: '销售专员', code: 'SALES', dataScope: '本人', description: '客户、线索、报价', userCount: 1, status: '正常', permissions: ['customer:list', 'lead:list', 'quote:list'] },
    { id: 5, name: '客服专员', code: 'SERVICE', dataScope: '公司级', description: '异常、回单、理赔处理', userCount: 1, status: '正常', permissions: ['exception:handle', 'receipt:review', 'claim:handle'] },
    { id: 6, name: '网点管理员', code: 'ORG_ADMIN', dataScope: '网点级', description: '本网点业务与人员', userCount: 1, status: '停用', permissions: ['waybill:list', 'org:view'] }
  ]
}

function configGroups(): Row[] {
  return [
    { group: '站点信息', items: [
      { key: 'site.name', label: '站点名称', value: '全达物流 SaaS', type: 'text' },
      { key: 'site.phone', label: '客服电话', value: '400-800-9000', type: 'text' },
      { key: 'site.workTime', label: '工作时间', value: '08:00 - 20:00', type: 'text' },
      { key: 'site.icp', label: 'ICP 备案号', value: '沪ICP备2026012345号', type: 'text' }
    ]},
    { group: '单号规则', items: [
      { key: 'sn.prefix', label: '运单前缀', value: 'YH', type: 'text' },
      { key: 'sn.dateFormat', label: '日期段', value: 'yyyyMMdd', type: 'text' },
      { key: 'sn.seqLength', label: '序号位数', value: '4', type: 'number' }
    ]},
    { group: '时效与提醒', items: [
      { key: 'timeliness.default', label: '默认时效（小时）', value: '24', type: 'number' },
      { key: 'alert.expireDays', label: '资质提前提醒天数', value: '30', type: 'number' },
      { key: 'alert.stagnantHours', label: '卡滞判定（小时）', value: '12', type: 'number' }
    ]},
    { group: '结算设置', items: [
      { key: 'finance.settleDay', label: '月结日', value: '每月 25 日', type: 'text' },
      { key: 'finance.taxRate', label: '默认税率（%）', value: '9', type: 'number' },
      { key: 'finance.varianceLimit', label: '对账差异容忍额（元）', value: '50', type: 'number' }
    ]}
  ]
}

function dictCategories(): Row[] {
  return [
    { id: 1, code: 'waybill_status', name: '运单状态', remark: '运单状态机使用，不可删除', itemCount: 21, builtin: true },
    { id: 2, code: 'customer_type', name: '客户类型', remark: '客户档案分类', itemCount: 4, builtin: true },
    { id: 3, code: 'settle_type', name: '结算方式', remark: '', itemCount: 4, builtin: true },
    { id: 4, code: 'vehicle_type', name: '车型', remark: '报价与调度下拉', itemCount: 8, builtin: true },
    { id: 5, code: 'exception_type', name: '异常类型', remark: '异常上报', itemCount: 11, builtin: true },
    { id: 6, code: 'industry', name: '行业分类', remark: '租户行业', itemCount: 6, builtin: false }
  ]
}

function messages() {
  return {
    channels: [
      { key: 'sms', name: '短信', enabled: true, desc: '阿里云 / 腾讯云短信通道' },
      { key: 'wechat', name: '微信模板消息', enabled: true, desc: '公众号服务号模板消息' },
      { key: 'miniprogram', name: '小程序订阅消息', enabled: true, desc: '客户与承运商小程序' },
      { key: 'email', name: '邮件', enabled: false, desc: 'SMTP 邮件通知' }
    ],
    smsChannel: '阿里云短信',
    sender: '全达物流',
    templates: [
      { id: 1, node: '下单成功', channel: '短信', templateId: 'SMS_202601', content: '您的运单 ${waybillNo} 已受理，我们将尽快为您安排提货。', enabled: true },
      { id: 2, node: '派车通知', channel: '微信模板消息', templateId: 'TPL_ASSIGN', content: '司机 ${driverName}（${plateNo}）已接单，预计 ${time} 上门。', enabled: true },
      { id: 3, node: '到达提醒', channel: '小程序订阅消息', templateId: 'SUB_ARRIVE', content: '您的货物预计 ${time} 送达，请保持电话畅通。', enabled: true },
      { id: 4, node: '签收通知', channel: '短信', templateId: 'SMS_202605', content: '运单 ${waybillNo} 已签收，签收人 ${signer}，感谢使用全达物流。', enabled: true },
      { id: 5, node: '对账提醒', channel: '微信模板消息', templateId: 'TPL_STATEMENT', content: '您有一份对账单待确认，金额 ${amount} 元，请及时处理。', enabled: false }
    ],
    logs: Array.from({ length: 8 }).map((_, i) => ({
      id: 5001 + i,
      node: ['下单成功', '派车通知', '到达提醒', '签收通知'][i % 4],
      channel: ['短信', '微信模板消息', '小程序订阅消息'][i % 3],
      receiver: `138****${String(1001 + i).slice(-4)}`,
      status: i === 2 || i === 6 ? '失败' : '成功',
      statusName: i === 2 || i === 6 ? '失败' : '成功',
      failReason: i === 2 ? '短信余额不足，已降级微信通道' : i === 6 ? '用户未授权订阅消息' : '',
      sentAt: dt(-i, 10, i * 7),
      waybillNo: `YH20261114${String(10001 + i)}`
    }))
  }
}

function menusConfig() {
  return {
    platforms: [
      {
        key: 'official', name: '官网导航', items: [
          { id: 'o1', title: '首页', link: '/', children: [] },
          { id: 'o2', title: '服务产品', link: '/services', children: [
            { id: 'o21', title: '零担快运', link: '/services/ltl' },
            { id: 'o22', title: '整车运输', link: '/services/ftl' },
            { id: 'o23', title: '城市配送', link: '/services/city' }
          ]},
          { id: 'o3', title: '价格查询', link: '/quote' },
          { id: 'o4', title: '网点查询', link: '/sites' },
          { id: 'o5', title: '关于我们', link: '/about' }
        ]
      },
      {
        key: 'miniprogram', name: '小程序 tabBar', items: [
          { id: 'm1', title: '首页', link: 'pages/index/index' },
          { id: 'm2', title: '下单', link: 'pages/order/create' },
          { id: 'm3', title: '运单', link: 'pages/order/list' },
          { id: 'm4', title: '我的', link: 'pages/mine/index' }
        ]
      },
      {
        key: 'h5', name: 'H5 底部导航', items: [
          { id: 'h1', title: '首页', link: '/' },
          { id: 'h2', title: '查价', link: '/quote' },
          { id: 'h3', title: '查单', link: '/track' },
          { id: 'h4', title: '我的', link: '/mine' }
        ]
      },
      {
        key: 'wechat', name: '公众号菜单', items: [
          { id: 'w1', title: '查价下单', link: '', children: [
            { id: 'w11', title: '立即查价', link: '/quote' },
            { id: 'w12', title: '在线下单', link: '/order' }
          ]},
          { id: 'w2', title: '我的运单', link: '/track' },
          { id: 'w3', title: '联系我们', link: '/contact' }
        ]
      }
    ]
  }
}

function cmsContents(): Row[] {
  return Array.from({ length: 6 }).map((_, i) => ({
    id: 17001 + i,
    title: ['全达物流荣获年度优秀物流企业', '冬季货物运输包装指南', '上海至杭州专线时效升级公告', '冷链运输服务正式上线', '企业月结申请流程说明', '2026 年春节放假安排'][i],
    category: ['公司新闻', '行业资讯', '服务公告', '服务公告', '帮助中心', '服务公告'][i],
    status: ['已发布', '已发布', '已发布', '草稿', '已发布', '草稿'][i],
    statusName: ['已发布', '已发布', '已发布', '草稿', '已发布', '草稿'][i],
    author: '运营小周',
    views: 320 + i * 87,
    publishedAt: dt(-20 + i * 3, 9, 0),
    content: `<p>全达物流持续深耕长三角区域物流服务，为客户提供零担、整车、城配一体化解决方案。</p><p>本次更新进一步优化了线路时效与在途可视化能力。</p>`
  }))
}

function announcements(): Row[] {
  return Array.from({ length: 6 }).map((_, i) => ({
    id: 18001 + i,
    title: ['平台 V2.6 版本更新说明', '11 月系统维护公告', '双十一运费结算提醒', '新增电子回单功能', '数据导出合规使用规范', '国庆假期服务安排'][i],
    type: ['版本通知', '维护公告', '运营活动', '版本通知', '政策通知', '运营活动'][i],
    typeName: ['版本通知', '维护公告', '运营活动', '版本通知', '政策通知', '运营活动'][i],
    status: i === 4 ? '草稿' : '已发布',
    statusName: i === 4 ? '草稿' : '已发布',
    publisher: ['平台运营', '平台运营', '平台财务'][i % 3],
    publishedAt: dt(-15 + i * 2, 10, 0),
    views: 120 + i * 44,
    content: '<p>各位用户，为提升系统稳定性，平台将进行例行维护，请提前安排业务。</p>'
  }))
}

function i18nList(): Row[] {
  return [
    { id: 1, lang: 'zh-CN', name: '简体中文', default: true, status: '启用', progress: 100 },
    { id: 2, lang: 'en-US', name: 'English', default: false, status: '启用', progress: 86 },
    { id: 3, lang: 'zh-TW', name: '繁體中文', default: false, status: '停用', progress: 72 }
  ]
}

function wechatAccounts(): Row[] {
  return [
    { id: 1, type: '公众号', name: '全达物流服务号', appId: 'wx8f2a1d3e5b7c9a01', appSecret: '••••••••3f2a', serverUrl: 'https://api.quantaida.cloud/wechat/callback', token: 'quantaida2026', encodingAesKey: '••••••••••••', authorizedDomain: 'h5.quantaida.cloud', status: '正常' },
    { id: 2, type: '小程序', name: '全达物流客户小程序', appId: 'wx1a2b3c4d5e6f7a89', appSecret: '••••••••8c1b', serverUrl: '', token: '', encodingAesKey: '', authorizedDomain: 'mp.quantaida.cloud', status: '正常' }
  ]
}

function buildList(key: string): Row[] {
  switch (key) {
    case 'platform/tenants': return tenants()
    case 'platform/plans': return plans()
    case 'platform/bills': return bills()
    case 'platform/users': return platformUsers()
    case 'platform/roles': return platformRoles()
    case 'platform/announcements': return announcements()
    case 'platform/dicts': return dictCategories()
    case 'tenant/customers': return customers()
    case 'tenant/agreements': return agreements()
    case 'tenant/leads': return leads()
    case 'tenant/quotes': return quotes()
    case 'tenant/waybills': return waybills()
    case 'tenant/vehicles': return vehicles()
    case 'tenant/drivers': return drivers()
    case 'tenant/carriers': return carriers()
    case 'tenant/qualifications/expiring': return qualifications()
    case 'tenant/exceptions': return exceptions()
    case 'tenant/receipts': return receipts()
    case 'tenant/returns': return returns()
    case 'tenant/receivables': return receivables()
    case 'tenant/payables': return payables()
    case 'tenant/statements': return statements()
    case 'tenant/invoices': return invoices()
    case 'tenant/payments': return payments()
    case 'tenant/cod': return cods()
    case 'tenant/claims': return claims()
    case 'tenant/orgs': return orgs()
    case 'tenant/users': return tenantUsers()
    case 'tenant/roles': return tenantRoles()
    case 'tenant/cms/contents': return cmsContents()
    case 'tenant/announcements': return announcements()
    case 'tenant/system/i18n': return i18nList()
    case 'tenant/system/wechat': return wechatAccounts()
    case 'tenant/system/message-templates': return messages().templates
    case 'tenant/system/message-logs': return messages().logs
    case 'tenant/system/dicts': return dictCategories()
    case 'platform/audits': return Array.from({ length: 14 }).map((_, i) => ({
      id: 19001 + i,
      operator: ['admin', 'operation', 'finance', 'dispatch01', 'finance01'][i % 5],
      operatorName: ['平台超管', '运营小周', '财务小李', '调度小陈', '财务小李'][i % 5],
      module: ['租户管理', '资质审核', '账单收款', '运单调度', '对账结算'][i % 5],
      action: ['开通租户', '审核通过', '收款登记', '派车', '确认对账单'][i % 5],
      target: ['杭州云仓供应链有限公司', '宁波甬通货运代理有限公司', '上海示例制造有限公司'][i % 3],
      tenantName: PLATFORM_TENANT_NAMES[i % 6],
      ip: `10.0.${i}.${100 + i}`,
      result: i === 6 ? '失败' : '成功',
      createdAt: dt(-i, 9 + (i % 10), 12)
    }))
    default: return []
  }
}

function resourceKey(pathname: string): string {
  return pathname.replace(/^\/api\//, '').replace(/^\//, '').replace(/\/$/, '')
}

function paginate(list: Row[], params: Record<string, any> = {}) {
  let rows = [...list]
  const { keyword, status, type, customerId, waybillId } = params
  if (keyword) {
    const k = String(keyword).toLowerCase()
    rows = rows.filter((r) => Object.values(r).some((v) => String(v ?? '').toLowerCase().includes(k)))
  }
  if (status) rows = rows.filter((r) => (r.statusName || r.status) === status)
  if (type) rows = rows.filter((r) => (r.typeName || r.type) === type)
  if (customerId) rows = rows.filter((r) => String(r.customerId) === String(customerId))
  const page = Number(params.page || 1)
  const size = Number(params.size || 10)
  return { list: rows.slice((page - 1) * size, page * size), total: rows.length, page, size }
}

function findDetail(key: string, id: number): Row | undefined {
  const list = getList(key)
  return list.find((r) => Number(r.id) === Number(id))
}

function getList(key: string): Row[] {
  if (!db.has(key)) db.set(key, buildList(key))
  return db.get(key)!
}

function ok<T>(data: T) {
  return { code: 0, message: '成功', data }
}

function quoteResult(body: Row) {
  const weight = Number(body.weight || 0)
  const volume = Number(body.volume || 0)
  const base = Math.max(120, Math.round(weight * 1.2 + volume * 30))
  const fuel = +(base * 0.1).toFixed(2)
  const loading = body.needLoading ? 50 : 0
  const upstairs = body.needUpstairs ? 80 : 0
  const insured = body.insured || body.value ? 30 : 0
  const codFee = body.codAmount ? +(Number(body.codAmount) * 0.006).toFixed(2) : 0
  const total = base + fuel + loading + upstairs + insured + codFee
  return {
    quoteNo: `BJ20261114${String(nextId()).slice(-4)}`,
    baseFreight: base,
    fuelSurcharge: fuel,
    loadingFee: loading,
    upstairsFee: upstairs,
    insuranceFee: insured,
    codFee,
    remoteFee: 0,
    holidayFee: 0,
    totalAmount: total,
    ruleVersion: 'V3',
    priceSource: '线路标准价',
    detail: [
      { name: '基础运费', amount: base, formula: `首重 100kg/120 元 + 续重 ${Math.max(0, weight - 100)}kg × 1.2 元/kg` },
      { name: '燃油附加费', amount: fuel, formula: '基础运费 × 10%' },
      ...(loading ? [{ name: '装卸费', amount: loading, formula: '上门提货固定 50 元' }] : []),
      ...(upstairs ? [{ name: '上楼费', amount: upstairs, formula: '无电梯楼层 +80 元' }] : []),
      ...(insured ? [{ name: '保险费', amount: insured, formula: '声明价值 × 0.3%，最低 30 元' }] : []),
      ...(codFee ? [{ name: '代收手续费', amount: codFee, formula: '代收金额 × 0.6%' }] : [])
    ]
  }
}

function trackData(waybillId: number) {
  const wb = findDetail('tenant/waybills', waybillId) || waybills()[0]
  const nodes = [
    { node: '已受理', time: dt(-2, 9, 10), reporter: '客服小周', address: '上海总部', remark: '客户电话确认下单', photos: [] },
    { node: '已派车', time: dt(-2, 10, 20), reporter: '调度小陈', address: '上海总部', remark: `司机 张师傅 沪A·8F236`, photos: [] },
    { node: '已提货', time: dt(-2, 13, 40), reporter: '张师傅', address: wb.senderAddress, remark: '货物已清点，共 12 件', photos: ['提货-1.jpg'] },
    { node: '运输中', time: dt(-2, 14, 5), reporter: '张师傅', address: 'G60 沪昆高速', remark: '车辆已出发', photos: [] },
    { node: '中转中', time: dt(-1, 8, 30), reporter: '杭州场站', address: '杭州中转场', remark: '中转卸货完成', photos: ['中转-1.jpg'] },
    { node: '到达待派送', time: dt(0, 7, 50), reporter: '杭州网点', address: '杭州市余杭区', remark: '已到达派送网点', photos: [] },
    { node: '派送中', time: dt(0, 9, 5), reporter: '李师傅', address: '杭州市余杭区良渚街道', remark: '正在派送', photos: [] }
  ]
  const points = Array.from({ length: 36 }).map((_, i) => ({
    lat: 31.23 - i * 0.028,
    lng: 121.47 + i * 0.035,
    speed: 48 + (i % 7) * 6,
    time: dt(-1, 8 + Math.floor(i / 4), (i % 4) * 15),
    address: i < 12 ? '上海市境内' : i < 24 ? '嘉兴市境内' : '杭州市境内'
  }))
  return { waybill: wb, nodes, points, latest: points[points.length - 1] }
}

function reportData(type: string) {
  const map: Record<string, { title: string; categories: string[]; series: { name: string; data: number[] }[]; summary: Row }> = {
    volume: { title: '业务量趋势', categories: ['11-08', '11-09', '11-10', '11-11', '11-12', '11-13', '11-14'], series: [{ name: '票数', data: [86, 92, 88, 106, 121, 115, 128] }, { name: '吨数', data: [216, 238, 221, 268, 302, 289, 318] }], summary: { 总票数: 736, 总吨数: 1852, 环比: 12.6 } },
    revenue: { title: '营收与毛利', categories: ['11-08', '11-09', '11-10', '11-11', '11-12', '11-13', '11-14'], series: [{ name: '营收', data: [72000, 78600, 74200, 90800, 102400, 98600, 112000] }, { name: '毛利', data: [21600, 24200, 22800, 28900, 33100, 31200, 35600] }], summary: { 营收: 628600, 毛利: 197400, 毛利率: 31.4 } },
    'line-profit': { title: '线路盈亏', categories: ['上海→杭州', '上海→广州', '苏州→北京', '宁波→厦门', '杭州→深圳'], series: [{ name: '营收', data: [42000, 68000, 52000, 31000, 47000] }, { name: '成本', data: [29000, 52000, 38000, 27000, 39000] }], summary: { 盈利线路: 4, 亏损线路: 1, 平均毛利率: 27.8 } },
    timeliness: { title: '时效达成率', categories: ['上海→杭州', '上海→广州', '苏州→北京', '宁波→厦门', '杭州→深圳'], series: [{ name: '达成率', data: [98.2, 92.6, 94.1, 89.5, 91.3] }], summary: { 综合达成率: 93.1, 超时票数: 18 } },
    exception: { title: '异常率', categories: ['延误', '破损', '拒收', '改址', '无人收货'], series: [{ name: '异常数', data: [12, 5, 3, 2, 4] }], summary: { 异常率: 3.6, 已处理: 21, 待处理: 5 } },
    driver: { title: '司机绩效', categories: ['张师傅', '李师傅', '王师傅', '赵师傅', '刘师傅'], series: [{ name: '完成票数', data: [68, 62, 59, 51, 47] }, { name: '准时率', data: [98, 96, 94, 91, 89] }], summary: { 平均准时率: 93.6, 平均评分: 4.6 } },
    customer: { title: '客户贡献', categories: ['上海示例制造', '杭州云仓', '苏州恒达', '广州南方家居', '深圳鲜达'], series: [{ name: '营收', data: [128000, 98000, 76000, 54000, 48000] }], summary: { Top5占比: 62.8, 活跃客户: 42 } },
    carrier: { title: '承运商评分', categories: ['安达运输', '顺捷物流', '联运天下', '恒通冷链'], series: [{ name: '评分', data: [4.8, 4.5, 4.2, 3.9] }], summary: { 合作承运商: 7, 平均评分: 4.35 } }
  }
  return map[type] || map.volume
}

function dashboardPlatform() {
  return {
    tenantTotal: 12,
    trialCount: 2,
    arrearsCount: 2,
    expiringCount: 3,
    waybillTotal: 18620,
    exceptionCount: 36,
    monthlyRevenue: 286400,
    activeTenant: 9,
    expiringTenants: tenants().filter((t) => t.expireAt && new Date(t.expireAt).getTime() < new Date('2026-12-20').getTime()).slice(0, 5),
    trend: {
      categories: ['06月', '07月', '08月', '09月', '10月', '11月'],
      newTenants: [1, 2, 1, 3, 2, 4],
      revenue: [168000, 192000, 210000, 236000, 264000, 286400]
    }
  }
}

function dashboardTenant() {
  return {
    todayNew: 18,
    inTransit: 26,
    waitDispatch: 7,
    exceptions: 5,
    todayRevenue: 48620.5,
    arrearsCustomers: 3,
    expiringQualifications: 4,
    monthWaybills: 386,
    monthRevenue: 1248600,
    grossProfit: 386200,
    statusDistribution: [
      { name: '待调度', value: 7 }, { name: '已派车', value: 9 }, { name: '运输中', value: 16 },
      { name: '异常处理中', value: 5 }, { name: '已签收', value: 42 }, { name: '已完结', value: 128 }
    ],
    trend: {
      categories: ['11-08', '11-09', '11-10', '11-11', '11-12', '11-13', '11-14'],
      waybills: [86, 92, 88, 106, 121, 115, 128],
      revenue: [72000, 78600, 74200, 90800, 102400, 98600, 112000]
    },
    todos: [
      { title: '待调度运单 7 单', type: '调度', level: 'warning', time: '刚刚' },
      { title: '异常单待处理 5 单', type: '异常', level: 'danger', time: '12 分钟前' },
      { title: '回单待审核 4 份', type: '回单', level: 'warning', time: '30 分钟前' },
      { title: '对账单待确认 3 份', type: '财务', level: 'primary', time: '1 小时前' }
    ],
    expiring: qualifications().slice(0, 4)
  }
}

function dispatchBoard() {
  const wbs = waybills()
  const columns = [
    { key: '待调度', title: '待调度', list: wbs.filter((w) => w.status === '待调度').slice(0, 4) },
    { key: '已派车', title: '已派车', list: wbs.filter((w) => w.status === '已派车').slice(0, 4) },
    { key: '在途', title: '在途', list: wbs.filter((w) => ['运输中', '中转中', '到达待派送', '派送中', '已提货'].includes(w.status)).slice(0, 4) },
    { key: '异常', title: '异常', list: wbs.filter((w) => w.status === '异常处理中').slice(0, 4) }
  ]
  const vehicles = [
    { id: 1, plateNo: '沪A·8F236', status: '在途', statusName: '在途', lat: 30.87, lng: 121.1, waybillNo: 'YH2026111410005', driverName: '张师傅', x: 31, y: 48 },
    { id: 2, plateNo: '浙B·5K129', status: '空闲', statusName: '空闲', lat: 30.27, lng: 120.15, waybillNo: '', driverName: '李师傅', x: 62, y: 62 },
    { id: 3, plateNo: '苏E·9M668', status: '异常', statusName: '异常', lat: 31.3, lng: 120.6, waybillNo: 'YH2026111410009', driverName: '王师傅', x: 48, y: 36 },
    { id: 4, plateNo: '粤A·3Q771', status: '在途', statusName: '在途', lat: 29.8, lng: 121.5, waybillNo: 'YH2026111410012', driverName: '赵师傅', x: 72, y: 30 },
    { id: 5, plateNo: '沪B·7H552', status: '空闲', statusName: '空闲', lat: 31.23, lng: 121.47, waybillNo: '', driverName: '刘师傅', x: 20, y: 22 },
    { id: 6, plateNo: '浙A·2N883', status: '在途', statusName: '在途', lat: 30.5, lng: 120.9, waybillNo: 'YH2026111410015', driverName: '陈师傅', x: 55, y: 75 }
  ]
  return { vehicles, columns }
}

function statementDetail(id: number) {
  const st = findDetail('tenant/statements', id) || statements()[0]
  const items = statementItems(id)
  const totalAmount = items.reduce((s, i) => s + i.amount, 0)
  const payableTotal = items.reduce((s, i) => s + i.payable, 0)
  const variance = items.reduce((s, i) => s + i.variance, 0)
  return {
    statement: { ...st, totalAmount, confirmedAmount: st.confirmedAmount || 0, variance },
    items,
    totals: { amount: totalAmount, payable: payableTotal, grossProfit: totalAmount - payableTotal, variance },
    logs: [
      { time: dt(-15, 10, 0), operator: '财务小李', action: '生成对账单', remark: `共 ${items.length} 票` },
      { time: dt(-14, 15, 20), operator: '系统', action: '推送客户', remark: '已通过短信与公众号推送' },
      { time: dt(-12, 9, 30), operator: '客户', action: '查看对账单', remark: 'IP 116.236.12.8' }
    ]
  }
}

function claimDetail(id: number) {
  const claim = findDetail('tenant/claims', id) || claims()[0]
  const steps = [
    { key: '立案', title: '立案', status: '已立案', time: dt(-12, 9, 40), operator: '客服小周', remark: '客户提交破损照片 4 张' },
    { key: '定责', title: '定责', status: claim.status === '待立案' ? '待处理' : '已完成', time: dt(-10, 14, 20), operator: '运营主管', remark: `责任方：${claim.liability}` },
    { key: '审批', title: '审批', status: ['待审批', '已赔付', '已结案'].includes(claim.status) ? '已完成' : '待处理', time: dt(-8, 10, 0), operator: '财务主管', remark: '赔付金额审批' },
    { key: '赔付', title: '赔付', status: ['已赔付', '已结案'].includes(claim.status) ? '已完成' : '待处理', time: dt(-5, 16, 0), operator: '财务小李', remark: '银行转账' },
    { key: '结案', title: '结案', status: claim.status === '已结案' ? '已完成' : '待处理', time: claim.status === '已结案' ? dt(-3, 10, 0) : '', operator: '客服小周', remark: '' }
  ]
  return {
    claim,
    steps,
    evidences: [
      { name: '货物破损照片-1.jpg', type: '图片' },
      { name: '货物破损照片-2.jpg', type: '图片' },
      { name: '签收单.jpg', type: '图片' },
      { name: '定损报告.pdf', type: '文件' }
    ],
    timeline: steps.filter((s) => s.time).map((s) => ({ time: s.time, title: s.title, operator: s.operator, remark: s.remark }))
  }
}

function customerDetail(id: number) {
  const c = findDetail('tenant/customers', id) || customers()[0]
  return {
    ...c,
    contacts: [
      { id: 1, name: c.contact, phone: c.phone, title: '物流负责人', email: 'logistics@example.com', primary: true },
      { id: 2, name: '王会计', phone: '13800002222', title: '财务对接', email: 'finance@example.com', primary: false }
    ],
    addresses: [
      { id: 1, name: '发货地址', contact: c.contact, phone: c.phone, province: '上海市', city: '上海', detail: c.address, type: '发货', primary: true },
      { id: 2, name: '收货地址', contact: '刘经理', phone: '13900003333', province: '浙江省', city: '杭州', detail: '杭州市余杭区良渚街道 2 号', type: '收货', primary: false }
    ],
    agreements: agreements().filter((a) => a.customerName === c.name).slice(0, 4).length ? agreements().filter((a) => a.customerName === c.name) : agreements().slice(0, 2),
    followups: [
      { id: 1, time: dt(-3, 10, 30), type: '电话回访', content: '确认本月发货计划，预计 40 票。', operator: '王销售' },
      { id: 2, time: dt(-12, 15, 0), type: '上门拜访', content: '沟通月结账期与协议价续签。', operator: '王销售' },
      { id: 3, time: dt(-30, 9, 0), type: '系统提醒', content: '客户信用额度使用率达 82%，建议关注回款。', operator: '系统' }
    ],
    stats: { waybillCount: 168, totalReceivable: c.receivable, monthWaybills: 24, avgAmount: 682 }
  }
}

function waybillTimeline(id: number) {
  const wb = findDetail('tenant/waybills', id) || waybills()[0]
  const t = trackData(id)
  return {
    waybill: wb,
    nodes: t.nodes,
    docs: [
      { name: '托运单.pdf', type: '托运单', createdAt: dt(-2, 9, 20) },
      { name: '回单.jpg', type: '回单', createdAt: dt(-1, 15, 0) }
    ],
    photos: ['装车照片-1.jpg', '装车照片-2.jpg', '回单照片-1.jpg'],
    costs: {
      receivable: [
        { name: '基础运费', amount: wb.receivableAmount * 0.78 },
        { name: '燃油附加费', amount: wb.receivableAmount * 0.1 },
        { name: '装卸费', amount: wb.receivableAmount * 0.12 }
      ],
      payable: [
        { name: '承运运费', amount: wb.payableAmount * 0.85 },
        { name: '中转费', amount: wb.payableAmount * 0.15 }
      ]
    },
    logs: [
      { time: dt(-2, 9, 10), operator: '客服小周', action: '创建运单', remark: '后台代录' },
      { time: dt(-2, 10, 20), operator: '调度小陈', action: '派车', remark: '沪A·8F236 / 张师傅' },
      { time: dt(-2, 13, 40), operator: '张师傅', action: '提货签到', remark: 'GPS 已校验' },
      { time: dt(-1, 8, 30), operator: '杭州场站', action: '中转到达', remark: '货物完好' }
    ]
  }
}

export interface DemoResult {
  code: number
  message: string
  data: any
}

export async function demoRequest(
  method: string,
  url: string,
  params: Record<string, any> = {},
  body: any = {}
): Promise<DemoResult> {
  await new Promise((r) => setTimeout(r, 220 + Math.random() * 220))
  const clean = url.split('?')[0]
  const key = resourceKey(clean)
  const m = method.toUpperCase()
  const parts = key.split('/')
  const last = parts[parts.length - 1]
  const secondLast = parts[parts.length - 2]
  const idNum = Number(last)

  // 认证
  if (key === 'auth/login' && m === 'POST') {
    const payload = typeof body === 'string' ? JSON.parse(body) : body
    if (payload.username !== 'admin' || payload.password !== 'admin123') {
      return { code: 400, message: '用户名或密码错误，演示账号为 admin / admin123', data: null }
    }
    const scope = payload.scope === 'platform' ? 'platform' : 'tenant'
    return ok({
      token: `demo-${scope}-token-${Date.now()}`,
      user: {
        id: scope === 'platform' ? 1 : 100,
        username: 'admin',
        nickname: scope === 'platform' ? '平台超管' : '企业管理员',
        scope,
        tenantId: scope === 'tenant' ? 1 : null,
        tenantName: scope === 'tenant' ? '示例物流有限公司' : '',
        orgName: scope === 'tenant' ? '上海总部' : '平台运营中心'
      },
      roles: scope === 'platform' ? ['平台超管'] : ['企业管理员'],
      menus: [],
      buttons: ['*']
    })
  }
  if (key === 'auth/me' && m === 'GET') {
    const token = localStorage.getItem('wuliu_token') || ''
    const scope = token.includes('platform') ? 'platform' : 'tenant'
    return ok({
      user: {
        id: scope === 'platform' ? 1 : 100,
        username: 'admin',
        nickname: scope === 'platform' ? '平台超管' : '企业管理员',
        scope,
        tenantId: scope === 'tenant' ? 1 : null,
        tenantName: scope === 'tenant' ? '示例物流有限公司' : '',
        orgName: scope === 'tenant' ? '上海总部' : '平台运营中心'
      },
      roles: scope === 'platform' ? ['平台超管'] : ['企业管理员'],
      menus: [],
      buttons: ['*']
    })
  }
  if (key === 'auth/logout') return ok(true)
  if (key === 'auth/password') return ok(true)

  if (key === 'platform/dashboard') return ok(dashboardPlatform())
  if (key === 'tenant/dashboard') return ok(dashboardTenant())
  if (key === 'tenant/pricing/quote' && m === 'POST') {
    const payload = typeof body === 'string' ? JSON.parse(body) : body
    return ok(quoteResult(payload))
  }
  if (key === 'tenant/dispatch/board') return ok(dispatchBoard())

  if (key === 'tenant/system/menus' && m === 'GET') return ok(menusConfig())
  if (key === 'tenant/system/menus' && m === 'PUT') return ok(true)
  if (key === 'tenant/system/configs' && m === 'GET') return ok(configGroups())
  if (key === 'tenant/system/configs' && m === 'PUT') return ok(true)
  if (key === 'tenant/system/storage' && m === 'GET') {
    return ok({
      current: 'aliyun-oss',
      drivers: [
        { key: 'local', name: '本地存储', desc: '服务器本地磁盘，适合小规模部署', configured: false },
        { key: 's3', name: 'Amazon S3', desc: '海外节点，标准 S3 协议', configured: false },
        { key: 'aliyun-oss', name: '阿里云 OSS', desc: '国内低延迟，支持图片处理', configured: true },
        { key: 'tencent-cos', name: '腾讯云 COS', desc: '与微信生态集成友好', configured: false },
        { key: 'minio', name: 'MinIO', desc: '私有化部署，S3 兼容', configured: false }
      ],
      config: {
        driver: 'aliyun-oss',
        endpoint: 'oss-cn-shanghai.aliyuncs.com',
        bucket: 'quantaida-tenant-001',
        accessKey: 'LTAI5t••••••••',
        secretKey: '••••••••••••',
        domain: 'https://cdn.quantaida.cloud',
        region: 'cn-shanghai',
        pathPrefix: 'tenant/1/'
      }
    })
  }
  if (key === 'tenant/system/storage/apply' && m === 'POST') return ok(true)
  if (key === 'tenant/system/sso' && m === 'GET') {
    return ok({
      enabled: true,
      provider: 'CAS',
      serverUrl: 'https://sso.quantaida.cloud/cas',
      clientId: 'wuliu-admin',
      clientSecret: '••••••••',
      loginUrl: 'https://sso.quantaida.cloud/cas/login',
      callbackUrl: 'https://admin.quantaida.cloud/api/auth/sso/cas',
      autoCreate: true
    })
  }
  if (key === 'tenant/system/sso' && m === 'PUT') return ok(true)
  if (key === 'tenant/system/messages' && m === 'GET') return ok(messages())
  if (key === 'tenant/system/message-templates' && m === 'GET') return ok(paginate(messages().templates, params))
  if (key === 'tenant/system/message-logs' && m === 'GET') return ok(paginate(messages().logs, params))
  if (key === 'tenant/system/message-templates' && m === 'POST') return ok({ id: nextId() })
  if (last === 'test' && key.includes('message-templates')) return ok({ sent: true })
  if (last === 'resend' && key.includes('message-logs')) return ok({ sent: true })

  if (parts[0] === 'tenant' && parts[1] === 'tracks') {
    const wid = Number(parts[2] || 7001)
    const t = trackData(wid)
    if (last === 'replay') return ok({ points: t.points, duration: 120, waybillNo: t.waybill.waybillNo })
    return ok(t)
  }

  if (parts[0] === 'tenant' && parts[1] === 'waybills' && parts[2]) {
    if (last === 'timeline') return ok(waybillTimeline(Number(parts[2])))
    if (last === 'print') return ok({ html: '<h1>托运单</h1><p>演示数据，后端未连接。</p>' })
    if (last === 'label') return ok({ html: '<h1>标签 100×150</h1>' })
    if (last === 'action') return ok({ success: true })
    if (m === 'GET') return ok(findDetail('tenant/waybills', Number(parts[2])) || waybills()[0])
    if (m === 'PUT') {
      const item = findDetail('tenant/waybills', Number(parts[2]))
      if (item) Object.assign(item, body)
      return ok(true)
    }
  }

  if (parts[0] === 'tenant' && parts[1] === 'customers' && parts[2]) {
    const cid = Number(parts[2])
    if (last === 'contacts' || last === 'addresses') {
      if (m === 'GET') return ok([])
      return ok({ id: nextId() })
    }
    if (m === 'GET') return ok(customerDetail(cid))
  }

  if (parts[0] === 'tenant' && parts[1] === 'statements' && parts[2]) {
    if (m === 'GET' && !isNaN(idNum)) return ok(statementDetail(idNum))
    if (last === 'confirm' || last === 'dispute') return ok(true)
    if (last === 'pdf') return ok({ url: '' })
  }

  if (parts[0] === 'tenant' && parts[1] === 'claims' && parts[2]) {
    if (['liability', 'approve', 'pay', 'close'].includes(last)) return ok(true)
    if (m === 'GET' && !isNaN(idNum)) return ok(claimDetail(idNum))
  }

  if (parts[0] === 'tenant' && parts[1] === 'reports') {
    if (last === 'export') return ok({ url: 'demo://export.xlsx', purpose: params.purpose || body?.purpose })
    return ok(reportData(parts[2] || 'volume'))
  }

  if (parts[0] === 'tenant' && parts[1] === 'aging') {
    return ok({
      buckets: [
        { name: '0-30 天', amount: 128600, count: 18 },
        { name: '31-60 天', amount: 86200, count: 11 },
        { name: '61-90 天', amount: 42800, count: 6 },
        { name: '90 天以上', amount: 21600, count: 3 }
      ],
      list: receivables().filter((r) => r.status !== '已结清'),
      total: 279200
    })
  }

  if (parts[0] === 'platform' && parts[1] === 'licenses' && m === 'GET') {
    const list = tenants().filter((t) => t.licenseStatus === '待审核' || t.licenseStatus === '已驳回').map((t, i) => ({
      id: 20001 + i,
      tenantId: t.id,
      tenantName: t.name,
      licenseType: ['营业执照', '道路运输经营许可证', '法人身份证'][i % 3],
      licenseNo: `91310000MA1FL${String(300 + i)}X`,
      status: ['待审核', '待审核', '已驳回'][i % 3],
      statusName: ['待审核', '待审核', '已驳回'][i % 3],
      submittedAt: dt(-3 + i, 11, 0),
      images: ['营业执照.jpg', '道路运输许可证.jpg'],
      rejectReason: i === 2 ? '道路运输经营许可证有效期已过，请重新上传' : ''
    }))
    return ok(paginate(list, params))
  }
  if (parts[0] === 'platform' && parts[1] === 'licenses' && last === 'review') return ok(true)

  if (parts[0] === 'tenant' && parts[1] === 'pricing' && parts[2] === 'quote') {
    return ok(quoteResult(body))
  }

  if (parts[0] === 'tenant' && parts[1] === 'dispatch') {
    if (last === 'assign' || last === 'reassign' || last === 'cancel') return ok({ success: true })
    if (parts[2] === 'pool') return ok(buildList('tenant/waybills').filter((w) => w.status === '待调度'))
    if (parts[2] === 'board') return ok(dispatchBoard())
  }

  if (parts[0] === 'tenant' && parts[1] === 'audits' && m === 'GET') {
    return ok(paginate(buildList('platform/audits'), params))
  }

  // 字典项
  if (parts[0] === 'tenant' && parts[1] === 'system' && parts[2] === 'dicts' && parts[4] === 'items') {
    return ok({ id: nextId() })
  }

  // 通用列表 / 详情 / CRUD
  if (m === 'GET') {
    if (!isNaN(idNum) && parts.length >= 3) {
      const item = findDetail(parts.slice(0, -1).join('/'), idNum)
      if (item) return ok(item)
      return { code: 404, message: '记录不存在', data: null }
    }
    let list = getList(key)
    // 自动补全缺失资源
    if (!list.length && parts[0] === 'tenant') {
      list = Array.from({ length: 6 }).map((_, i) => ({
        id: 20001 + i,
        name: `${parts[1]}示例 ${i + 1}`,
        no: `NO2026${String(5001 + i)}`,
        status: '正常',
        statusName: '正常',
        amount: 1000 + i * 300,
        createdAt: dt(-i, 10, 0)
      }))
      db.set(key, list)
    }
    if (key.endsWith('/expiring')) return ok(list)
    return ok(paginate(list, params))
  }

  if (m === 'POST') {
    const createKey = key
    const list = getList(createKey)
    const payload = typeof body === 'string' ? (body ? JSON.parse(body) : {}) : (body || {})
    const item = { id: nextId(), createdAt: dt(0, 10, 0), status: payload.status || '正常', statusName: payload.status || '正常', ...payload }
    list.unshift(item)
    return ok(item)
  }

  if (m === 'PUT') {
    const targetKey = parts.slice(0, -1).join('/')
    const item = findDetail(targetKey, idNum)
    const payload = typeof body === 'string' ? JSON.parse(body) : body
    if (item) Object.assign(item, payload)
    return ok(true)
  }

  if (m === 'DELETE') {
    const targetKey = parts.slice(0, -1).join('/')
    const list = getList(targetKey)
    const idx = list.findIndex((r) => Number(r.id) === idNum)
    if (idx >= 0) list.splice(idx, 1)
    return ok(true)
  }

  return ok(null)
}
