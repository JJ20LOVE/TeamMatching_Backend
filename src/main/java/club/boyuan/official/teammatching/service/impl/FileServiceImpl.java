package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.common.constants.RedisConstants;
import club.boyuan.official.teammatching.common.utils.UserContextUtil;
import club.boyuan.official.teammatching.dto.response.file.FileInfoResponse;
import club.boyuan.official.teammatching.dto.response.file.FileUploadResponse;
import club.boyuan.official.teammatching.entity.User;
import club.boyuan.official.teammatching.entity.FileResource;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.mapper.FileResourceMapper;
import club.boyuan.official.teammatching.service.FileService;
import club.boyuan.official.teammatching.common.utils.OssUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 文件服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    
    private final FileResourceMapper fileResourceMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OssUtil ossUtil;
    
    /**
     * 允许上传的文件类型
     */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "zip", "rar"
    );
    
    /**
     * 最大文件大小 50MB
     */
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;
    
    @Override
    public FileUploadResponse uploadFile(MultipartFile file, Integer targetType, Boolean isTemp, Integer userId) {
        log.info("开始上传文件，文件名：{}, 大小：{}", file.getOriginalFilename(), file.getSize());
        
        // 1. 校验文件
        validateFile(file);
        
        // 2. 计算文件 MD5
        String md5Hash = calculateMd5(file);
        
        // 3. 检查文件是否已存在（基于 MD5 去重）
        FileResource existingFile = fileResourceMapper.selectOne(
            new LambdaQueryWrapper<FileResource>()
                .eq(FileResource::getMd5Hash, md5Hash)
                .eq(FileResource::getIsDeleted, false)
        );
        
        if (existingFile != null) {
            log.info("文件已存在，返回已有文件信息，fileId: {}", existingFile.getFileId());
            return buildUploadResponse(existingFile);
        }
        
        // 4. 生成 OSS 对象名称
        String objectName = ossUtil.generateObjectName(file.getOriginalFilename());
        
        // 5. 上传文件到 OSS
        String fileUrl;
        try {
            fileUrl = ossUtil.uploadFile(file, objectName);
        } catch (Exception e) {
            log.error("上传文件到 OSS 失败", e);
            throw new BusinessException("上传文件失败：" + e.getMessage());
        }
        
        // 6. 创建文件记录
        FileResource fileResource = new FileResource();
        fileResource.setFileName(file.getOriginalFilename());
        fileResource.setFileKey(objectName); // OSS 的 objectName
        fileResource.setFileUrl(fileUrl); // OSS 访问 URL
        fileResource.setFileSize(file.getSize());
        fileResource.setFileType(file.getContentType());
        fileResource.setFileExtension(getFileExtension(file));
        fileResource.setMd5Hash(md5Hash);
        fileResource.setUserId(userId);
        fileResource.setTargetType(targetType);
        fileResource.setTargetId(0); // 初始为 0，后续关联业务时更新
        fileResource.setIsTemp(isTemp != null ? isTemp : false);
        fileResource.setIsDeleted(false);
        fileResource.setCreatedTime(LocalDateTime.now());
        fileResource.setUpdateTime(LocalDateTime.now());
        
        // 7. 保存到数据库
        fileResourceMapper.insert(fileResource);
        
        log.info("文件上传成功，fileId: {}, url: {}", fileResource.getFileId(), fileUrl);
        
        return buildUploadResponse(fileResource);
    }
    
    @Override
    public FileInfoResponse getFileInfo(Long fileId) {
        log.info("获取文件信息，fileId: {}", fileId);
        
        // 1. 查询文件信息
        FileResource fileResource = fileResourceMapper.selectById(fileId);
        if (fileResource == null) {
            throw new BusinessException("文件不存在");
        }
        
        // 2. 检查文件是否被删除
        if (fileResource.getIsDeleted()) {
            throw new BusinessException("文件已被删除");
        }
        
        // 3. 构建响应
        return buildFileInfoResponse(fileResource);
    }
    
    @Override
    public void deleteFile(Long fileId, Integer currentUserId) {
        log.info("删除文件，fileId: {}, currentUserId: {}", fileId, currentUserId);
        
        // 1. 查询文件信息
        FileResource fileResource = fileResourceMapper.selectById(fileId);
        if (fileResource == null) {
            throw new BusinessException("文件不存在");
        }
        
        // 2. 检查文件是否已被删除
        if (fileResource.getIsDeleted()) {
            log.info("文件已被删除，fileId: {}", fileId);
            return;
        }
        
        // 3. 权限校验：检查文件归属
        checkDeletePermission(fileResource, currentUserId);
        
        // 4. 软删除标记
        fileResource.setIsDeleted(true);
        fileResource.setDeletedTime(LocalDateTime.now());
        fileResourceMapper.updateById(fileResource);
        
        // 5. 删除 OSS 中的文件（异步）
        String objectName = fileResource.getFileKey();
        if (objectName != null && !objectName.isEmpty()) {
            try {
                ossUtil.deleteFile(objectName);
                log.info("OSS 文件已删除，objectName: {}", objectName);
            } catch (Exception e) {
                log.error("删除 OSS 文件失败，objectName: {}", objectName, e);
            }
        }
        
        // 6. 如果是临时文件，可以设置定时任务清理记录
        if (fileResource.getIsTemp()) {
            schedulePhysicalDelete(fileId);
        }
        
        log.info("文件删除成功，fileId: {}", fileId);
    }
    
    /**
     * 检查删除权限
     * @param fileResource 文件资源
     * @param currentUserId 当前用户 ID
     */
    private void checkDeletePermission(FileResource fileResource, Integer currentUserId) {
        // 获取当前用户信息
        User currentUser = UserContextUtil.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 管理员拥有所有权限
        if ("admin".equals(currentUser.getRole())) {
            return;
        }
        
        // 普通用户只能删除自己的文件
        if (!fileResource.getUserId().equals(currentUserId)) {
            throw new BusinessException("无权删除他人文件");
        }
    }
    
    /**
     * 校验文件
     */
    private void validateFile(MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        
        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小超过限制（最大 50MB）");
        }
        
        // 检查文件扩展名
        String extension = getFileExtension(file);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException("不支持的文件类型：" + extension);
        }
    }
    
    /**
     * 计算文件 MD5
     */
    private String calculateMd5(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                digest.update(buffer, 0, bytesRead);
            }
            
            byte[] md5Bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : md5Bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("计算文件 MD5 失败", e);
            throw new BusinessException("计算文件 MD5 失败");
        }
    }
    
    /**
     * 生成唯一文件名
     */
    private String generateFileName(MultipartFile file) {
        String extension = getFileExtension(file);
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
        return uuid + "." + extension;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * 构建上传响应
     */
    private FileUploadResponse buildUploadResponse(FileResource fileResource) {
        FileUploadResponse response = new FileUploadResponse();
        response.setFileId(fileResource.getFileId());
        response.setFileName(fileResource.getFileName());
        response.setFileUrl(fileResource.getFileUrl());
        response.setFileSize(fileResource.getFileSize());
        response.setFileType(fileResource.getFileType());
        response.setFileExtension(fileResource.getFileExtension());
        response.setMd5Hash(fileResource.getMd5Hash());
        return response;
    }
    
    /**
     * 构建文件信息响应
     */
    private FileInfoResponse buildFileInfoResponse(FileResource fileResource) {
        FileInfoResponse response = new FileInfoResponse();
        response.setFileId(fileResource.getFileId());
        response.setFileName(fileResource.getFileName());
        response.setFileUrl(fileResource.getFileUrl());
        response.setFileSize(fileResource.getFileSize());
        response.setFileType(fileResource.getFileType());
        response.setFileExtension(fileResource.getFileExtension());
        response.setMd5Hash(fileResource.getMd5Hash());
        response.setTargetType(fileResource.getTargetType());
        response.setTargetId(fileResource.getTargetId());
        response.setIsTemp(fileResource.getIsTemp());
        response.setUserId(fileResource.getUserId());
        response.setCreatedTime(fileResource.getCreatedTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return response;
    }
    
    /**
     * 调度物理删除任务
     */
    private void schedulePhysicalDelete(Long fileId) {
        // TODO使用 Redis 延迟队列或定时任务清理临时文件
        // 这里简单实现：设置 Redis 过期键，实际项目中应该使用定时任务
        String key = "temp_file:" + fileId;  // 将 Long 转换为 String
        redisTemplate.opsForValue().set(key, fileId.toString(), 24, TimeUnit.HOURS);
        log.info("已安排临时文件物理删除，fileId: {}", fileId);
    }
}
