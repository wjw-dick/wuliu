import { apiGet, apiPost } from './client'
import {
  DEMO_CONTENTS,
  DEMO_PRICE_GUIDE,
  DEMO_SITES,
  demoQuote,
  demoTrack
} from '../data/demo'
import type {
  ContentItem,
  LeadPayload,
  MonthlyPayload,
  Paged,
  PriceGuide,
  QuoteInput,
  QuoteResult,
  Site,
  TrackResult
} from '../types'

function asList<T>(data: unknown, fallback: T[]): T[] {
  if (Array.isArray(data)) return data as T[]
  if (data && typeof data === 'object' && Array.isArray((data as { list?: T[] }).list)) {
    return (data as { list: T[] }).list
  }
  return fallback
}

export async function getSites(): Promise<Site[]> {
  const data = await apiGet<Site[] | { list: Site[] }>(
    '/public/sites',
    undefined,
    DEMO_SITES
  )
  return asList(data, DEMO_SITES)
}

export async function getPriceGuide(): Promise<PriceGuide> {
  return apiGet<PriceGuide>('/public/price-guide', undefined, DEMO_PRICE_GUIDE)
}

export async function getContents(type = '资讯', page = 1, size = 50): Promise<Paged<ContentItem>> {
  const fallback: Paged<ContentItem> = {
    list: DEMO_CONTENTS,
    total: DEMO_CONTENTS.length,
    page: 1,
    size: DEMO_CONTENTS.length
  }
  const data = await apiGet<Paged<ContentItem> | ContentItem[]>(
    '/public/contents',
    { type, page, size },
    fallback
  )
  if (Array.isArray(data)) {
    return { list: data, total: data.length, page: 1, size: data.length }
  }
  const list = Array.isArray(data?.list) ? data.list : DEMO_CONTENTS
  return {
    list,
    total: typeof data?.total === 'number' ? data.total : list.length,
    page: typeof data?.page === 'number' ? data.page : 1,
    size: typeof data?.size === 'number' ? data.size : list.length
  }
}

export async function getContentDetail(id: number): Promise<ContentItem | null> {
  const paged = await getContents('资讯', 1, 100)
  const found = paged.list.find((item) => Number(item.id) === Number(id))
  if (found) {
    if (!found.content) {
      found.content = `<p>${found.summary || ''}</p>`
    }
    return found
  }
  return DEMO_CONTENTS.find((item) => item.id === Number(id)) || null
}

export async function queryQuote(input: QuoteInput): Promise<QuoteResult> {
  return apiPost<QuoteResult>('/public/quote', input, () => demoQuote(input))
}

export async function submitLead(payload: LeadPayload): Promise<boolean> {
  const result = await apiPost<unknown>('/public/lead', payload, true)
  return result !== false
}

export async function queryTrack(waybillNo: string, phone: string): Promise<TrackResult> {
  return apiGet<TrackResult>(
    '/public/track',
    { waybillNo, phone },
    () => demoTrack(waybillNo, phone)
  )
}

export async function applyMonthly(payload: MonthlyPayload): Promise<boolean> {
  const result = await apiPost<unknown>('/public/apply-monthly', payload, true)
  return result !== false
}
