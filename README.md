# 性格本色测试小程序

微信小程序 + Spring Boot + MySQL + DeepSeek，实现性格测试、个人报告、好友邀请和双人关系分析。

## 目录

- `miniprogram/`：微信小程序前端
- `backend/`：Spring Boot 2.7 后端

## 本地开发

1. 启动 MySQL，创建数据库：

```sql
CREATE DATABASE persona_test DEFAULT CHARACTER SET utf8mb4;
```

2. 配置后端环境变量：

```powershell
$env:MYSQL_USER="root"
$env:MYSQL_PASSWORD="123456"
$env:AI_API_KEY="你的 DeepSeek Key"
$env:WECHAT_MOCK_ENABLED="true"
$env:AUTH_DEV_LOGIN_ENABLED="true"
```

3. 启动后端：

```powershell
cd backend
mvn spring-boot:run
```

4. 用微信开发者工具打开 `miniprogram/`。开发环境默认后端地址为 `http://localhost:8080`。

## 生产环境

生产环境必须关闭开发登录和微信 mock，并注入真实密钥：

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:MYSQL_USER="..."
$env:MYSQL_PASSWORD="..."
$env:AI_API_KEY="..."
$env:WECHAT_APP_ID="..."
$env:WECHAT_APP_SECRET="..."
$env:AUTH_DEV_LOGIN_ENABLED="false"
$env:WECHAT_MOCK_ENABLED="false"
```

生产部署前还需完成：

- 将小程序 `request` 合法域名配置为 HTTPS API 域名。
- 将前端 `app.js` 中 `apiBase` 改为生产 HTTPS 地址。
- 使用数据库迁移工具管理 schema，避免直接使用 `ddl-auto: update`。
- 将 CORS 白名单调整为生产域名。
- 将 DeepSeek Key 和数据库密码放入密钥管理服务。

## 后端测试

```powershell
cd backend
mvn test
```

## 数据库迁移

项目使用 Flyway 管理数据库结构，迁移脚本位于：

```text
backend/src/main/resources/db/migration
```

现有数据库首次接入时会自动建立 `flyway_schema_history` 并标记 baseline，不会删除已有数据。后续表结构变更按 `V2__xxx.sql`、`V3__xxx.sql` 顺序新增即可。

生产环境启动前应使用：

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
mvn spring-boot:run
```

生产 profile 会将 Hibernate 设为 `ddl-auto: validate`，由 Flyway 负责 schema 演进。

## 后台维护

后端启动后会按固定间隔清理已撤销或已过期的登录 Token，并将过期分享链接标记为 `EXPIRED`。可通过环境变量调整间隔：

```powershell
$env:MAINTENANCE_INTERVAL_MS="3600000"
```
