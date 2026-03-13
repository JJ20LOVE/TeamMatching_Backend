package club.boyuan.official.teammatching.dto.response.project;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 申请项目响应 DTO（用于投递接口返回）
 */
@Data
@ApiModel(value = "申请项目响应")
public class ApplyProjectResponse {

    @ApiModelProperty(value = "申请ID", example = "401")
    private Integer applicationId;

    @ApiModelProperty(value = "提示消息", example = "投递成功，等待队长回复")
    private String message;

    @ApiModelProperty(value = "创建的会话ID", example = "501")
    private Integer sessionId;
}

