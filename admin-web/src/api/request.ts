import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { demoRequest } from '@/mock/demo'
import { markDemoMode } from '@/utils/demo-state'

export const TOKEN_KEY = 'wuliu_token'
export const USER_KEY = 'wuliu_user'
export const SCOPE_KEY = 'wuliu_scope'

declare module 'axios' {
  export interface AxiosRequestConfig {
    skipDemo?: boolean
  }
}

export interface PageResult<T = any> {
  list: T[]
  total: number
  page: number
  size: number
}

export interface PageQuery {
  page?: number
  size?: number
  keyword?: string
  [key: string]: any
}

const service = axios.create({
  baseURL: '/api',
  timeout: 12000
})

function clearSession(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  localStorage.removeItem(SCOPE_KEY)
  localStorage.removeItem('wuliu_tabs')
}

let redirecting = false
function handleUnauthorized(): void {
  clearSession()
  if (redirecting || window.location.pathname === '/login') return
  redirecting = true
  const redirect = encodeURIComponent(window.location.pathname + window.location.search)
  window.location.replace(`/login?redirect=${redirect}`)
  setTimeout(() => (redirecting = false), 1500)
}

service.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  const method = (config.method || 'get').toUpperCase()
  if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)) {
    config.headers = config.headers || {}
    config.headers['X-Idempotency-Key'] = `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 0) return body.data
      if (body.code === 401) {
        handleUnauthorized()
        return Promise.reject(new Error(body.message || '登录已过期，请重新登录'))
      }
      ElMessage.error(body.message || '操作失败，请稍后重试')
      return Promise.reject(new Error(body.message || '操作失败'))
    }
    return body
  },
  async (error) => {
    const status = error?.response?.status
    if (status === 401) {
      handleUnauthorized()
      return Promise.reject(error)
    }
    const config = error?.config
    const networkFailure =
      !error?.response ||
      error?.code === 'ERR_NETWORK' ||
      error?.code === 'ECONNABORTED' ||
      error?.code === 'ERR_CONNECTION_REFUSED' ||
      status >= 500

    if (networkFailure && config && !config.skipDemo) {
      try {
        let body = config.data
        if (typeof body === 'string') {
          try {
            body = JSON.parse(body)
          } catch {
            /* keep raw */
          }
        }
        const result = await demoRequest(
          config.method || 'get',
          String(config.url || ''),
          config.params || {},
          body || {}
        )
        markDemoMode()
        if (result.code === 0) return result.data
        ElMessage.error(result.message || '演示数据不可用')
        return Promise.reject(new Error(result.message))
      } catch (demoError: any) {
        ElMessage.error(demoError?.message || '网络异常，请稍后重试')
        return Promise.reject(demoError)
      }
    }

    const message =
      error?.response?.data?.message ||
      (status === 404 ? '请求的资源不存在' : status === 403 ? '无权限执行该操作' : '网络异常，请稍后重试')
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

function request<T>(config: AxiosRequestConfig): Promise<T> {
  return service.request(config) as unknown as Promise<T>
}

export function get<T = any>(url: string, params?: Record<string, any>, config?: AxiosRequestConfig): Promise<T> {
  return request<T>({ url, method: 'get', params, ...config })
}

export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return request<T>({ url, method: 'post', data, ...config })
}

export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return request<T>({ url, method: 'put', data, ...config })
}

export function del<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return request<T>({ url, method: 'delete', ...config })
}

export default service
