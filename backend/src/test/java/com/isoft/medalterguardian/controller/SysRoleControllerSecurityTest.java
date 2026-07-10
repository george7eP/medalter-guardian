package com.isoft.medalterguardian.controller;

import com.isoft.medalterguardian.config.JwtAuthenticationFilter;
import com.isoft.medalterguardian.config.SecurityConfig;
import com.isoft.medalterguardian.service.SysRoleService;
import com.isoft.medalterguardian.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 回归测试：SysRoleController 的方法级鉴权。
 * 该控制器此前缺失 @PreAuthorize，任何登录用户均可增删角色（提权漏洞）。
 * 本测试锁定修复：/role 全部端点需 'system:role' 权限。
 *
 * 采用 @WebMvcTest 切片 + spring-security-test，无需数据库即可验证授权链。
 */
@WebMvcTest(SysRoleController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://localhost:5173",
        "jwt.header=Authorization",
        "jwt.prefix=Bearer ",
        "jwt.secret=test-secret-key-that-is-long-enough-for-hs256-2026-medical-device",
        "jwt.expiration=86400000"
})
class SysRoleControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SysRoleService sysRoleService;

    // JwtAuthenticationFilter 的协作依赖：@WebMvcTest 不做组件扫描，需显式补齐
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("未认证访问角色列表被拦截")
    void listRoles_unauthenticated_isRejected() throws Exception {
        mockMvc.perform(get("/role"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(authorities = "device:list")
    @DisplayName("已认证但无 system:role 权限 → 403")
    void listRoles_withoutRoleAuthority_isForbidden() throws Exception {
        mockMvc.perform(get("/role"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "device:list")
    @DisplayName("普通用户尝试新增角色 → 403（提权被阻止）")
    void createRole_withoutRoleAuthority_isForbidden() throws Exception {
        mockMvc.perform(post("/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleCode\":\"ROLE_HACK\",\"roleName\":\"越权角色\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "system:role")
    @DisplayName("持有 system:role 权限可新增角色 → 200")
    void createRole_withRoleAuthority_isAllowed() throws Exception {
        when(sysRoleService.save(any())).thenReturn(true);

        mockMvc.perform(post("/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleCode\":\"ROLE_X\",\"roleName\":\"测试角色\"}"))
                .andExpect(status().isOk());
    }
}
