package club.boyuan.official.teammatching.dto.response.team;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 团队详情响应DTO
 */
@Data
@ApiModel(value = "团队详情响应")
public class TeamDetailResponse {
    @ApiModelProperty(value = "项目ID")
    private Integer projectId;

    @ApiModelProperty(value = "团队名称")
    private String name;

    @ApiModelProperty(value = "项目状态")
    private Integer status;

    @ApiModelProperty(value = "团队成员列表")
    private List<TeamMemberResponse> members;

    @ApiModelProperty(value = "项目进展")
    private String progress;

    @ApiModelProperty(value = "任务统计")
    private TaskStats taskStats;

    @Data
    @ApiModel(value = "任务统计")
    public static class TaskStats {
        @ApiModelProperty(value = "总任务数")
        private Integer total;

        @ApiModelProperty(value = "已完成任务数")
        private Integer completed;

        @ApiModelProperty(value = "进行中任务数")
        private Integer inProgress;
    }
}
