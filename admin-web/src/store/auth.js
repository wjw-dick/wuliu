import { defineStore } from 'pinia'
import { login as apiLogin, me as apiMe, logout as apiLogout } from '../api/http'

export const useAuth = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('wl_token') || '',
    user: JSON.parse(localStorage.getItem('wl_user') || 'null'),
    scope: localStorage.getItem('wl_scope') || ''
  }),
  getters: {
    isLogin: (s) => !!s.token
  },
  actions: {
    async login(username, password, scope) {
      const res = await apiLogin({ username, password, scope })
      this.token = res.data.token
      this.scope = res.data.user.scope
      this.user = res.data.user
      localStorage.setItem('wl_token', this.token)
      localStorage.setItem('wl_scope', this.scope)
      localStorage.setItem('wl_user', JSON.stringify(this.user))
      return res.data
    },
    async refreshMe() {
      try {
        const res = await apiMe()
        this.user = res.data.user
        localStorage.setItem('wl_user', JSON.stringify(this.user))
        return res.data
      } catch (e) {
        this.logout()
        return null
      }
    },
    logout() {
      apiLogout().catch(() => {})
      this.token = ''
      this.user = null
      this.scope = ''
      localStorage.removeItem('wl_token')
      localStorage.removeItem('wl_scope')
      localStorage.removeItem('wl_user')
    }
  }
})
