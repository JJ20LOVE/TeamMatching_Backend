package club.boyuan.official.teammatching.dto.request.project;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 申请项目请求 DTO
 */
@Data
@ApiModel(value = "申请项目请求")
public class ApplyProjectRequest {

    @NotNull(message = "角色要求ID不能为空")
    @ApiModelProperty(value = "申请的角色要求ID", required = true, example = "301")
    private Integer requirementId;

    @NotBlank(message = "申请原因不能为空")
    @ApiModelProperty(value = "申请原因/自我介绍", required = true,
            example = "我有2年前端开发经验，参与过类似项目")
    private String applyReason;

    @ApiModelProperty(value = "投递专用简历文件ID（通过上传接口获取，targetType=1）",
            example = "10005")
    private Long customResumeFileId;

    @ApiModelProperty(value = "其他附件文件ID（通过上传接口获取，targetType=5）",
            example = "10006")
    private Long applicationAttachmentFileId;
}