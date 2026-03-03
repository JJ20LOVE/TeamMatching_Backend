package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.dto.request.auth.WxLoginRequest;
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
public class WxLoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testWxLogin_Success() throws Exception {
        // 正确的请求数据
        WxLoginRequest request = new WxLoginRequest();
        request.setCode("test_code_123456");
        request.setEncryptedData("test_encrypted_data");
        request.setIv("test_iv");

        mockMvc.perform(post("/auth/wx-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.expiresIn").exists())
                .andExpect(jsonPath("$.authStatus").exists())
                .andExpect(jsonPath("$.isNewUser").exists());
    }

    @Test
    public void testWxLogin_EmptyCode() throws Exception {
        // code为空
        WxLoginRequest request = new WxLoginRequest();
        request.setCode("");
        request.setEncryptedData("test_encrypted_data");
        request.setIv("test_iv");

        mockMvc.perform(post("/auth/wx-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    public void testWxLogin_NoCode() throws Exception {
        // 不提供code参数
        WxLoginRequest request = new WxLoginRequest();
        request.setEncryptedData("test_encrypted_data");
        request.setIv("test_iv");

        mockMvc.perform(post("/auth/wx-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }
}