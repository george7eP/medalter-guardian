package com.isoft.yidajava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.isoft.yidajava.dao.SysPermissionMapper;
import com.isoft.yidajava.entity.SysPermission;
import com.isoft.yidajava.service.SysPermissionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {
    @Override
    public List<SysPermission> getPermissionsByUserId(Long userId) {
        return this.baseMapper.getPermissionsByUserId(userId);
    }
}
