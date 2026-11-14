const api = require('../../utils/request')
Page({
  data: { no: '', result: null, loading: false, msg: '', ok: false },
  onLoad(query) { if (query.no) { this.setData({ no: query.no }); this.track() } },
  onNo(e) { this.setData({ no: e.detail.value }) },
  async track() {
    if (!this.data.no) { this.setData({ msg: '请输入运单号', ok: false }); return }
    this.setData({ loading: true, msg: '' })
    try {
      const data = await api.get('/public/track', { waybillNo: this.data.no })
      this.setData({ result: data, ok: true })
    } catch (e) { this.setData({ msg: e.message, ok: false }) }
    finally { this.setData({ loading: false }) }
  }
})
