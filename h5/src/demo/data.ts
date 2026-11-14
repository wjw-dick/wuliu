import type { DriverTask, DriverUser, ReceiptData, SharedQuote } from '../types'

export const DEMO_QUOTE: SharedQuote = {
  quoteNo: 'BJ202611140001',
  origin: '上海',
  destination: '杭州',
  cargoType: '普货 · 机械配件',
  weight: 500,
  volume: 2.5,
  pieces: 10,
  vehicleType: '6.8 米厢式',
  customerName: '上海示例制造有限公司',
  createdAt: '2026-11-14 10:00:00',
  validUntil: '2026-11-15 10:00:00',
  totalAmount: 740,
  lines: [
    {
      name: '基础运费',
      amount: 600,
      formula: '首重 100kg/120 元 + 续重 400kg × 1.2 元/kg'
    },
    { name: '燃油附加', amount: 60, formula: '基础运费 × 10%' },
    { name: '装卸费', amount: 50, formula: '上门提货装卸 50 元/票' },
    { name: '上楼费', amount: 0, formula: '无需上楼' },
    { name: '保险', amount: 30, formula: '声明价值 10000 元 × 0.3%' },
    { name: '代收手续费', amount: 0, formula: '无代收货款' }
  ],
  remark: '报价有效期 24 小时，实际以客服确认为准；如需开票请联系客服。'
}

export function demoReceipt(waybillNo: string): ReceiptData {
  const no = waybillNo.trim() || 'YH0001202611140001'
  return {
    waybillNo: no,
    status: '已签收',
    statusName: '已签收',
    signedAt: '2026-11-14 16:42:00',
    signerName: '李明',
    signerPhone: '138****0002',
    signerType: '收货人本人',
    signatureImage: '/demo/signature.svg',
    coordinates: {
      lat: 30.2741,
      lng: 120.1551,
      address: '杭州市余杭区仓前街道文一西路 1500 号 8 幢 1 层'
    },
    sender: {
      name: '张经理',
      phone: '138****0001',
      address: '上海市嘉定区兴荣路 1288 号 A3 库'
    },
    receiver: {
      name: '李经理',
      phone: '138****0002',
      address: '杭州市余杭区仓前街道文一西路 1500 号 8 幢 1 层'
    },
    cargo: {
      name: '机械配件',
      pieces: 10,
      weight: 500,
      volume: 2.5
    },
    driver: {
      name: '王师傅',
      phone: '139****6688',
      vehicleNo: '沪B·8F216'
    },
    images: [
      { url: '/demo/receipt-1.svg', caption: '托运单' },
      { url: '/demo/receipt-2.svg', caption: '货物装车' },
      { url: '/demo/receipt-3.svg', caption: '签收回单' },
      { url: '/demo/receipt-4.svg', caption: '卸货现场' }
    ],
    company: {
      name: '上海驰远物流有限公司',
      phone: '400-880-1688'
    },
    remark: '外包装完好，件数相符，收货人当场验货后签收。'
  }
}

export const DEMO_DRIVER_USER: DriverUser = {
  name: '王师傅',
  phone: '139****6688',
  vehicleNo: '沪B·8F216'
}

export const DEMO_TASKS: DriverTask[] = [
  {
    id: 101,
    waybillNo: 'YH0001202611140001',
    status: '待提货',
    statusName: '待提货',
    origin: '上海',
    destination: '杭州',
    cargoName: '机械配件',
    pieces: 10,
    weight: 500,
    pickupTime: '2026-11-15 09:00:00',
    deliveryTime: '2026-11-16 18:00:00',
    sender: {
      name: '张经理',
      phone: '13800000001',
      address: '上海市嘉定区兴荣路 1288 号 A3 库'
    },
    receiver: {
      name: '李经理',
      phone: '13800000002',
      address: '杭州市余杭区仓前街道文一西路 1500 号 8 幢'
    }
  },
  {
    id: 102,
    waybillNo: 'YH0001202611140002',
    status: '运输中',
    statusName: '运输中',
    origin: '上海',
    destination: '南京',
    cargoName: '电子元器件',
    pieces: 24,
    weight: 320,
    pickupTime: '2026-11-14 08:30:00',
    deliveryTime: '2026-11-15 12:00:00',
    sender: {
      name: '陈主管',
      phone: '13700000011',
      address: '上海市青浦区华新镇华隆路 1777 号'
    },
    receiver: {
      name: '赵工',
      phone: '13700000022',
      address: '南京市江宁区秣陵街道天元中路 128 号'
    },
    node: '已装车'
  },
  {
    id: 103,
    waybillNo: 'YH0001202611130008',
    status: '待回单',
    statusName: '待回单',
    origin: '苏州',
    destination: '上海',
    cargoName: '服装鞋帽',
    pieces: 46,
    weight: 680,
    pickupTime: '2026-11-13 14:00:00',
    deliveryTime: '2026-11-14 12:00:00',
    sender: {
      name: '周女士',
      phone: '13600000031',
      address: '苏州市工业园区唯亭镇亭荣街 18 号'
    },
    receiver: {
      name: '吴店长',
      phone: '13600000032',
      address: '上海市静安区南京西路 1266 号'
    },
    node: '已签收'
  }
]

export const NODE_OPTIONS = [
  { value: '到达发货地', desc: '已到达提货点，准备验货装车' },
  { value: '已装车', desc: '货物装车完毕，准备发车' },
  { value: '到达中转场', desc: '到达分拨/中转场站' },
  { value: '到达收货地', desc: '到达收货地址，准备卸货' },
  { value: '已签收', desc: '收货人已签收' }
]
