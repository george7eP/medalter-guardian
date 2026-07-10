package com.isoft.medalterguardian.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.isoft.medalterguardian.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    @Select("SELECT DISTINCT p.*\n" +
            "        FROM sys_permission p\n" +
            "        INNER JOIN sys_role_permission rp ON p.id = rp.perm_id\n" +
            "        INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id\n" +
            "        WHERE ur.user_id = #{userId}\n" +
            "        ORDER BY p.sort ASC")
    List<SysPermission> getPermissionsByUserId(@Param("userId") Long userId);
}
