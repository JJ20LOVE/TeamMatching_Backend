package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 短信服务实现类（模拟实现）
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {
    
    @Value("${sms.enabled:false}")
    private boolean smsEnabled;
    
    @Override
    public void sendVerifyCode(String phoneNumber, String verifyCode) {
        if (!smsEnabled) {
            log.warn("短信服务未启用，验证码: {} (仅用于测试)", verifyCode);
            return;
        }
        
        try {
            // 这里应该是调用第三方短信服务商的API
            // 示例：阿里云短信、腾讯云短信等
            String message = buildVerifyCodeMessage(verifyCode);
            sendSms(phoneNumber, message);
            log.info("验证码短信发送成功: {}", phoneNumber);
        } catch (Exception e) {
            log.error("发送验证码短信失败: {}, 错误: {}", phoneNumber, e.getMessage(), e);
            // 生产环境中应该抛出异常
        }
    }
    
    @Override
    public void sendTemplateSms(String phoneNumber, String templateCode, String... params) {
        if (!smsEnabled) {
            log.warn("短信服务未启用，模板编码: {}, 参数: {} (仅用于测试)", templateCode, String.join(",", params));
            return;
        }
        
        try {
            // 调用第三方短信服务商的模板短信API
            sendTemplateMessage(phoneNumber, templateCode, params);
            log.info("模板短信发送成功: {}, 模板: {}", phoneNumber, templateCode);
        } catch (Exception e) {
            log.error("发送模板短信失败: {}, 模板: {}, 错误: {}", phoneNumber, templateCode, e.getMessage(), e);
        }
    }
    
    /**
     * 模拟发送短信（实际项目中需要替换为真实的短信服务商API调用）
     */
    private void sendSms(String phoneNumber, String message) {
        // 模拟发送过程
        log.info("模拟发送短信到 {}: {}", phoneNumber, message);
        
        // 实际实现示例（伪代码）：
        /*
        // 阿里云短信示例
        IAcsClient client = new DefaultAcsClient(profile);
        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers(phoneNumber);
        request.setSignName("TeamMatch");
        request.setTemplateCode("SMS_XXXXXXXXX");
        request.setTemplateParam("{\"code\":\"" + verifyCode + "\"}");
        SendSmsResponse response = client.getAcsResponse(request);
        */
    }
    
    /**
     * 模拟发送模板短信
     */
    private void sendTemplateMessage(String phoneNumber, String templateCode, String... params) {
        // 模拟发送过程
        log.info("模拟发送模板短信到 {}: 模板={}, 参数={}", phoneNumber, templateCode, String.join(",", params));
        
        // 实际实现示例（伪代码）：
        /*
        // 腾讯云短信示例
        SmsSingleSender ssender = new SmsSingleSender(appId, appKey);
        ArrayList<String> paramsList = new ArrayList<>(Arrays.asList(params));
        SmsSingleSenderResult result = ssender.sendWithParam("86", phoneNumber,
            Integer.parseInt(templateCode), paramsList, "TeamMatch", "", "");
        */
    }
    
    /**
     * 构建验证码短信内容
     */
    private String buildVerifyCodeMessage(String verifyCode) {
        return String.format("【TeamMatch】您的验证码是%s，5分钟内有效。请勿泄露给他人。", verifyCode);
    }
}