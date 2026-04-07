package club.boyuan.official.teammatching.common.constants;

/**
 * 认证相关常量
 */
public class AuthConstants {
    
    /**
     * Authorization请求头名称
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";
    
    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * Token参数名（用于Query Parameter传递）
     */
    public static final String TOKEN_PARAM = "token";
    
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
     * 刷新令牌阈值(小时) - 当剩余时间小于这个值时需要刷新
     */
    public static final long REFRESH_THRESHOLD = 1;

    /** 管理员角色，与 user 表 role 字段一致 */
    public static final String ROLE_ADMIN = "admin";
}