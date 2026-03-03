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
 * 收藏表
 * </p>
 *
 * @author dhy
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("favorite")
@ApiModel(value="Favorite对象", description="收藏表")
public class Favorite implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "收藏ID，主键")
    @TableId(value = "favorite_id", type = IdType.AUTO)
    private Integer favoriteId;

    @ApiModelProperty(value = "用户ID，关联user表")
    private Integer userId;

    @ApiModelProperty(value = "类型：1-项目 2-帖子 3-人才卡片")
    private Integer targetType;

    @ApiModelProperty(value = "目标ID")
    private Integer targetId;

    @ApiModelProperty(value = "收藏时间")
    private LocalDateTime createdTime;


}
