package club.boyuan.official.teammatching.dto.request.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;



/**
 * 发送验证码请求DTO
 */
@Data
@ApiModel(value = "发送验证码请求DTO")
public class SendVerifyCodeRequest {
    
    @ApiModelProperty(value = "邮箱或手机号", required = true)
    @NotBlank(message = "目标不能为空")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$|^1[3-9]\\d{9}$", 
             message = "请输入正确的邮箱或手机号格式")
    private String target;
    
    @ApiModelProperty(value = "类型：register-注册 login-登录 reset-重置密码", 
                     example = "register", required = true)
    @NotBlank(message = "类型不能为空")
    @Pattern(regexp = "^(register|login|reset)$", 
             message = "类型只能是 register/login/reset")
    private String type;
}