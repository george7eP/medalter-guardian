package com.isoft.medalterguardian.service;

import com.isoft.medalterguardian.entity.SysPermission;
import com.isoft.medalterguardian.entity.SysUser;
import com.isoft.medalterguardian.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * 纯 Mockito 单元测试：UserDetailsServiceImpl 的登录用户装载逻辑（无需 Spring / DB）。
 *
 * 覆盖四条关键分支：
 *  1. 用户不存在 → UsernameNotFoundException
 *  2. 权限正常映射为 authorities，并过滤 null/空 permCode
 *  3. 无任何权限时兜底为 ROLE_GUEST（避免 Spring Security 空权限崩溃）
 *  4. status != 1 的账号被标记为 disabled（停用提示而非密码错误）
 */
@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private SysUserService sysUserService;

    @Mock
    private SysPermissionService sysPermissionService;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private SysUser enabledUser(Long id, String username) {
        SysUser u = new SysUser();
        u.setId(id);
        u.setUsername(username);
        u.setPassword("{bcrypt}$2a$10$hash");
        u.setStatus(1);
        return u;
    }

    private SysPermission perm(String code) {
        SysPermission p = new SysPermission();
        p.setPermCode(code);
        return p;
    }

    @Test
    @DisplayName("用户不存在时抛出 UsernameNotFoundException")
    void loadUserByUsername_notFound_throws() {
        when(sysUserService.getUserByUsername("ghost")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("ghost"));
    }

    @Test
    @DisplayName("权限被映射为对应的 authorities")
    void loadUserByUsername_mapsPermissionsToAuthorities() {
        SysUser user = enabledUser(1L, "admin");
        when(sysUserService.getUserByUsername("admin")).thenReturn(user);
        when(sysPermissionService.getPermissionsByUserId(1L))
                .thenReturn(List.of(perm("device:list"), perm("system:role")));

        UserDetails details = userDetailsService.loadUserByUsername("admin");

        assertEquals("admin", details.getUsername());
        assertTrue(details.isEnabled());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("device:list")));
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("system:role")));
    }

    @Test
    @DisplayName("null / 空字符串 permCode 被过滤，不产生空 authority")
    void loadUserByUsername_filtersBlankPermCodes() {
        SysUser user = enabledUser(2L, "tech");
        when(sysUserService.getUserByUsername("tech")).thenReturn(user);
        when(sysPermissionService.getPermissionsByUserId(2L))
                .thenReturn(List.of(perm("device:list"), perm(null), perm("")));

        UserDetails details = userDetailsService.loadUserByUsername("tech");

        // 仅保留有效的 device:list，且不含空 authority
        assertEquals(1, details.getAuthorities().size());
        assertTrue(details.getAuthorities().stream()
                .allMatch(a -> a.getAuthority() != null && !a.getAuthority().isEmpty()));
    }

    @Test
    @DisplayName("无任何权限时兜底为 ROLE_GUEST")
    void loadUserByUsername_noPermissions_fallsBackToGuest() {
        SysUser user = enabledUser(3L, "tech01");
        when(sysUserService.getUserByUsername("tech01")).thenReturn(user);
        when(sysPermissionService.getPermissionsByUserId(3L)).thenReturn(List.of());

        UserDetails details = userDetailsService.loadUserByUsername("tech01");

        assertEquals(1, details.getAuthorities().size());
        assertEquals("ROLE_GUEST", details.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    @DisplayName("status != 1 的账号被标记为 disabled")
    void loadUserByUsername_disabledWhenStatusNotOne() {
        SysUser user = enabledUser(4L, "banned");
        user.setStatus(0);
        when(sysUserService.getUserByUsername("banned")).thenReturn(user);
        when(sysPermissionService.getPermissionsByUserId(4L))
                .thenReturn(List.of(perm("device:list")));

        UserDetails details = userDetailsService.loadUserByUsername("banned");

        assertFalse(details.isEnabled());
    }

    @Test
    @DisplayName("status 为 null 时同样视为 disabled")
    void loadUserByUsername_disabledWhenStatusNull() {
        SysUser user = enabledUser(5L, "legacy");
        user.setStatus(null);
        when(sysUserService.getUserByUsername("legacy")).thenReturn(user);
        when(sysPermissionService.getPermissionsByUserId(5L))
                .thenReturn(List.of(perm("device:list")));

        UserDetails details = userDetailsService.loadUserByUsername("legacy");

        assertFalse(details.isEnabled());
    }
}
