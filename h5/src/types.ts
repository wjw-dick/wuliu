export interface ApiEnvelope<T> {
  code: number
  message: string
  data: T
}

export interface QuoteLine {
  name: string
  amount: number
  formula?: string
}

export interface SharedQuote {
  quoteNo: string
  origin: string
  destination: string
  cargoType: string
  weight: number
  volume: number
  pieces: number
  vehicleType: string
  customerName?: string
  createdAt: string
  validUntil: string
  lines: QuoteLine[]
  totalAmount: number
  remark?: string
}

export interface Coordinates {
  lat: number
  lng: number
  address: string
}

export interface ReceiptParty {
  name: string
  phone: string
  address: string
}

export interface ReceiptImage {
  url: string
  caption: string
}

export interface ReceiptData {
  waybillNo: string
  status: string
  statusName: string
  signedAt: string
  signerName: string
  signerPhone: string
  signerType: string
  signatureImage?: string
  coordinates: Coordinates
  sender: ReceiptParty
  receiver: ReceiptParty
  cargo: {
    name: string
    pieces: number
    weight: number
    volume: number
  }
  driver: {
    name: string
    phone: string
    vehicleNo: string
  }
  images: ReceiptImage[]
  pdfUrl?: string
  company: {
    name: string
    phone: string
  }
  remark?: string
}

export interface DriverParty {
  name: string
  phone: string
  address: string
}

export interface DriverTask {
  id: number
  waybillNo: string
  status: string
  statusName: string
  origin: string
  destination: string
  cargoName: string
  pieces: number
  weight: number
  pickupTime: string
  deliveryTime: string
  sender: DriverParty
  receiver: DriverParty
  node?: string
}

export interface DriverUser {
  name: string
  phone: string
  vehicleNo?: string
}

export interface LoginResult {
  token: string
  user: DriverUser
}
