package club.boyuan.official.teammatching.service;

import club.boyuan.official.teammatching.dto.request.auth.ChangePasswordRequest;
import club.boyuan.official.teammatching.dto.request.auth.ForgotPasswordRequest;
import club.boyuan.official.teammatching.dto.request.auth.LoginRequest;
import club.boyuan.official.teammatching.dto.request.auth.RegisterRequest;
import club.boyuan.official.teammatching.dto.request.auth.SendVerifyCodeRequest;
import club.boyuan.official.teammatching.dto.request.auth.WxLoginRequest;
import club.boyuan.official.teammatching.dto.response.auth.RegisterResponse;
import club.boyuan.official.teammatching.dto.response.auth.WxLoginResponse;

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
    
    /**
     * 微信一键登录
     * @param wxLoginRequest 微信登录请求参数
     * @return 微信登录响应信息
     */
    WxLoginResponse wxLogin(WxLoginRequest wxLoginRequest);
    
    /**
     * 密码登录
     * @param loginRequest 登录请求参数
     * @return 登录响应信息
     */
    RegisterResponse login(LoginRequest loginRequest);
    
    /**
     * 刷新Token
     * @param oldToken 旧的Token
     * @return 新的Token信息
     */
    RegisterResponse refreshToken(String oldToken);
    

    /**
     * 找回密码
     * @param forgotPasswordRequest 找回密码请求参数
     */
    void forgotPassword(ForgotPasswordRequest forgotPasswordRequest);
    
    /**
     * 修改密码
     * @param changePasswordRequest 修改密码请求参数
     * @param userId 用户 ID
     */
    void changePassword(ChangePasswordRequest changePasswordRequest, Integer userId);
    
    // 其他认证服务接口方法
}