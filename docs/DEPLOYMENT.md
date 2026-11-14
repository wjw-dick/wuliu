# 部署与运行指引

本文档面向运维/实施人员，说明如何在本地或服务器运行「示例物流 · 物流货运数字化多端运营平台」。

## 一、架构总览

```
┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  admin-web   │  │ official-web │  │     h5      │  │ driver-app  │
│  管理后台     │  │   官网       │  │  移动端H5    │  │ 司机App     │
│  Vue3+EP     │  │  Vue3       │  │  Vue3       │  │ Flutter     │
└──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘
       │                 │                │                │
       └─────────────────┴───────┬────────┴────────────────┘
                                 ▼
                        ┌────────────────┐
                        │  wuliu-api      │  Spring Boot 3 · MyBatis-Plus
                        │  后端 REST       │  多租户 · RBAC · 审计 · 计费引擎
                        └───────┬────────┘
                                │
              ┌─────────────────┼─────────────────┐
              ▼                 ▼                 ▼
        ┌──────────┐      ┌──────────┐      ┌──────────┐
        │  MySQL    │      │  Redis    │      │ 对象存储   │
        │  59 张表   │      │  缓存     │      │ S3/OSS/...│
        └──────────┘      └──────────┘      └──────────┘
```

- **后端**：4 模块 Maven 工程（common/domain/service/api），59 张表，13 个业务域。
- **前端**：管理后台（平台端+租户端）、官网、H5 移动端、司机 App（Flutter）、客户/承运商小程序（原生微信）。

## 二、后端运行

### 开发模式（H2 内存库，零依赖）

```bash
cd backend
mvn -q -DskipTests install      # 首次需将模块安装到本地仓库
mvn -q -DskipTests spring-boot:run -pl wuliu-api
# 访问 http://localhost:8080/api/public/price-guide
```

H2 以 MySQL 兼容模式运行，启动时自动执行 `schema.sql`（59 张表）并写入演示数据。

### 生产模式（MySQL + Redis）

```bash
cd backend
mvn -q -DskipTests package
java -jar wuliu-api/target/wuliu-api-*.jar --spring.profiles.active=prod
# 或 Docker 部署见 deploy/README.md
```

生产数据库初始化：`deploy/mysql/schema.sql`。

## 三、前端运行

```bash
# 管理后台（5173）
cd admin-web && npm install && npm run dev

# 官网（5174）
cd official-web && npm install && npm run dev

# H5（5175）
cd h5 && npm install && npm run dev
```

生产构建：`npm run build`，产物 `dist/` 交给 Nginx 或 Docker 镜像。

## 四、小程序（原生，无构建）

- `miniprogram-customer`：客户小程序（询价 / 跟踪 / 我的订单）。
- `miniprogram-carrier`：承运商小程序（登录 / 任务 / 对账）。
- 用微信开发者工具导入对应目录即可；`app.js` 中 `baseUrl` 指向后端地址。

## 五、司机 App（Flutter）

```bash
cd driver-app
flutter pub get
flutter run          # 真机/模拟器运行
flutter analyze      # 静态检查
flutter build apk    # 产物 build/app/outputs/flutter-apk/app-release.apk
```

演示登录：任意手机号 + 验证码 `1234`。

## 六、接口契约

所有客户端遵循 `docs/API.md` 中约定的 REST 契约：

- 统一响应包裹：`{ code, message, data }`，`code=0` 成功。
- 列表接口返回 `{ list, total, page, size }`。
- 认证：`POST /api/auth/login` 返回 `data.token`，请求头携带 `Authorization: Bearer <token>`。
