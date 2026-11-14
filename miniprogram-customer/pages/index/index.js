const api = require('../../utils/request')
Page({
  data: { origin: '上海', destination: '北京', weight: 500, quoting: false, quote: null, trackNo: '' },
  onOrigin(e) { this.setData({ origin: e.detail.value }) },
  onDest(e) { this.setData({ destination: e.detail.value }) },
  onWeight(e) { this.setData({ weight: e.detail.value }) },
  onTrackNo(e) { this.setData({ trackNo: e.detail.value }) },
  async quote() {
    if (!this.data.origin || !this.data.destination) return
    this.setData({ quoting: true })
    try {
      const data = await api.post('/public/quote', {
        origin: this.data.origin, destination: this.data.destination,
        weight: Number(this.data.weight), volume: 2, pieces: 5, vehicleType: '9.6米', cargoType: '普货'
      })
      this.setData({ quote: data })
    } catch (e) { wx.showToast({ title: e.message, icon: 'none' }) }
    finally { this.setData({ quoting: false }) }
  },
  goTrack() {
    if (!this.data.trackNo) return wx.showToast({ title: '请输入运单号', icon: 'none' })
    wx.switchTab({ url: '/pages/track/track?no=' + this.data.trackNo })
  }
})
