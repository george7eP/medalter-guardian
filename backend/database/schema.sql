-- ----------------------------
-- 1. 用户表
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  username VARCHAR(50) NOT NULL COMMENT '登录账号',
  password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
  real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ----------------------------
-- 2. 角色表
-- ----------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
  role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ----------------------------
-- 3. 权限表
-- ----------------------------
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  perm_code VARCHAR(100) NOT NULL COMMENT '权限编码',
  perm_name VARCHAR(100) NOT NULL COMMENT '权限名称',
  perm_type VARCHAR(20) DEFAULT 'MENU' COMMENT '权限类型 MENU-菜单 BUTTON-按钮 API-接口',
  parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
  path VARCHAR(255) DEFAULT NULL COMMENT '路由路径',
  api_url VARCHAR(255) DEFAULT NULL COMMENT 'API接口地址',
  sort INT DEFAULT 0 COMMENT '排序号',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_perm_code (perm_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ----------------------------
-- 4. 用户-角色关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ----------------------------
-- 5. 角色-权限关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  perm_id BIGINT NOT NULL COMMENT '权限ID',
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_perm (role_id, perm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ----------------------------
-- 以下是原有业务表（保留不变）
-- ----------------------------
DROP TABLE IF EXISTS device_info;
CREATE TABLE device_info (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
  device_model VARCHAR(50) DEFAULT NULL COMMENT '设备型号',
  manufacturer VARCHAR(100) DEFAULT NULL COMMENT '生产厂家',
  purchase_date DATE DEFAULT NULL COMMENT '购买日期',
  use_department VARCHAR(100) NOT NULL COMMENT '使用科室',
  inspect_cycle INT NOT NULL DEFAULT 90 COMMENT '检修周期(天)',
  warn_days INT NOT NULL DEFAULT 7 COMMENT '提前预警天数',
  last_inspect_date DATE DEFAULT NULL COMMENT '上次检修日期',
  device_status VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '设备状态 NORMAL-正常 WARN-预警 FAULT-故障',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗设备信息表';

DROP TABLE IF EXISTS inspect_plan;
CREATE TABLE inspect_plan (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '计划ID',
  device_id BIGINT NOT NULL COMMENT '设备ID',
  plan_date DATE NOT NULL COMMENT '计划检修日期',
  inspect_content VARCHAR(500) DEFAULT NULL COMMENT '检修内容',
  principal VARCHAR(50) DEFAULT NULL COMMENT '负责人',
  plan_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '计划状态 PENDING-待执行 COMPLETED-已完成',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备检修计划表';

DROP TABLE IF EXISTS inspect_record;
CREATE TABLE inspect_record (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  device_id BIGINT NOT NULL COMMENT '设备ID',
  inspect_date DATE NOT NULL COMMENT '检修日期',
  inspect_content VARCHAR(500) NOT NULL COMMENT '检修内容',
  inspect_result VARCHAR(20) NOT NULL COMMENT '检修结果 PASS-合格 PARTIAL-部分合格 FAIL-不合格',
  report_file VARCHAR(255) DEFAULT NULL COMMENT '报告路径',
  operator VARCHAR(50) NOT NULL COMMENT '操作人',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备检修记录表';

DROP TABLE IF EXISTS warn_rule;
CREATE TABLE warn_rule (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  device_id BIGINT DEFAULT NULL COMMENT '设备ID(全局为NULL)',
  warn_condition VARCHAR(200) NOT NULL COMMENT '预警条件',
  warn_level VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' COMMENT '预警级别 LOW-低 MEDIUM-中 HIGH-高 URGENT-紧急',
  notify_type VARCHAR(100) DEFAULT NULL COMMENT '通知方式',
  rule_status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警规则表';

DROP TABLE IF EXISTS warn_info;
CREATE TABLE warn_info (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '预警ID',
  device_id BIGINT NOT NULL COMMENT '设备ID',
  warn_level VARCHAR(20) NOT NULL COMMENT '预警级别',
  warn_content VARCHAR(500) NOT NULL COMMENT '预警内容',
  warn_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预警时间',
  handle_status VARCHAR(20) NOT NULL DEFAULT 'UNHANDLED' COMMENT '处理状态 UNHANDLED-未处理 PROCESSED-已处理 IGNORED-已忽略',
  handle_user VARCHAR(50) DEFAULT NULL COMMENT '处理人',
  handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
  handle_remark VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警信息表';