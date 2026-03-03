package club.boyuan.official.teammatching.dto.response.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 发送验证码响应 DTO
 */
@Data
@ApiModel(value = "发送验证码响应 DTO")
public class SendVerifyCodeResponse {
    
    @ApiModelProperty(value = "发送成功提示")
    private String message;
    
    @ApiModelProperty(value = "验证码有效期（秒）")
    private Integer expireIn;
}
