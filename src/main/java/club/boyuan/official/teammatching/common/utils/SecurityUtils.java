package club.boyuan.official.teammatching.common.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * 安全工具类
 */
public class SecurityUtils {
    
    /**
     * 对密码进行BCrypt加密
     * @param password 原始密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    
    /**
     * 验证密码是否正确
     * @param plainPassword 明文密码
     * @param hashedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
    
    /**
     * 判断字符串是否为邮箱格式
     * @param str 待判断字符串
     * @return 是否为邮箱格式
     */
    public static boolean isEmail(String str) {
        return str != null && str.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    /**
     * 判断字符串是否为手机号格式
     * @param str 待判断字符串
     * @return 是否为手机号格式
     */
    public static boolean isPhone(String str) {
        return str != null && str.matches("^1[3-9]\\d{9}$");
    }
}