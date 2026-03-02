package club.boyuan.official.teammatching.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
public class JwtUtils {
    
    /**
     * JWT密钥 - 使用HS512算法所需的安全密钥
     * 通过Keys.secretKeyFor(SignatureAlgorithm.HS512)生成，确保符合RFC 7518安全标准
     */
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    
    /**
     * JWT过期时间(毫秒) - 24小时
     */
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;
    /**
     * 生成JWT令牌
     * @param userId 用户ID
     * @return JWT令牌
     */
    public static String generateToken(Integer userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SECRET_KEY)
                .compact();
    }
    
    /**
     * 解析JWT令牌
     * @param token JWT令牌
     * @return Claims对象
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 验证JWT令牌是否有效
     * @param token JWT令牌
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 从JWT令牌中获取用户ID
     * @param token JWT令牌
     * @return 用户ID
     */
    public static Integer getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Integer.valueOf(claims.getSubject());
    }
    
    /**
     * 获取JWT令牌过期时间(秒)
     * @return 过期时间(秒)
     */
    public static Long getExpirationTimeInSeconds() {
        return EXPIRATION_TIME / 1000;
    }
}