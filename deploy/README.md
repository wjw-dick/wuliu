# 部署说明

本目录提供基于 Docker Compose 的一键部署编排，覆盖后端服务、MySQL、Redis 与三个前端（管理后台、官网、H5）。

## 前置条件

- Docker + Docker Compose
- 已构建三个前端 `dist`（详见各前端 README）

## 一键启动

```bash
cd deploy
docker compose up -d --build
```

启动后访问：

| 服务 | 地址 | 说明 |
| --- | --- | --- |
| 管理后台 | http://localhost:5173 | 平台端 / 租户端登录 |
| 官网 | http://localhost:5174 | 对外宣传与在线询价 |
| H5 移动端 | http://localhost:5175 | 手机浏览器询价 / 跟踪 |
| 后端 API | http://localhost:8080/api | 接口入口（Swagger/健康检查） |
| MySQL | localhost:13306 | 数据库（演示默认库 wuliu） |
| Redis | localhost:16379 | 缓存 / 会话 |

## 演示账号

| 端 | 账号 | 密码 |
| --- | --- | --- |
| 租户端 | admin | admin123 |
| 平台端 | admin | admin123 |
| 司机端 | 任意手机号 | 验证码 1234 |
| 承运商端 | 任意手机号 | 验证码 1234 |

## 生产注意事项

- 修改 `APP_JWT_SECRET` 环境变量为强随机值。
- MySQL / Redis 端口与密码按需修改，勿直接暴露到公网。
- 数据库初始化脚本位于 `deploy/mysql/schema.sql`（由 `backend/tools/schema.mjs` 生成，59 张表）。
- 首次部署会自动执行 `schema.sql` 初始化库表；业务演示数据默认不写入（生产建议 `app.seed.enabled=false`）。

## 本地开发

```bash
# 后端（H2 内存库，零依赖）
cd backend && mvn -DskipTests spring-boot:run -pl wuliu-api

# 前端
cd admin-web && npm install && npm run dev
cd official-web && npm install && npm run dev
cd h5 && npm install && npm run dev
```

后端默认使用 H2（MySQL 兼容模式）并自动初始化 schema 与演示数据；切换生产库见 `application-prod.yml` 与 `docs/CONFIG.md`。
