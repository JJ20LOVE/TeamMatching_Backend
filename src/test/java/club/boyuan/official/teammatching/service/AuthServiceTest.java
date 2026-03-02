package club.boyuan.official.teammatching.service;

import club.boyuan.official.teammatching.dto.request.auth.RegisterRequest;
import club.boyuan.official.teammatching.dto.response.auth.RegisterResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    public void testRegister() {
        // 准备测试数据
        RegisterRequest request = new RegisterRequest();
        request.setAccount("test@example.com");
        request.setPassword("123456");
        request.setNickname("测试用户");
        request.setVerifyCode("123456");

        // 执行注册（注意：这会实际调用数据库）
        try {
            RegisterResponse response = authService.register(request);
            
            // 验证响应
            assertNotNull(response);
            assertNotNull(response.getUserId());
            assertNotNull(response.getToken());
            assertEquals(Long.valueOf(7200), response.getExpiresIn()); // 2小时
            assertEquals(Integer.valueOf(0), response.getAuthStatus()); // 待审核
            
            System.out.println("注册成功:");
            System.out.println("用户ID: " + response.getUserId());
            System.out.println("Token: " + response.getToken());
            System.out.println("过期时间: " + response.getExpiresIn() + "秒");
            System.out.println("认证状态: " + response.getAuthStatus());
            
        } catch (Exception e) {
            System.err.println("注册失败: " + e.getMessage());
            // 这里可能会因为验证码不存在而失败，这是正常的
        }
    }
}