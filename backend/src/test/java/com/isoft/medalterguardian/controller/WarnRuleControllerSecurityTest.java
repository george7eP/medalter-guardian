package com.isoft.medalterguardian.controller;

import com.isoft.medalterguardian.config.JwtAuthenticationFilter;
import com.isoft.medalterguardian.config.SecurityConfig;
import com.isoft.medalterguardian.service.WarnRuleService;
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
 * 回归测试：WarnRuleController 的方法级鉴权。
 * 全部 4 个端点均需 'warn:rule' 权限。
 */
@WebMvcTest(WarnRuleController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://localhost:5173",
        "jwt.header=Authorization",
        "jwt.prefix=Bearer ",
        "jwt.secret=test-secret-key-that-is-long-enough-for-hs256-2026-medical-device",
        "jwt.expiration=86400000"
})
class WarnRuleControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WarnRuleService warnRuleService;

    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("未认证访问预警规则列表被拦截")
    void listRules_unauthenticated_isRejected() throws Exception {
        mockMvc.perform(get("/warn/rule"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(authorities = "warn:handle")
    @DisplayName("已认证但无 warn:rule 权限 → 403")
    void listRules_wrongAuthority_isForbidden() throws Exception {
        mockMvc.perform(get("/warn/rule"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "warn:rule")
    @DisplayName("持有 warn:rule 权限可查阅列表 → 200")
    void listRules_withAuthority_isAllowed() throws Exception {
        mockMvc.perform(get("/warn/rule"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "warn:handle")
    @DisplayName("无 warn:rule 权限无法新增规则 → 403")
    void createRule_wrongAuthority_isForbidden() throws Exception {
        mockMvc.perform(post("/warn/rule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ruleName\":\"越权规则\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "warn:rule")
    @DisplayName("持有 warn:rule 权限可新增规则 → 200")
    void createRule_withAuthority_isAllowed() throws Exception {
        when(warnRuleService.save(any())).thenReturn(true);

        mockMvc.perform(post("/warn/rule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ruleName\":\"测试规则\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "warn:handle")
    @DisplayName("无 warn:rule 权限无法编辑规则 → 403")
    void updateRule_wrongAuthority_isForbidden() throws Exception {
        mockMvc.perform(put("/warn/rule/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ruleName\":\"篡改\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "warn:rule")
    @DisplayName("持有 warn:rule 权限可编辑规则 → 200")
    void updateRule_withAuthority_isAllowed() throws Exception {
        when(warnRuleService.updateById(any())).thenReturn(true);

        mockMvc.perform(put("/warn/rule/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ruleName\":\"更新规则\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "warn:handle")
    @DisplayName("无 warn:rule 权限无法删除规则 → 403")
    void deleteRule_wrongAuthority_isForbidden() throws Exception {
        mockMvc.perform(delete("/warn/rule/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "warn:rule")
    @DisplayName("持有 warn:rule 权限可删除规则 → 200")
    void deleteRule_withAuthority_isAllowed() throws Exception {
        when(warnRuleService.removeById(1L)).thenReturn(true);

        mockMvc.perform(delete("/warn/rule/1"))
                .andExpect(status().isOk());
    }
}
