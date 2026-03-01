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
 * 点赞表
 * </p>
 *
 * @author dhy
 * @since 2026-03-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("like_record")
@ApiModel(value="LikeRecord对象", description="点赞表")
public class LikeRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "点赞ID，主键")
    @TableId(value = "like_id", type = IdType.AUTO)
    private Integer likeId;

    @ApiModelProperty(value = "点赞用户ID，关联user表")
    private Integer userId;

    @ApiModelProperty(value = "目标类型：1-帖子 2-评论")
    private Integer targetType;

    @ApiModelProperty(value = "目标ID")
    private Integer targetId;

    @ApiModelProperty(value = "点赞时间")
    private LocalDateTime createdTime;


}
