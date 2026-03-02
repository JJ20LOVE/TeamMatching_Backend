package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;



/**
 * 邮件服务实现类
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:}")
    private String fromEmail;
    
    @Value("${spring.mail.enabled:false}")
    private boolean mailEnabled;
    
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    @Override
    public void sendVerifyCode(String toEmail, String verifyCode) {
        if (!mailEnabled) {
            log.warn("邮件服务未启用，验证码: {} (仅用于测试)", verifyCode);
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("TeamMatch 验证码");
            message.setText(buildVerifyCodeText(verifyCode));
            
            mailSender.send(message);
            log.info("验证码邮件发送成功: {}", toEmail);
        } catch (Exception e) {
            log.error("发送验证码邮件失败: {}, 错误: {}", toEmail, e.getMessage(), e);
            //抛出发送验证码失败的异常
            throw new RuntimeException("发送验证码邮件失败");
        }
    }
    
    @Override
    public void sendHtmlVerifyCode(String toEmail, String verifyCode, String subject) {
        if (!mailEnabled) {
            log.warn("邮件服务未启用，验证码: {} (仅用于测试)", verifyCode);
            return;
        }
        
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject != null ? subject : "TeamMatch 验证码");
            helper.setText(buildVerifyCodeHtml(verifyCode), true);
            
            mailSender.send(mimeMessage);
            log.info("HTML验证码邮件发送成功: {}", toEmail);
        } catch (MessagingException e) {
            log.error("发送HTML验证码邮件失败: {}, 错误: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("发送HTML验证码邮件失败");
        }
    }
    
    /**
     * 构建验证码文本内容
     */
    private String buildVerifyCodeText(String verifyCode) {
        return String.format(
            "您好！\n\n" +
            "您的验证码是：%s\n\n" +
            "该验证码5分钟内有效，请勿泄露给他人。\n\n" +
            "TeamMatch 团队",
            verifyCode
        );
    }
    
    /**
     * 构建验证码HTML内容
     */
    private String buildVerifyCodeHtml(String verifyCode) {
        return String.format(
            "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>" +
            "  <div style='background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>" +
            "    <h1 style='margin: 0; font-size: 28px;'>TeamMatch</h1>" +
            "    <p style='margin: 10px 0 0 0; font-size: 16px; opacity: 0.9;'>团队协作平台</p>" +
            "  </div>" +
            "  <div style='background: white; padding: 40px; border: 1px solid #eee; border-top: none; border-radius: 0 0 10px 10px;'>" +
            "    <h2 style='color: #333; margin-top: 0;'>验证码</h2>" +
            "    <div style='text-align: center; margin: 30px 0;'>" +
            "      <div style='display: inline-block; background: #f8f9fa; border: 2px dashed #667eea; border-radius: 10px; padding: 20px 40px;'>" +
            "        <span style='font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 5px;'>%s</span>" +
            "      </div>" +
            "    </div>" +
            "    <p style='color: #666; line-height: 1.6;'>" +
            "      您正在注册 TeamMatch 账号，此验证码用于身份验证。<br>" +
            "      <strong style='color: #e74c3c;'>请注意保密，不要告知他人。</strong>" +
            "    </p>" +
            "    <div style='background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 5px; padding: 15px; margin: 20px 0;'>" +
            "      <strong style='color: #856404;'>⚠️ 重要提醒：</strong>" +
            "      <p style='margin: 5px 0 0 0; color: #856404; font-size: 14px;'>此验证码有效期为5分钟，如非本人操作请忽略此邮件。</p>" +
            "    </div>" +
            "    <hr style='border: none; border-top: 1px solid #eee; margin: 30px 0;'>" +
            "    <p style='color: #999; font-size: 12px; text-align: center;'>" +
            "      此邮件由系统自动发送，请勿回复<br>" +
            "      © 2026 TeamMatch. All rights reserved." +
            "    </p>" +
            "  </div>" +
            "</div>",
            verifyCode
        );
    }
}