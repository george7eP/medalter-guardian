package com.isoft.medalterguardian.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isoft.medalterguardian.common.Result;
import com.isoft.medalterguardian.entity.SysUser;
import com.isoft.medalterguardian.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate; // ✨ 引入 JDBC 工具
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.isoft.medalterguardian.annotation.Log;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate; // ✨ 注入 JDBC 以操作關係表

    /**
     * 獲取指定用戶當前擁有的角色 ID 列表 (管理員專用)
     */
    @PreAuthorize("hasAuthority('system:user')")
    @GetMapping("/{id}/roles")
    public Result<List<Long>> getUserRoleIds(@PathVariable Long id) {
        List<Long> roleIds = jdbcTemplate.queryForList(
                "SELECT role_id FROM sys_user_role WHERE user_id = ?", Long.class, id);
        return Result.success(roleIds);
    }

    /**
     * 給指定用戶重新分配角色 (管理員專用 - 賦權與撤權的核心)
     */
    @PreAuthorize("hasAuthority('system:user')")
    @Log("分配用戶角色")
    @PutMapping("/{id}/roles")
    public Result<Void> assignUserRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        // 1. 先清除該用戶舊的角色綁定關係 (撤權)
        jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id = ?", id);

        // 2. 重新寫入新的角色綁定關係 (賦權)
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                jdbcTemplate.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)", id, roleId);
            }
        }
        return Result.success("角色及權限分配成功", null);
    }

    /**
     * 獲取當前登入用戶的完整個人資訊
     */
    @GetMapping("/info")
    public Result<SysUser> getCurrentUserInfo() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUsername;
        if (principal instanceof UserDetails) {
            currentUsername = ((UserDetails) principal).getUsername();
        } else {
            return Result.error("無法獲取登入狀態");
        }

        SysUser user = sysUserService.getUserByUsername(currentUsername);
        if (user != null) {
            user.setPassword(null);
            return Result.success(user);
        }
        return Result.notFound("用戶不存在");
    }

    /**
     * 修改密碼 API
     */
    @Log("修改個人密碼")
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUsername;
        if (principal instanceof UserDetails) {
            currentUsername = ((UserDetails) principal).getUsername();
        } else {
            return Result.error("無法獲取登入狀態");
        }

        SysUser user = sysUserService.getUserByUsername(currentUsername);
        if (user == null) {
            return Result.notFound("用戶不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error("原密碼輸入錯誤");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        boolean success = sysUserService.updateById(user);

        if (success) {
            return Result.success("密碼修改成功", null);
        } else {
            return Result.error("密碼修改失敗");
        }
    }

    @PreAuthorize("hasAuthority('system:user')")
    @Log("新增系統用戶")
    @PostMapping
    public Result<SysUser> createUser(@RequestBody SysUser user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        boolean success = sysUserService.save(user);
        return success ? Result.success("用户创建成功", user) : Result.error("用户创建失败");
    }

    @PreAuthorize("hasAuthority('system:user')")
    @Log("刪除系統用戶")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        boolean success = sysUserService.removeById(id);
        return success ? Result.success("用户删除成功", null) : Result.notFound("用户不存在或删除失败");
    }

    @PreAuthorize("hasAuthority('system:user')")
    @Log("修改用戶資訊")
    @PutMapping("/{id}")
    public Result<SysUser> updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        SysUser existingUser = sysUserService.getById(id);
        if (existingUser == null) return Result.notFound("用户不存在");

        user.setId(id);
        boolean success = sysUserService.updateById(user);
        return success ? Result.success("用户更新成功", user) : Result.error("用户更新失败");
    }

    @PreAuthorize("hasAuthority('system:user')")
    @Log("更新用戶狀態")
    @PatchMapping("/{id}")
    public Result<SysUser> patchUser(@PathVariable Long id, @RequestBody SysUser updates) {
        SysUser existingUser = sysUserService.getById(id);
        if (existingUser == null) return Result.notFound("用户不存在");

        if (updates.getUsername() != null) existingUser.setUsername(updates.getUsername());
        if (updates.getPassword() != null && !updates.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updates.getPassword()));
        }
        if (updates.getRealName() != null) existingUser.setRealName(updates.getRealName());
        if (updates.getPhone() != null) existingUser.setPhone(updates.getPhone());
        if (updates.getEmail() != null) existingUser.setEmail(updates.getEmail());
        if (updates.getStatus() != null) existingUser.setStatus(updates.getStatus());

        boolean success = sysUserService.updateById(existingUser);
        return success ? Result.success("用户部分更新成功", existingUser) : Result.error("用户更新失败");
    }

    @PreAuthorize("hasAuthority('system:user')")
    @GetMapping("/{id}")
    public Result<SysUser> getUserById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        return user != null ? Result.success(user) : Result.notFound("用户不存在");
    }

    @PreAuthorize("hasAuthority('system:user')")
    @GetMapping
    public Result<Page<SysUser>> getUserList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {

        Page<SysUser> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();

        if (username != null && !username.isEmpty()) queryWrapper.like(SysUser::getUsername, username);
        if (status != null) queryWrapper.eq(SysUser::getStatus, status);
        queryWrapper.orderByDesc(SysUser::getCreateTime);

        return Result.success(sysUserService.page(page, queryWrapper));
    }

    @PreAuthorize("hasAuthority('system:user')")
    @GetMapping("/all")
    public Result<List<SysUser>> getAllUsers() {
        return Result.success(sysUserService.list());
    }
}