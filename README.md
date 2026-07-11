# 🏥 医疗设备检修与预警系统

<p align="center">
  <b>中文</b> | <a href="./README_en.md">English</a>
</p>

---

## 📖 项目简介

本项目是一个面向医疗机构设备运维场景的数字化管理平台。针对医疗设备运维中纸质记录繁琐、排程效率低、设备状态不透明、故障突发率高等痛点，依托前后端分离架构，实现医疗设备台账数字化管理、检修工作流自动化、设备故障智能预警、全流程操作日志审计等核心能力。

系统通过 RBAC 精细化权限管控、JWT 无状态认证、AOP 自动审计三条安全主线，为医疗设备运维提供标准化、流程化、可追溯的完整解决方案。

---

## 🎯 需求分析

### 1. 业务痛点

- **状态不透明**：医疗设备状态信息分散，无法实时掌握设备运行与检修状态。
- **排程效率低**：人工排程易遗漏，检修逾期风险高，缺乏自动化提醒机制。
- **留存追溯差**：纸质记录难以留存，操作审计工作量大、追溯性差。

### 2. 核心功能目标

- **精细化权限体系**：基于 RBAC（用户 → 角色 → 权限）三级管控模型，支持前端路由与后端 API 双重细粒度权限控制。
- **全业务闭环管理**：覆盖设备入库、台账维护、检修计划制定、检修执行记录、故障预警处理全流程。
- **全流程安全审计**：基于 AOP 切面技术，自动记录所有核心业务操作日志，操作人、IP、耗时、参数全程可追溯。

### 3. 非功能需求

- **数据安全**：用户密码采用 BCrypt 单向加密存储。
- **接口安全**：基于 JWT（JJWT 0.12.x）实现无状态身份认证。
- **系统性能**：MyBatis-Plus 分页插件 + MySQL 8.0，保障高频运维操作场景下的查询性能。

---

## 💻 技术栈

### 后端

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | **21** | 核心语言 |
| Spring Boot | **3.2.6** | 基础框架 |
| Spring Security | 3.2.6（内置） | 认证与授权 |
| MyBatis-Plus | **3.5.6** | ORM（spring-boot3-starter） |
| JJWT | **0.12.6** | JWT 令牌（api / impl / jackson 三模块） |
| MySQL | **8.0** | 关系型数据库 |
| Maven | 3.x | 构建工具（阿里云镜像加速） |
| Lombok | **1.18.40** | 代码简化 |

### 前端

| 组件 | 版本 | 说明 |
|------|------|------|
| Node.js | **^20.19.0 \|\| >=22.12.0** | 运行环境 |
| Vue | **3.5**（Composition API） | 核心框架 |
| Vite | **8.0** | 构建工具 |
| TypeScript | **6.0** | 类型安全 |
| vue-router | **5.0** | 前端路由 |
| Pinia | **3.0** | 状态管理 |
| Axios | **1.16** | HTTP 请求 |
| Element Plus | **2.14** | UI 组件库 |

### 开发工具

- 后端开发：IntelliJ IDEA
- 前端开发：VS Code
- 适配系统：Windows 11 / macOS

---

## 🛠️ 项目架构设计

### 整体架构

```
┌─────────────────────────────────────────────────────┐
│                   浏览器 / Nginx                      │
├────────────────────┬────────────────────────────────┤
│   Frontend :5173   │      Backend :9999              │
│   Vue 3 + Vite     │      Spring Boot 3              │
│                    │                                 │
│   axios ──REST─────▶  Controller（10 个）              │
│   Bearer Token     │      │                          │
│                    │   Service（9 对接口/实现）         │
│   路由守卫           │      │                          │
│   菜单动态过滤       │   Mapper（10 个 MyBatis-Plus）    │
│                    │      │                          │
│                    │   MySQL 8.0（medical_device）    │
│                    │                                 │
│                    │   安全层：                        │
│                    │   JwtAuthFilter → SecurityConfig │
│                    │   @Log AOP → SysLog 审计          │
└────────────────────┴────────────────────────────────┘
```

