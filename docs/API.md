# 接口契约（各端共用）

- Base URL：`http://localhost:8080/api`（开发环境，前端用 Vite 代理 `/api`）
- 认证：`Authorization: Bearer <token>`；登录接口返回 token、用户信息、角色、菜单、按钮权限
- 租户上下文由 token 解析，前端**不要**传 tenantId；平台端通过登录 scope 区分
- 时间格式：`yyyy-MM-dd HH:mm:ss`；金额单位：元（两位小数）；重量 kg、体积 m³
- 统一返回：`{"code":0,"message":"成功","data":...}`；分页 data 为 `{"list":[],"total":0,"page":1,"size":10}`
- 错误：`code` 非 0（400 参数/401 未登录/403 无权限/404 不存在/409 冲突/500 异常），`message` 为中文
- 幂等：业务写接口可带 `X-Idempotency-Key`，重复提交返回同结果
- 所有枚举字段同时返回编码与中文：`status` + `statusName`、`type` + `typeName`

## 1. 认证

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/auth/login` | body `{username,password,scope:"platform"\|"tenant"}` |
| POST | `/auth/logout` | 退出 |
| GET | `/auth/me` | 当前用户、角色、菜单树、按钮权限、租户信息 |
| POST | `/auth/password` | `{oldPassword,newPassword}` |
| POST | `/auth/sso/:provider` | SSO 登录（CAS/oauth2/oidc/sa-token，演示返回降级提示） |

登录返回：
```json
{"code":0,"message":"成功","data":{"token":"...","user":{"id":1,"username":"admin","nickname":"企业管理员","scope":"tenant","tenantId":1,"tenantName":"示例物流","orgName":"总部"},"roles":["企业管理员"],"menus":[...],"buttons":["waybill:create"]}}
```

## 2. 平台运营端（scope=platform）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/platform/dashboard` | 租户数、试用/欠费/到期、平台运单量、异常量、到期租户 |
| GET/POST | `/platform/tenants` | 列表（keyword/status/page/size）/ 开通 |
| GET/PUT/DELETE | `/platform/tenants/{id}` | 详情 / 编辑 / 删除（归档） |
| POST | `/platform/tenants/{id}/renew` | `{months}` 续期 |
| POST | `/platform/tenants/{id}/suspend` | `{reason}` 停用 |
| POST | `/platform/tenants/{id}/enable` | 启用 |
| GET | `/platform/licenses` | 资质审核列表 `status=待审核` |
| POST | `/platform/licenses/{id}/review` | `{pass:true,reason}` |
| GET/POST/PUT/DELETE | `/platform/plans` | 套餐 |
| GET/POST | `/platform/seats` | 席位增购记录 |
| GET/POST | `/platform/bills` | 账单 |
| POST | `/platform/bills/{id}/pay` | `{amount,method,remark}` 收款登记 |
| GET/POST/PUT/DELETE | `/platform/users` | 平台账号 |
| GET/POST/PUT/DELETE | `/platform/roles` | 平台角色 |
| GET | `/platform/audits` | 平台审计（operator/action/tenantId/start/end/page/size） |
| GET/POST/PUT/DELETE | `/platform/announcements` | 公告 |
| GET/POST/PUT/DELETE | `/platform/dicts` | 平台字典 |
| GET/PUT | `/platform/configs` | 平台参数 |

