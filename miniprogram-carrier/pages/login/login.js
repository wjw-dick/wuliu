const api = require('../../utils/request')
Page({
  data: { phone: '', code: '', loading: false, msg: '', ok: false },
  onPhone(e) { this.setData({ phone: e.detail.value }) },
  onCode(e) { this.setData({ code: e.detail.value }) },
  async login() {
    if (!this.data.phone) { this.setData({ msg: '请输入手机号', ok: false }); return }
    if (this.data.code !== '1234') { this.setData({ msg: '验证码错误（演示：1234）', ok: false }); return }
    this.setData({ loading: true, msg: '' })
    try {
      // 演示：承运商登录走平台登录（scope=tenant）简化通道；实际为手机号+验证码换取 token
      const data = await api.post('/auth/login', { username: 'admin', password: 'admin123', scope: 'tenant' })
      const app = getApp()
      app.globalData.token = data.token
      wx.setStorageSync('mp_carrier_token', data.token)
      this.setData({ ok: true, msg: '登录成功' })
      setTimeout(() => wx.redirectTo({ url: '/pages/index/index' }), 500)
    } catch (e) { this.setData({ msg: e.message, ok: false }) }
    finally { this.setData({ loading: false }) }
  }
})
