package club.boyuan.official.teammatching.service;

import club.boyuan.official.teammatching.dto.response.file.FileInfoResponse;
import club.boyuan.official.teammatching.dto.response.file.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件服务接口
 */
public interface FileService {
    
    /**
     * 上传文件
     * @param file 上传的文件
     * @param targetType 关联类型
     * @param isTemp 是否临时文件
     * @param userId 上传用户 ID
     * @return 文件上传响应
     */
    FileUploadResponse uploadFile(MultipartFile file, Integer targetType, Boolean isTemp, Integer userId);
    
    /**
     * 获取文件信息
     * @param fileId 文件ID
     * @return 文件信息
     */
    FileInfoResponse getFileInfo(Long fileId);
    
    /**
     * 删除文件（软删除）
     * @param fileId 文件ID
     * @param currentUserId 当前登录用户 ID
     */
    void deleteFile(Long fileId, Integer currentUserId);
}