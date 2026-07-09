package com.isoft.yidajava.controller;

import com.isoft.yidajava.common.LoginRequest;
import com.isoft.yidajava.common.LoginResponse;
import com.isoft.yidajava.common.Result;
import com.isoft.yidajava.entity.SysPermission;
import com.isoft.yidajava.entity.SysUser;
import com.isoft.yidajava.service.SysPermissionService;
import com.isoft.yidajava.service.SysUserService;
import com.isoft.yidajava.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.isoft.yidajava.annotation.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysPermissionService sysPermissionService;

    @Autowired
    private JwtUtil jwtUtil;

    @Log("用戶登入系統")
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SysUser sysUser = sysUserService.getUserByUsername(loginRequest.getUsername());
            if (sysUser == null) {
                return Result.error("用户不存在");
            }

            List<SysPermission> permissions = sysPermissionService.getPermissionsByUserId(sysUser.getId());

            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", sysUser.getId());
            claims.put("realName", sysUser.getRealName());

            String token = jwtUtil.generateToken(loginRequest.getUsername(), claims);

            LoginResponse loginResponse = LoginResponse.builder()
                    .token(token)
                    .username(sysUser.getUsername())
                    .realName(sysUser.getRealName())
                    .permissions(permissions)
                    .build();

            return Result.success("登录成功", loginResponse);

        } catch (BadCredentialsException e) {
            return Result.error("用户名或密码错误");
        } catch (Exception e) {
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    @Log("用戶登出系統")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // ✨ 關鍵修復：手動清除 SecurityContext
        SecurityContextHolder.clearContext();
        return Result.success("退出成功", null);
    }
}
