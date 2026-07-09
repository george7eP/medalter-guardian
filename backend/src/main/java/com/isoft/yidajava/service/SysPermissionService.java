package com.isoft.yidajava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.isoft.yidajava.entity.SysPermission;

import java.util.List;

public interface SysPermissionService extends IService<SysPermission> {
    List<SysPermission> getPermissionsByUserId(Long userId);
}
