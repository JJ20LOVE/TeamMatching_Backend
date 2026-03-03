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
 * 用户-技能标签关联表
 * </p>
 *
 * @author dhy
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_skill_relation")
@ApiModel(value="UserSkillRelation对象", description="用户-技能标签关联表")
public class UserSkillRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "关联ID，主键")
    @TableId(value = "relation_id", type = IdType.AUTO)
    private Integer relationId;

    @ApiModelProperty(value = "用户ID，关联user表")
    private Integer userId;

    @ApiModelProperty(value = "标签ID，关联skill_tag表")
    private Integer tagId;

    @ApiModelProperty(value = "熟练度：1-了解 2-熟悉 3-精通")
    private Integer proficiency;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
