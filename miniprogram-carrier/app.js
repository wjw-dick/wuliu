App({
  globalData: { baseUrl: 'https://www.quantaida.cloud/api', token: '' },
  onLaunch() {
    const token = wx.getStorageSync('mp_carrier_token')
    if (token) this.globalData.token = token
  }
})
