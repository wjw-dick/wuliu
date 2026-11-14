import { get, post, put, del, type PageQuery } from './request'

export interface TenantPayload {
  name: string
  industry?: string
  region?: string
  contact: string
  phone: string
  planId?: number
  planName?: string
  seats: number
  creditCode?: string
  address?: string
  licenseImages?: string[]
  remark?: string
}

export const getPlatformDashboard = () => get('/platform/dashboard')

export const getTenants = (params: PageQuery) => get('/platform/tenants', params)
export const createTenant = (data: TenantPayload) => post('/platform/tenants', data)
export const updateTenant = (id: number, data: Partial<TenantPayload>) => put(`/platform/tenants/${id}`, data)
export const deleteTenant = (id: number) => del(`/platform/tenants/${id}`)
export const renewTenant = (id: number, months: number) => post(`/platform/tenants/${id}/renew`, { months })
export const suspendTenant = (id: number, reason: string) => post(`/platform/tenants/${id}/suspend`, { reason })
export const enableTenant = (id: number) => post(`/platform/tenants/${id}/enable`)

export const getLicenses = (params: PageQuery) => get('/platform/licenses', params)
export const reviewLicense = (id: number, pass: boolean, reason: string) =>
  post(`/platform/licenses/${id}/review`, { pass, reason })

export const getPlans = (params?: PageQuery) => get('/platform/plans', params)
export const createPlan = (data: any) => post('/platform/plans', data)
export const updatePlan = (id: number, data: any) => put(`/platform/plans/${id}`, data)
export const deletePlan = (id: number) => del(`/platform/plans/${id}`)

export const getSeats = (params?: PageQuery) => get('/platform/seats', params)
export const addSeats = (data: any) => post('/platform/seats', data)

export const getBills = (params: PageQuery) => get('/platform/bills', params)
export const createBill = (data: any) => post('/platform/bills', data)
export const payBill = (id: number, data: { amount: number; method: string; remark?: string }) =>
  post(`/platform/bills/${id}/pay`, data)

export const getPlatformUsers = (params: PageQuery) => get('/platform/users', params)
export const createPlatformUser = (data: any) => post('/platform/users', data)
export const updatePlatformUser = (id: number, data: any) => put(`/platform/users/${id}`, data)
export const deletePlatformUser = (id: number) => del(`/platform/users/${id}`)

export const getPlatformRoles = (params?: PageQuery) => get('/platform/roles', params)
export const createPlatformRole = (data: any) => post('/platform/roles', data)
export const updatePlatformRole = (id: number, data: any) => put(`/platform/roles/${id}`, data)
export const deletePlatformRole = (id: number) => del(`/platform/roles/${id}`)

export const getAudits = (params: PageQuery) => get('/platform/audits', params)

export const getPlatformAnnouncements = (params: PageQuery) => get('/platform/announcements', params)
export const createPlatformAnnouncement = (data: any) => post('/platform/announcements', data)
export const updatePlatformAnnouncement = (id: number, data: any) => put(`/platform/announcements/${id}`, data)
export const deletePlatformAnnouncement = (id: number) => del(`/platform/announcements/${id}`)

export const getPlatformDicts = (params?: PageQuery) => get('/platform/dicts', params)
export const createPlatformDict = (data: any) => post('/platform/dicts', data)
export const updatePlatformDict = (id: number, data: any) => put(`/platform/dicts/${id}`, data)
export const deletePlatformDict = (id: number) => del(`/platform/dicts/${id}`)

export const getPlatformConfigs = () => get('/platform/configs')
export const updatePlatformConfigs = (data: any) => put('/platform/configs', data)
