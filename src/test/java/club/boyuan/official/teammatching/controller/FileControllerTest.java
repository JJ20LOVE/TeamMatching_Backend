package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.dto.response.file.FileInfoResponse;
import club.boyuan.official.teammatching.dto.response.file.FileUploadResponse;
import club.boyuan.official.teammatching.entity.SkillTag;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 文件控制器测试
 */
@SpringBootTest
@AutoConfigureMockMvc
public class FileControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 测试获取技能标签列表
     */
    @Test
    public void testGetSkillTags() throws Exception {
        mockMvc.perform(get("/common/skill-tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    /**
     * 测试获取文件信息（需要先有文件）
     */
    @Test
    public void testGetFileInfo() throws Exception {
        // 这个测试需要数据库中有文件记录
        // 假设 fileId=1 存在
        mockMvc.perform(get("/common/file/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    
    /**
     * 测试文件上传（需要登录）
     */
    @Test
    public void testUploadFile() throws Exception {
        // 创建模拟文件
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "This is a test file".getBytes()
        );
        
        // 注意：这个测试会失败，因为没有提供登录 token
        mockMvc.perform(multipart("/common/upload/file")
                .file(file)
                .param("targetType", "1")
                .param("isTemp", "false")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }
    
    /**
     * 测试删除文件（需要登录）
     */
    @Test
    public void testDeleteFile() throws Exception {
        // 注意：这个测试会失败，因为没有提供登录 token
        mockMvc.perform(delete("/common/file/1"))
                .andExpect(status().isUnauthorized());
    }
}
