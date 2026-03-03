package club.boyuan.official.teammatching.dto.request.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;



/**
 * 找回密码请求 DTO
 */
@Data
@ApiModel(value = "找回密码请求 DTO")
public class ForgotPasswordRequest {
    
    @ApiModelProperty(value = "邮箱或手机号", required = true)
    @NotBlank(message = "账号不能为空")
    private String account;
    
    @ApiModelProperty(value = "6 位验证码", required = true)
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须为 6 位数字")
    private String verifyCode;
    
    @ApiModelProperty(value = "新密码 (6-20 位)", required = true)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在 6-20 位之间")
    private String newPassword;
}
