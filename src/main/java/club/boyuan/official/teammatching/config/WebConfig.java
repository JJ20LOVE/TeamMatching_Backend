package club.boyuan.official.teammatching.config;

import club.boyuan.official.teammatching.interceptor.AuthenticationInterceptor;
import club.boyuan.official.teammatching.interceptor.LogInterceptor;
import club.boyuan.official.teammatching.interceptor.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web相关配置
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final AuthenticationInterceptor authenticationInterceptor;
    private final LogInterceptor logInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 认证拦截器 - 根据OpenAPI文档调整拦截规则
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 认证相关接口
                        "/auth/register",
                        "/auth/wx-login",
                        "/auth/login",
                        "/auth/send-code",
                        "/auth/forgot-password",
                        // 公共接口
                        "/public/**",
                        "/common/**",
                        // 文档相关
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/doc.html",
                        "/webjars/**",
                        // WebSocket
                        "/ws/**",
                        // 静态资源
                        "/favicon.ico"
                );
        
        // 日志拦截器 - 记录所有请求日志
        registry.addInterceptor(logInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/doc.html",
                        "/webjars/**"
                );
        
        // 限流拦截器 - 对特定接口进行限流
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns(
                        "/api/auth/send-code",     // 发送验证码接口
                        "/api/auth/login",         // 登录接口
                        "/api/auth/register"       // 注册接口
                );
    }
}