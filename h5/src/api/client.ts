import axios from 'axios'
import { ref } from 'vue'
import type { ApiEnvelope } from '../types'

export const demoMode = ref(false)
export const demoNotice = ref('')

const TOKEN_KEY = 'chiyuan_h5_driver_token'

export function getDriverToken(): string {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setDriverToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearDriverToken() {
  localStorage.removeItem(TOKEN_KEY)
}

const http = axios.create({
  baseURL: '/api',
  timeout: 9000,
  headers: { 'Content-Type': 'application/json' }
})

http.interceptors.request.use((config) => {
  const token = getDriverToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

function markDemo(error: unknown) {
  demoMode.value = true
  if (axios.isAxiosError(error)) {
    demoNotice.value = error.code === 'ECONNABORTED' ? '接口响应超时' : '后端服务暂未连接'
  } else if (error instanceof Error) {
    demoNotice.value = error.message
  } else {
    demoNotice.value = '后端服务暂未连接'
  }
}

function unwrap<T>(body: ApiEnvelope<T>): T {
  if (!body || typeof body.code !== 'number') {
    throw new Error('接口返回格式异常')
  }
  if (body.code !== 0) {
    throw new Error(body.message || '接口调用失败')
  }
  return body.data
}

function resolveFallback<T>(fallback: T | (() => T)): T {
  return typeof fallback === 'function' ? (fallback as () => T)() : fallback
}

export async function apiGet<T>(
  url: string,
  params: Record<string, unknown> | undefined,
  fallback: T | (() => T)
): Promise<T> {
  try {
    const { data } = await http.get<ApiEnvelope<T>>(url, { params })
    return unwrap(data)
  } catch (error) {
    markDemo(error)
    return resolveFallback(fallback)
  }
}

export async function apiPost<T>(
  url: string,
  payload: unknown,
  fallback: T | (() => T)
): Promise<T> {
  try {
    const { data } = await http.post<ApiEnvelope<T>>(url, payload)
    return unwrap(data)
  } catch (error) {
    markDemo(error)
    return resolveFallback(fallback)
  }
}
