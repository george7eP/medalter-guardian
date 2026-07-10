package com.isoft.medalterguardian.controller;

import com.isoft.medalterguardian.config.JwtAuthenticationFilter;
import com.isoft.medalterguardian.config.SecurityConfig;
import com.isoft.medalterguardian.service.SysLogService;
import com.isoft.medalterguardian.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 回归测试：SysLogController 的方法级鉴权。
 * 审计日志查询端点需 'system:log' 权限（避免普通用户读取全量操作日志）。
 */
@WebMvcTest(SysLogController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://localhost:5173",
        "jwt.header=Authorization",
        "jwt.prefix=Bearer ",
        "jwt.secret=test-secret-key-that-is-long-enough-for-hs256-2026-medical-device",
        "jwt.expiration=86400000"
})
class SysLogControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SysLogService sysLogService;

    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("未认证访问系统日志被拦截")
    void listLogs_unauthenticated_isRejected() throws Exception {
        mockMvc.perform(get("/system/log"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(authorities = "device:list")
    @DisplayName("已认证但无 system:log 权限 → 403")
    void listLogs_wrongAuthority_isForbidden() throws Exception {
        mockMvc.perform(get("/system/log"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "system:log")
    @DisplayName("持有 system:log 权限可查阅日志 → 200")
    void listLogs_withAuthority_isAllowed() throws Exception {
        mockMvc.perform(get("/system/log"))
                .andExpect(status().isOk());
    }
}
