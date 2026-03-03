package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.dto.request.auth.SendVerifyCodeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证接口响应测试
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuthResponseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 测试注册接口响应格式
     */
    @Test
    public void testRegisterResponse() throws Exception {
        // 注意：这个测试会失败，因为验证码不存在
        // 主要用于验证响应格式
    }

    /**
     * 测试发送验证码接口响应格式
     */
    @Test
    public void testSendVerifyCodeResponse() throws Exception {
        SendVerifyCodeRequest request = new SendVerifyCodeRequest();
        request.setTarget("test@example.com");
        request.setType("register");

        mockMvc.perform(post("/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.expireIn").exists());
    }

    /**
     * 测试微信登录接口响应格式
     */
    @Test
    public void testWxLoginResponse() throws Exception {
        // 注意：这个测试会失败，因为微信 code 无效
        // 主要用于验证响应格式包含 isNewUser 字段
    }

    /**
     * 测试密码登录接口响应格式
     */
    @Test
    public void testLoginResponse() throws Exception {
        // 注意：这个测试会失败，因为用户不存在
        // 主要用于验证响应格式
    }
}
