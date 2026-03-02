package club.boyuan.official.teammatching.service;

/**
 * 短信服务接口
 */
public interface SmsService {
    
    /**
     * 发送验证码短信
     * @param phoneNumber 手机号码
     * @param verifyCode 验证码
     */
    void sendVerifyCode(String phoneNumber, String verifyCode);
    
    /**
     * 发送模板短信
     * @param phoneNumber 手机号码
     * @param templateCode 模板编码
     * @param params 模板参数
     */
    void sendTemplateSms(String phoneNumber, String templateCode, String... params);
}