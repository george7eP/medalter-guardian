package com.isoft.medalterguardian.controller;

import com.isoft.medalterguardian.config.JwtAuthenticationFilter;
import com.isoft.medalterguardian.config.SecurityConfig;
import com.isoft.medalterguardian.entity.SysUser;
import com.isoft.medalterguardian.service.SysPermissionService;
import com.isoft.medalterguardian.service.SysUserService;
import com.isoft.medalterguardian.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 登录流程端到端切片测试：请求穿过 SecurityConfig 过滤器链到达 AuthController。
 *
 * 关键安全属性：/auth/login 属于放行路径，未携带令牌也应可访问（否则用户永远无法登录）。
 * 同时验证成功签发令牌与凭证错误两条业务分支。
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://localhost:5173",
        "jwt.header=Authorization",
        "jwt.prefix=Bearer ",
        "jwt.secret=test-secret-key-that-is-long-enough-for-hs256-2026-medical-device",
        "jwt.expiration=86400000"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private SysUserService sysUserService;
    @MockBean
    private SysPermissionService sysPermissionService;

    // JwtAuthenticationFilter 的协作依赖
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("登录端点无需认证即可访问（放行路径），凭证正确时签发令牌")
    void login_validCredentials_returnsToken() throws Exception {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setRealName("系统管理员");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("admin", "pw", List.of()));
        when(sysUserService.getUserByUsername("admin")).thenReturn(user);
        when(sysPermissionService.getPermissionsByUserId(1L)).thenReturn(List.of());
        when(jwtUtil.generateToken(anyString(), any(Map.class))).thenReturn("signed-jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("signed-jwt-token"))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    @DisplayName("凭证错误时返回业务错误信息，不泄露具体原因")
    void login_badCredentials_returnsError() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("认证通过但用户不存在时返回错误")
    void login_userNotFound_returnsError() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("ghost", "pw", List.of()));
        when(sysUserService.getUserByUsername("ghost")).thenReturn(null);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ghost\",\"password\":\"x\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }
}
