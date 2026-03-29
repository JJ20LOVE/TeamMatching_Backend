package club.boyuan.official.teammatching.dto.response.team;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 我的团队列表响应DTO
 */
@Data
@ApiModel(value = "我的团队列表响应")
public class MyTeamListResponse {
    @ApiModelProperty(value = "我领导的团队")
    private List<TeamBriefItem> leading;

    @ApiModelProperty(value = "我加入的团队")
    private List<TeamBriefItem> joining;

    @Data
    @ApiModel(value = "团队简要信息")
    public static class TeamBriefItem {
        @ApiModelProperty(value = "项目ID")
        private Integer projectId;

        @ApiModelProperty(value = "团队名称")
        private String name;

        @ApiModelProperty(value = "项目状态")
        private Integer status;

        @ApiModelProperty(value = "成员数量")
        private Integer memberCount;

        @ApiModelProperty(value = "身份角色：队长/队员")
        private String role;

        @ApiModelProperty(value = "未读消息数")
        private Integer unreadCount;
    }
}
