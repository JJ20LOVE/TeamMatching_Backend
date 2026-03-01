package club.boyuan.official.teammatch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 社区帖子表实体类
 */
@Data
@TableName("community_post")
public class CommunityPost {

    /**
     * 帖子ID，主键
     */
    @TableId(value = "post_id", type = IdType.AUTO)
    private Integer postId;

    /**
     * 发布者ID，关联user表
     */
    private Integer userId;

    /**
     * 板块：1-技术交流 2-灵感分享 3-组队经验
     */
    private Integer section;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 图片URL列表（JSON数组）
     */
    private String images;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 是否置顶
     */
    private Boolean isTop;

    /**
     * 是否精华
     */
    private Boolean isEssence;

    /**
     * 状态：1-正常 0-删除 2-违规下架
     */
    private Integer status;

    /**
     * 发布时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}