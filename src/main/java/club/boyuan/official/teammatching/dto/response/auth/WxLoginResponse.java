package club.boyuan.official.teammatching.dto.response.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 微信登录响应DTO
 */
@Data
@ApiModel(value = "微信登录响应DTO")
public class WxLoginResponse {
    
    @ApiModelProperty(value = "用户ID")
    private Integer userId;
    
    @ApiModelProperty(value = "访问令牌")
    private String token;
    
    @ApiModelProperty(value = "令牌过期时间(秒)")
    private Long expiresIn;
    
    @ApiModelProperty(value = "认证状态：0-待审核 1-已通过 2-未认证")
    private Integer authStatus;
    
    @ApiModelProperty(value = "是否为新用户（true 表示需引导绑定手机号/邮箱）")
    private Boolean isNewUser;
}