import { get, post, put, del, type PageQuery } from './request'

/* ---------- 工作台 / 组织 / 账号 ---------- */
export const getTenantDashboard = () => get('/tenant/dashboard')

export const getOrgs = (params?: PageQuery) => get('/tenant/orgs', params)
export const createOrg = (data: any) => post('/tenant/orgs', data)
export const updateOrg = (id: number, data: any) => put(`/tenant/orgs/${id}`, data)
export const deleteOrg = (id: number) => del(`/tenant/orgs/${id}`)

export const getTenantUsers = (params: PageQuery) => get('/tenant/users', params)
export const createTenantUser = (data: any) => post('/tenant/users', data)
export const updateTenantUser = (id: number, data: any) => put(`/tenant/users/${id}`, data)
export const deleteTenantUser = (id: number) => del(`/tenant/users/${id}`)

export const getTenantRoles = (params?: PageQuery) => get('/tenant/roles', params)
export const createTenantRole = (data: any) => post('/tenant/roles', data)
export const updateTenantRole = (id: number, data: any) => put(`/tenant/roles/${id}`, data)
export const deleteTenantRole = (id: number) => del(`/tenant/roles/${id}`)
export const getMenuTree = () => get('/tenant/menus/tree')

export const getTenantAnnouncements = (params?: PageQuery) => get('/tenant/announcements', params)

/* ---------- 客户 / 协议 / 线索 ---------- */
export const getCustomers = (params: PageQuery) => get('/tenant/customers', params)
export const getCustomerDetail = (id: number) => get(`/tenant/customers/${id}`)
export const createCustomer = (data: any) => post('/tenant/customers', data)
export const updateCustomer = (id: number, data: any) => put(`/tenant/customers/${id}`, data)
export const deleteCustomer = (id: number) => del(`/tenant/customers/${id}`)
export const addCustomerContact = (id: number, data: any) => post(`/tenant/customers/${id}/contacts`, data)
export const updateCustomerContact = (id: number, contactId: number, data: any) =>
  put(`/tenant/customers/${id}/contacts/${contactId}`, data)
export const deleteCustomerContact = (id: number, contactId: number) =>
  del(`/tenant/customers/${id}/contacts/${contactId}`)
export const addCustomerAddress = (id: number, data: any) => post(`/tenant/customers/${id}/addresses`, data)
export const updateCustomerAddress = (id: number, addressId: number, data: any) =>
  put(`/tenant/customers/${id}/addresses/${addressId}`, data)
export const deleteCustomerAddress = (id: number, addressId: number) =>
  del(`/tenant/customers/${id}/addresses/${addressId}`)

export const getAgreements = (params: PageQuery) => get('/tenant/agreements', params)
export const createAgreement = (data: any) => post('/tenant/agreements', data)
export const updateAgreement = (id: number, data: any) => put(`/tenant/agreements/${id}`, data)
export const deleteAgreement = (id: number) => del(`/tenant/agreements/${id}`)

export const getLeads = (params: PageQuery) => get('/tenant/leads', params)
export const createLead = (data: any) => post('/tenant/leads', data)
export const updateLead = (id: number, data: any) => put(`/tenant/leads/${id}`, data)
export const deleteLead = (id: number) => del(`/tenant/leads/${id}`)
export const assignLead = (id: number, userId: number) => post(`/tenant/leads/${id}/assign`, { userId })
export const convertLead = (id: number) => post(`/tenant/leads/${id}/convert`)

export const getFollowups = (params?: PageQuery) => get('/tenant/followups', params)
export const createFollowup = (data: any) => post('/tenant/followups', data)

/* ---------- 询价 / 报价 ---------- */
export const tenantQuote = (data: any) => post('/tenant/pricing/quote', data)
export const getQuotes = (params: PageQuery) => get('/tenant/quotes', params)
export const getQuoteDetail = (id: number) => get(`/tenant/quotes/${id}`)
export const createQuote = (data: any) => post('/tenant/quotes', data)
export const convertQuote = (id: number) => post(`/tenant/quotes/${id}/convert`)
export const approveQuote = (id: number, pass: boolean, reason: string) =>
  post(`/tenant/quotes/${id}/approve`, { pass, reason })
export const updateQuote = (id: number, data: any) => put(`/tenant/quotes/${id}`, data)
export const deleteQuote = (id: number) => del(`/tenant/quotes/${id}`)

