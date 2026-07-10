# 🏥 醫療設備檢修與預警系統

<p align="center">
  <b>中文</b> | <a href="./README_en.md">English</a>
</p>

---

## 📖 專案簡介

本專案是一個面向醫療機構設備運維場景的數位化管理平台。針對醫療設備運維中紙質記錄繁瑣、排程效率低、設備狀態不透明、故障突發率高等痛點，依托前後端分離架構，實現醫療設備台賬數位化管理、檢修工作流自動化、設備故障智能預警、全流程操作日誌審計等核心能力。

系統通過 RBAC 精細化權限管控、JWT 無狀態認證、AOP 自動審計三條安全主線，為醫療設備運維提供標準化、流程化、可追溯的完整解決方案。

---

## 🎯 需求分析

### 1. 業務痛點

- **狀態不透明**：醫療設備狀態資訊分散，無法即時掌握設備運行與檢修狀態。
- **排程效率低**：人工排程易遺漏，檢修逾期風險高，缺乏自動化提醒機制。
- **留存追溯差**：紙質記錄難以留存，操作審計工作量大、追溯性差。

### 2. 核心功能目標

- **精細化權限體系**：基於 RBAC（用戶 → 角色 → 權限）三級管控模型，支援前端路由與後端 API 雙重細粒度權限控制。
- **全業務閉環管理**：覆蓋設備入庫、台賬維護、檢修計劃制定、檢修執行記錄、故障預警處理全流程。
- **全流程安全審計**：基於 AOP 切面技術，自動記錄所有核心業務操作日誌，操作人、IP、耗時、參數全程可追溯。

### 3. 非功能需求

- **資料安全**：用戶密碼採用 BCrypt 單向加密存儲。
- **介面安全**：基於 JWT（JJWT 0.12.x）實現無狀態身份認證。
- **系統性能**：MyBatis-Plus 分頁插件 + MySQL 8.0，保障高頻運維操作場景下的查詢效能。

---

## 💻 技術棧

### 後端

| 組件 | 版本 | 說明 |
|------|------|------|
| Java | **21** | 核心語言 |
| Spring Boot | **3.2.6** | 基礎框架 |
| Spring Security | 3.2.6（內置） | 認證與授權 |
| MyBatis-Plus | **3.5.6** | ORM（spring-boot3-starter） |
| JJWT | **0.12.6** | JWT 令牌（api / impl / jackson 三模組） |
| MySQL | **8.0** | 關聯式資料庫 |
| Maven | 3.x | 構建工具（阿里雲鏡像加速） |
| Lombok | **1.18.40** | 代碼簡化 |

### 前端

| 組件 | 版本 | 說明 |
|------|------|------|
| Node.js | **^20.19.0 \|\| >=22.12.0** | 執行環境 |
| Vue | **3.5**（Composition API） | 核心框架 |
| Vite | **8.0** | 構建工具 |
| TypeScript | **6.0** | 型別安全 |
| vue-router | **5.0** | 前端路由 |
| Pinia | **3.0** | 狀態管理 |
| Axios | **1.16** | HTTP 請求 |
| Element Plus | **2.14** | UI 組件庫 |

### 開發工具

- 後端開發：IntelliJ IDEA
- 前端開發：VS Code
- 適配系統：Windows 11 / macOS

---

## 🛠️ 專案架構設計

### 整體架構

```
┌─────────────────────────────────────────────────────┐
│                   瀏覽器 / Nginx                      │
├────────────────────┬────────────────────────────────┤
│   Frontend :5173   │      Backend :9999              │
│   Vue 3 + Vite     │      Spring Boot 3              │
│                    │                                 │
│   axios ──REST─────▶  Controller（10 個）              │
│   Bearer Token     │      │                          │
│                    │   Service（9 對介面/實現）         │
│   路由守衛           │      │                          │
│   菜單動態過濾       │   Mapper（10 個 MyBatis-Plus）    │
│                    │      │                          │
│                    │   MySQL 8.0（medical_device）    │
│                    │                                 │
│                    │   安全層：                        │
│                    │   JwtAuthFilter → SecurityConfig │
│                    │   @Log AOP → SysLog 審計          │
└────────────────────┴────────────────────────────────┘
```

### 後端分層結構

