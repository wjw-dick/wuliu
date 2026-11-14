// 全局中文字典与状态样式（后端不可用时的前端兜底）
export type TagType = 'primary' | 'success' | 'info' | 'warning' | 'danger' | 'default'

export const WAYBILL_STATUSES = [
  '待报价', '待客户确认', '待支付', '已受理', '待调度', '已派车', '已提货',
  '运输中', '中转中', '到达待派送', '派送中', '已签收', '待对账',
  '已对账待开票', '已结算', '已完结', '异常处理中', '退回中', '已退回',
  '理赔中', '已取消'
]

export const DICTS: Record<string, string[]> = {
  运单状态: WAYBILL_STATUSES,
  客户类型: ['企业客户', '个体客户', '月结客户', '平台会员'],
  客户等级: ['战略客户', '重点客户', '普通客户', '潜在客户'],
  结算方式: ['现结', '到付', '月结', '预付定金'],
  提货方式: ['上门提货', '客户自送到站'],
  配送方式: ['送货上门', '收货人自提', '送指定驿站'],
  车型: ['面包车', '4.2 米厢式', '6.8 米厢式', '9.6 米厢式', '13 米高栏', '平板车', '冷藏车', '危险品专用车'],
  货物类型: ['普货', '大件', '易碎品', '需冷藏', '需防震', '精密设备', '文件票据'],
  计费方式: ['按重量', '按体积', '按重量体积取大', '按公里', '按票', '最低一票', '包车'],
  增值服务: ['装卸', '上楼', '拆包装', '打木架', '保险', '代收货款', '回单返回', '签收回单原件'],
  异常类型: ['延误', '破损', '丢失', '短少', '拒收', '改址', '无人收货', '车辆故障', '交通事故', '扣押', '天气不可抗力'],
  责任方: ['承运商', '自有司机', '装卸方', '客户包装不当', '不可抗力', '待定'],
  结算状态: ['待生成', '待对账', '客户已确认', '有异议', '已开票', '部分收款', '已结清', '已挂账'],
  发票类型: ['增值税专用发票', '增值税普通发票', '电子普通发票', '不开发票'],
  支付方式: ['微信支付', '银行转账', '现金', '月结记账', '代收货款抵扣'],
  派车来源: ['自有车辆', '外协承运商', '临时外调'],
  理赔状态: ['待立案', '已立案', '定责中', '待审批', '已赔付', '已结案', '驳回'],
  承运商状态: ['待审核', '合作中', '已暂停', '已终止'],
  资质审核状态: ['待审核', '已通过', '已驳回', '已过期'],
  客户来源: ['官网询价', '公众号', '小程序', '电话', '销售开发', '老客推荐'],
  租户状态: ['试用中', '正常', '欠费', '已到期', '已停用', '已归档'],
  套餐类型: ['基础版', '标准版', '专业版', '旗舰版'],
  公告类型: ['版本通知', '维护公告', '运营活动', '政策通知'],
  行业分类: ['制造业', '商贸批发', '冷链生鲜', '家居建材', '电商零售', '医药器械'],
  司机状态: ['空闲', '任务中', '请假', '停用'],
  车辆状态: ['空闲', '在途', '维修', '停用'],
  对账状态: ['待生成', '待对账', '客户已确认', '有异议', '已开票', '部分收款', '已结清', '已挂账'],
  派车状态: ['待调度', '已派车', '在途', '异常', '已完成'],
  回单状态: ['待审核', '已通过', '已驳回'],
  异常处理状态: ['待处理', '处理中', '已解决', '已升级'],
  账单状态: ['待支付', '部分收款', '已结清', '已逾期'],
  报价状态: ['待报价', '已报价', '已确认', '已失效', '已转单'],
  线索状态: ['待跟进', '跟进中', '已转客户', '已失效'],
  理赔处理状态: ['待立案', '已立案', '定责中', '待审批', '已赔付', '已结案', '驳回']
}

