package club.boyuan.official.teammatching.integration;

import club.boyuan.official.teammatching.TeamMatchingApplication;
import club.boyuan.official.teammatching.dto.request.auth.LoginRequest;
import club.boyuan.official.teammatching.dto.request.auth.RegisterRequest;
import club.boyuan.official.teammatching.dto.response.auth.RegisterResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TeamMatchingApplication.class)
@AutoConfigureMockMvc
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String validToken;

    @BeforeEach
    public void setUp() throws Exception {
        // 先注册一个测试用户
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setAccount("test@example.com");
        registerRequest.setPassword("123456");
        registerRequest.setNickname("测试用户");
        registerRequest.setVerifyCode("123456");

        // 注意：这里可能因为验证码不存在而失败，这是正常的
        // 在实际测试中应该先发送验证码
    }

    @Test
    public void testPublicEndpoint_NoAuthRequired() throws Exception {
        mockMvc.perform(get("/api/test/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("这是一个公开接口，任何人都可以访问"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    public void testLoginRequiredEndpoint_WithoutToken_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/test/login-required"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("未提供访问令牌"));
    }

    @Test
    public void testAuthRequiredEndpoint_WithoutToken_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/test/auth-required"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("未提供访问令牌"));
    }

    @Test
    public void testLoginSuccessFlow() throws Exception {
        // 1. 用户登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setAccount("test@example.com");
        loginRequest.setPassword("123456");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.expiresIn").exists())
                .andReturn();

        // 2. 提取token
        String responseContent = loginResult.getResponse().getContentAsString();
        RegisterResponse loginResponse = objectMapper.readValue(responseContent, RegisterResponse.class);
        String token = loginResponse.getToken();

        // 3. 使用token访问需要登录的接口
        mockMvc.perform(get("/api/test/login-required")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("这是一个需要登录的接口"))
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.nickname").exists());

        // 4. 使用token访问需要认证的接口（可能失败，因为用户未通过认证）
        mockMvc.perform(get("/api/test/auth-required")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden()); // 应该返回403，因为用户未通过认证
    }

    @Test
    public void testRefreshToken() throws Exception {
        // 先登录获取token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setAccount("test@example.com");
        loginRequest.setPassword("123456");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        RegisterResponse loginResponse = objectMapper.readValue(responseContent, RegisterResponse.class);
        String oldToken = loginResponse.getToken();

        // 刷新token
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh-token")
                .header("Authorization", "Bearer " + oldToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.expiresIn").exists())
                .andReturn();

        String refreshResponseContent = refreshResult.getResponse().getContentAsString();
        RegisterResponse refreshResponse = objectMapper.readValue(refreshResponseContent, RegisterResponse.class);
        String newToken = refreshResponse.getToken();

        // 验证新旧token不同
        assert !oldToken.equals(newToken);

        // 使用新token访问受保护接口
        mockMvc.perform(get("/api/test/login-required")
                .header("Authorization", "Bearer " + newToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testLogout() throws Exception {
        // 先登录获取token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setAccount("test@example.com");
        loginRequest.setPassword("123456");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        RegisterResponse loginResponse = objectMapper.readValue(responseContent, RegisterResponse.class);
        String token = loginResponse.getToken();

        // 登出
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 使用已登出的token访问接口应该失败
        mockMvc.perform(get("/api/test/login-required")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testInvalidToken() throws Exception {
        // 使用无效token访问受保护接口
        mockMvc.perform(get("/api/test/login-required")
                .header("Authorization", "Bearer invalid_token_123"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("无效的访问令牌"));
    }
}