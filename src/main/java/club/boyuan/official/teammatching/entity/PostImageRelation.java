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
 * 帖子图片关联表
 * </p>
 *
 * @author dhy
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("post_image_relation")
@ApiModel(value="PostImageRelation对象", description="帖子图片关联表")
public class PostImageRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "关联ID")
    @TableId(value = "relation_id", type = IdType.AUTO)
    private Integer relationId;

    @ApiModelProperty(value = "帖子ID")
    private Integer postId;

    @ApiModelProperty(value = "文件ID")
    private Long fileId;

    @ApiModelProperty(value = "排序顺序")
    private Integer sortOrder;

    private LocalDateTime createdTime;


}
