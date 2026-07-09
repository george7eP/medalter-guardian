package com.isoft.yidajava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.isoft.yidajava.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    SysUser getUserByUsername(String username);
}
