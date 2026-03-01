package club.boyuan.official.teammatch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 人才浏览记录表实体类
 */
@Data
@TableName("talent_view_history")
public class TalentViewHistory {

    /**
     * 浏览记录ID
     */
    @TableId(value = "view_id", type = IdType.AUTO)
    private Long viewId;

    /**
     * 浏览者ID（通常是队长）
     */
    private Integer viewerId;

    /**
     * 被浏览的人才ID
     */
    private Integer talentId;

    /**
     * 被浏览的卡片ID
     */
    private Integer talentCardId;

    /**
     * 浏览时长（秒）
     */
    private Integer viewDuration;

    /**
     * 来源：搜索/推荐/关注列表
     */
    private String source;

    /**
     * 浏览时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}