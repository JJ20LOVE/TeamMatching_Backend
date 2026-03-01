package club.boyuan.official.teammatch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 队长邀请记录表实体类
 */
@Data
@TableName("talent_invitation")
public class TalentInvitation {

    /**
     * 邀请ID
     */
    @TableId(value = "invitation_id", type = IdType.AUTO)
    private Integer invitationId;

    /**
     * 队长ID（邀请方）
     */
    private Integer captainId;

    /**
     * 人才ID（被邀请方）
     */
    private Integer talentId;

    /**
     * 关联项目ID
     */
    private Integer projectId;

    /**
     * 被邀请的人才卡片ID
     */
    private Integer talentCardId;

    /**
     * 邀请附言
     */
    private String invitationMessage;

    /**
     * 项目名称（冗余）
     */
    private String projectName;

    /**
     * 邀请担任的角色
     */
    private String projectRole;

    /**
     * 状态：0-待回复 1-已接受 2-已拒绝 3-已过期
     */
    private Integer status;

    /**
     * 是否已读
     */
    private Boolean readStatus;

    /**
     * 发送时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime sendTime;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 回复时间
     */
    private LocalDateTime responseTime;
}