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
 * 团队成员表
 * </p>
 *
 * @author dhy
 * @since 2026-03-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("team_member")
@ApiModel(value="TeamMember对象", description="团队成员表")
public class TeamMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "成员关联ID，主键")
    @TableId(value = "team_member_id", type = IdType.AUTO)
    private Integer teamMemberId;

    @ApiModelProperty(value = "项目ID，关联project表")
    private Integer projectId;

    @ApiModelProperty(value = "用户ID，关联user表")
    private Integer userId;

    @ApiModelProperty(value = "在团队中的角色")
    private String role;

    @ApiModelProperty(value = "加入时间")
    private LocalDateTime joinTime;

    @ApiModelProperty(value = "状态：0-在队 1-已退出 2-被移除")
    private Integer status;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
