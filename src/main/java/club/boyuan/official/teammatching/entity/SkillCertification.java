package club.boyuan.official.teammatching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
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
 * 技能认证表
 * </p>
 *
 * @author dhy
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("skill_certification")
@ApiModel(value="SkillCertification对象", description="技能认证表")
public class SkillCertification implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "认证ID，主键")
    @TableId(value = "cert_id", type = IdType.AUTO)
    private Integer certId;

    @ApiModelProperty(value = "用户ID，关联user表")
    private Integer userId;

    @ApiModelProperty(value = "技能名称")
    private String skillName;

    @ApiModelProperty(value = "证书名称")
    private String certName;

    @ApiModelProperty(value = "证书文件ID，关联file_resource表")
    private Long certFileId;

    @ApiModelProperty(value = "发证日期")
    private LocalDate issueDate;

    @ApiModelProperty(value = "发证机构")
    private String issuer;

    @ApiModelProperty(value = "证书编号")
    private String certNumber;

    @ApiModelProperty(value = "证书描述/补充说明")
    private String description;

    @ApiModelProperty(value = "状态：1-正常 0-删除")
    private Integer status;

    @ApiModelProperty(value = "审核状态：0-待审核 1-已通过 2-已驳回")
    private Integer auditStatus;

    @ApiModelProperty(value = "审核时间")
    private LocalDateTime auditTime;

    @ApiModelProperty(value = "审核人ID")
    private Integer auditorUserId;

    @ApiModelProperty(value = "审核备注/驳回原因")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
