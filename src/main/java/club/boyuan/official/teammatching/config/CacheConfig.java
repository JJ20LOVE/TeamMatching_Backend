package club.boyuan.official.teammatching.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 缓存配置：用于智能匹配、项目广场等读多写少接口的加速。
 */
@Configuration
public class CacheConfig {

    /**
     * 智能匹配结果缓存：
     * - key: userId
     * - TTL: 60s（保证足够新鲜，避免频繁打分计算）
     * - maxSize: 5000（按用户量/并发可调整）
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(60))
                .maximumSize(5000));
        return manager;
    }
}

