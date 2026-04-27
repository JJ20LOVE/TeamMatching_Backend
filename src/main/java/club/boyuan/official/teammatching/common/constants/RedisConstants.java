package club.boyuan.official.teammatching.common.constants;

/**
 * Redis key常量
 */
public class RedisConstants {
    
    /**
     * 验证码前缀
     */
    public static final String VERIFY_CODE_PREFIX = "verify_code:";
    
    /**
     * 用户注册验证码key格式: verify_code:{account}
     */
    public static final String REGISTER_VERIFY_CODE_KEY = VERIFY_CODE_PREFIX + "%s";
    
    /**
     * 验证码有效期(分钟)
     */
    public static final long VERIFY_CODE_EXPIRE_TIME = 5;
    
    /**
     * JWT令牌前缀
     */
    public static final String JWT_TOKEN_PREFIX = "jwt_token:";
    
    /**
     * 用户JWT令牌key格式: jwt_token:{userId}
     */
    public static final String USER_JWT_TOKEN_KEY = JWT_TOKEN_PREFIX + "%s";
    
    /**
     * JWT令牌有效期(小时)
     */
    public static final long JWT_TOKEN_EXPIRE_TIME = 2;

    /**
     * 临时文件延迟队列（ZSET：score=执行时间戳ms，member=fileId）
     */
    public static final String TEMP_FILE_DELAY_QUEUE_KEY = "temp_file:delay_queue";

    /**
     * 临时文件自动清理延迟（小时）
     */
    public static final long TEMP_FILE_DELETE_DELAY_HOURS = 24;

    /**
     * 通知异步队列（Redis List，JSON 字符串元素；生产者 {@code RPUSH}，消费者建议 {@code BLPOP}）
     */
    public static final String NOTIFICATION_QUEUE_KEY = "queue:notification";

    // ==================== 缓存 Key 前缀 ====================

    /**
     * 缓存通用前缀
     */
    public static final String CACHE_KEY_PREFIX = "cache:";

    /**
     * 项目模块缓存前缀
     */
    public static final String CACHE_PROJECT = CACHE_KEY_PREFIX + "project:";

    /**
     * 社区模块缓存前缀
     */
    public static final String CACHE_COMMUNITY = CACHE_KEY_PREFIX + "community:";

    /**
     * 人才模块缓存前缀
     */
    public static final String CACHE_TALENT = CACHE_KEY_PREFIX + "talent:";

    /**
     * 用户模块缓存前缀
     */
    public static final String CACHE_USER = CACHE_KEY_PREFIX + "user:";

    // ==================== 缓存 TTL（秒） ====================

    /** 智能匹配结果缓存 TTL */
    public static final long CACHE_TTL_MATCH = 60;
    /** 列表缓存 TTL */
    public static final long CACHE_TTL_LIST = 300;
    /** 详情缓存 TTL */
    public static final long CACHE_TTL_DETAIL = 600;
    /** 统计数据缓存 TTL */
    public static final long CACHE_TTL_STATS = 120;
    /** 默认缓存 TTL */
    public static final long CACHE_TTL_DEFAULT = 300;
}