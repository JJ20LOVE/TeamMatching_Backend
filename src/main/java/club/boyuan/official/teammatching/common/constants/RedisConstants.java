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
}