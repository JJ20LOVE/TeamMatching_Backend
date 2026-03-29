package club.boyuan.official.teammatching.dto.response.team;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 团队成员响应DTO
 */
@Data
@ApiModel(value = "团队成员响应")
public class TeamMemberResponse {
    @ApiModelProperty(value = "用户ID")
    private Integer userId;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "头像URL")
    private String avatar;

    @ApiModelProperty(value = "身份角色：队长/队员")
    private String role;

    @ApiModelProperty(value = "团队角色")
    private String teamRole;

    @ApiModelProperty(value = "加入时间，格式：yyyy-MM-dd")
    private String joinTime;

    @ApiModelProperty(value = "成员状态：0-在队 1-已退出 2-被移除")
    private Integer status;
}
