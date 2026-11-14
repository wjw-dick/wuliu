import { apiGet, apiPost, getDriverToken, setDriverToken } from './client'
import { DEMO_DRIVER_USER, DEMO_TASKS, demoReceipt } from '../demo/data'
import type { DriverTask, LoginResult, ReceiptData, SharedQuote } from '../types'

export async function createQuoteShare(
  quote: SharedQuote,
  fallbackUrl: string
): Promise<string> {
  const payload = {
    quoteNo: quote.quoteNo,
    origin: quote.origin,
    destination: quote.destination,
    cargoType: quote.cargoType,
    weight: quote.weight,
    volume: quote.volume,
    pieces: quote.pieces,
    vehicleType: quote.vehicleType,
    totalAmount: quote.totalAmount,
    lines: quote.lines,
    validUntil: quote.validUntil
  }
  const data = await apiPost<{ url?: string; shareUrl?: string; link?: string } | string>(
    '/h5/quote/share',
    payload,
    () => ({ url: fallbackUrl })
  )
  if (typeof data === 'string') return data
  return data?.url || data?.shareUrl || data?.link || fallbackUrl
}

export async function getReceipt(waybillNo: string, sign: string): Promise<ReceiptData> {
  const data = await apiGet<ReceiptData>(
    `/h5/receipt/${encodeURIComponent(waybillNo)}`,
    { sign },
    () => demoReceipt(waybillNo)
  )
  if (!data) return demoReceipt(waybillNo)

  const rawImages = (data as unknown as { images?: Array<string | { url: string; caption?: string }> }).images
  const images = Array.isArray(rawImages)
    ? rawImages.map((item, index) =>
        typeof item === 'string'
          ? { url: item, caption: `回单照片 ${index + 1}` }
          : { url: item.url, caption: item.caption || `回单照片 ${index + 1}` }
      )
    : []

  return {
    ...data,
    images: images.length ? images : demoReceipt(waybillNo).images,
    coordinates: data.coordinates || demoReceipt(waybillNo).coordinates,
    company: data.company || { name: '上海驰远物流有限公司', phone: '400-880-1688' }
  }
}

export async function driverLogin(phone: string, code: string): Promise<LoginResult> {
  const data = await apiPost<LoginResult | string>('/driver/login', { phone, code }, () => ({
    token: 'demo-driver-token',
    user: DEMO_DRIVER_USER
  }))
  if (typeof data === 'string') {
    setDriverToken(data)
    return { token: data, user: { ...DEMO_DRIVER_USER, phone } }
  }
  if (data?.token) setDriverToken(data.token)
  return {
    token: data?.token || 'demo-driver-token',
    user: data?.user || { ...DEMO_DRIVER_USER, phone }
  }
}

export function logoutDriver() {
  setDriverToken('')
}

export async function getDriverTasks(): Promise<DriverTask[]> {
  const data = await apiGet<DriverTask[] | { list: DriverTask[] }>(
    '/driver/tasks',
    undefined,
    DEMO_TASKS
  )
  if (Array.isArray(data)) return data
  if (data && Array.isArray((data as { list?: DriverTask[] }).list)) {
    return (data as { list: DriverTask[] }).list
  }
  return DEMO_TASKS
}

export async function reportTaskNode(
  taskId: number,
  payload: { node: string; lat: number; lng: number; remark?: string }
): Promise<boolean> {
  const result = await apiPost<unknown>(`/driver/tasks/${taskId}/nodes`, payload, true)
  return result !== false
}

export async function uploadTaskReceipt(taskId: number, images: string[]): Promise<boolean> {
  const result = await apiPost<unknown>(`/driver/tasks/${taskId}/receipt`, { images }, true)
  return result !== false
}

export { getDriverToken }
