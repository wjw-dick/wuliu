import axios from 'axios'

export const http = axios.create({ baseURL: '/api', timeout: 30000 })

http.interceptors.response.use(
  (res) => {
    const data = res.data
    if (data && typeof data === 'object' && 'code' in data) {
      if (data.code === 0) return data
      return Promise.reject(new Error(data.message || '操作失败'))
    }
    return data
  },
  (err) => Promise.reject(new Error(err.response?.data?.message || err.message || '网络异常'))
)

export const get = (url, params) => http.get(url, { params })
export const post = (url, data) => http.post(url, data)
