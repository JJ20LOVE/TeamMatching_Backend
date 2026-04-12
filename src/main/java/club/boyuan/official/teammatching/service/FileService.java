package club.boyuan.official.teammatching.service;

import club.boyuan.official.teammatching.dto.response.file.FileInfoResponse;
import club.boyuan.official.teammatching.dto.response.file.FileUploadResponse;
import club.boyuan.official.teammatching.dto.response.file.MyUploadedFileItemResponse;
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
     * 分页查询当前用户已上传文件（按 targetType，与上传接口一致）
     *
     * @param userId     当前用户 ID
     * @param targetType 关联类型，必填，1-8
     * @param page       页码，默认 1
     * @param size       每页条数，默认 10
     * @return 文件列表，按上传时间倒序
     */
    List<MyUploadedFileItemResponse> listMyUploadedFiles(Integer userId, Integer targetType, Integer page, Integer size);

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