/* ---------- 运单 / 调度 / 运力 ---------- */
export const getWaybills = (params: PageQuery) => get('/tenant/waybills', params)
export const getWaybill = (id: number) => get(`/tenant/waybills/${id}`)
export const createWaybill = (data: any) => post('/tenant/waybills', data)
export const updateWaybill = (id: number, data: any) => put(`/tenant/waybills/${id}`, data)
export const deleteWaybill = (id: number) => del(`/tenant/waybills/${id}`)
export const getWaybillTimeline = (id: number) => get(`/tenant/waybills/${id}/timeline`)
export const waybillAction = (id: number, data: { action: string; reason?: string; payload?: any }) =>
  post(`/tenant/waybills/${id}/action`, data)
export const getWaybillPrint = (id: number) => get(`/tenant/waybills/${id}/print`)
export const getWaybillLabel = (id: number) => get(`/tenant/waybills/${id}/label`)

export const getDispatchPool = (params?: PageQuery) => get('/tenant/dispatch/pool', params)
export const getDispatchBoard = () => get('/tenant/dispatch/board')
export const assignDispatch = (data: { waybillId: number; vehicleId: number; driverId: number; carrierId?: number }) =>
  post('/tenant/dispatch/assign', data)
export const reassignDispatch = (id: number, data: any) => post(`/tenant/dispatch/${id}/reassign`, data)
export const cancelDispatch = (id: number, data: { reason: string; emptyFee?: number }) =>
  post(`/tenant/dispatch/${id}/cancel`, data)

export const getVehicles = (params: PageQuery) => get('/tenant/vehicles', params)
export const createVehicle = (data: any) => post('/tenant/vehicles', data)
export const updateVehicle = (id: number, data: any) => put(`/tenant/vehicles/${id}`, data)
export const deleteVehicle = (id: number) => del(`/tenant/vehicles/${id}`)

export const getDrivers = (params: PageQuery) => get('/tenant/drivers', params)
export const createDriver = (data: any) => post('/tenant/drivers', data)
export const updateDriver = (id: number, data: any) => put(`/tenant/drivers/${id}`, data)
export const deleteDriver = (id: number) => del(`/tenant/drivers/${id}`)

export const getCarriers = (params: PageQuery) => get('/tenant/carriers', params)
export const createCarrier = (data: any) => post('/tenant/carriers', data)
export const updateCarrier = (id: number, data: any) => put(`/tenant/carriers/${id}`, data)
export const deleteCarrier = (id: number) => del(`/tenant/carriers/${id}`)

export const getExpiringQualifications = (params?: PageQuery) => get('/tenant/qualifications/expiring', params)

/* ---------- 在途 / 异常 / 签收 / 退回 ---------- */
export const getTrack = (waybillId: number) => get(`/tenant/tracks/${waybillId}`)
export const getTrackReplay = (waybillId: number) => get(`/tenant/tracks/${waybillId}/replay`)

export const getExceptions = (params: PageQuery) => get('/tenant/exceptions', params)
export const createException = (data: any) => post('/tenant/exceptions', data)
export const handleException = (id: number, data: { conclusion: string; assigneeId?: number; escalate?: boolean }) =>
  post(`/tenant/exceptions/${id}/handle`, data)

export const getReceipts = (params: PageQuery) => get('/tenant/receipts', params)
export const reviewReceipt = (id: number, pass: boolean, reason: string) =>
  post(`/tenant/receipts/${id}/review`, { pass, reason })
export const getReceiptPdf = (id: number) => get(`/tenant/receipts/${id}/pdf`)

export const getReturns = (params: PageQuery) => get('/tenant/returns', params)
export const createReturn = (data: any) => post('/tenant/returns', data)
export const completeReturn = (id: number, data?: any) => post(`/tenant/returns/${id}/complete`, data || {})

/* ---------- 结算 / 对账 / 发票 / 代收 ---------- */
export const getReceivables = (params: PageQuery) => get('/tenant/receivables', params)
export const createReceivable = (data: any) => post('/tenant/receivables', data)

export const getPayables = (params: PageQuery) => get('/tenant/payables', params)
export const createPayable = (data: any) => post('/tenant/payables', data)

export const getStatements = (params: PageQuery) => get('/tenant/statements', params)
export const getStatementDetail = (id: number) => get(`/tenant/statements/${id}`)
export const createStatement = (data: any) => post('/tenant/statements', data)
export const confirmStatement = (id: number) => post(`/tenant/statements/${id}/confirm`)
export const disputeStatement = (id: number, reason: string) => post(`/tenant/statements/${id}/dispute`, { reason })
export const getStatementPdf = (id: number) => get(`/tenant/statements/${id}/pdf`)

