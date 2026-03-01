package club.boyuan.official.teammatch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户-技能标签关联表实体类
 */
@Data
@TableName("user_skill_relation")
public class UserSkillRelation {

    /**
     * 关联ID，主键
     */
    @TableId(value = "relation_id", type = IdType.AUTO)
    private Integer relationId;

    /**
     * 用户ID，关联user表
     */
    private Integer userId;

    /**
     * 标签ID，关联skill_tag表
     */
    private Integer tagId;

    /**
     * 熟练度：1-了解 2-熟悉 3-精通
     */
    private Integer proficiency;

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