package com.pingpong;

import com.pingpong.common.ApiResponse;
import com.pingpong.controller.UserController;
import com.pingpong.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PingPongApplicationTests {

    @Autowired
    private UserService userService;

    @Test
    void testPasswordHashing() {
        String hash1 = userService.hashPassword("test123");
        String hash2 = userService.hashPassword("test123");
        assertEquals(hash1, hash2, "相同密码应产生相同哈希");
        assertNotEquals("test123", hash1, "密码不应明文存储");
    }

    @Test
    void testSmsCodeGeneration() {
        String code = userService.sendSmsCode("13800138000");
        assertNotNull(code);
        assertEquals(6, code.length(), "验证码应为6位数字");
        assertTrue(code.matches("\\d{6}"), "验证码应全为数字");
    }

    @Test
    void testSmsCodeFormat() {
        String code = userService.sendSmsCode("13900139000");
        int num = Integer.parseInt(code);
        assertTrue(num >= 0 && num < 1000000, "验证码应在0-999999范围内");
    }
}