## 3. 租户后台工作台 / 组织 / 用户

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/tenant/dashboard` | 今日新增/在途/待调度/异常/今日营收/欠款/到期资质/状态分布/近7日趋势 |
| GET/POST/PUT/DELETE | `/tenant/orgs` | 网点/部门/车队 |
| GET/POST/PUT/DELETE | `/tenant/users` | 租户账号（席位校验） |
| GET/POST/PUT/DELETE | `/tenant/roles` | 角色（菜单/按钮授权） |
| GET | `/tenant/menus/tree` | 菜单树（授权用） |
| GET | `/tenant/announcements` | 平台+本租户公告 |

## 4. 客户、协议价、线索

| 方法 | 路径 | 说明 |
|---|---|---|
| GET/POST/PUT/DELETE | `/tenant/customers` | 客户档案；列表支持 keyword/type/settleType/page/size |
| GET | `/tenant/customers/{id}` | 详情含联系人、地址簿、协议、跟进 |
| POST/PUT/DELETE | `/tenant/customers/{id}/contacts` | 联系人 |
| POST/PUT/DELETE | `/tenant/customers/{id}/addresses` | 地址簿 |
| GET/POST/PUT/DELETE | `/tenant/agreements` | 协议价（客户×线路×车型×有效期） |
| GET/POST/PUT/DELETE | `/tenant/leads` | 线索 |
| POST | `/tenant/leads/{id}/assign` | `{userId}` |
| POST | `/tenant/leads/{id}/convert` | 转客户 |
| GET/POST | `/tenant/followups` | 跟进记录 / 回访计划 |

## 5. 询价、报价、计费

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/public/quote` | 官网/小程序/H5 询价（免登录） |
| POST | `/tenant/pricing/quote` | 后台试算/报价 |
| GET/POST/PUT/DELETE | `/tenant/pricing/rules` | 计费规则（版本、生效时间） |
| POST | `/tenant/pricing/rules/{id}/publish` | 发布新版本 |
| POST | `/tenant/pricing/simulate` | 规则试算器（不落单） |
| GET/POST | `/tenant/quotes` | 报价单列表/新建 |
| POST | `/tenant/quotes/{id}/convert` | 报价转下单 |
| POST | `/tenant/quotes/{id}/approve` | 低于底价审批 `{pass,reason}` |
| GET/PUT/DELETE | `/tenant/quotes/{id}` | 详情/失效/删除 |

询价入参：
```json
{"origin":"上海","destination":"杭州","cargoType":"普货","weight":500,"volume":2.5,"pieces":10,"vehicleType":"6.8 米厢式","value":10000,"needLoading":true,"needUpstairs":false,"codAmount":0,"customerId":null,"ladder":false}
```
报价返回：
```json
{"quoteNo":"BJ202611140001","baseFreight":600.00,"fuelSurcharge":60.00,"loadingFee":50.00,"upstairsFee":0,"insuranceFee":30.00,"codFee":0,"remoteFee":0,"holidayFee":0,"totalAmount":740.00,"ruleVersion":"V1","priceSource":"线路标准价","detail":[{"name":"基础运费","amount":600.00,"formula":"首重 100kg/120 元 + 续重 400kg × 1.2 元/kg"}]}
```

## 6. 运单、调度、运力

| 方法 | 路径 | 说明 |
|---|---|---|
| GET/POST | `/tenant/waybills` | 列表 / 新建（客户端下单与后台代录共用） |
| GET/PUT/DELETE | `/tenant/waybills/{id}` | 详情 / 改单 / 删除（仅待报价） |
| GET | `/tenant/waybills/{id}/timeline` | 时间轴 + 单据 + 照片 + 费用 |
| POST | `/tenant/waybills/{id}/action` | 状态机迁移 `{action,reason,payload}` |
| GET | `/tenant/waybills/{id}/print` | 托运单 HTML |
| GET | `/tenant/waybills/{id}/label` | 标签 HTML（100×150） |
| POST | `/tenant/waybills/import` | 批量导入 |
| GET | `/tenant/dispatch/pool` | 待调度池（按线路/车型分组） |
| GET | `/tenant/dispatch/board` | 看板：vehicles[{plateNo,status,lat,lng,waybillNo}] + columns |
| POST | `/tenant/dispatch/assign` | `{waybillId,vehicleId,driverId,carrierId}` |
| POST | `/tenant/dispatch/{id}/reassign` | 改派 |
| POST | `/tenant/dispatch/{id}/cancel` | 取消派车 `{reason,emptyFee}` |
| GET/POST/PUT/DELETE | `/tenant/vehicles` | 车辆档案 |
| GET/POST/PUT/DELETE | `/tenant/drivers` | 司机档案 |
| GET/POST/PUT/DELETE | `/tenant/carriers` | 承运商档案 |
| GET | `/tenant/qualifications/expiring` | 到期资质（10/30 天） |

