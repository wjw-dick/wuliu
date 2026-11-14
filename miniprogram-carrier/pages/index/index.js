const api = require('../../utils/request')
Page({
  data: { orders: [] },
  onShow() { this.load() },
  async load() {
    try { this.setData({ orders: await api.get('/carrier/orders') }) }
    catch (e) { this.setData({ orders: [] }) }
  },
  goTasks() { wx.navigateTo({ url: '/pages/tasks/tasks' }) },
  goSettle() { wx.navigateTo({ url: '/pages/settle/settle' }) }
})
