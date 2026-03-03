package club.boyuan.official.teammatching.common.utils;

import club.boyuan.official.teammatching.entity.User;

/**
 * 用户上下文工具类
 * 使用ThreadLocal存储当前线程的用户信息
 */
public class UserContextUtil {
    
    private static final ThreadLocal<User> USER_CONTEXT = new ThreadLocal<>();
    
    /**
     * 设置当前用户信息
     * @param user 用户信息
     */
    public static void setCurrentUser(User user) {
        USER_CONTEXT.set(user);
    }
    
    /**
     * 获取当前用户信息
     * @return 当前用户信息
     */
    public static User getCurrentUser() {
        return USER_CONTEXT.get();
    }
    
    /**
     * 获取当前用户ID
     * @return 当前用户ID
     */
    public static Integer getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }
    
    /**
     * 清除当前用户信息
     */
    public static void clear() {
        USER_CONTEXT.remove();
    }
    
    /**
     * 检查是否有用户上下文
     * @return 是否有用户上下文
     */
    public static boolean hasCurrentUser() {
        return getCurrentUser() != null;
    }
}