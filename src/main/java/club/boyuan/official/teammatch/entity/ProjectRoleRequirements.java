package club.boyuan.official.teammatch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 项目角色要求表实体类
 */
@Data
@TableName("project_role_requirements")
public class ProjectRoleRequirements {

    /**
     * 角色要求ID，主键
     */
    @TableId(value = "requirement_id", type = IdType.AUTO)
    private Integer requirementId;

    /**
     * 项目ID，关联project表
     */
    private Integer projectId;

    /**
     * 所需角色名（如：后端开发）
     */
    private String role;

    /**
     * 招募人数
     */
    private Integer memberQuota;

    /**
     * 当前已申请人数
     */
    private Integer currentApplicants;

    /**
     * 当前已加入人数
     */
    private Integer currentMembers;

    /**
     * 具体招募要求（技能要求）
     */
    private String recruitRequirements;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}