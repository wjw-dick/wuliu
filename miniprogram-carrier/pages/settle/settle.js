const api = require('../../utils/request')
Page({
  data: { list: [], total: 0 },
  onShow() { this.load() },
  async load() {
    try {
      const list = await api.get('/carrier/statements')
      const total = list.reduce((s, i) => s + Number(i.amount || 0), 0)
      this.setData({ list, total: total.toFixed(2) })
    } catch (e) { this.setData({ list: [], total: 0 }) }
  }
})
