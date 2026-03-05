package club.boyuan.official.teammatching.dto.response.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 提交身份认证响应 DTO
 */
@Data
@ApiModel(value = "提交身份认证响应")
public class SubmitAuthResponse {
    
    @ApiModelProperty(value = "认证申请 ID")
    private Integer authId;
    
    @ApiModelProperty(value = "响应消息")
    private String message;
    
    @ApiModelProperty(value = "认证状态：0-待审核 1-已通过 2-已驳回")
    private Integer authStatus;
}
