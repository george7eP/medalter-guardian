package com.isoft.medalterguardian.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.isoft.medalterguardian.dao.SysPermissionMapper;
import com.isoft.medalterguardian.entity.SysPermission;
import com.isoft.medalterguardian.service.SysPermissionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {
    @Override
    public List<SysPermission> getPermissionsByUserId(Long userId) {
        return this.baseMapper.getPermissionsByUserId(userId);
    }
}
