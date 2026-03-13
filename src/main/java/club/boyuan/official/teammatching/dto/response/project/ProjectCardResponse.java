package club.boyuan.official.teammatching.dto.response.project;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目卡片响应 DTO（用于列表、我发布的项目等场景）
 */
@Data
@ApiModel(value = "项目卡片响应")
public class ProjectCardResponse {

    @ApiModelProperty(value = "项目ID", example = "201")
    private Integer projectId;

    @ApiModelProperty(value = "项目名称", example = "基于AI的校园组队平台")
    private String name;

    @ApiModelProperty(value = "所属赛道（大创、挑战杯等）", example = "大创")
    private String belongTrack;

    @ApiModelProperty(value = "项目状态：0-草拟 1-实施 2-招募中 3-完成 4-终止", example = "2")
    private Integer status;

    @ApiModelProperty(value = "审核状态：0-待审核 1-通过 2-驳回", example = "1")
    private Integer auditStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @ApiModelProperty(value = "发布时间", example = "2024-01-01T10:00:00Z")
    private LocalDateTime releaseTime;

    @ApiModelProperty(value = "浏览次数", example = "156")
    private Integer viewCount;

    @ApiModelProperty(value = "申请人数", example = "8")
    private Integer applyCount;

    @ApiModelProperty(value = "总角色数（角色要求条数）", example = "3")
    private Integer totalRoles;

    @ApiModelProperty(value = "已填满的角色数（当前成员数大于等于招募人数）", example = "1")
    private Integer filledRoles;
}
