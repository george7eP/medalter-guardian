package com.isoft.medalterguardian.controller;

import com.isoft.medalterguardian.config.JwtAuthenticationFilter;
import com.isoft.medalterguardian.config.SecurityConfig;
import com.isoft.medalterguardian.service.InspectPlanService;
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
 * 回归测试：InspectPlanController 的方法级鉴权。
 * 全部 4 个端点均需 'inspect:plan' 权限。
 */
@WebMvcTest(InspectPlanController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://localhost:5173",
        "jwt.header=Authorization",
        "jwt.prefix=Bearer ",
        "jwt.secret=test-secret-key-that-is-long-enough-for-hs256-2026-medical-device",
        "jwt.expiration=86400000"
})
class InspectPlanControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InspectPlanService inspectPlanService;

    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("未认证访问检修计划列表被拦截")
    void listPlans_unauthenticated_isRejected() throws Exception {
        mockMvc.perform(get("/inspect/plan"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(authorities = "device:list")
    @DisplayName("已认证但无 inspect:plan 权限 → 403")
    void listPlans_wrongAuthority_isForbidden() throws Exception {
        mockMvc.perform(get("/inspect/plan"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "inspect:plan")
    @DisplayName("持有 inspect:plan 权限可查阅列表 → 200")
    void listPlans_withAuthority_isAllowed() throws Exception {
        mockMvc.perform(get("/inspect/plan"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "device:list")
    @DisplayName("无 inspect:plan 权限无法新增 → 403")
    void createPlan_wrongAuthority_isForbidden() throws Exception {
        mockMvc.perform(post("/inspect/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planName\":\"越权计划\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "inspect:plan")
    @DisplayName("持有 inspect:plan 权限可新增 → 200")
    void createPlan_withAuthority_isAllowed() throws Exception {
        when(inspectPlanService.save(any())).thenReturn(true);

        mockMvc.perform(post("/inspect/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planName\":\"测试计划\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "device:list")
    @DisplayName("无 inspect:plan 权限无法编辑 → 403")
    void updatePlan_wrongAuthority_isForbidden() throws Exception {
        mockMvc.perform(put("/inspect/plan/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planName\":\"篡改\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "inspect:plan")
    @DisplayName("持有 inspect:plan 权限可编辑 → 200")
    void updatePlan_withAuthority_isAllowed() throws Exception {
        when(inspectPlanService.updateById(any())).thenReturn(true);

        mockMvc.perform(put("/inspect/plan/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planName\":\"更新计划\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "device:list")
    @DisplayName("无 inspect:plan 权限无法删除 → 403")
    void deletePlan_wrongAuthority_isForbidden() throws Exception {
        mockMvc.perform(delete("/inspect/plan/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "inspect:plan")
    @DisplayName("持有 inspect:plan 权限可删除 → 200")
    void deletePlan_withAuthority_isAllowed() throws Exception {
        when(inspectPlanService.removeById(1L)).thenReturn(true);

        mockMvc.perform(delete("/inspect/plan/1"))
                .andExpect(status().isOk());
    }
}
