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
 * 团队帖子表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("team_post")
@ApiModel(value = "TeamPost对象", description = "团队帖子表")
public class TeamPost implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "帖子ID，主键")
    @TableId(value = "post_id", type = IdType.AUTO)
    private Integer postId;

    @ApiModelProperty(value = "项目ID")
    private Integer projectId;

    @ApiModelProperty(value = "发布者ID")
    private Integer userId;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "内容")
    private String content;

    @ApiModelProperty(value = "状态：1-正常 0-删除")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