```
src/main/java/com/isoft/medalterguardian/
├── annotation/     # @Log 自定義審計註解
├── aspect/         # LogAspect AOP 環繞切面
├── common/         # Result 統一回應、LoginRequest/Response
├── config/         # SecurityConfig、JwtAuthFilter、MybatisPlusConfig、MyMetaObjectHandler
├── controller/     # REST 控制器（10 個）
├── dao/            # MyBatis-Plus Mapper（10 個，零 XML 配置）
├── entity/         # 資料庫實體映射（11 個，含 RBAC 五表 + 業務五表 + SysLog）
├── service/        # 業務介面（9 個）
├── service/impl/   # 業務實現（10 個，含 UserDetailsServiceImpl）
└── util/           # JwtUtil、PasswordGenerator
```

### 前端目錄結構

```
frontend/src/
├── api/                  # API 層（按模組分目錄，對齊後端 Controller）
│   ├── device/index.ts   # 設備 CRUD API
│   ├── inspect/          # plan.ts + record.ts
│   ├── system/           # log.ts + role.ts
│   ├── user/index.ts     # 登入 + 用戶 CRUD + 角色分配
│   └── warn/             # handle.ts + rule.ts
├── components/layout/    # top.vue（頂欄）+ leftside.vue（動態菜單）
├── router/index.ts       # 路由定義 + beforeEach 權限守衛
├── stores/users.ts       # Pinia 用戶狀態（token / permissions / userInfo）
├── util/request.ts       # Axios 封裝（JWT 攔截器 + 統一錯誤處理）
└── views/                # 頁面視圖（按業務模組分目錄）
    ├── device/index.vue
    ├── inspect/{plan,record}.vue
    ├── system/{log,role}.vue
    ├── user/{login,profile,userlist}.vue
    ├── warn/{handle,rule}.vue
    └── main.vue
```

---

## 📁 資料庫設計（9 張表）

### RBAC 權限體系（5 張表）

| 表名 | 說明 | 關鍵欄位 |
|------|------|---------|
| `sys_user` | 系統用戶 | username, password(BCrypt), status(0/1) |
| `sys_role` | 角色 | role_code, role_name |
| `sys_permission` | 權限 | perm_code, perm_type(MENU/BUTTON/API), path, api_url |
| `sys_user_role` | 用戶-角色關聯 | user_id ⟷ role_id（多對多） |
| `sys_role_permission` | 角色-權限關聯 | role_id ⟷ perm_id（多對多） |

### 業務體系（4 張表）

| 表名 | 說明 | 關鍵欄位 |
|------|------|---------|
| `device_info` | 醫療設備台賬 | device_status(NORMAL/WARN/FAULT), inspect_cycle, warn_days |
| `inspect_plan` | 檢修計劃 | plan_status(PENDING/COMPLETED), plan_date, principal |
| `inspect_record` | 檢修記錄 | inspect_result(PASS/PARTIAL/FAIL), operator |
| `warn_rule` | 預警規則 | warn_condition, warn_level, rule_status |
| `warn_info` | 預警資訊 | handle_status(UNHANDLED/PROCESSED/IGNORED), handle_user |

### 審計（1 張表）

| 表名 | 說明 | 關鍵欄位 |
|------|------|---------|
| `sys_log` | AOP 操作審計日誌 | username, operation, method, params, time, ip |

---

## 🔐 安全設計詳解

### 三層防護體系

```
第一層 ─ 前端路由守衛
  router.beforeEach → 檢查 to.meta.permission
  leftside.vue → filterMenuByPermission() 動態過濾菜單
  userStore.hasPermission() → 前端權限攔截

第二層 ─ 後端 URL 級認證
  SecurityConfig:
    ├─ /auth/** 放行（登入登出）
    ├─ OPTIONS /** 放行（CORS 預檢）
    └─ 其餘請求 → JwtAuthFilter 驗證 Token → SecurityContext

第三層 ─ 後端方法級鑑權
  @PreAuthorize("hasAuthority('system:user')") → 用戶管理
  @PreAuthorize("hasAuthority('system:role')") → 角色管理
```

### JWT 認證流程

