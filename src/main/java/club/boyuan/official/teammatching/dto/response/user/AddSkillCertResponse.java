package club.boyuan.official.teammatching.dto.response.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 添加技能认证响应 DTO
 */
@Data
@ApiModel(value = "添加技能认证响应")
public class AddSkillCertResponse {
    
    @ApiModelProperty(value = "认证 ID")
    private Integer certId;
    
    @ApiModelProperty(value = "响应消息")
    private String message;
    
    @ApiModelProperty(value = "审核状态：0-待审核 1-已通过 2-已驳回")
    private Integer auditStatus;
}
