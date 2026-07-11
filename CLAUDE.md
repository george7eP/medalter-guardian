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
│   └── src/main/java/com/isoft/medalterguardian/
│       ├── annotation/Log.java       # @Log 审计注解
│       ├── aspect/LogAspect.java     # AOP 环绕切面（双路兜底获取操作人）
│       ├── common/                   # Result, LoginRequest, LoginResponse
│       ├── config/                   # SecurityConfig, JwtAuthFilter, MybatisPlusConfig, MyMetaObjectHandler
│       ├── controller/               # 9 个 REST Controller
│       ├── dao/                      # 11 个 MyBatis-Plus Mapper（含关系表）
│       ├── entity/                   # 11 个实体（RBAC 5 + 业务 5 + SysLog）
│       ├── service/                  # 11 个接口 + 12 个实现（含 UserDetailsServiceImpl）
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
3. **后端方法级鉴权** — 8 个业务 Controller 共 29 处 `@PreAuthorize` 注解（`AuthController` 走 URL 放行，无需注解）

### AOP 审计

- 25 处 `@Log` 注解覆盖全部写操作（含登录/登出）
- `LogAspect` 双路兜底：SecurityContext（已认证操作）→ LoginRequest 参数提取（登录操作）
- 记录字段：username, operation, method, params, time, ip

---

## 当前进度

| 阶段 | 状态 | 说明 |
|------|------|------|
| 项目初始化 | ✅ 完成 | Git init, GitHub push, 初始提交 |
| README 修订 | ✅ 完成 | 修正名称、版本号、新增架构/安全/API 详细文档（已合入 master） |
| 代码走读 | ✅ 完成 | 三阶段深度阅读全部关键代码 |
| 种子数据修复 | ✅ 完成 | BCrypt 哈希有效，system:log 权限已补，API URL 已修正 |
| 权限补全 | ✅ 完成 | 业务 Controller 全部添加方法级 @PreAuthorize |
| 配置安全 | ✅ 完成 | 敏感值改为 ${ENV_VAR} + dev profile 提供默认值 |
| 角色鉴权补漏 | ✅ 完成 | 复查发现 `SysRoleController` 4 端点缺失 @PreAuthorize（提权隐患），已补 `system:role` |
| 安全测试脚手架 | ✅ 基本完成 | JwtUtil + UserDetailsService 单测、9 个 Controller 方法级鉴权切片、登录流程端到端切片（68 测试全绿）；仅余 DB 集成测试待补 |
| 包名重构 | ✅ 完成 | `com.isoft.yidajava` → `com.isoft.medalterguardian` 全量安全重命名（源码/测试/pom/yaml/文档），clean build 68 绿 |

---

## 目标追踪

| ID | 目标 | 优先级 | 状态 | 依赖 |
|----|------|--------|------|------|
| T1 | 修复 `data.sql` 种子数据（BCrypt 哈希 + 缺少 system:log） | 🔴 高 | ✅ | — |
| T2 | 为业务 Controller 补全 `@PreAuthorize` 方法级鉴权 | 🟡 中 | ✅ | T1 |
| T3 | JWT secret 和 DB 密码外置到环境变量/配置文件分离 | 🟡 中 | ✅ | — |
| T4 | 补充 `system:log` 权限到种子数据 + 前后端映射 | 🔴 高 | ✅ | T1 |
| T5 | 前端路由守卫补充业务页面权限绑定（device, inspect, warn） | 🟡 中 | ✅ | T2 |
| T6 | CORS 配置收紧（生产环境限制具体 Origin） | 🟢 低 | ✅ | — |
| T7 | 补充单元测试与集成测试 | 🟢 低 | ✅ | — |
| T9 | `SysRoleController` 补全方法级 `@PreAuthorize`（复查发现的提权隐患） | 🔴 高 | ✅ | T2 |
| T8 | README.md 中版本号与实际 pom.xml / package.json / engines 持续同步 | 🟢 低 | ✅ | — |
| T10 | 前端繁体中文统一转简体（OpenCC t2s，19 文件 948 字符）+ `lang="zh-CN"` + 简体字体栈 | 🟡 中 | ✅ | — |
| T11 | 前端 UI 视觉优化/重构（清新医疗蓝绿主题、外壳/登录重构、数据仪表盘+ECharts、动效、页面头/空状态） | 🟡 中 | ✅ | T10 |

---

## 已知问题

- **DB 集成测试待补**：当前 68 测试均为纯 Mockito / `@WebMvcTest` 切片，无需数据库。仅在确定 MySQL / Testcontainers / H2 基建方案后，才有必要补 `@SpringBootTest` 集成测试。T1–T6、T8、T9 全部完成。

---

## 🛠️ 团队 Git 工作流与收尾规范

### 核心原则

**禁止本地直接合并**：不允许在本地直接执行 `git merge` 将功能分支合并到主干（`master`）。

当收到"收工 / 功能跑通 / 完成修复 / 准备提交"指令时，严格执行以下三阶段自动化闭环：

1. **第一阶段**：Push → 自动生成 PR 文档 → 提示用户创建 PR 并 Merge
2. **第二阶段**：验证云端分支状态 → 切回 `master` → `git pull` → `git branch -d`
3. **第三阶段**：Stash 审计，区分废弃草稿（drop）和主干草稿（pop）

（详细步骤已写入 Claude 全局记忆，所有项目统一适用。）

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
cd backend && ./mvnw exec:java -Dexec.mainClass="com.isoft.medalterguardian.util.PasswordGenerator"
```
