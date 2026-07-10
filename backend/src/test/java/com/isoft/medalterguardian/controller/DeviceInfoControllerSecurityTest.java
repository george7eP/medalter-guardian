package com.isoft.medalterguardian.controller;

import com.isoft.medalterguardian.config.JwtAuthenticationFilter;
import com.isoft.medalterguardian.config.SecurityConfig;
import com.isoft.medalterguardian.service.DeviceInfoService;
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
 * 回归测试：DeviceInfoController 的方法级鉴权。
 * 4 个端点分别需要 device:list / device:add / device:edit / device:delete 权限。
 * 采用 @WebMvcTest 切片 + spring-security-test，无需数据库即可验证授权链。
 */
@WebMvcTest(DeviceInfoController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://localhost:5173",
        "jwt.header=Authorization",
        "jwt.prefix=Bearer ",
        "jwt.secret=test-secret-key-that-is-long-enough-for-hs256-2026-medical-device",
        "jwt.expiration=86400000"
})
class DeviceInfoControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeviceInfoService deviceInfoService;

    // JwtAuthenticationFilter 的协作依赖：@WebMvcTest 不做组件扫描，需显式补齐
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserDetailsService userDetailsService;

    // ── GET /device 需 device:list ──

    @Test
    @DisplayName("未认证访问设备列表被拦截")
    void listDevices_unauthenticated_isRejected() throws Exception {
        mockMvc.perform(get("/device"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(authorities = "warn:rule")
    @DisplayName("已认证但无 device:list 权限 → 403")
    void listDevices_wrongAuthority_isForbidden() throws Exception {
        mockMvc.perform(get("/device"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "device:list")
    @DisplayName("持有 device:list 权限可查阅列表 → 200")
    void listDevices_withAuthority_isAllowed() throws Exception {
        mockMvc.perform(get("/device"))
                .andExpect(status().isOk());
    }

    // ── POST /device 需 device:add ──

    @Test
    @WithMockUser(authorities = "device:list")
    @DisplayName("仅有 device:list 无法新增设备 → 403")
    void createDevice_listOnly_isForbidden() throws Exception {
        mockMvc.perform(post("/device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deviceName\":\"越权设备\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "device:add")
    @DisplayName("持有 device:add 权限可新增设备 → 200")
    void createDevice_withAuthority_isAllowed() throws Exception {
        when(deviceInfoService.save(any())).thenReturn(true);

        mockMvc.perform(post("/device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deviceName\":\"测试设备\"}"))
                .andExpect(status().isOk());
    }

    // ── PUT /device/{id} 需 device:edit ──

    @Test
    @WithMockUser(authorities = "device:list")
    @DisplayName("仅有 device:list 无法编辑设备 → 403")
    void updateDevice_listOnly_isForbidden() throws Exception {
        mockMvc.perform(put("/device/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deviceName\":\"篡改\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "device:edit")
    @DisplayName("持有 device:edit 权限可编辑设备 → 200")
    void updateDevice_withAuthority_isAllowed() throws Exception {
        when(deviceInfoService.getById(1L)).thenReturn(new com.isoft.medalterguardian.entity.DeviceInfo());
        when(deviceInfoService.updateById(any())).thenReturn(true);

        mockMvc.perform(put("/device/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deviceName\":\"更新设备\"}"))
                .andExpect(status().isOk());
    }

    // ── DELETE /device/{id} 需 device:delete ──

    @Test
    @WithMockUser(authorities = "device:list")
    @DisplayName("仅有 device:list 无法删除设备 → 403")
    void deleteDevice_listOnly_isForbidden() throws Exception {
        mockMvc.perform(delete("/device/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "device:delete")
    @DisplayName("持有 device:delete 权限可删除设备 → 200")
    void deleteDevice_withAuthority_isAllowed() throws Exception {
        when(deviceInfoService.removeById(1L)).thenReturn(true);

        mockMvc.perform(delete("/device/1"))
                .andExpect(status().isOk());
    }
}
