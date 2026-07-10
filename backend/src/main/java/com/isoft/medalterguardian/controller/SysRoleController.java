package com.isoft.medalterguardian.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isoft.medalterguardian.common.Result;
import com.isoft.medalterguardian.entity.SysRole;
import com.isoft.medalterguardian.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.isoft.medalterguardian.annotation.Log;

@RestController
@RequestMapping("/role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    /**
     * 獲取角色分頁列表 (精確對齊前端傳入的 page, pageSize, roleName 欄位)
     */
    @PreAuthorize("hasAuthority('system:role')")
    @GetMapping
    public Result<Page<SysRole>> getRoleList(
            @RequestParam(value = "page", defaultValue = "1") int current,
            @RequestParam(value = "pageSize", defaultValue = "10") int size,
            @RequestParam(required = false) String roleName) {

        Page<SysRole> page = new Page<>(current, size);
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();

        if (roleName != null && !roleName.isEmpty()) {
            queryWrapper.like(SysRole::getRoleName, roleName);
        }

        queryWrapper.orderByAsc(SysRole::getId);
        Page<SysRole> rolePage = sysRoleService.page(page, queryWrapper);
        return Result.success(rolePage);
    }

    /**
     * 創建新角色
     */
    @PreAuthorize("hasAuthority('system:role')")
    @Log("新增角色")
    @PostMapping
    public Result<SysRole> createRole(@RequestBody SysRole role) {
        boolean success = sysRoleService.save(role);
        if (success) {
            return Result.success("角色創建成功", role);
        } else {
            return Result.error("角色創建失敗");
        }
    }

    /**
     * 更新角色資訊
     */
    @PreAuthorize("hasAuthority('system:role')")
    @Log("修改角色")
    @PutMapping("/{id}")
    public Result<SysRole> updateRole(@PathVariable Long id, @RequestBody SysRole role) {
        SysRole existingRole = sysRoleService.getById(id);
        if (existingRole == null) {
            return Result.notFound("角色不存在");
        }

        role.setId(id);
        boolean success = sysRoleService.updateById(role);
        if (success) {
            return Result.success("角色更新成功", role);
        } else {
            return Result.error("角色更新失敗");
        }
    }

    /**
     * 刪除角色
     */
    @PreAuthorize("hasAuthority('system:role')")
    @Log("刪除角色")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        boolean success = sysRoleService.removeById(id);
        if (success) {
            return Result.success("角色刪除成功", null);
        } else {
            return Result.notFound("角色不存在或刪除失敗");
        }
    }
}