# 配置说明

## 应用配置（`application.yml`）

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `server.port` | 8080 | 后端服务端口 |
| `spring.datasource.url` | H2 内存库 | 默认演示库；生产见 `application-prod.yml` |
| `app.seed.enabled` | true | 是否初始化演示数据（2 租户、20+ 运单、客户/司机/车辆等） |
| `app.jwt.secret` | 演示密钥 | JWT 签名密钥（生产必须修改） |
| `app.jwt.ttl-hours` | 12 | 令牌有效期（小时） |
| `app.cache.type` | memory | 缓存类型：memory / redis |

## 生产配置（`application-prod.yml`，激活 `--spring.profiles.active=prod`）

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `DB_URL` | jdbc:mysql://127.0.0.1:3306/wuliu | MySQL 连接串 |
| `DB_USER` | wuliu | 数据库账号 |
| `DB_PASSWORD` | wuliu123 | 数据库密码 |
| `APP_CACHE_TYPE` | redis | 缓存类型 |
| `SPRING_DATA_REDIS_HOST` / `PORT` | 127.0.0.1 / 6379 | Redis 连接 |
| `APP_JWT_SECRET` | - | JWT 密钥（必改） |

## 多租户

- 基于 MyBatis-Plus `TenantLineInnerInterceptor` 自动追加 `tenant_id` 隔离。
- 忽略表：`tenant`、`plan`、`bill`、`seat_order`、`platform_*`、`announcement`、`audit_log`（全局共享）。
- `TenantContext` 使用 ThreadLocal 在请求级维护当前租户与操作人。

## 手机号脱敏与加密

- 敏感手机号使用 AES 加密存储（`CryptoTypeHandler`），输出时经 `PhoneMaskSerializer` 脱敏（如 `138****0000`）。
- 更新时若传入掩码（含 `*`）则视为未修改，保留原密文。

## 对象存储驱动

`app.storage.driver` 支持：`local`（默认）、`s3`、`oss`、`cos`、`minio`。
租户后台「对象存储」页可在线切换并测试连接；本地驱动存储于 `./uploads`。

## 计费引擎

- 体积重：`体积(m³) × 250kg`，按重量体积取大。
- 首重 100kg 内一口价，超出部分按 `1.2 元/kg` 计。
- 附加费：燃油 10%、装卸 50 元/票、上楼 30 元/票、保险费 货值×3‰、代收手续费 代收×6‰、偏远 20 元/票。
- 协议价（`agreement`）优先于标准价；低于底价需审批。