// 英文编码兜底翻译，任何页面都不直接暴露英语编码
export const CODE_LABELS: Record<string, string> = {
  trial: '试用中', active: '正常', normal: '正常', enabled: '正常', disabled: '已停用',
  arrears: '欠费', overdue: '已逾期', expired: '已过期', suspended: '已停用',
  archived: '已归档', pending: '待处理', processing: '处理中', resolved: '已解决',
  closed: '已结案', rejected: '已驳回', approved: '已通过', passed: '已通过',
  paid: '已结清', partial: '部分收款', unpaid: '待支付', settled: '已结清',
  draft: '待报价', quoted: '已报价', confirmed: '已确认', invalid: '已失效',
  converted: '已转单', canceled: '已取消', cancelled: '已取消', dispatching: '待调度',
  assigned: '已派车', intransit: '在途', exception: '异常', completed: '已完成',
  online: '在途', offline: '停用', free: '空闲', busy: '任务中', repair: '维修',
  reviewing: '待审核', create: '新增', update: '更新', delete: '删除', login: '登录',
  logout: '退出', export: '导出', review: '审核', pay: '收款', confirm: '确认',
  dispute: '异议', renew: '续期', suspend: '停用', enable: '启用', assign: '派车',
  PASS: '已通过', REJECT: '已驳回', WAIT: '待审核', YES: '是', NO: '否'
}

const STATUS_TYPE_MAP: Record<string, TagType> = {
  待报价: 'info', 待客户确认: 'warning', 待支付: 'warning', 已受理: 'primary',
  待调度: 'warning', 已派车: 'primary', 已提货: 'primary', 运输中: 'primary',
  中转中: 'primary', 到达待派送: 'primary', 派送中: 'primary', 已签收: 'success',
  待对账: 'warning', 已对账待开票: 'warning', 已结算: 'success', 已完结: 'success',
  异常处理中: 'danger', 退回中: 'warning', 已退回: 'info', 理赔中: 'danger', 已取消: 'info',
  试用中: 'warning', 正常: 'success', 欠费: 'danger', 已到期: 'danger', 已停用: 'info', 已归档: 'info',
  待审核: 'warning', 已通过: 'success', 已驳回: 'danger', 已过期: 'danger',
  待支付账单: 'warning', 部分收款: 'warning', 已结清: 'success', 已逾期: 'danger',
  待生成: 'info', 待对账账单: 'warning', 客户已确认: 'success', 有异议: 'danger', 已开票: 'primary', 已挂账: 'warning',
  待处理: 'warning', 处理中: 'primary', 已解决: 'success', 已升级: 'danger',
  待立案: 'warning', 已立案: 'primary', 定责中: 'primary', 待审批: 'warning', 已赔付: 'success', 已结案: 'success', 驳回: 'danger',
  合作中: 'success', 已暂停: 'warning', 已终止: 'info',
  空闲: 'success', 在途: 'primary', 任务中: 'primary', 维修: 'warning', 停用: 'info', 请假: 'warning',
  已报价: 'primary', 已确认报价: 'success', 已失效: 'info', 已转单: 'success',
  待跟进: 'warning', 跟进中: 'primary', 已转客户: 'success',
  待调度派车: 'warning', 已派车派车: 'primary', 异常派车: 'danger', 已完成: 'success',
  是: 'success', 否: 'info', 待付款: 'warning', 已付款: 'success', 待确认: 'warning'
}

export function translateCode(value?: string | null): string {
  if (value === undefined || value === null || value === '') return '—'
  return CODE_LABELS[value] || CODE_LABELS[String(value).toLowerCase()] || String(value)
}

export function statusType(name?: string | null): TagType {
  if (!name) return 'info'
  const label = translateCode(name)
  return STATUS_TYPE_MAP[label] || 'info'
}

export function dictOptions(key: string): { label: string; value: string }[] {
  return (DICTS[key] || []).map((v) => ({ label: v, value: v }))
}

export function labelOf(value?: string | null): string {
  return translateCode(value)
}

export function dictItems(): { category: string; items: string[] }[] {
  return Object.keys(DICTS).map((category) => ({ category, items: DICTS[category] }))
}

export const PROVINCES = ['上海市', '浙江省', '江苏省', '广东省', '北京市', '山东省', '安徽省', '福建省']
export const CITIES = ['上海', '杭州', '苏州', '南京', '广州', '深圳', '北京', '宁波', '合肥', '厦门']
