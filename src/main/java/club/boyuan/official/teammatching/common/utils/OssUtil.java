package club.boyuan.official.teammatching.common.utils;

import club.boyuan.official.teammatching.config.OssConfig;
import com.aliyun.oss.OSS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.util.UUID.randomUUID;

/**
 * 阿里云 OSS 工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OssUtil {
    
    private final OssConfig ossConfig;
    private final OSS ossClient;
    
    /**
     * 上传文件到 OSS
     * @param file 上传的文件
     * @param objectName OSS 中的对象名称（包含路径）
     * @return 文件访问 URL
     */
    public String uploadFile(MultipartFile file, String objectName) throws IOException {
        if (!ossConfig.isEnabled()) {
            log.warn("OSS 未启用，返回空 URL");
            return "";
        }
        
        try (InputStream inputStream = file.getInputStream()) {
            // 上传文件到 OSS
            ossClient.putObject(ossConfig.getBucketName(), objectName, inputStream);
            
            // 生成访问 URL
            String fileUrl = getFileUrl(objectName);
            log.info("文件上传成功，objectName: {}, url: {}", objectName, fileUrl);
            
            return fileUrl;
        } catch (IOException e) {
            log.error("上传文件到 OSS 失败，objectName: {}", objectName, e);
            throw new IOException("上传文件失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 上传字节数组到 OSS
     * @param content 文件内容
     * @param objectName OSS 中的对象名称
     * @param contentType 内容类型
     * @return 文件访问 URL
     */
    public String uploadBytes(byte[] content, String objectName, String contentType) throws IOException {
        if (!ossConfig.isEnabled()) {
            log.warn("OSS 未启用，返回空 URL");
            return "";
        }
        
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
            // 上传字节数组到 OSS
            ossClient.putObject(ossConfig.getBucketName(), objectName, inputStream);
            
            String fileUrl = getFileUrl(objectName);
            log.info("字节数组上传成功，objectName: {}, url: {}", objectName, fileUrl);
            
            return fileUrl;
        } catch (Exception e) {
            log.error("上传字节数组到 OSS 失败，objectName: {}", objectName, e);
            throw new IOException("上传文件失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 删除 OSS 中的文件
     * @param objectName OSS 中的对象名称
     */
    public void deleteFile(String objectName) {
        if (!ossConfig.isEnabled()) {
            log.warn("OSS 未启用，跳过删除");
            return;
        }
        
        try {
            ossClient.deleteObject(ossConfig.getBucketName(), objectName);
            log.info("文件删除成功，objectName: {}", objectName);
        } catch (Exception e) {
            log.error("删除 OSS 文件失败，objectName: {}", objectName, e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 检查文件是否存在
     * @param objectName OSS 中的对象名称
     * @return 是否存在
     */
    public boolean doesObjectExist(String objectName) {
        if (!ossConfig.isEnabled()) {
            return false;
        }
        
        try {
            return ossClient.doesObjectExist(ossConfig.getBucketName(), objectName);
        } catch (Exception e) {
            log.error("检查文件存在失败，objectName: {}", objectName, e);
            return false;
        }
    }
    
    /**
     * 获取文件访问 URL
     * @param objectName OSS 中的对象名称
     * @return 文件访问 URL
     */
    private String getFileUrl(String objectName) {
        // 如果配置了自定义域名，使用自定义域名
        if (ossConfig.getCustomDomain() != null && !ossConfig.getCustomDomain().isEmpty()) {
            return "https://" + ossConfig.getCustomDomain() + "/" + objectName;
        }
        
        // 否则使用 OSS 默认域名
        String bucketName = ossConfig.getBucketName();
        String endpoint = ossConfig.getEndpoint();
        
        // 构建 URL（格式：https://{bucket}.{endpoint}/{objectName}）
        return String.format("https://%s.%s/%s", bucketName, endpoint, objectName);
    }
    
    /**
     * 生成 OSS 对象名称（包含日期路径）
     * @param fileName 原始文件名
     * @return OSS 对象名称
     */
    public String generateObjectName(String fileName) {
        // 生成日期路径：yyyy/MM/dd/
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd/"));
        
        // 生成唯一文件名（UUID）
        String uniqueFileName = randomUUID().toString().replace("-", "");
        
        // 获取文件扩展名
        String extension = "";
        if (fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf("."));
        }
        
        // 组合完整路径
        String objectName = datePath + uniqueFileName + extension;
        
        // 添加前缀
        if (ossConfig.getFilePrefix() != null && !ossConfig.getFilePrefix().isEmpty()) {
            objectName = ossConfig.getFilePrefix() + objectName;
        }
        
        return objectName;
    }
}
