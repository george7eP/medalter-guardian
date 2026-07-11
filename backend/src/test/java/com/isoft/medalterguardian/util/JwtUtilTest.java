package com.isoft.medalterguardian.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JwtUtil 纯单元测试（无需 Spring 上下文 / 数据库）。
 * 通过反射注入 @Value 字段，验证签发→解析往返、篡改/过期/垃圾令牌一律拒绝。
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // HS256 要求密钥 >= 256bit（32 字节），此处使用测试专用长密钥
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "test-secret-key-that-is-long-enough-for-hs256-2026-medical-device");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    @DisplayName("签发的令牌可解析回原用户名且校验通过")
    void generateAndParse_roundTrip() {
        String token = jwtUtil.generateToken("admin");

        assertEquals("admin", jwtUtil.getUsernameFromToken(token));
        assertTrue(jwtUtil.validateToken(token));
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("自定义 claims 不影响 subject 解析")
    void generateWithClaims_preservesSubject() {
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("userId", 1L);
        claims.put("realName", "系统管理员");

        String token = jwtUtil.generateToken("admin", claims);

        assertEquals("admin", jwtUtil.getUsernameFromToken(token));
    }

    @Test
    @DisplayName("非法/垃圾字符串不被当作有效令牌")
    void validateToken_rejectsGarbage() {
        assertFalse(jwtUtil.validateToken("not-a-jwt-token"));
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    @DisplayName("签名被篡改的令牌校验失败")
    void validateToken_rejectsTampered() {
        String token = jwtUtil.generateToken("admin");
        // 篡改签名段（第三段）的首字符，而非末字符：
        // HS256 的 32 字节签名编码为 43 个 Base64url 字符，末字符仅含 4 位有效位（2 位未用低位），
        // 翻转末字符可能落到解码结果相同的等价字符，令牌仍有效 → 偶发假绿。
        // 首字符承载签名首字节的高 6 位，全部有效，改成任一不同合法字符必然改变签名。
        int sigStart = token.lastIndexOf('.') + 1;
        char first = token.charAt(sigStart);
        char replacement = (first == 'A') ? 'B' : 'A';
        String tampered = token.substring(0, sigStart) + replacement + token.substring(sigStart + 1);

        assertFalse(jwtUtil.validateToken(tampered));
    }

    @Test
    @DisplayName("已过期令牌校验失败")
    void validateToken_rejectsExpired() {
        // 负过期时间 → 签发即过期
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String expired = jwtUtil.generateToken("admin");

        assertFalse(jwtUtil.validateToken(expired));
    }
}
