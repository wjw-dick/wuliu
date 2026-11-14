import axios from 'axios'
import { ElMessage } from 'element-plus'

export const http = axios.create({ baseURL: '/api', timeout: 30000 })

let onUnauthorized = null
export function setUnauthorized(fn) { onUnauthorized = fn }

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('wl_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (res) => {
    const data = res.data
    if (data && typeof data === 'object' && 'code' in data) {
      if (data.code === 0) return data
      ElMessage.error(data.message || '操作失败')
      return Promise.reject(new Error(data.message))
    }
    return data
  },
  (err) => {
    const status = err.response?.status
    const msg = err.response?.data?.message || err.message || '网络异常'
    if (status === 401) {
      if (onUnauthorized) onUnauthorized()
      ElMessage.error(msg || '登录已失效')
    } else {
      ElMessage.error(msg)
    }
    return Promise.reject(err)
  }
)

// 通用请求封装
export const get = (url, params) => http.get(url, { params })
export const post = (url, data, config) => http.post(url, data, config)
export const put = (url, data) => http.put(url, data)
export const del = (url) => http.delete(url)

export const login = (data) => post('/auth/login', data)
export const me = () => get('/auth/me')
export const logout = () => post('/auth/logout')