### 后端分层结构

```
src/main/java/com/isoft/medalterguardian/
├── annotation/     # @Log 自定义审计注解
├── aspect/         # LogAspect AOP 环绕切面
├── common/         # Result 统一响应、LoginRequest/Response
├── config/         # SecurityConfig、JwtAuthFilter、MybatisPlusConfig、MyMetaObjectHandler
├── controller/     # REST 控制器（10 个）
├── dao/            # MyBatis-Plus Mapper（10 个，零 XML 配置）
├── entity/         # 数据库实体映射（11 个，含 RBAC 五表 + 业务五表 + SysLog）
├── service/        # 业务接口（9 个）
├── service/impl/   # 业务实现（10 个，含 UserDetailsServiceImpl）
└── util/           # JwtUtil、PasswordGenerator
```

### 前端目录结构

```
frontend/src/
├── api/                  # API 层（按模块分目录，对齐后端 Controller）
│   ├── device/index.ts   # 设备 CRUD API
│   ├── inspect/          # plan.ts + record.ts
│   ├── system/           # log.ts + role.ts
│   ├── user/index.ts     # 登录 + 用户 CRUD + 角色分配
│   └── warn/             # handle.ts + rule.ts
├── components/layout/    # top.vue（顶栏）+ leftside.vue（动态菜单）
├── router/index.ts       # 路由定义 + beforeEach 权限守卫
├── stores/users.ts       # Pinia 用户状态（token / permissions / userInfo）
├── util/request.ts       # Axios 封装（JWT 拦截器 + 统一错误处理）
└── views/                # 页面视图（按业务模块分目录）
    ├── device/index.vue
    ├── inspect/{plan,record}.vue
    ├── system/{log,role}.vue
    ├── user/{login,profile,userlist}.vue
    ├── warn/{handle,rule}.vue
    └── main.vue
```

---

## 📁 数据库设计（9 张表）

### RBAC 权限体系（5 张表）

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `sys_user` | 系统用户 | username, password(BCrypt), status(0/1) |
| `sys_role` | 角色 | role_code, role_name |
| `sys_permission` | 权限 | perm_code, perm_type(MENU/BUTTON/API), path, api_url |
| `sys_user_role` | 用户-角色关联 | user_id ⟷ role_id（多对多） |
| `sys_role_permission` | 角色-权限关联 | role_id ⟷ perm_id（多对多） |

### 业务体系（4 张表）

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `device_info` | 医疗设备台账 | device_status(NORMAL/WARN/FAULT), inspect_cycle, warn_days |
| `inspect_plan` | 检修计划 | plan_status(PENDING/COMPLETED), plan_date, principal |
| `inspect_record` | 检修记录 | inspect_result(PASS/PARTIAL/FAIL), operator |
| `warn_rule` | 预警规则 | warn_condition, warn_level, rule_status |
| `warn_info` | 预警信息 | handle_status(UNHANDLED/PROCESSED/IGNORED), handle_user |

### 审计（1 张表）

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `sys_log` | AOP 操作审计日志 | username, operation, method, params, time, ip |

---

## 🔐 安全设计详解

### 三层防护体系

```
第一层 ─ 前端路由守卫
  router.beforeEach → 检查 to.meta.permission
  leftside.vue → filterMenuByPermission() 动态过滤菜单
  userStore.hasPermission() → 前端权限拦截

第二层 ─ 后端 URL 级认证
  SecurityConfig:
    ├─ /auth/** 放行（登录登出）
    ├─ OPTIONS /** 放行（CORS 预检）
    └─ 其余请求 → JwtAuthFilter 验证 Token → SecurityContext

第三层 ─ 后端方法级鉴权
  @PreAuthorize("hasAuthority('system:user')") → 用户管理
  @PreAuthorize("hasAuthority('system:role')") → 角色管理
```

### JWT 认证流程

