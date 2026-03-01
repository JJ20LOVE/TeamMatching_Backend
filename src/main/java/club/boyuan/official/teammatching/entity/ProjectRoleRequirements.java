package club.boyuan.official.teammatching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 项目角色要求表
 * </p>
 *
 * @author dhy
 * @since 2026-03-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("project_role_requirements")
@ApiModel(value="ProjectRoleRequirements对象", description="项目角色要求表")
public class ProjectRoleRequirements implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "角色要求ID，主键")
    @TableId(value = "requirement_id", type = IdType.AUTO)
    private Integer requirementId;

    @ApiModelProperty(value = "项目ID，关联project表")
    private Integer projectId;

    @ApiModelProperty(value = "所需角色名（如：后端开发）")
    private String role;

    @ApiModelProperty(value = "招募人数")
    private Integer memberQuota;

    @ApiModelProperty(value = "当前已申请人数")
    private Integer currentApplicants;

    @ApiModelProperty(value = "当前已加入人数")
    private Integer currentMembers;

    @ApiModelProperty(value = "具体招募要求（技能要求）")
    private String recruitRequirements;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
