package club.boyuan.official.teammatching.service;

import club.boyuan.official.teammatching.dto.request.auth.RegisterRequest;
import club.boyuan.official.teammatching.dto.request.auth.SendVerifyCodeRequest;
import club.boyuan.official.teammatching.dto.response.auth.RegisterResponse;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户注册
     * @param registerRequest 注册请求参数
     * @return 注册响应信息
     */
    RegisterResponse register(RegisterRequest registerRequest);
    
    /**
     * 发送验证码
     * @param sendVerifyCodeRequest 发送验证码请求参数
     */
    void sendVerifyCode(SendVerifyCodeRequest sendVerifyCodeRequest);
    
    // 其他认证服务接口方法
}