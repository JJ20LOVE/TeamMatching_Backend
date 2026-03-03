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
 * 关注表
 * </p>
 *
 * @author dhy
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("follow")
@ApiModel(value="Follow对象", description="关注表")
public class Follow implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "关注ID，主键")
    @TableId(value = "follow_id", type = IdType.AUTO)
    private Integer followId;

    @ApiModelProperty(value = "关注者ID，关联user表")
    private Integer followerId;

    @ApiModelProperty(value = "被关注者ID，关联user表")
    private Integer followingId;

    @ApiModelProperty(value = "关注时间")
    private LocalDateTime createdTime;


}
