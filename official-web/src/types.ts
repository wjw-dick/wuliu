export interface ApiEnvelope<T> {
  code: number
  message: string
  data: T
}

export interface Site {
  id: number
  name: string
  city: string
  address: string
  phone: string
  businessHours: string
  serviceArea: string
  lat: number
  lng: number
  type?: string
}

export interface QuoteInput {
  origin: string
  destination: string
  cargoType: string
  weight: number
  volume: number
  pieces: number
  vehicleType: string
  value: number
  needLoading: boolean
  needUpstairs: boolean
  codAmount: number
  ladder: boolean
}

export interface QuoteDetailItem {
  name: string
  amount: number
  formula: string
}

export interface QuoteResult {
  quoteNo: string
  baseFreight: number
  fuelSurcharge: number
  loadingFee: number
  upstairsFee: number
  insuranceFee: number
  codFee: number
  remoteFee: number
  holidayFee: number
  totalAmount: number
  ruleVersion: string
  priceSource: string
  detail: QuoteDetailItem[]
  validUntil?: string
}

export interface TrackNode {
  time: string
  status: string
  statusName?: string
  description?: string
  location?: string
  operator?: string
}

export interface TrackResult {
  waybillNo: string
  status: string
  statusName: string
  origin: string
  destination: string
  cargoName: string
  pieces: number
  weight: number
  volume?: number
  senderName?: string
  receiverName?: string
  createdAt: string
  estimatedArrival: string
  driverName?: string
  vehicleNo?: string
  nodes: TrackNode[]
}

export interface ContentItem {
  id: number
  title: string
  summary: string
  cover?: string
  content?: string
  author?: string
  publishedAt: string
  views?: number
  category?: string
}

export interface Paged<T> {
  list: T[]
  total: number
  page: number
  size: number
}

export interface PriceGuideItem {
  title: string
  rule: string
  example: string
}

export interface PriceGuide {
  updatedAt: string
  intro: string
  items: PriceGuideItem[]
  surcharges: Array<{ name: string; standard: string; remark?: string }>
  notes: string[]
}

export interface LeadPayload {
  name: string
  phone: string
  company: string
  source: string
  remark?: string
}

export interface MonthlyPayload {
  company: string
  contact: string
  phone: string
  monthlyVolume: string
  routes: string
  settleCycle: string
  remark?: string
}
