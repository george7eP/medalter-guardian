package com.isoft.medalterguardian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.isoft.medalterguardian.entity.SysPermission;

import java.util.List;

public interface SysPermissionService extends IService<SysPermission> {
    List<SysPermission> getPermissionsByUserId(Long userId);
}
