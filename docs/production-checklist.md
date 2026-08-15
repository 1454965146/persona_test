# 生产上线检查清单

## 身份与权限

- [x] 后端使用随机 Bearer Token 保存用户会话。
- [x] 报告、分享绑定、对比结果均校验资源归属。
- [x] 开发账号登录默认仅本地开启，生产 profile 默认关闭。
- [x] 微信登录通过 `jscode2session` 获取 openid。
- [ ] 上线前配置正式小程序 AppID 和 AppSecret。
- [ ] 为小程序后台配置 HTTPS 合法请求域名。

## 稳定性

- [x] 双人对比改为异步生成，支持 `PENDING`、`PROCESSING`、`COMPLETED`、`FAILED`。
- [x] 重复绑定和重复对比通过查询和状态校验避免重复生成。
- [x] AI 调用失败时有本地降级模板。
- [x] 已补充后端基础单元测试。
- [ ] 多实例部署时将内存限流替换为 Redis 限流。
- [ ] 为异步任务接入可靠队列、重试和监控。

## 数据与安全

- [x] 数据库密码、AI Key、微信密钥全部改为环境变量注入。
- [x] 密码使用 PBKDF2 加盐哈希。
- [x] Dockerfile 不再硬编码默认密码。
- [x] 引入 Flyway 管理数据库迁移，并提供现有数据库 baseline 策略。
- [ ] 为 `report_code`、`share_code`、`session_code` 增加索引并检查唯一约束。
- [ ] 增加审计日志和敏感操作监控。

## 前端与体验

- [x] 历史记录改为服务端查询，按用户隔离。
- [x] 被邀请者看不到邀请者选择的测评方向。
- [x] 历史进入双人对比可正常显示雷达图、维度对比和匹配度。
- [x] 登录失效时前端统一清理状态并跳回首页。
- [ ] 清理本地残留的 ECharts 目录和 `package-lock` 中旧依赖。
- [ ] 增加弱网、AI 超时、分享链接过期等异常状态的可视化验证。

## 数据库迁移

- [x] `flyway-core` 和 `flyway-mysql` 已加入依赖。
- [x] `V1__init_schema.sql` 定义全新数据库完整 schema。
- [x] 开发环境使用 `baseline-on-migrate=true` 兼容现有数据库。
- [x] 生产 profile 使用 `ddl-auto: validate`，后续 schema 变更由 Flyway 控制。
- [ ] 发布前在隔离数据库验证 `V1__init_schema.sql` 和 Hibernate 实体完全匹配。
