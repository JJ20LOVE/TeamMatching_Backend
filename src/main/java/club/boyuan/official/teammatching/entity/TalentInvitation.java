package club.boyuan.official.teammatching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 队长邀请记录表
 * </p>
 *
 * @author dhy
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("talent_invitation")
@ApiModel(value="TalentInvitation对象", description="队长邀请记录表")
public class TalentInvitation implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "邀请ID")
    @TableId(value = "invitation_id", type = IdType.AUTO)
    private Integer invitationId;

    @ApiModelProperty(value = "队长ID（邀请方）")
    private Integer captainId;

    @ApiModelProperty(value = "人才ID（被邀请方）")
    private Integer talentId;

    @ApiModelProperty(value = "关联项目ID")
    private Integer projectId;

    @ApiModelProperty(value = "被邀请的人才卡片ID")
    private Integer talentCardId;

    @ApiModelProperty(value = "邀请附言")
    private String invitationMessage;

    @ApiModelProperty(value = "项目名称（冗余）")
    private String projectName;

    @ApiModelProperty(value = "邀请担任的角色")
    private String projectRole;

    @ApiModelProperty(value = "状态：0-待回复 1-已接受 2-已拒绝 3-已过期")
    private Integer status;

    @ApiModelProperty(value = "是否已读")
    private Boolean readStatus;

    @ApiModelProperty(value = "发送时间")
    private LocalDateTime sendTime;

    @ApiModelProperty(value = "阅读时间")
    private LocalDateTime readTime;

    @ApiModelProperty(value = "回复时间")
    private LocalDateTime responseTime;


}
