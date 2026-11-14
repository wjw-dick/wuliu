function request(method, url, data) {
  const app = getApp()
  return new Promise((resolve, reject) => {
    wx.request({
      url: app.globalData.baseUrl + url,
      method,
      data,
      header: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + app.globalData.token },
      success(res) {
        const body = res.data
        if (body && body.code === 0) resolve(body.data)
        else reject(new Error((body && body.message) || '请求失败'))
      },
      fail(err) { reject(new Error(err.errMsg || '网络异常')) }
    })
  })
}
module.exports = {
  get: (url, data) => request('GET', url, data),
  post: (url, data) => request('POST', url, data)
}
