Page({
  data: { orders: [] },
  onShow() {
    // 演示：拉取公开轨迹示例；实际接入需登录态查询客户订单
    this.setData({ orders: [] })
  }
})
