package club.boyuan.official.teammatching.dto.request.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录请求DTO
 */
@Data
@ApiModel(value = "微信登录请求DTO")
public class WxLoginRequest {
    
    @ApiModelProperty(value = "微信临时登录凭证", required = true)
    @NotBlank(message = "code不能为空")
    private String code;
    
    @ApiModelProperty(value = "加密数据")
    private String encryptedData;
    
    @ApiModelProperty(value = "加密算法的初始向量")
    private String iv;
}