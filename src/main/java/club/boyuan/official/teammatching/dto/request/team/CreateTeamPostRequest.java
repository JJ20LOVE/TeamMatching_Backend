package club.boyuan.official.teammatching.dto.request.team;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 发布团队帖子请求DTO
 */
@Data
@ApiModel(value = "发布团队帖子请求")
public class CreateTeamPostRequest {
    @ApiModelProperty(value = "帖子标题", required = true)
    @NotBlank(message = "帖子标题不能为空")
    private String title;

    @ApiModelProperty(value = "帖子内容", required = true)
    @NotBlank(message = "帖子内容不能为空")
    private String content;

    @ApiModelProperty(value = "附件文件ID列表")
    private List<Long> attachments;
}
