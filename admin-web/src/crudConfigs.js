// 通用 CRUD 页面配置：endpoint 为相对 /api/tenant 或 /api/platform 的路径
// columns: 表格列；form: 表单字段；search: 搜索字段；disabled: 禁用新增按钮；readonly: 只读
export const crudConfigs = {
  // ---------- 平台端 ----------
  plans: {
    title: '套餐管理',
    scope: 'platform',
    endpoint: '/platform/plans',
    pageMode: false,
    search: [],
    columns: [
      { prop: 'name', label: '套餐名称', width: 160 },
      { prop: 'code', label: '编码', width: 120 },
      { prop: 'price', label: '年费(元)', width: 110 },
      { prop: 'seatQuota', label: '席位', width: 90 },
      { prop: 'waybillQuota', label: '运单配额', width: 110 },
      { prop: 'features', label: '功能', minWidth: 240 },
      { prop: 'status', label: '状态', width: 90, type: 'tag', tagMap: { 启用: 'success', 停用: 'info' } },
      { prop: 'sortNo', label: '排序', width: 70 }
    ],
    form: [
      { prop: 'name', label: '套餐名称', required: true },
      { prop: 'code', label: '编码', required: true },
      { prop: 'price', label: '年费(元)', type: 'number' },
      { prop: 'seatQuota', label: '席位', type: 'number' },
      { prop: 'waybillQuota', label: '运单配额', type: 'number' },
      { prop: 'features', label: '功能说明', type: 'textarea' },
      { prop: 'status', label: '状态', type: 'select', options: ['启用', '停用'] },
      { prop: 'sortNo', label: '排序', type: 'number' }
    ]
  },

  platformUsers: {
    title: '平台账号',
    scope: 'platform',
    endpoint: '/platform/users',
    pageMode: false,
    search: [],
    columns: [
      { prop: 'username', label: '用户名', width: 150 },
      { prop: 'nickname', label: '昵称', width: 150 },
      { prop: 'role', label: '角色', width: 120 },
      { prop: 'email', label: '邮箱', minWidth: 200 },
      { prop: 'status', label: '状态', width: 90, type: 'tag', tagMap: { 正常: 'success', 停用: 'info' } }
    ],
    form: [
      { prop: 'username', label: '用户名', required: true },
      { prop: 'nickname', label: '昵称', required: true },
      { prop: 'password', label: '密码(留空不修改)', type: 'password' },
      { prop: 'role', label: '角色' },
      { prop: 'email', label: '邮箱' },
      { prop: 'status', label: '状态', type: 'select', options: ['正常', '停用'] }
    ]
  },

  platformAnnouncements: {
    title: '平台公告',
    scope: 'platform',
    endpoint: '/platform/announcements',
    pageMode: false,
    search: [],
    columns: [
      { prop: 'title', label: '标题', minWidth: 220 },
      { prop: 'recordType', label: '类型', width: 120 },
      { prop: 'status', label: '状态', width: 100, type: 'tag', tagMap: { 已发布: 'success', 草稿: 'info' } },
      { prop: 'publishedAt', label: '发布时间', width: 170 }
    ],
    form: [
      { prop: 'title', label: '标题', required: true },
      { prop: 'recordType', label: '类型', type: 'select', options: ['通知', '公告', '活动'] },
      { prop: 'content', label: '内容', type: 'textarea' },
      { prop: 'status', label: '状态', type: 'select', options: ['已发布', '草稿'] }
    ]
  },

  // ---------- 租户端 ----------
  vehicles: {
    title: '车辆档案',
    scope: 'tenant',
    endpoint: '/tenant/vehicles',
    search: [{ prop: 'keyword', label: '关键字', placeholder: '车牌/车型/车队' }],
    columns: [
      { prop: 'plateNo', label: '车牌号', width: 130 },
      { prop: 'vehicleType', label: '车型', width: 110 },
      { prop: 'fleet', label: '所属车队', width: 130 },
      { prop: 'owner', label: '车主', width: 110 },
      { prop: 'tonnage', label: '载重(吨)', width: 100 },
      { prop: 'volume', label: '方量(m³)', width: 100 },
      { prop: 'status', label: '状态', width: 90, type: 'tag', tagMap: { 正常: 'success', 维修: 'warning', 停用: 'info' } },
      { prop: 'insuranceExpiry', label: '保险到期', width: 130 },
      { prop: 'inspectionExpiry', label: '年检到期', width: 130 }
    ],
    form: [
      { prop: 'plateNo', label: '车牌号', required: true },
      { prop: 'vehicleType', label: '车型', type: 'select', options: ['4.2米', '6.8米', '9.6米', '13米', '17.5米', '厢式货车', '高栏车', '冷藏车'] },
      { prop: 'fleet', label: '所属车队' },
      { prop: 'owner', label: '车主' },
      { prop: 'tonnage', label: '载重(吨)', type: 'number' },
      { prop: 'volume', label: '方量(m³)', type: 'number' },
      { prop: 'status', label: '状态', type: 'select', options: ['正常', '维修', '停用'] },
      { prop: 'insuranceExpiry', label: '保险到期', type: 'date' },
      { prop: 'inspectionExpiry', label: '年检到期', type: 'date' },
      { prop: 'remark', label: '备注', type: 'textarea' }
    ]
  },

  drivers: {
    title: '司机档案',
    scope: 'tenant',
    endpoint: '/tenant/drivers',
    search: [{ prop: 'keyword', label: '关键字', placeholder: '姓名/手机号' }],
    columns: [
      { prop: 'name', label: '姓名', width: 120 },
      { prop: 'phone', label: '手机号', width: 140 },
      { prop: 'licenseNo', label: '驾驶证号', width: 180 },
      { prop: 'licenseType', label: '准驾车型', width: 100 },
      { prop: 'licenseExpiry', label: '驾照到期', width: 130 },
      { prop: 'practiceExpiry', label: '从业资格到期', width: 140 },
      { prop: 'status', label: '状态', width: 90, type: 'tag', tagMap: { 在职: 'success', 停用: 'info', 请假: 'warning' } }
    ],
    form: [
      { prop: 'name', label: '姓名', required: true },
      { prop: 'phone', label: '手机号', required: true },
      { prop: 'licenseNo', label: '驾驶证号' },
      { prop: 'licenseType', label: '准驾车型', type: 'select', options: ['A1', 'A2', 'B1', 'B2', 'C1', 'C2'] },
      { prop: 'licenseExpiry', label: '驾照到期', type: 'date' },
      { prop: 'practiceExpiry', label: '从业资格到期', type: 'date' },
      { prop: 'status', label: '状态', type: 'select', options: ['在职', '请假', '停用'] }
    ]
  },

  carriers: {
    title: '承运商档案',
    scope: 'tenant',
    endpoint: '/tenant/carriers',
    search: [{ prop: 'keyword', label: '关键字', placeholder: '名称/联系人' }, { prop: 'status', label: '状态', type: 'select', options: ['待审核', '合作中', '已停用'] }],
    columns: [
      { prop: 'name', label: '承运商名称', minWidth: 200 },
      { prop: 'contactName', label: '联系人', width: 110 },
      { prop: 'contactPhone', label: '联系电话', width: 140 },
      { prop: 'settleType', label: '结算方式', width: 110 },
      { prop: 'creditLimit', label: '授信额度(元)', width: 120 },
      { prop: 'status', label: '状态', width: 100, type: 'tag', tagMap: { 待审核: 'warning', 合作中: 'success', 已停用: 'info' } }
    ],
    form: [
      { prop: 'name', label: '承运商名称', required: true },
      { prop: 'contactName', label: '联系人' },
      { prop: 'contactPhone', label: '联系电话' },
      { prop: 'settleType', label: '结算方式', type: 'select', options: ['现结', '周结', '月结', '货到付款'] },
      { prop: 'creditLimit', label: '授信额度(元)', type: 'number' },
      { prop: 'status', label: '状态', type: 'select', options: ['待审核', '合作中', '已停用'] }
    ]
  },

  receivables: {
    title: '应收结算',
    scope: 'tenant',
    endpoint: '/tenant/receivables',
    search: [],
    columns: [
      { prop: 'waybillNo', label: '运单号', width: 160 },
      { prop: 'customerName', label: '客户', minWidth: 160 },
      { prop: 'amount', label: '应收(元)', width: 110 },
      { prop: 'received', label: '已收(元)', width: 110 },
      { prop: 'status', label: '状态', width: 110, type: 'tag', tagMap: { 待收: 'warning', 部分收: 'primary', 已收: 'success', 逾期: 'danger' } },
      { prop: 'dueDate', label: '到期日', width: 130 }
    ]
  },

  payables: {
    title: '应付结算',
    scope: 'tenant',
    endpoint: '/tenant/payables',
    search: [],
    columns: [
      { prop: 'waybillNo', label: '运单号', width: 160 },
      { prop: 'carrierName', label: '承运商', minWidth: 160 },
      { prop: 'driverName', label: '司机', width: 110 },
      { prop: 'amount', label: '应付(元)', width: 110 },
      { prop: 'paid', label: '已付(元)', width: 110 },
      { prop: 'status', label: '状态', width: 120, type: 'tag', tagMap: { 待付: 'warning', 已付: 'success', 有异议: 'danger' } }
    ]
  },

  invoices: {
    title: '发票管理',
    scope: 'tenant',
    endpoint: '/tenant/invoices',
    search: [],
    columns: [
      { prop: 'invoiceNo', label: '发票号', width: 170 },
      { prop: 'statementNo', label: '对账单号', width: 160 },
      { prop: 'invoiceType', label: '发票类型', width: 150 },
      { prop: 'amount', label: '金额(元)', width: 110 },
      { prop: 'status', label: '状态', width: 100, type: 'tag', tagMap: { 已开具: 'success', 待开具: 'warning' } },
      { prop: 'issuedAt', label: '开票日期', width: 150 }
    ]
  },

  payments: {
    title: '收款登记',
    scope: 'tenant',
    endpoint: '/tenant/payments',
    search: [],
    columns: [
      { prop: 'statementNo', label: '对账单号', width: 160 },
      { prop: 'customerName', label: '客户', minWidth: 160 },
      { prop: 'amount', label: '收款(元)', width: 110 },
      { prop: 'method', label: '方式', width: 120 },
      { prop: 'paidAt', label: '收款时间', width: 150 }
    ]
  },

  orgs: {
    title: '网点与组织',
    scope: 'tenant',
    endpoint: '/tenant/orgs',
    search: [{ prop: 'keyword', label: '关键字', placeholder: '名称/编码' }],
    columns: [
      { prop: 'name', label: '名称', minWidth: 180 },
      { prop: 'code', label: '编码', width: 120 },
      { prop: 'orgType', label: '类型', width: 110 },
      { prop: 'contactName', label: '负责人', width: 110 },
      { prop: 'contactPhone', label: '电话', width: 140 },
      { prop: 'status', label: '状态', width: 90, type: 'tag', tagMap: { 启用: 'success', 停用: 'info' } }
    ],
    form: [
      { prop: 'name', label: '名称', required: true },
      { prop: 'code', label: '编码', required: true },
      { prop: 'orgType', label: '类型', type: 'select', options: ['总部', '分公司', '营业网点', '中转场', '门店'] },
      { prop: 'contactName', label: '负责人' },
      { prop: 'contactPhone', label: '电话' },
      { prop: 'address', label: '地址' },
      { prop: 'status', label: '状态', type: 'select', options: ['启用', '停用'] }
    ]
  },

  tenantUsers: {
    title: '账号管理',
    scope: 'tenant',
    endpoint: '/tenant/users',
    search: [{ prop: 'keyword', label: '关键字', placeholder: '账号/昵称' }],
    columns: [
      { prop: 'username', label: '账号', width: 140 },
      { prop: 'nickname', label: '昵称', width: 140 },
      { prop: 'phone', label: '手机号', width: 140 },
      { prop: 'roleId', label: '角色ID', width: 90 },
      { prop: 'status', label: '状态', width: 90, type: 'tag', tagMap: { 正常: 'success', 停用: 'info' } }
    ],
    form: [
      { prop: 'username', label: '账号', required: true },
      { prop: 'nickname', label: '昵称', required: true },
      { prop: 'password', label: '密码(留空不修改)', type: 'password' },
      { prop: 'phone', label: '手机号' },
      { prop: 'roleId', label: '角色ID', type: 'number' },
      { prop: 'status', label: '状态', type: 'select', options: ['正常', '停用'] }
    ]
  },

  tenantRoles: {
    title: '角色权限',
    scope: 'tenant',
    endpoint: '/tenant/roles',
    pageMode: false,
    search: [],
    columns: [
      { prop: 'name', label: '角色名称', width: 150 },
      { prop: 'dataScope', label: '数据范围', width: 120 },
      { prop: 'status', label: '状态', width: 90, type: 'tag', tagMap: { 启用: 'success', 停用: 'info' } },
      { prop: 'menus', label: '菜单权限', minWidth: 200 }
    ],
    form: [
      { prop: 'name', label: '角色名称', required: true },
      { prop: 'dataScope', label: '数据范围', type: 'select', options: ['公司级', '网点级', '个人级'] },
      { prop: 'status', label: '状态', type: 'select', options: ['启用', '停用'] },
      { prop: 'menus', label: '菜单权限(JSON数组)', type: 'textarea' },
      { prop: 'buttons', label: '按钮权限(JSON数组)', type: 'textarea' }
    ]
  },

  i18n: {
    title: '多语言',
    scope: 'tenant',
    endpoint: '/tenant/system/i18n',
    pageMode: false,
    search: [],
    columns: [
      { prop: 'lang', label: '语言', width: 100 },
      { prop: 'module', label: '模块', width: 120 },
      { prop: 'key', label: '键', minWidth: 180 },
      { prop: 'value', label: '值', minWidth: 200 }
    ],
    form: [
      { prop: 'lang', label: '语言', type: 'select', options: ['zh-CN', 'en-US', 'ja-JP'] },
      { prop: 'module', label: '模块' },
      { prop: 'key', label: '键', required: true },
      { prop: 'value', label: '值', required: true }
    ]
  },

  tenantAnnouncements: {
    title: '租户公告',
    scope: 'tenant',
    endpoint: '/tenant/announcements',
    pageMode: false,
    search: [],
    columns: [
      { prop: 'title', label: '标题', minWidth: 220 },
      { prop: 'recordType', label: '类型', width: 120 },
      { prop: 'status', label: '状态', width: 100, type: 'tag', tagMap: { 已发布: 'success', 草稿: 'info' } },
      { prop: 'publishedAt', label: '发布时间', width: 170 }
    ],
    form: [
      { prop: 'title', label: '标题', required: true },
      { prop: 'recordType', label: '类型', type: 'select', options: ['通知', '公告', '活动'] },
      { prop: 'content', label: '内容', type: 'textarea' },
      { prop: 'status', label: '状态', type: 'select', options: ['已发布', '草稿'] }
    ]
  }
}
