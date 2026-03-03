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
 * 身份认证材料表
 * </p>
 *
 * @author dhy
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auth_material")
@ApiModel(value="AuthMaterial对象", description="身份认证材料表")
public class AuthMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "材料ID")
    @TableId(value = "material_id", type = IdType.AUTO)
    private Integer materialId;

    @ApiModelProperty(value = "用户ID")
    private Integer userId;

    @ApiModelProperty(value = "材料类型：1-学生证 2-身份证 3-校园卡 4-学信网证明")
    private Integer materialType;

    @ApiModelProperty(value = "文件ID，关联file_resource表")
    private Long fileId;

    @ApiModelProperty(value = "材料描述")
    private String description;

    @ApiModelProperty(value = "审核状态：0-待审核 1-已通过 2-已驳回")
    private Integer auditStatus;

    @ApiModelProperty(value = "审核时间")
    private LocalDateTime auditTime;

    @ApiModelProperty(value = "审核人ID")
    private Integer auditorUserId;

    @ApiModelProperty(value = "审核备注")
    private String remark;

    private LocalDateTime createdTime;


}
