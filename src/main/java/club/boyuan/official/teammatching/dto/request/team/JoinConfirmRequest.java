package club.boyuan.official.teammatching.dto.request.team;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 确认入队请求 DTO
 */
@Data
@ApiModel(value = "确认入队请求")
public class JoinConfirmRequest {

    @NotNull(message = "项目ID不能为空")
    @ApiModelProperty(value = "项目ID", required = true, example = "201")
    private Integer projectId;

    @NotNull(message = "会话ID不能为空")
    @ApiModelProperty(value = "会话ID", required = true, example = "501")
    private Integer sessionId;
}

