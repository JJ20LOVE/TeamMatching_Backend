package club.boyuan.official.teammatching.service;

/**
 * 邮件服务接口
 */
public interface EmailService {
    
    /**
     * 发送验证码邮件
     * @param toEmail 接收邮箱
     * @param verifyCode 验证码
     */
    void sendVerifyCode(String toEmail, String verifyCode);
    
    /**
     * 发送HTML格式验证码邮件
     * @param toEmail 接收邮箱
     * @param verifyCode 验证码
     * @param subject 邮件主题
     */
    void sendHtmlVerifyCode(String toEmail, String verifyCode, String subject);
}