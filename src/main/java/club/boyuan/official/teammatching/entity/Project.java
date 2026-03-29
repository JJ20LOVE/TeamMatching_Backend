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
 * 项目表
 * </p>
 *
 * @author dhy
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("project")
@ApiModel(value="Project对象", description="项目表")
public class Project implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "项目ID，主键")
    @TableId(value = "project_id", type = IdType.AUTO)
    private Integer projectId;

    @ApiModelProperty(value = "项目名称")
    private String name;

    @ApiModelProperty(value = "所属赛道（大创、挑战杯等）")
    private String belongTrack;

    @ApiModelProperty(value = "级别：1-校级 2-省级 3-国家级")
    private Integer level;

    @ApiModelProperty(value = "类型：创新训练/创业实践")
    private String projectType;

    @ApiModelProperty(value = "项目详细介绍")
    private String projectIntro;

    @ApiModelProperty(value = "项目进展说明")
    private String projectProgress;

    @ApiModelProperty(value = "项目特点/亮点")
    private String projectFeatures;

    @ApiModelProperty(value = "项目标签（逗号分隔）")
    private String tags;

    @ApiModelProperty(value = "是否允许跨专业申请")
    private Boolean allowCrossMajorApplication;

    @ApiModelProperty(value = "发布人ID，关联user表")
    private Integer publisherUserId;

    @ApiModelProperty(value = "是否匿名发布")
    private Boolean isAnonymous;

    @ApiModelProperty(value = "匿名时显示的临时联系方式")
    private String contactInfo;

    @ApiModelProperty(value = "发布时间")
    private LocalDateTime releaseTime;

    @ApiModelProperty(value = "招募截止时间")
    private LocalDateTime deadlineRecruit;

    @ApiModelProperty(value = "浏览次数")
    private Integer viewCount;

    @ApiModelProperty(value = "收藏次数")
    private Integer favoriteCount;

    @ApiModelProperty(value = "申请人数")
    private Integer applyCount;

    @ApiModelProperty(value = "审核状态：0-待审核 1-通过 2-驳回")
    private Integer auditStatus;

    @ApiModelProperty(value = "审核时间")
    private LocalDateTime auditTime;

    @ApiModelProperty(value = "审核人ID，关联user表")
    private Integer auditorUserId;

    @ApiModelProperty(value = "审核备注/驳回原因")
    private String remark;

    @ApiModelProperty(value = "项目状态：0-草拟 1-实施 2-招募中 3-完成 4-终止")
    private Integer status;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