1. 用户在 `login.vue` 输入账号密码 → `POST /auth/login`
2. `AuthController` → `AuthenticationManager.authenticate()` → `UserDetailsServiceImpl.loadUserByUsername()`
3. 通过 `SysPermissionMapper.getPermissionsByUserId()` 三表 JOIN 查询用户所有权限
4. `JwtUtil.generateToken()` 生成 Token（含 userId、realName，24h 有效期）
5. 前端 `userStore.login()` 存入 Pinia + localStorage
6. 后续所有请求由 `request.ts` 拦截器自动附加 `Authorization: Bearer <token>`
7. `JwtAuthenticationFilter` 解析 Token 并设置 `SecurityContext`

### 权限查询 SQL（唯一手写 SQL）

```sql
SELECT DISTINCT p.*
FROM sys_permission p
INNER JOIN sys_role_permission rp ON p.id = rp.perm_id
INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id
WHERE ur.user_id = #{userId}
ORDER BY p.sort ASC
```

---

## 📊 AOP 审计机制

### 实现方式

- 自定义 `@Log("操作描述")` 注解 → 打在 Controller 方法上
- `LogAspect` 环绕切面（`@Around`）拦截所有 `@Log` 标记的方法
- 双路兜底获取操作人：SecurityContext（已认证操作）→ LoginRequest 参数提取（登录操作）

### 记录字段

| 字段 | 来源 |
|------|------|
| username | SecurityContext / LoginRequest 兜底 |
| operation | `@Log` 注解的 value |
| method | `类名.方法名()` |
| params | `Arrays.toString(参数)` |
| time | 执行耗时（ms） |
| ip | `request.getRemoteAddr()` |

### 已审计操作（19 处 @Log）

登录登出、用户 CRUD、密码修改、角色分配、设备 CRUD、检修计划/记录、预警规则/处理 — 覆盖全部写操作。

---

## 🔗 API 路由一览

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/auth/login` | 用户登录 | 无（放行） |
| POST | `/auth/logout` | 用户登出 | 需认证 |
| GET/POST/PUT/DELETE | `/user/**` | 用户管理 | `system:user` |
| PUT | `/user/password` | 修改密码 | 需认证 |
| GET | `/user/info` | 当前用户信息 | 需认证 |
| GET/POST/PUT/DELETE | `/role/**` | 角色管理 | 需认证 |
| GET/POST/PUT/DELETE | `/device/**` | 设备管理 | 需认证 |
| GET/POST/PUT/DELETE | `/inspect/plan/**` | 检修计划 | 需认证 |
| GET/POST/PUT/DELETE | `/inspect/record/**` | 检修记录 | 需认证 |
| GET/POST/PUT/DELETE | `/warn/rule/**` | 预警规则 | 需认证 |
| GET/PUT/DELETE | `/warn/info/**` | 预警处理 | 需认证 |
| GET | `/system/log/**` | 操作日志 | 需认证 |

---

## 🚀 快速启动

### 1. 数据库初始化

```bash
mysql -u root -p < backend/database/schema.sql
mysql -u root -p < backend/database/data.sql
```

### 2. 后端启动

```bash
cd backend
./mvnw spring-boot:run
# 服务启动于 http://localhost:9999
```

### 3. 前端启动

```bash
cd frontend
npm install
npm run dev
# 开发服务器启动于 http://localhost:5173
```

### 4. 测试账号

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | 123456 | 超级管理员（全部权限） |
| tech01 | 123456 | 维修技术员（设备、检修、预警权限） |
| user01 | 123456 | 普通操作员（仅查看设备） |

---

## ✅ 项目亮点

- 前后端分离架构，代码完全解耦，便于迭代维护与团队协作。
- RBAC 精细化权限管控，前端路由 + 后端 API 双重防护，适配多角色运维场景。
- JWT 无状态认证 + BCrypt 密码单向加密，双重保障系统安全。
- AOP 自动日志审计，双路兜底获取操作人（含登录操作），实现全流程可追溯。
- MyBatis-Plus 零 XML 配置，仅一条手写 SQL（三表 JOIN 权限查询），代码极简。
- 设备检修 + 故障预警双向管控，面向医疗设备运维的完整业务闭环。
