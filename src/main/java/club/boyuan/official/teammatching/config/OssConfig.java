package club.boyuan.official.teammatching.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssConfig {
    
    /**
     * OSS  endpoint（如：oss-cn-hangzhou.aliyuncs.com）
     */
    private String endpoint;
    
    /**
     * 访问密钥 ID
     */
    private String accessKeyId;
    
    /**
     * 访问密钥秘密
     */
    private String accessKeySecret;
    
    /**
     * Bucket 名称
     */
    private String bucketName;
    
    /**
     * 自定义域名（可选，用于 CDN 加速）
     */
    private String customDomain;
    
    /**
     * 文件前缀路径（如：team-matching/）
     */
    private String filePrefix;
    
    /**
     * 是否启用 OSS
     */
    private boolean enabled = true;
    
    /**
     * 创建 OSS 客户端 Bean
     */
    @Bean
    public OSS ossClient() {
        if (!enabled) {
            return null;
        }
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
}
