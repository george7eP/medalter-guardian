-- ----------------------------
-- 权限系统测试数据（可直接登录）
-- ----------------------------
-- 密码都是 123456 加密后
INSERT INTO sys_user (username, password, real_name, phone, email, status) VALUES
('admin','$2a$10$XxWn2Vh2GkQ1LzTn3Hs1eOa3Vh2GkQ1LzTn3Hs1eOa3Vh2GkQ1Lz','系统管理员','13800138000','admin@hospital.com',1),
('tech01','$2a$10$XxWn2Vh2GkQ1LzTn3Hs1eOa3Vh2GkQ1LzTn3Hs1eOa3Vh2GkQ1Lz','维修技术员','13700137000','tech@hospital.com',1),
('user01','$2a$10$XxWn2Vh2GkQ1LzTn3Hs1eOa3Vh2GkQ1LzTn3Hs1eOa3Vh2GkQ1Lz','普通操作员','13900139000','user@hospital.com',1);

INSERT INTO sys_role (role_code, role_name, remark) VALUES
('ROLE_ADMIN','超级管理员','拥有全部权限'),
('ROLE_TECH','维修技术员','设备检修、预警处理'),
('ROLE_USER','普通操作员','仅查看数据');

INSERT INTO sys_permission (perm_code, perm_name, perm_type, parent_id, path, api_url, sort) VALUES
('device:list','设备列表查询','API',0,NULL,'/device/list',1),
('device:add','设备新增','API',0,NULL,'/device/add',2),
('device:edit','设备修改','API',0,NULL,'/device/edit',3),
('device:delete','设备删除','API',0,NULL,'/device/delete',4),
('inspect:plan','检修计划管理','API',0,NULL,'/inspect/plan/**',5),
('inspect:record','检修记录管理','API',0,NULL,'/inspect/record/**',6),
('warn:rule','预警规则配置','API',0,NULL,'/warn/rule/**',7),
('warn:handle','预警信息处理','API',0,NULL,'/warn/handle/**',8),
('system:user','用户管理','API',0,NULL,'/system/user/**',9),
('system:role','角色管理','API',0,NULL,'/system/role/**',10);

-- 用户-角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1,1), -- admin → 管理员
(2,2), -- tech01 → 技术员
(3,3);-- user01 → 普通用户

-- 角色-权限
INSERT INTO sys_role_permission (role_id, perm_id) VALUES
-- 管理员全部权限
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),
-- 技术员权限
(2,1),(2,5),(2,6),(2,8),
-- 普通用户仅查看
(3,1);

-- ----------------------------
-- 原有业务测试数据
-- ----------------------------
INSERT INTO device_info (device_name, device_model, manufacturer, purchase_date, use_department, inspect_cycle, warn_days, last_inspect_date, device_status, remark) VALUES
('CT扫描仪','CT-2000','西门子','2023-01-15','放射科',90,7,'2024-02-01','NORMAL','CT检查设备'),
('核磁共振仪','MRI-3000','通用电气','2022-06-20','放射科',180,15,'2024-01-10','WARN','磁共振设备'),
('心电图机','ECG-8000','迈瑞','2023-05-08','心内科',30,3,'2024-04-01','NORMAL','心电设备'),
('呼吸机','VENT-500','德尔格','2023-03-12','ICU',15,2,'2024-04-25','NORMAL','呼吸支持'),
('除颤仪','DEFIB-100','飞利浦','2023-07-22','急诊科',30,5,'2024-04-10','FAULT','急救设备');

INSERT INTO inspect_plan (device_id, plan_date, inspect_content, principal, plan_status, remark) VALUES
(1,'2024-05-02','CT性能检测','李四','PENDING','季度检修'),
(2,'2024-05-10','MRI校准','李四','PENDING','重点检查'),
(3,'2024-05-04','心电图精度测试','张三','COMPLETED','月度检查');

INSERT INTO inspect_record (device_id, inspect_date, inspect_content, inspect_result, report_file, operator) VALUES
(1,'2024-02-01','CT全面检测','PASS','/files/ct.pdf','admin'),
(2,'2024-01-10','MRI校准','PASS','/files/mri.pdf','admin');

INSERT INTO warn_rule (device_id, warn_condition, warn_level, notify_type, rule_status, remark) VALUES
(NULL,'days <= warn_days','MEDIUM','SYSTEM,EMAIL',1,'全局规则'),
(2,'days <= 15','HIGH','SYSTEM,SMS',1,'MRI专用规则');

INSERT INTO warn_info (device_id, warn_level, warn_content, warn_time, handle_status) VALUES
(2,'HIGH','MRI即将到期检修','2024-04-25 10:30:00','UNHANDLED'),
(5,'URGENT','除颤仪故障','2024-04-20 08:15:00','UNHANDLED');