1. 用戶在 `login.vue` 輸入帳密 → `POST /auth/login`
2. `AuthController` → `AuthenticationManager.authenticate()` → `UserDetailsServiceImpl.loadUserByUsername()`
3. 通過 `SysPermissionMapper.getPermissionsByUserId()` 三表 JOIN 查詢用戶所有權限
4. `JwtUtil.generateToken()` 生成 Token（含 userId、realName，24h 有效期）
5. 前端 `userStore.login()` 存入 Pinia + localStorage
6. 後續所有請求由 `request.ts` 攔截器自動附加 `Authorization: Bearer <token>`
7. `JwtAuthenticationFilter` 解析 Token 並設置 `SecurityContext`

### 權限查詢 SQL（唯一手寫 SQL）

```sql
SELECT DISTINCT p.*
FROM sys_permission p
INNER JOIN sys_role_permission rp ON p.id = rp.perm_id
INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id
WHERE ur.user_id = #{userId}
ORDER BY p.sort ASC
```

---

## 📊 AOP 審計機制

### 實現方式

- 自定義 `@Log("操作描述")` 註解 → 打在 Controller 方法上
- `LogAspect` 環繞切面（`@Around`）攔截所有 `@Log` 標記的方法
- 雙路兜底獲取操作人：SecurityContext（已認證操作）→ LoginRequest 參數提取（登入操作）

### 記錄欄位

| 欄位 | 來源 |
|------|------|
| username | SecurityContext / LoginRequest 兜底 |
| operation | `@Log` 註解的 value |
| method | `類名.方法名()` |
| params | `Arrays.toString(參數)` |
| time | 執行耗時（ms） |
| ip | `request.getRemoteAddr()` |

### 已審計操作（19 處 @Log）

登入登出、用戶 CRUD、密碼修改、角色分配、設備 CRUD、檢修計劃/記錄、預警規則/處理 — 覆蓋全部寫操作。

---

## 🔗 API 路由一覽

| 方法 | 路徑 | 說明 | 權限 |
|------|------|------|------|
| POST | `/auth/login` | 用戶登入 | 無（放行） |
| POST | `/auth/logout` | 用戶登出 | 需認證 |
| GET/POST/PUT/DELETE | `/user/**` | 用戶管理 | `system:user` |
| PUT | `/user/password` | 修改密碼 | 需認證 |
| GET | `/user/info` | 當前用戶資訊 | 需認證 |
| GET/POST/PUT/DELETE | `/role/**` | 角色管理 | 需認證 |
| GET/POST/PUT/DELETE | `/device/**` | 設備管理 | 需認證 |
| GET/POST/PUT/DELETE | `/inspect/plan/**` | 檢修計劃 | 需認證 |
| GET/POST/PUT/DELETE | `/inspect/record/**` | 檢修記錄 | 需認證 |
| GET/POST/PUT/DELETE | `/warn/rule/**` | 預警規則 | 需認證 |
| GET/PUT/DELETE | `/warn/info/**` | 預警處理 | 需認證 |
| GET | `/system/log/**` | 操作日誌 | 需認證 |

---

## 🚀 快速啟動

### 1. 資料庫初始化

```bash
mysql -u root -p < backend/database/schema.sql
mysql -u root -p < backend/database/data.sql
```

### 2. 後端啟動

```bash
cd backend
./mvnw spring-boot:run
# 服務啟動於 http://localhost:9999
```

### 3. 前端啟動

```bash
cd frontend
npm install
npm run dev
# 開發伺服器啟動於 http://localhost:5173
```

### 4. 測試帳號

| 帳號 | 密碼 | 角色 |
|------|------|------|
| admin | 123456 | 超級管理員（全部權限） |
| tech01 | 123456 | 維修技術員（設備、檢修、預警權限） |
| user01 | 123456 | 普通操作員（僅查看設備） |

---

## ✅ 專案亮點

- 前後端分離架構，代碼完全解耦，便於迭代維護與團隊協作。
- RBAC 精細化權限管控，前端路由 + 後端 API 雙重防護，適配多角色運維場景。
- JWT 無狀態認證 + BCrypt 密碼單向加密，雙重保障系統安全。
- AOP 自動日誌審計，雙路兜底獲取操作人（含登入操作），實現全流程可追溯。
- MyBatis-Plus 零 XML 配置，僅一條手寫 SQL（三表 JOIN 權限查詢），代碼極簡。
- 設備檢修 + 故障預警雙向管控，面向醫療設備運維的完整業務閉環。
