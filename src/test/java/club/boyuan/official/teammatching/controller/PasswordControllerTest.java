package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.dto.request.auth.ChangePasswordRequest;
import club.boyuan.official.teammatching.dto.request.auth.ForgotPasswordRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 密码管理功能测试
 */
@SpringBootTest
@AutoConfigureMockMvc
public class PasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 测试找回密码 - 成功场景
     */
    @Test
    public void testForgotPassword_Success() throws Exception {
        // 准备请求数据
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setAccount("test@example.com");
        request.setVerifyCode("123456");
        request.setNewPassword("newpassword123");

        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("密码重置成功"));
    }

    /**
     * 测试找回密码 - 账号为空
     */
    @Test
    public void testForgotPassword_EmptyAccount() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setAccount("");
        request.setVerifyCode("123456");
        request.setNewPassword("newpassword123");

        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.errors.account").value("账号不能为空"));
    }

    /**
     * 测试找回密码 - 验证码格式错误
     */
    @Test
    public void testForgotPassword_InvalidVerifyCode() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setAccount("test@example.com");
        request.setVerifyCode("abc123"); // 包含字母
        request.setNewPassword("newpassword123");

        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.errors.verifyCode").value("验证码必须为 6 位数字"));
    }

    /**
     * 测试找回密码 - 密码长度不符合要求
     */
    @Test
    public void testForgotPassword_ShortPassword() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setAccount("test@example.com");
        request.setVerifyCode("123456");
        request.setNewPassword("123"); // 密码太短

        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.errors.newPassword").value("密码长度必须在 6-20 位之间"));
    }

    /**
     * 测试修改密码 - 需要登录
     */
    @Test
    public void testChangePassword_RequiresLogin() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldpassword123");
        request.setNewPassword("newpassword123");

        // 未登录状态下访问应该返回 401
        mockMvc.perform(put("/auth/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 测试修改密码 - 旧密码为空
     */
    @Test
    public void testChangePassword_EmptyOldPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("");
        request.setNewPassword("newpassword123");

        mockMvc.perform(put("/auth/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.errors.oldPassword").value("旧密码不能为空"));
    }

    /**
     * 测试修改密码 - 新密码长度不符合要求
     */
    @Test
    public void testChangePassword_ShortNewPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldpassword123");
        request.setNewPassword("123"); // 密码太短

        mockMvc.perform(put("/auth/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.errors.newPassword").value("密码长度必须在 6-20 位之间"));
    }
}
