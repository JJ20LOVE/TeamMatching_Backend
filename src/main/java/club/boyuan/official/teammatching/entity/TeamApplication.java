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
 * 入队申请表
 * </p>
 *
 * @author dhy
 * @since 2026-03-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("team_application")
@ApiModel(value="TeamApplication对象", description="入队申请表")
public class TeamApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "申请ID，主键")
    @TableId(value = "application_id", type = IdType.AUTO)
    private Integer applicationId;

    @ApiModelProperty(value = "申请人ID，关联user表")
    private Integer applicantUserId;

    @ApiModelProperty(value = "项目ID，关联project表")
    private Integer projectId;

    @ApiModelProperty(value = "申请的角色要求ID")
    private Integer requirementId;

    @ApiModelProperty(value = "申请的岗位角色")
    private String role;

    @ApiModelProperty(value = "申请原因/自我介绍")
    private String applyReason;

    @ApiModelProperty(value = "投递专用简历URL")
    private String customResumeUrl;

    @ApiModelProperty(value = "投递专用简历文件名")
    private String customResumeName;

    @ApiModelProperty(value = "其他附件链接（获奖证书等）")
    private String applicationAttachmentUrl;

    @ApiModelProperty(value = "结果：0-待审核 1-通过 2-驳回")
    private Integer result;

    @ApiModelProperty(value = "审核时间")
    private LocalDateTime auditTime;

    @ApiModelProperty(value = "审核人ID（队长），关联user表")
    private Integer auditorUserId;

    @ApiModelProperty(value = "审核备注/驳回原因")
    private String remark;

    @ApiModelProperty(value = "申请时间")
    private LocalDateTime applyTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
