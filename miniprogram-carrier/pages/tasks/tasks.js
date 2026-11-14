const api = require('../../utils/request')
Page({
  data: { tasks: [] },
  onShow() { this.load() },
  async load() {
    try { this.setData({ tasks: await api.get('/carrier/tasks') }) }
    catch (e) { this.setData({ tasks: [] }) }
  },
  report(e) {
    const id = e.currentTarget.dataset.id
    api.post(`/carrier/tasks/${id}/report`, { node: '已发车', lat: 31.23, lng: 121.47, remark: '小程序报节点' })
      .then(() => wx.showToast({ title: '已上报节点' })).catch((err) => wx.showToast({ title: err.message, icon: 'none' }))
  },
  sign(e) {
    const id = e.currentTarget.dataset.id
    api.post(`/driver/tasks/${id}/sign`, { signerName: '收货人', signerPhone: '13700000001', signImage: '', lat: 31.23, lng: 121.47 })
      .then(() => { wx.showToast({ title: '已签收' }); this.load() }).catch((err) => wx.showToast({ title: err.message, icon: 'none' }))
  }
})
