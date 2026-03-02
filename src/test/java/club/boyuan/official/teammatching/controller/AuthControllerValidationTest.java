package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.dto.request.auth.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegisterValidation_Success() throws Exception {
        // 正确的请求数据
        RegisterRequest request = new RegisterRequest();
        request.setAccount("test@example.com");
        request.setPassword("123456");
        request.setNickname("测试用户");
        request.setVerifyCode("123456");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    public void testRegisterValidation_EmptyAccount() throws Exception {
        // 账号为空
        RegisterRequest request = new RegisterRequest();
        request.setAccount("");
        request.setPassword("123456");
        request.setVerifyCode("123456");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.errors.account").value("账号不能为空"));
    }

    @Test
    public void testRegisterValidation_ShortPassword() throws Exception {
        // 密码太短
        RegisterRequest request = new RegisterRequest();
        request.setAccount("test@example.com");
        request.setPassword("123");
        request.setVerifyCode("123456");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.errors.password").value("密码长度必须在6-20位之间"));
    }

    @Test
    public void testRegisterValidation_InvalidVerifyCode() throws Exception {
        // 验证码格式错误
        RegisterRequest request = new RegisterRequest();
        request.setAccount("test@example.com");
        request.setPassword("123456");
        request.setVerifyCode("abc123"); // 包含字母

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.errors.verifyCode").value("验证码必须为6位数字"));
    }

    @Test
    public void testRegisterValidation_MultipleErrors() throws Exception {
        // 多个字段错误
        RegisterRequest request = new RegisterRequest();
        request.setAccount("");
        request.setPassword("123");
        request.setVerifyCode("123");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.errors.account").value("账号不能为空"))
                .andExpect(jsonPath("$.errors.password").value("密码长度必须在6-20位之间"))
                .andExpect(jsonPath("$.errors.verifyCode").value("验证码必须为6位数字"));
    }
}