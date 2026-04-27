package club.boyuan.official.teammatching.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

/**
 * 缓存配置：用于智能匹配、项目广场等读多写少接口的加速；
 * 以及发送方用户信息短时缓存，减少发消息时的查库。
 */
@Configuration
public class CacheConfig {

    /**
     * 智能匹配结果缓存（本地缓存，作为 Redis 缓存的一级缓存补充）：
     * - key: userId
     * - TTL: 60s（保证足够新鲜，避免频繁打分计算）
     * - maxSize: 5000（按用户量/并发可调整）
     * 注意：RedisConfig 中的 RedisCacheManager 为 @Primary 主缓存管理器，
     * 此 Caffeine 缓存仅用于特定场景（如高频本地访问）。
     */
    @Bean("caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCacheNames(java.util.List.of("projectMatch"));
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(60))
                .maximumSize(5000));
        return manager;
    }

    /**
     * 发送方用户信息缓存（用于聊天推送昵称/头像）：
     * - key: userId (Integer)
     * - value: User 实体
     * - TTL: 2 分钟，避免每次发消息都查库
     * - maxSize: 10000
     */
    @Bean("userCacheManager")
    public CacheManager userCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCacheNames(java.util.List.of("user"));
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(2))
                .maximumSize(10_000));
        return manager;
    }
}

