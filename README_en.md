# 🏥 Medical Device Maintenance and Early Warning System

<p align="center">
  <a href="./README.md">中文</a> | <b>English</b>
</p>

---

## 📖 Project Introduction

This project is a digital management platform purpose-built for medical equipment operation and maintenance (O&M) scenarios in healthcare institutions. Addressing common pain points — cumbersome paper records, inefficient offline scheduling, opaque equipment statuses, and high rates of unexpected breakdowns — the platform leverages a fully decoupled front-end and back-end architecture to deliver core capabilities including digital equipment ledger management, automated maintenance workflows, intelligent fault early warnings, and full-process operational audit logging.

Built around three security pillars — RBAC fine-grained authorization, JWT stateless authentication, and AOP-driven automatic auditing — the system provides a standardized, process-driven, and fully traceable solution for medical equipment O&M.

---

## 🎯 Requirements Analysis

### 1. Business Pain Points

- **Opaque Status**: Equipment status information is fragmented, making real-time monitoring of equipment health and maintenance conditions difficult.
- **Low Scheduling Efficiency**: Manual scheduling is prone to omissions with high overdue risk and no automated reminders.
- **Poor Traceability**: Paper-based records are hard to retain; manual auditing is labor-intensive and lacks reliable traceability.

### 2. Core Functional Objectives

- **Fine-grained Authorization**: Implements a three-tier RBAC model (User → Role → Permission) with dual protection at both the frontend routing and backend API levels.
- **Full-business Closed-loop**: Covers equipment onboarding, ledger management, maintenance plan generation, execution recording, and fault warning processing end to end.
- **Full-process Auditing**: Leverages AOP aspect technology to automatically capture all core operational logs — operator identity, IP address, latency, and parameters — for complete traceability.

### 3. Non-functional Requirements

- **Data Security**: User passwords stored with BCrypt one-way hashing.
- **API Security**: Stateless authentication via JWT (JJWT 0.12.x).
- **System Performance**: MyBatis-Plus pagination plugin + MySQL 8.0 ensures responsive queries under high-frequency O&M workloads.

---

## 💻 Tech Stack

### Backend

| Component | Version | Notes |
|-----------|---------|-------|
| Java | **21** | Core language |
| Spring Boot | **3.2.6** | Application framework |
| Spring Security | 3.2.6 (bundled) | Authentication & authorization |
| MyBatis-Plus | **3.5.6** | ORM (spring-boot3-starter) |
| JJWT | **0.12.6** | JWT tokens (api / impl / jackson modules) |
| MySQL | **8.0** | Relational database |
| Maven | 3.x | Build tool (Alibaba Cloud mirror) |
| Lombok | **1.18.40** | Boilerplate reduction |

### Frontend

| Component | Version | Notes |
|-----------|---------|-------|
| Node.js | **^20.19.0 \|\| >=22.12.0** | Runtime |
| Vue | **3.5** (Composition API) | Core framework |
| Vite | **8.0** | Build tool |
| TypeScript | **6.0** | Type safety |
| vue-router | **5.0** | Client-side routing |
| Pinia | **3.0** | State management |
| Axios | **1.16** | HTTP client |
| Element Plus | **2.14** | UI component library |

### Development Tools

- Backend: IntelliJ IDEA
- Frontend: VS Code
- Target OS: Windows 11 / macOS

---

## 🛠️ Architecture Design

