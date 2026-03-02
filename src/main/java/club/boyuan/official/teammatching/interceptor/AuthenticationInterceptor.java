package club.boyuan.official.teammatching.interceptor;

import club.boyuan.official.teammatching.common.annotation.NeedAuth;
import club.boyuan.official.teammatching.common.annotation.NeedLogin;
import club.boyuan.official.teammatching.common.constants.AuthConstants;
import club.boyuan.official.teammatching.common.enums.AuthStatusEnum;
import club.boyuan.official.teammatching.common.utils.JwtUtils;
import club.boyuan.official.teammatching.common.utils.RedisUtils;
import club.boyuan.official.teammatching.common.utils.UserContextUtil;
import club.boyuan.official.teammatching.entity.User;
import club.boyuan.official.teammatching.exception.ForbiddenException;
import club.boyuan.official.teammatching.exception.UnauthorizedException;
import club.boyuan.official.teammatching.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

/**
 * 认证拦截器
 * 负责JWT Token验证、用户权限检查、用户上下文设置
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {
    
    private final UserMapper userMapper;
    private final RedisUtils redisUtils;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 检查是否需要登录
        boolean needLogin = handlerMethod.hasMethodAnnotation(NeedLogin.class) 
                          || handlerMethod.getBeanType().isAnnotationPresent(NeedLogin.class);
        
        // 检查是否需要认证
        boolean needAuth = handlerMethod.hasMethodAnnotation(NeedAuth.class) 
                         || handlerMethod.getBeanType().isAnnotationPresent(NeedAuth.class);
        
        // 如果不需要认证，直接通过
        if (!needLogin && !needAuth) {
            return true;
        }
        
        // 获取Token
        String token = getTokenFromRequest(request);
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedException("未提供访问令牌");
        }
        
        // 验证Token
        if (!JwtUtils.validateToken(token)) {
            throw new UnauthorizedException("无效的访问令牌");
        }
        
        // 从Token中获取用户ID
        Integer userId;
        try {
            userId = JwtUtils.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new UnauthorizedException("令牌解析失败");
        }
        
        // 检查Token是否在Redis中存在（防止Token被盗用）
        String tokenKey = String.format(AuthConstants.USER_JWT_TOKEN_KEY, userId);
        String storedToken = (String) redisUtils.get(tokenKey);
        if (!Objects.equals(storedToken, token)) {
            throw new UnauthorizedException("令牌已失效，请重新登录");
        }
        
        // 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UnauthorizedException("用户不存在");
        }
        
        // 检查用户状态
        if (user.getStatus() == null || user.getStatus()) {
            throw new ForbiddenException("账号已被冻结");
        }
        
        // 如果需要认证，检查认证状态
        if (needAuth) {
            AuthStatusEnum authStatus = AuthStatusEnum.getByCode(user.getAuthStatus());
            if (authStatus != AuthStatusEnum.APPROVED) {
                if (authStatus == AuthStatusEnum.REJECTED) {
                    throw new ForbiddenException("账号认证已被驳回: " + user.getRemark());
                } else {
                    throw new ForbiddenException("账号尚未通过认证，请等待管理员审核");
                }
            }
        }
        
        // 设置用户上下文
        UserContextUtil.setCurrentUser(user);
        
        log.debug("用户认证成功: userId={}, nickname={}, needAuth={}", 
                 userId, user.getNickname(), needAuth);
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                              Object handler, Exception ex) throws Exception {
        // 清除用户上下文，防止内存泄漏
        UserContextUtil.clear();
    }
    
    /**
     * 从请求中获取Token
     * 支持Header和Query Parameter两种方式
     * @param request HTTP请求
     * @return Token字符串
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 1. 从Authorization Header获取
        String authorizationHeader = request.getHeader(AuthConstants.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authorizationHeader) && 
            authorizationHeader.startsWith(AuthConstants.TOKEN_PREFIX)) {
            return authorizationHeader.substring(AuthConstants.TOKEN_PREFIX.length()).trim();
        }
        
        // 2. 从Query Parameter获取
        String tokenParam = request.getParameter(AuthConstants.TOKEN_PARAM);
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }
        
        return null;
    }
}