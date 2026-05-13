package club.boyuan.official.teammatching.dto.response.talent;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邀请列表项（可用于我发送/我收到）
 */
@Data
@ApiModel(value = "邀请列表项")
public class TalentInvitationListItemResponse {

    @ApiModelProperty(value = "邀请 ID")
    private Integer invitationId;

    @ApiModelProperty(value = "项目 ID")
    private Integer projectId;

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "邀请担任角色")
    private String projectRole;

    @ApiModelProperty(value = "人才卡 ID")
    private Integer talentCardId;

    @ApiModelProperty(value = "邀请附言")
    private String invitationMessage;

    @ApiModelProperty(value = "联系方式")
    private String contactInfo;

    @ApiModelProperty(value = "状态：0-待回复 1-已接受 2-已拒绝 3-已过期")
    private Integer status;

    @ApiModelProperty(value = "是否已读")
    private Boolean readStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @ApiModelProperty(value = "发送时间")
    private LocalDateTime sendTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @ApiModelProperty(value = "回复时间")
    private LocalDateTime responseTime;

    @ApiModelProperty(value = "对端用户 ID（发送列表里是人才，接收列表里是队长）")
    private Integer counterpartUserId;

    @ApiModelProperty(value = "对端用户昵称")
    private String counterpartNickname;

    @ApiModelProperty(value = "对端用户头像")
    private String counterpartAvatar;
}