### System Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Browser / Nginx                    │
├────────────────────┬────────────────────────────────┤
│   Frontend :5173   │      Backend :9999              │
│   Vue 3 + Vite     │      Spring Boot 3              │
│                    │                                 │
│   axios ──REST─────▶  Controllers (10)               │
│   Bearer Token     │      │                          │
│                    │   Services (9 interfaces/impls) │
│   Route Guards      │      │                          │
│   Dynamic Menu      │   Mappers (10, zero XML)        │
│                    │      │                          │
│                    │   MySQL 8.0 (medical_device)    │
│                    │                                 │
│                    │   Security Layer:                │
│                    │   JwtAuthFilter → SecurityConfig │
│                    │   @Log AOP → SysLog Audit        │
└────────────────────┴────────────────────────────────┘
```

### Backend Package Structure

```
src/main/java/com/isoft/medalterguardian/
├── annotation/     # @Log custom audit annotation
├── aspect/         # LogAspect around-advice aspect
├── common/         # Result unified response, LoginRequest/Response DTOs
├── config/         # SecurityConfig, JwtAuthFilter, MybatisPlusConfig, MyMetaObjectHandler
├── controller/     # REST controllers (10)
├── dao/            # MyBatis-Plus mappers (10, zero XML)
├── entity/         # Database entity mappings (11: RBAC 5 + business 5 + SysLog)
├── service/        # Business interfaces (9)
├── service/impl/   # Business implementations (10, including UserDetailsServiceImpl)
└── util/           # JwtUtil, PasswordGenerator
```

### Frontend Directory Structure

```
frontend/src/
├── api/                  # API layer (by module, aligned with backend controllers)
│   ├── device/index.ts   # Device CRUD APIs
│   ├── inspect/          # plan.ts + record.ts
│   ├── system/           # log.ts + role.ts
│   ├── user/index.ts     # Login + user CRUD + role assignment
│   └── warn/             # handle.ts + rule.ts
├── components/layout/    # top.vue (header) + leftside.vue (dynamic menu)
├── router/index.ts       # Route definitions + beforeEach permission guard
├── stores/users.ts       # Pinia user store (token / permissions / userInfo)
├── util/request.ts       # Axios wrapper (JWT interceptor + unified error handling)
└── views/                # Page views (by business module)
    ├── device/index.vue
    ├── inspect/{plan,record}.vue
    ├── system/{log,role}.vue
    ├── user/{login,profile,userlist}.vue
    ├── warn/{handle,rule}.vue
    └── main.vue
```

---

## 📁 Database Design (9 Tables)

### RBAC Authorization (5 Tables)

| Table | Description | Key Columns |
|-------|-------------|-------------|
| `sys_user` | System users | username, password(BCrypt), status(0/1) |
| `sys_role` | Roles | role_code, role_name |
| `sys_permission` | Permissions | perm_code, perm_type(MENU/BUTTON/API), path, api_url |
| `sys_user_role` | User-role mapping | user_id ⟷ role_id (many-to-many) |
| `sys_role_permission` | Role-permission mapping | role_id ⟷ perm_id (many-to-many) |

### Business Domain (4 Tables)

| Table | Description | Key Columns |
|-------|-------------|-------------|
| `device_info` | Medical device ledger | device_status(NORMAL/WARN/FAULT), inspect_cycle, warn_days |
| `inspect_plan` | Maintenance plans | plan_status(PENDING/COMPLETED), plan_date, principal |
| `inspect_record` | Maintenance records | inspect_result(PASS/PARTIAL/FAIL), operator |
| `warn_rule` | Warning rules | warn_condition, warn_level, rule_status |
| `warn_info` | Warning notifications | handle_status(UNHANDLED/PROCESSED/IGNORED), handle_user |

### Audit (1 Table)

| Table | Description | Key Columns |
|-------|-------------|-------------|
| `sys_log` | AOP operational audit log | username, operation, method, params, time, ip |

---

## 🔐 Security Design

### Three-Layer Protection

```
Layer 1 ─ Frontend Route Guards
  router.beforeEach → checks to.meta.permission
  leftside.vue → filterMenuByPermission() dynamic menu filtering
  userStore.hasPermission() → client-side permission check