运单字段（创建/详情统一）：
```json
{"id":1,"waybillNo":"YH0001202611140001","customerId":1,"customerName":"上海示例制造有限公司",
 "senderName":"张经理","senderPhone":"13800000001","senderAddress":"上海市嘉定区xx路1号",
 "receiverName":"李经理","receiverPhone":"13800000002","receiverAddress":"杭州市余杭区xx路2号",
 "origin":"上海","destination":"杭州","cargoName":"机械配件","cargoType":"普货","weight":500,"volume":2.5,"pieces":10,
 "vehicleType":"6.8 米厢式","pickupType":"上门提货","deliveryType":"送货上门","settleType":"月结",
 "pickupTime":"2026-11-15 09:00:00","deliveryTime":"2026-11-16 18:00:00",
 "status":"已受理","statusName":"已受理","totalAmount":740.00,"receivableAmount":740.00,"payableAmount":520.00,"grossProfit":220.00,
 "insured":false,"codAmount":0,"remark":"","createdAt":"2026-11-14 10:00:00"}
```

## 7. 在途、异常、签收、回单

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/tenant/tracks/{waybillId}` | 轨迹点 + 节点 + 最新位置 |
| GET | `/tenant/tracks/{waybillId}/replay` | 回放（按分钟聚合） |
| GET/POST | `/tenant/exceptions` | 异常单列表 / 上报 |
| POST | `/tenant/exceptions/{id}/handle` | `{conclusion,assigneeId,escalate}` |
| GET | `/tenant/receipts` | 回单列表（待审核/已通过/已驳回） |
| POST | `/tenant/receipts/{id}/review` | `{pass:true,reason}` |
| GET | `/tenant/receipts/{id}/pdf` | 电子回单 PDF 链接 |
| GET/POST | `/tenant/returns` | 退回任务 |
| POST | `/tenant/returns/{id}/complete` | 退回签收 |

## 8. 结算、对账、发票、代收

| 方法 | 路径 | 说明 |
|---|---|---|
| GET/POST | `/tenant/receivables` | 应收 |
| GET/POST | `/tenant/payables` | 应付 |
| GET/POST | `/tenant/statements` | 对账单（`{customerId,periodStart,periodEnd,type:"客户"\|"承运商"}`） |
| GET | `/tenant/statements/{id}` | 明细 + 合计 |
| POST | `/tenant/statements/{id}/confirm` | 客户确认 |
| POST | `/tenant/statements/{id}/dispute` | 异议 `{reason}` |
| GET | `/tenant/statements/{id}/pdf` | PDF |
| GET/POST | `/tenant/invoices` | 发票申请/登记（校验金额） |
| GET/POST | `/tenant/payments` | 收款登记 `{statementId,amount,method,remark,differenceReason}` |
| GET | `/tenant/aging` | 账龄分桶 + 催收清单 |
| GET/POST | `/tenant/cod` | 代收货款结算 |
| GET | `/tenant/profit` | 单票/线路毛利 |

## 9. 理赔、报表

| 方法 | 路径 | 说明 |
|---|---|---|
| GET/POST | `/tenant/claims` | 理赔 |
| POST | `/tenant/claims/{id}/liability` | 定责 `{liability,reason}` |
| POST | `/tenant/claims/{id}/approve` | 审批 `{pass,reason,amount}` |
| POST | `/tenant/claims/{id}/pay` | 赔付登记 |
| POST | `/tenant/claims/{id}/close` | 结案 |
| GET | `/tenant/complaints` | 投诉与满意度 |
| GET | `/tenant/reports/{type}` | type: volume/revenue/line-profit/timeliness/exception/driver/customer/carrier |
| GET | `/tenant/reports/{type}/export` | 导出（记录用途，返回下载地址） |
| GET | `/tenant/stagnant` | 卡滞看板 |

## 10. 系统配置

| 方法 | 路径 | 说明 |
|---|---|---|
| GET/PUT | `/tenant/system/configs` | 系统参数（分组） |
| GET/POST/PUT/DELETE | `/tenant/system/dicts` | 字典分类 |
| POST/PUT/DELETE | `/tenant/system/dicts/{code}/items` | 字典项（中文名必填） |
| GET/PUT | `/tenant/system/menus` | 菜单可视化（官网/小程序/H5/公众号） |
| GET/POST/PUT/DELETE | `/tenant/system/message-templates` | 模板绑定 |
| POST | `/tenant/system/message-templates/{id}/test` | 测试发送 |
| GET | `/tenant/system/message-logs` | 推送日志（含降级） |
| POST | `/tenant/system/message-logs/{id}/resend` | 补发 |
| GET/PUT | `/tenant/system/sso` | SSO 配置 |
| GET/POST | `/tenant/system/storage` | 存储配置 / 连接测试 `{driver,config}` |
| POST | `/tenant/system/storage/apply` | 保存应用（二次确认） |
| GET/POST | `/tenant/system/wechat` | 公众号/小程序账号 |
| GET/POST/PUT/DELETE | `/tenant/system/i18n` | 多语言 |
| GET/POST/PUT/DELETE | `/tenant/cms/contents` | 富文本内容 |

## 11. 司机端 / 承运商端 / H5

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/driver/login` | 手机号+验证码（演示 1234） |
| GET | `/driver/tasks` | 任务列表（离线缓存） |
| POST | `/driver/tasks/{id}/checkin` | `{lat,lng,address}` |
| POST | `/driver/tasks/{id}/weight` | `{weight,volume,pieces,photos}` |
| POST | `/driver/tasks/{id}/load` | `{items,photos}` |
| POST | `/driver/tasks/{id}/nodes` | `{node,lat,lng,remark}` |
| POST | `/driver/tasks/{id}/sign` | `{signerName,signerPhone,signImage}` |
| POST | `/driver/tasks/{id}/receipt` | `{images}` |
| POST | `/driver/tasks/{id}/exception` | `{type,description,images}` |
| POST | `/driver/gps` | `{lat,lng,speed,waybillId}` |
| GET | `/driver/income` | 收入明细 |
| POST | `/carrier/login` | 手机号登录 |
| GET | `/carrier/orders` | 接单大厅 |
| POST | `/carrier/orders/{id}/accept` | 接单 |
| GET | `/carrier/tasks` | 我的派车单 |
| POST | `/carrier/tasks/{id}/report` | 作业上报（同司机） |
| GET | `/carrier/statements` | 应付对账 |
| POST | `/carrier/statements/{id}/confirm` | 确认 / 申诉 |
| GET | `/h5/receipt/{waybillNo}?sign=xxx` | 电子回单页数据 |
| POST | `/h5/quote/share` | 生成分享链接 |

## 12. 公共

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/public/sites` | 网点 |
| GET | `/public/track?waybillNo=&phone=` | 运单轨迹（中文） |
| POST | `/public/lead` | 留资 `{name,phone,company,source,remark}` |
| GET | `/public/contents?type=资讯` | 官网内容 |
| GET | `/public/price-guide` | 计费口径说明 |
| POST | `/public/apply-monthly` | 企业月结申请 |
| POST | `/public/pay/callback` | 支付回调（验签，幂等） |
| POST | `/wechat/callback` | 公众号服务器回调（Token 校验） |
