package com.isoft.medalterguardian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.isoft.medalterguardian.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    SysUser getUserByUsername(String username);
}
