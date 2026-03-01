package club.boyuan.official.teammatch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 关注表实体类
 */
@Data
@TableName("follow")
public class Follow {

    /**
     * 关注ID，主键
     */
    @TableId(value = "follow_id", type = IdType.AUTO)
    private Integer followId;

    /**
     * 关注者ID，关联user表
     */
    private Integer followerId;

    /**
     * 被关注者ID，关联user表
     */
    private Integer followingId;

    /**
     * 关注时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}