package com.isoft.medalterguardian.controller;

import com.isoft.medalterguardian.config.JwtAuthenticationFilter;
import com.isoft.medalterguardian.config.SecurityConfig;
import com.isoft.medalterguardian.service.InspectRecordService;
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
 * 回归测试：InspectRecordController 的方法级鉴权。
 * 全部 4 个端点均需 'inspect:record' 权限。
 */
@WebMvcTest(InspectRecordController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://localhost:5173",
        "jwt.header=Authorization",
        "jwt.prefix=Bearer ",
        "jwt.secret=test-secret-key-that-is-long-enough-for-hs256-2026-medical-device",
        "jwt.expiration=86400000"
})
class InspectRecordControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InspectRecordService inspectRecordService;

    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("未认证访问检修记录列表被拦截")
    void listRecords_unauthenticated_isRejected() throws Exception {
        mockMvc.perform(get("/inspect/record"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(authorities = "device:list")
    @DisplayName("已认证但无 inspect:record 权限 → 403")
    void listRecords_wrongAuthority_isForbidden() throws Exception {
        mockMvc.perform(get("/inspect/record"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "inspect:record")
    @DisplayName("持有 inspect:record 权限可查阅列表 → 200")
    void listRecords_withAuthority_isAllowed() throws Exception {
        mockMvc.perform(get("/inspect/record"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "inspect:plan")
    @DisplayName("仅有 inspect:plan 无法新增检修记录 → 403")
    void createRecord_planOnly_isForbidden() throws Exception {
        mockMvc.perform(post("/inspect/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"remark\":\"越权记录\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "inspect:record")
    @DisplayName("持有 inspect:record 权限可新增 → 200")
    void createRecord_withAuthority_isAllowed() throws Exception {
        when(inspectRecordService.save(any())).thenReturn(true);

        mockMvc.perform(post("/inspect/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"remark\":\"测试记录\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "inspect:plan")
    @DisplayName("仅有 inspect:plan 无法编辑检修记录 → 403")
    void updateRecord_planOnly_isForbidden() throws Exception {
        mockMvc.perform(put("/inspect/record/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"remark\":\"篡改\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "inspect:record")
    @DisplayName("持有 inspect:record 权限可编辑 → 200")
    void updateRecord_withAuthority_isAllowed() throws Exception {
        when(inspectRecordService.updateById(any())).thenReturn(true);

        mockMvc.perform(put("/inspect/record/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"remark\":\"更新记录\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "device:list")
    @DisplayName("无 inspect:record 权限无法删除 → 403")
    void deleteRecord_wrongAuthority_isForbidden() throws Exception {
        mockMvc.perform(delete("/inspect/record/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "inspect:record")
    @DisplayName("持有 inspect:record 权限可删除 → 200")
    void deleteRecord_withAuthority_isAllowed() throws Exception {
        when(inspectRecordService.removeById(1L)).thenReturn(true);

        mockMvc.perform(delete("/inspect/record/1"))
                .andExpect(status().isOk());
    }
}
