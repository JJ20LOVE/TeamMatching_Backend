package club.boyuan.official.teammatching.config;

import club.boyuan.official.teammatching.common.constants.RedisConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 配置：序列化 + 缓存管理器（含各缓存区域独立 TTL）
 */
@Configuration
public class RedisConfig {

    private GenericJackson2JsonRedisSerializer buildJsonSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    /**
     * RedisTemplate：Key 使用 String 序列化，Value 使用 JSON 序列化
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key 序列化：String
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());

        // Value 序列化：JSON（含 @class 类型信息，支持反序列化，支持 Java 8 日期时间）
        GenericJackson2JsonRedisSerializer jsonSerializer = buildJsonSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Spring Cache 缓存管理器（主缓存管理器，使用 Redis）
     * <p>
     * 按缓存区域配置不同 TTL：
     * <pre>
     *   projectMatch       → 60s   智能匹配
     *   projectStats       → 120s  项目统计
     *   projectList        → 300s  项目列表
     *   communityList      → 300s  社区列表
     *   communityComments  → 300s  社区评论
     *   talentList         → 300s  人才列表
     *   projectDetail      → 600s  项目详情
     *   communityDetail    → 600s  社区详情
     *   talentDetail       → 600s  人才详情
     *   userProfile        → 600s  用户资料
     *   userSkillCerts     → 600s  技能认证
     *   userNotificationSettings → 600s  通知设置
     * </pre>
     */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认配置：Key 使用 String 序列化，Value 使用 JSON 序列化，禁止缓存 null，默认 TTL 5分钟
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(buildJsonSerializer()))
                .disableCachingNullValues()
                .entryTtl(Duration.ofSeconds(RedisConstants.CACHE_TTL_DEFAULT));

        // 各缓存区域独立 TTL 配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // === 60s ===
        cacheConfigurations.put("projectMatch", defaultConfig.entryTtl(Duration.ofSeconds(RedisConstants.CACHE_TTL_MATCH)));

        // === 120s ===
        cacheConfigurations.put("projectStats", defaultConfig.entryTtl(Duration.ofSeconds(RedisConstants.CACHE_TTL_STATS)));

        // === 300s (5min) — 列表类缓存 ===
        for (String name : new String[]{"projectList", "communityList", "communityComments", "talentList"}) {
            cacheConfigurations.put(name, defaultConfig.entryTtl(Duration.ofSeconds(RedisConstants.CACHE_TTL_LIST)));
        }

        // === 600s (10min) — 详情类缓存 ===
        for (String name : new String[]{"projectDetail", "communityDetail", "talentDetail",
                "userProfile", "userSkillCerts", "userNotificationSettings"}) {
            cacheConfigurations.put(name, defaultConfig.entryTtl(Duration.ofSeconds(RedisConstants.CACHE_TTL_DETAIL)));
        }

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}