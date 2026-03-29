package club.boyuan.official.teammatching.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 团队帖子附件关联表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("team_post_attachment")
@ApiModel(value = "TeamPostAttachment对象", description = "团队帖子附件关联表")
public class TeamPostAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "关联ID，主键")
    @TableId(value = "relation_id", type = IdType.AUTO)
    private Integer relationId;

    @ApiModelProperty(value = "帖子ID")
    private Integer postId;

    @ApiModelProperty(value = "文件ID")
    private Long fileId;

    @ApiModelProperty(value = "排序")
    private Integer sortOrder;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdTime;
}
