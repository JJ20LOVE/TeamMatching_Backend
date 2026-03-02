package club.boyuan.official.teammatching.dto.request.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;



/**
 * 注册请求DTO
 */
@Data
@ApiModel(value = "注册请求DTO")
public class RegisterRequest {
    
    @ApiModelProperty(value = "邮箱或手机号", required = true)
    @NotBlank(message = "账号不能为空")
    private String account;
    
    @ApiModelProperty(value = "密码(6-20位)", required = true)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String password;
    
    @ApiModelProperty(value = "昵称")
    private String nickname;
    
    @ApiModelProperty(value = "6位验证码", required = true)
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须为6位数字")
    private String verifyCode;
}