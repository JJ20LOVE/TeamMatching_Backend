package club.boyuan.official.teammatching.dto.request.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
@ApiModel(value = "登录请求DTO")
public class LoginRequest {
    
    @ApiModelProperty(value = "邮箱/手机号/学号", required = true)
    @NotBlank(message = "账号不能为空")
    private String account;
    
    @ApiModelProperty(value = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;
}