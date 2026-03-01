package club.boyuan.official.teammatch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 点赞表实体类
 */
@Data
@TableName("like_record")
public class LikeRecord {

    /**
     * 点赞ID，主键
     */
    @TableId(value = "like_id", type = IdType.AUTO)
    private Integer likeId;

    /**
     * 点赞用户ID，关联user表
     */
    private Integer userId;

    /**
     * 目标类型：1-帖子 2-评论
     */
    private Integer targetType;

    /**
     * 目标ID
     */
    private Integer targetId;

    /**
     * 点赞时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}