import axios from 'axios'
export const http = axios.create({ baseURL: '/api', timeout: 30000 })
http.interceptors.response.use(
  (res) => {
    const d = res.data
    if (d && typeof d === 'object' && 'code' in d) return d.code === 0 ? d : Promise.reject(new Error(d.message))
    return d
  },
  (err) => Promise.reject(new Error(err.response?.data?.message || err.message || '网络异常'))
)
export const get = (url, params) => http.get(url, { params })
export const post = (url, data) => http.post(url, data)