Layer 2 ─ Backend URL-Level Authentication
  SecurityConfig:
    ├─ /auth/** public (login/logout)
    ├─ OPTIONS /** public (CORS preflight)
    └─ All others → JwtAuthFilter token validation → SecurityContext

Layer 3 ─ Backend Method-Level Authorization
  @PreAuthorize("hasAuthority('system:user')") → User management
  @PreAuthorize("hasAuthority('system:role')") → Role management
```

### JWT Authentication Flow

1. User enters credentials in `login.vue` → `POST /auth/login`
2. `AuthController` → `AuthenticationManager.authenticate()` → `UserDetailsServiceImpl.loadUserByUsername()`
3. `SysPermissionMapper.getPermissionsByUserId()` performs a 3-table JOIN to fetch all user permissions
4. `JwtUtil.generateToken()` creates a token (with userId and realName claims, 24h expiry)
5. Frontend `userStore.login()` stores token + permissions in Pinia + localStorage
6. All subsequent requests automatically attach `Authorization: Bearer <token>` via `request.ts` interceptor
7. `JwtAuthenticationFilter` parses the token and sets the `SecurityContext`

### Permission Query (the only hand-written SQL in the project)

```sql
SELECT DISTINCT p.*
FROM sys_permission p
INNER JOIN sys_role_permission rp ON p.id = rp.perm_id
INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id
WHERE ur.user_id = #{userId}
ORDER BY p.sort ASC
```

---

## 📊 AOP Audit Mechanism

### Implementation

- Custom `@Log("operation description")` annotation placed on Controller methods
- `LogAspect` around-advice (`@Around`) intercepts all `@Log`-annotated methods
- Dual-fallback operator resolution: SecurityContext (authenticated ops) → LoginRequest parameter extraction (login ops)

### Recorded Fields

| Field | Source |
|-------|--------|
| username | SecurityContext / LoginRequest fallback |
| operation | `@Log` annotation value |
| method | `ClassName.methodName()` |
| params | `Arrays.toString(arguments)` |
| time | Execution time (ms) |
| ip | `request.getRemoteAddr()` |

### Audited Operations (19 @Log annotations)

Login/logout, user CRUD, password changes, role assignments, device CRUD, maintenance plans/records, warning rules/handling — covering all write operations.

---

## 🔗 API Endpoints

| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/auth/login` | User login | Public |
| POST | `/auth/logout` | User logout | Authenticated |
| GET/POST/PUT/DELETE | `/user/**` | User management | `system:user` |
| PUT | `/user/password` | Change password | Authenticated |
| GET | `/user/info` | Current user info | Authenticated |
| GET/POST/PUT/DELETE | `/role/**` | Role management | Authenticated |
| GET/POST/PUT/DELETE | `/device/**` | Device management | Authenticated |
| GET/POST/PUT/DELETE | `/inspect/plan/**` | Maintenance plans | Authenticated |
| GET/POST/PUT/DELETE | `/inspect/record/**` | Maintenance records | Authenticated |
| GET/POST/PUT/DELETE | `/warn/rule/**` | Warning rules | Authenticated |
| GET/PUT/DELETE | `/warn/info/**` | Warning handling | Authenticated |
| GET | `/system/log/**` | Operation logs | Authenticated |

---

## 🚀 Quick Start

### 1. Database Initialization

```bash
mysql -u root -p < backend/database/schema.sql
mysql -u root -p < backend/database/data.sql
```

### 2. Backend

```bash
cd backend
./mvnw spring-boot:run
# Server starts at http://localhost:9999
```

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
# Dev server starts at http://localhost:5173
```

### 4. Test Accounts

| Username | Password | Role |
|----------|----------|------|
| admin | 123456 | Super Admin (all permissions) |
| tech01 | 123456 | Technician (device, inspection, warning) |
| user01 | 123456 | Operator (device view only) |

---

## ✅ Project Highlights

- Fully decoupled frontend-backend architecture enabling independent iteration and team collaboration.
- RBAC fine-grained authorization with dual frontend route + backend API protection, adaptable to multi-role O&M scenarios.
- Dual security: JWT stateless authentication + BCrypt one-way password encryption.
- AOP-powered automatic audit logging with dual-fallback operator resolution (including login operations), delivering end-to-end traceability.
- MyBatis-Plus with zero XML configuration — only a single hand-written SQL for the 3-table permission JOIN.
- Complete closed-loop medical device O&M covering both planned maintenance and fault early warning.
