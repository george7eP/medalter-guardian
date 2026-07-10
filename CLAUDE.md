# CLAUDE.md

## 项目概述

**医疗设备检修与预警系统**（Medical Device Maintenance and Early Warning System）是一个面向医疗机构设备运维场景的数字化管理平台。前后端分离架构：Spring Boot 3.2.6（Java 21）+ Vue 3.5（TypeScript 6.0）。

- 仓库：`git@github.com:george7eP/medalter-guardian.git`
- 分支：`master`
- 数据库：MySQL 8.0 `medical_device`

---

## 技术栈速览

| 层 | 技术 | 版本 |
|----|------|------|
| 后端框架 | Spring Boot + Security | 3.2.6 |
| Java | Java | 21 |
| ORM | MyBatis-Plus (spring-boot3-starter) | 3.5.6 |
| JWT | JJWT (api/impl/jackson) | 0.12.6 |
| 前端框架 | Vue 3 (Composition API) | 3.5 |
| 构建 | Vite | 8.0 |
| UI | Element Plus | 2.14 |
| 状态管理 | Pinia | 3.0 |
| 路由 | vue-router | 5.0 |

---

## 项目结构

```
medalter-guardian/
├── backend/                          # Spring Boot 3.2.6, port 9999
│   ├── database/schema.sql           # 9 张表 DDL
│   ├── database/data.sql             # 种子数据（3 用户, 3 角色, 10 权限）
│   └── src/main/java/com/isoft/yidajava/
│       ├── annotation/Log.java       # @Log 审计注解
│       ├── aspect/LogAspect.java     # AOP 环绕切面（双路兜底获取操作人）
│       ├── common/                   # Result, LoginRequest, LoginResponse
│       ├── config/                   # SecurityConfig, JwtAuthFilter, MybatisPlusConfig, MyMetaObjectHandler
│       ├── controller/               # 10 个 REST Controller
│       ├── dao/                      # 10 个 MyBatis-Plus Mapper（仅 1 条手写 SQL）
│       ├── entity/                   # 11 个实体（RBAC 5 + 业务 5 + SysLog）
│       ├── service/                  # 9 个接口 + 10 个实现（含 UserDetailsServiceImpl）
│       └── util/                     # JwtUtil, PasswordGenerator
├── frontend/                         # Vue 3 + Vite 8, port 5173
│   └── src/
│       ├── api/                      # 8 个 API 模块（按业务分目录）
│       ├── components/layout/        # top.vue + leftside.vue（动态菜单）
│       ├── router/index.ts           # 路由 + beforeEach 权限守卫
│       ├── stores/users.ts           # Pinia: token, permissions, userInfo
│       ├── util/request.ts           # Axios 封装（JWT 拦截 + 错误处理）
│       └── views/                    # 12 个页面视图
└── README.md / README_en.md
```

---

## 安全架构（三层防护）

1. **前端路由守卫** — `router.beforeEach` 检查 `meta.permission`，`leftside.vue` 动态过滤菜单
2. **后端 URL 认证** — `SecurityConfig` 仅放行 `/auth/**` + `OPTIONS`，其余由 `JwtAuthenticationFilter` 验证
3. **后端方法级鉴权** — `@PreAuthorize("hasAuthority('system:user')")`（当前仅在 `SysUserController` 使用）

### AOP 审计

- 19 处 `@Log` 注解覆盖全部写操作
- `LogAspect` 双路兜底：SecurityContext（已认证操作）→ LoginRequest 参数提取（登录操作）
- 记录字段：username, operation, method, params, time, ip

---

## 当前进度

| 阶段 | 状态 | 说明 |
|------|------|------|
| 项目初始化 | ✅ 完成 | Git init, GitHub push, 初始提交 |
| README 修订 | ✅ 已暂存 | 修正名称、版本号、新增架构/安全/API 详细文档 |
| 代码走读 | ✅ 完成 | 三阶段深度阅读全部关键代码 |
| 种子数据修复 | ✅ 完成 | BCrypt 哈希有效，system:log 权限已补，API URL 已修正 |
| 权限补全 | ⬜ 待开始 | 业务 Controller 缺少 @PreAuthorize |
| 配置安全 | ⬜ 待开始 | JWT secret / DB 密码硬编码 |

---

## 目标追踪

| ID | 目标 | 优先级 | 状态 | 依赖 |
|----|------|--------|------|------|
| T1 | 修复 `data.sql` 种子数据（BCrypt 哈希 + 缺少 system:log） | 🔴 高 | ✅ | — |
| T2 | 为业务 Controller 补全 `@PreAuthorize` 方法级鉴权 | 🟡 中 | ⬜ | T1 |
| T3 | JWT secret 和 DB 密码外置到环境变量/配置文件分离 | 🟡 中 | ⬜ | — |
| T4 | 补充 `system:log` 权限到种子数据 + 前后端映射 | 🔴 高 | ✅ | T1 |
| T5 | 前端路由守卫补充业务页面权限绑定（device, inspect, warn） | 🟡 中 | ⬜ | T2 |
| T6 | CORS 配置收紧（生产环境限制具体 Origin） | 🟢 低 | ⬜ | — |
| T7 | 补充单元测试与集成测试 | 🟢 低 | ⬜ | — |
| T8 | README.md 中版本号与实际 pom.xml / package.json / engines 持续同步 | 🟢 低 | ⬜ | — |

---

## 已知问题

1. **业务 Controller 无 `@PreAuthorize`**：`DeviceInfoController`、`InspectPlanController`、`InspectRecordController`、`WarnRuleController`、`WarnInfoController` 均未添加方法级鉴权注解，仅依赖 URL 级认证。
2. **`application.yaml` 敏感信息硬编码**：JWT secret 和数据库密码明文存储。
3. **README 版本滞后**：此前 README 写 Java 17 / Vite 5 / Node 18，实际为 Java 21 / Vite 8 / Node 22（已于当前暂存中修正）。

---

## 常用命令

```bash
# 后端
cd backend && ./mvnw spring-boot:run          # 启动 (port 9999)

# 前端
cd frontend && npm install && npm run dev      # 启动 (port 5173)

# 数据库初始化
mysql -u root -p < backend/database/schema.sql
mysql -u root -p < backend/database/data.sql

# 生成 BCrypt 密码哈希
cd backend && ./mvnw exec:java -Dexec.mainClass="com.isoft.yidajava.util.PasswordGenerator"
```
