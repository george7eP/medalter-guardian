package com.isoft.medalterguardian.controller;

import com.isoft.medalterguardian.config.JwtAuthenticationFilter;
import com.isoft.medalterguardian.config.SecurityConfig;
import com.isoft.medalterguardian.service.WarnInfoService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 回归测试：WarnInfoController 的方法级鉴权。
 * 全部 3 个端点（列表 / 处理 / 删除）均需 'warn:handle' 权限。
 */
@WebMvcTest(WarnInfoController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://localhost:5173",
        "jwt.header=Authorization",
        "jwt.prefix=Bearer ",
        "jwt.secret=test-secret-key-that-is-long-enough-for-hs256-2026-medical-device",
        "jwt.expiration=86400000"
})
class WarnInfoControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WarnInfoService warnInfoService;

    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("未认证访问预警列表被拦截")
    void listWarns_unauthenticated_isRejected() throws Exception {
        mockMvc.perform(get("/warn/info"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(authorities = "warn:rule")
    @DisplayName("已认证但无 warn:handle 权限 → 403")
    void listWarns_wrongAuthority_isForbidden() throws Exception {
        mockMvc.perform(get("/warn/info"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "warn:handle")
    @DisplayName("持有 warn:handle 权限可查阅列表 → 200")
    void listWarns_withAuthority_isAllowed() throws Exception {
        mockMvc.perform(get("/warn/info"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "warn:rule")
    @DisplayName("无 warn:handle 权限无法处理预警 → 403")
    void handleWarn_wrongAuthority_isForbidden() throws Exception {
        mockMvc.perform(put("/warn/info/handle/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"handleStatus\":\"HANDLED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "warn:handle")
    @DisplayName("持有 warn:handle 权限可处理预警 → 200")
    void handleWarn_withAuthority_isAllowed() throws Exception {
        when(warnInfoService.getById(1L)).thenReturn(new com.isoft.medalterguardian.entity.WarnInfo());
        when(warnInfoService.updateById(any())).thenReturn(true);

        mockMvc.perform(put("/warn/info/handle/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"handleStatus\":\"HANDLED\",\"handleUser\":\"admin\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "warn:rule")
    @DisplayName("无 warn:handle 权限无法删除预警 → 403")
    void deleteWarn_wrongAuthority_isForbidden() throws Exception {
        mockMvc.perform(delete("/warn/info/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "warn:handle")
    @DisplayName("持有 warn:handle 权限可删除预警 → 200")
    void deleteWarn_withAuthority_isAllowed() throws Exception {
        when(warnInfoService.removeById(1L)).thenReturn(true);

        mockMvc.perform(delete("/warn/info/1"))
                .andExpect(status().isOk());
    }
}
