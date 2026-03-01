package club.boyuan.official.teammatch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评论表实体类
 */
@Data
@TableName("comment")
public class Comment {

    /**
     * 评论ID，主键
     */
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Integer commentId;

    /**
     * 帖子ID，关联community_post表
     */
    private Integer postId;

    /**
     * 评论者ID，关联user表
     */
    private Integer userId;

    /**
     * 父评论ID（支持楼中楼）
     */
    private Integer parentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 状态：1-正常 0-删除
     */
    private Integer status;

    /**
     * 评论时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}