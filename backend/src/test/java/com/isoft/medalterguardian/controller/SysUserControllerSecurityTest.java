package com.isoft.medalterguardian.controller;

import com.isoft.medalterguardian.config.JwtAuthenticationFilter;
import com.isoft.medalterguardian.config.SecurityConfig;
import com.isoft.medalterguardian.service.SysUserService;
import com.isoft.medalterguardian.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 回归测试：SysUserController 现有方法级鉴权（'system:user'）。
 * 验证已完成的 T2 安全加固不被后续改动破坏。
 */
@WebMvcTest(SysUserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://localhost:5173",
        "jwt.header=Authorization",
        "jwt.prefix=Bearer ",
        "jwt.secret=test-secret-key-that-is-long-enough-for-hs256-2026-medical-device",
        "jwt.expiration=86400000"
})
class SysUserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SysUserService sysUserService;
    // 控制器直接注入 JdbcTemplate 操作 sys_user_role 关系表
    @MockBean
    private JdbcTemplate jdbcTemplate;

    // JwtAuthenticationFilter 的协作依赖
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("未认证访问用户列表被拦截")
    void listUsers_unauthenticated_isRejected() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(authorities = "device:list")
    @DisplayName("已认证但无 system:user 权限 → 403")
    void listUsers_withoutUserAuthority_isForbidden() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "system:user")
    @DisplayName("持有 system:user 权限可访问用户列表 → 200")
    void listUsers_withUserAuthority_isAllowed() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isOk());
    }
}
