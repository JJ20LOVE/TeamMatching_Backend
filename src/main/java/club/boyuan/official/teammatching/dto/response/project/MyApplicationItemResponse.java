package club.boyuan.official.teammatching.dto.response.project;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 当前用户对项目的投递记录（用于「我投递过的项目」列表）
 */
@Data
@ApiModel(value = "我的投递记录项")
public class MyApplicationItemResponse {

    @ApiModelProperty(value = "申请 ID")
    private Integer applicationId;

    @ApiModelProperty(value = "项目 ID")
    private Integer projectId;

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "所属赛道")
    private String belongTrack;

    @ApiModelProperty(value = "项目状态：0-草拟 1-实施 2-招募中 3-完成 4-终止")
    private Integer projectStatus;

    @ApiModelProperty(value = "投递岗位/角色")
    private String role;

    @ApiModelProperty(value = "角色要求 ID")
    private Integer requirementId;

    @ApiModelProperty(value = "审核结果：0-待审核 1-通过 2-驳回")
    private Integer result;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @ApiModelProperty(value = "申请时间")
    private LocalDateTime applyTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @ApiModelProperty(value = "审核时间（未审核为 null）")
    private LocalDateTime auditTime;

    @ApiModelProperty(value = "审核备注/驳回原因")
    private String remark;

    @ApiModelProperty(value = "与队长沟通的会话 ID（无则 null）")
    private Integer sessionId;
}
