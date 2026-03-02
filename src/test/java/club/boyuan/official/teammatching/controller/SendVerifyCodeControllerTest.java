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

@SpringBootTest
@AutoConfigureMockMvc
public class SendVerifyCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testSendVerifyCode_Email_Success() throws Exception {
        SendVerifyCodeRequest request = new SendVerifyCodeRequest();
        request.setTarget("test@example.com");
        request.setType("register");

        mockMvc.perform(post("/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void testSendVerifyCode_Phone_Success() throws Exception {
        SendVerifyCodeRequest request = new SendVerifyCodeRequest();
        request.setTarget("13800138000");
        request.setType("register");

        mockMvc.perform(post("/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void testSendVerifyCode_InvalidEmailFormat() throws Exception {
        SendVerifyCodeRequest request = new SendVerifyCodeRequest();
        request.setTarget("invalid-email");
        request.setType("register");

        mockMvc.perform(post("/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    public void testSendVerifyCode_InvalidPhoneFormat() throws Exception {
        SendVerifyCodeRequest request = new SendVerifyCodeRequest();
        request.setTarget("12345");
        request.setType("register");

        mockMvc.perform(post("/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    public void testSendVerifyCode_InvalidType() throws Exception {
        SendVerifyCodeRequest request = new SendVerifyCodeRequest();
        request.setTarget("test@example.com");
        request.setType("invalid_type");

        mockMvc.perform(post("/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    public void testSendVerifyCode_EmptyTarget() throws Exception {
        SendVerifyCodeRequest request = new SendVerifyCodeRequest();
        request.setTarget("");
        request.setType("register");

        mockMvc.perform(post("/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.errors.target").value("目标不能为空"));
    }
}