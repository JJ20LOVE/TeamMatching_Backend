package club.boyuan.official.teammatching.dto.response.team;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 确认入队响应 DTO
 */
@Data
@ApiModel(value = "确认入队响应")
public class JoinConfirmResponse {

    @ApiModelProperty(value = "提示消息", example = "恭喜你加入团队！")
    private String message;

    @ApiModelProperty(value = "团队ID（这里复用项目ID）", example = "201")
    private Integer teamId;

    @ApiModelProperty(value = "建议隐藏人才卡片", example = "true")
    private Boolean suggestHideTalent;
}

