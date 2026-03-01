package club.boyuan.official.teammatch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 入队申请表实体类
 */
@Data
@TableName("team_application")
public class TeamApplication {

    /**
     * 申请ID，主键
     */
    @TableId(value = "application_id", type = IdType.AUTO)
    private Integer applicationId;

    /**
     * 申请人ID，关联user表
     */
    private Integer applicantUserId;

    /**
     * 项目ID，关联project表
     */
    private Integer projectId;

    /**
     * 申请的角色要求ID
     */
    private Integer requirementId;

    /**
     * 申请的岗位角色
     */
    private String role;

    /**
     * 申请原因/自我介绍
     */
    private String applyReason;

    /**
     * 投递专用简历URL
     */
    private String customResumeUrl;

    /**
     * 投递专用简历文件名
     */
    private String customResumeName;

    /**
     * 其他附件链接（获奖证书等）
     */
    private String applicationAttachmentUrl;

    /**
     * 结果：0-待审核 1-通过 2-驳回
     */
    private Integer result;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核人ID（队长），关联user表
     */
    private Integer auditorUserId;

    /**
     * 审核备注/驳回原因
     */
    private String remark;

    /**
     * 申请时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime applyTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}