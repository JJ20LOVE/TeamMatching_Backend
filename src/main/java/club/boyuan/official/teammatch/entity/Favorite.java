package club.boyuan.official.teammatch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 收藏表实体类
 */
@Data
@TableName("favorite")
public class Favorite {

    /**
     * 收藏ID，主键
     */
    @TableId(value = "favorite_id", type = IdType.AUTO)
    private Integer favoriteId;

    /**
     * 用户ID，关联user表
     */
    private Integer userId;

    /**
     * 类型：1-项目 2-帖子 3-人才卡片
     */
    private Integer targetType;

    /**
     * 目标ID
     */
    private Integer targetId;

    /**
     * 收藏时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}