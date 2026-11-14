# 示例物流 · 物流货运数字化多端运营平台

面向物流货运企业的 **SaaS 多租户** 数字化平台，覆盖 **运单、调度、在途、签收、结算、理赔、报表** 全链路，配套 **管理后台、官网、H5、司机 App、客户/承运商小程序** 多端。

## 目录结构

```
wuliu/
├── PRO.md                 # 产品需求
├── tasks.md               # 开发任务（T01~Tnn）
├── checklist.md           # 上线验收清单
├── docs/
│   ├── API.md             # REST 接口契约（所有客户端遵循）
│   ├── DEPLOYMENT.md      # 部署与运行指引
│   └── CONFIG.md          # 配置说明
├── backend/               # 后端（Spring Boot 3 + MyBatis-Plus，4 模块，59 表）
│   ├── wuliu-common/      # 通用：工具、上下文、异常、响应、加密
│   ├── wuliu-domain/      # 领域：实体 + Mapper + DDL 生成源
│   ├── wuliu-service/     # 服务：认证、计费、运单状态机、调度、结算…
│   ├── wuliu-api/         # 接口：Controller + 启动类 + 配置
│   └── tools/schema.mjs   # 59 张表/实体/DDL 唯一生成源
├── admin-web/             # 管理后台（Vue3 + Element Plus，平台端+租户端）
├── official-web/          # 官网（Vue3，询价/跟踪/网点）
├── h5/                    # H5 移动端（Vue3，询价/跟踪/电子回单）
├── driver-app/            # 司机 App（Flutter）
├── miniprogram-customer/  # 客户小程序（原生微信）
├── miniprogram-carrier/   # 承运商小程序（原生微信）
└── deploy/                # Docker Compose + Nginx + MySQL 初始化
```

## 快速开始

### 后端（零依赖演示，H2 内存库）

```bash
cd backend
mvn -q -DskipTests install
mvn -q -DskipTests spring-boot:run -pl wuliu-api
# http://localhost:8080/api/public/price-guide
```

### 前端

```bash
cd admin-web && npm install && npm run dev     # http://localhost:5173
cd official-web && npm install && npm run dev  # http://localhost:5174
cd h5 && npm install && npm run dev            # http://localhost:5175
```

### 演示账号

| 端 | 账号 | 密码 |
| --- | --- | --- |
| 租户端 | admin | admin123 |
| 平台端 | admin | admin123 |
| 司机端 | 任意手机号 | 验证码 1234 |
| 承运商端 | 任意手机号 | 验证码 1234 |

## 核心能力

- **多租户隔离**：MyBatis-Plus 租户插件自动追加 `tenant_id`，平台/租户双入口。
- **RBAC 角色权限**：平台角色 + 租户角色，菜单/按钮/数据范围三级授权。
- **全操作审计**：`audit_log` 记录关键动作，平台可追溯。
- **计费引擎**：重量体积取大、首重续重、燃油/装卸/上楼/保险/代收/偏远附加，协议价与底价审批。
- **运单状态机**：受理→调度→提货→运输→签收→结算，异常/取消/驳回闭环。
- **在途跟踪**：节点回放、GPS、卡滞看板、超时自动扫描。
- **结算对账**：应收/应付、对账单生成确认、账龄催收、代收货款、毛利。
- **理赔售后**：责任认定、赔付审批、投诉管理。
- **系统配置**：字典、菜单可视化、消息模板、SSO、对象存储多驱动、公众号、多语言、CMS。

详细接口契约见 [docs/API.md](docs/API.md)，部署见 [deploy/README.md](deploy/README.md)。