export const getInvoices = (params: PageQuery) => get('/tenant/invoices', params)
export const createInvoice = (data: any) => post('/tenant/invoices', data)

export const getPayments = (params: PageQuery) => get('/tenant/payments', params)
export const createPayment = (data: any) => post('/tenant/payments', data)

export const getAging = () => get('/tenant/aging')
export const getCod = (params: PageQuery) => get('/tenant/cod', params)
export const settleCod = (data: any) => post('/tenant/cod', data)
export const getProfit = (params?: PageQuery) => get('/tenant/profit', params)

/* ---------- 理赔 / 报表 ---------- */
export const getClaims = (params: PageQuery) => get('/tenant/claims', params)
export const getClaimDetail = (id: number) => get(`/tenant/claims/${id}`)
export const createClaim = (data: any) => post('/tenant/claims', data)
export const claimLiability = (id: number, liability: string, reason: string) =>
  post(`/tenant/claims/${id}/liability`, { liability, reason })
export const approveClaim = (id: number, pass: boolean, reason: string, amount: number) =>
  post(`/tenant/claims/${id}/approve`, { pass, reason, amount })
export const payClaim = (id: number, data?: any) => post(`/tenant/claims/${id}/pay`, data || {})
export const closeClaim = (id: number) => post(`/tenant/claims/${id}/close`)

export const getReport = (type: string, params?: PageQuery) => get(`/tenant/reports/${type}`, params)
export const exportReport = (type: string, data: { purpose: string; start?: string; end?: string }) =>
  get(`/tenant/reports/${type}/export`, data)

/* ---------- 系统配置 ---------- */
export const getSystemConfigs = () => get('/tenant/system/configs')
export const updateSystemConfigs = (data: any) => put('/tenant/system/configs', data)

export const getDictCategories = (params?: PageQuery) => get('/tenant/system/dicts', params)
export const createDictCategory = (data: any) => post('/tenant/system/dicts', data)
export const updateDictCategory = (id: number, data: any) => put(`/tenant/system/dicts/${id}`, data)
export const deleteDictCategory = (id: number) => del(`/tenant/system/dicts/${id}`)
export const createDictItem = (code: string, data: any) => post(`/tenant/system/dicts/${code}/items`, data)
export const updateDictItem = (code: string, itemId: number, data: any) =>
  put(`/tenant/system/dicts/${code}/items/${itemId}`, data)
export const deleteDictItem = (code: string, itemId: number) => del(`/tenant/system/dicts/${code}/items/${itemId}`)

export const getSystemMenus = () => get('/tenant/system/menus')
export const updateSystemMenus = (data: any) => put('/tenant/system/menus', data)

export const getMessageTemplates = (params?: PageQuery) => get('/tenant/system/message-templates', params)
export const createMessageTemplate = (data: any) => post('/tenant/system/message-templates', data)
export const updateMessageTemplate = (id: number, data: any) => put(`/tenant/system/message-templates/${id}`, data)
export const deleteMessageTemplate = (id: number) => del(`/tenant/system/message-templates/${id}`)
export const testMessageTemplate = (id: number, receiver: string) =>
  post(`/tenant/system/message-templates/${id}/test`, { receiver })
export const getMessageLogs = (params?: PageQuery) => get('/tenant/system/message-logs', params)
export const resendMessageLog = (id: number) => post(`/tenant/system/message-logs/${id}/resend`)

export const getSso = () => get('/tenant/system/sso')
export const updateSso = (data: any) => put('/tenant/system/sso', data)

export const getStorage = () => get('/tenant/system/storage')
export const testStorage = (data: { driver: string; config: any }) => post('/tenant/system/storage', data)
export const applyStorage = (data: any) => post('/tenant/system/storage/apply', data)

export const getWechat = (params?: PageQuery) => get('/tenant/system/wechat', params)
export const createWechat = (data: any) => post('/tenant/system/wechat', data)

export const getI18n = (params?: PageQuery) => get('/tenant/system/i18n', params)
export const createI18n = (data: any) => post('/tenant/system/i18n', data)
export const updateI18n = (id: number, data: any) => put(`/tenant/system/i18n/${id}`, data)
export const deleteI18n = (id: number) => del(`/tenant/system/i18n/${id}`)

/* ---------- CMS ---------- */
export const getCmsContents = (params: PageQuery) => get('/tenant/cms/contents', params)
export const createCmsContent = (data: any) => post('/tenant/cms/contents', data)
export const updateCmsContent = (id: number, data: any) => put(`/tenant/cms/contents/${id}`, data)
export const deleteCmsContent = (id: number) => del(`/tenant/cms/contents/${id}`)
