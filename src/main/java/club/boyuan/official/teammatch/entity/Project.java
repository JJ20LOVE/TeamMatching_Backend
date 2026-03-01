package club.boyuan.official.teammatch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 项目表实体类
 */
@Data
@TableName("project")
public class Project {

    /**
     * 项目ID，主键
     */
    @TableId(value = "project_id", type = IdType.AUTO)
    private Integer projectId;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 所属赛道（大创、挑战杯等）
     */
    private String belongTrack;

    /**
     * 级别：1-校级 2-省级 3-国家级
     */
    private Integer level;

    /**
     * 类型：创新训练/创业实践
     */
    private String projectType;

    /**
     * 项目详细介绍
     */
    private String projectIntro;

    /**
     * 项目特点/亮点
     */
    private String projectFeatures;

    /**
     * 项目标签（逗号分隔）
     */
    private String tags;

    /**
     * 是否允许跨专业申请
     */
    private Boolean allowCrossMajorApplication;

    /**
     * 发布人ID，关联user表
     */
    private Integer publisherUserId;

    /**
     * 是否匿名发布
     */
    private Boolean isAnonymous;

    /**
     * 匿名时显示的临时联系方式
     */
    private String contactInfo;

    /**
     * 发布时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime releaseTime;

    /**
     * 招募截止时间
     */
    private LocalDateTime deadlineRecruit;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 收藏次数
     */
    private Integer favoriteCount;

    /**
     * 申请人数
     */
    private Integer applyCount;

    /**
     * 审核状态：0-待审核 1-通过 2-驳回
     */
    private Integer auditStatus;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核人ID，关联user表
     */
    private Integer auditorUserId;

    /**
     * 审核备注/驳回原因
     */
    private String remark;

    /**
     * 项目状态：0-草拟 1-实施 2-招募中 3-完成 4-终止
     */
    private Integer status;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}