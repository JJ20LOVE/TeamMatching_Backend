package club.boyuan.official.teammatch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 人才卡片表实体类
 */
@Data
@TableName("talent_card")
public class TalentCard {

    /**
     * 卡片ID，主键
     */
    @TableId(value = "card_id", type = IdType.AUTO)
    private Integer cardId;

    /**
     * 用户ID，关联user表
     */
    private Integer userId;

    /**
     * 状态：0-关闭（求组队关） 1-开启（求组队开） 2-已组队完成
     */
    private Integer status;

    /**
     * 是否可见（F-15隐私设置）
     */
    private Boolean isVisible;

    /**
     * 展示姓名（可匿名）
     */
    private String displayName;

    /**
     * 专业
     */
    private String major;

    /**
     * 年级
     */
    private String grade;

    /**
     * 卡片标题（如：寻找大创队友）
     */
    private String cardTitle;

    /**
     * 期望方向（如：后端开发/算法/产品）
     */
    private String targetDirection;

    /**
     * 期望参赛（大创/挑战杯/互联网+）
     */
    private String expectedCompetition;

    /**
     * 期望角色（队员/队长）
     */
    private String expectedRole;

    /**
     * 自我陈述（补充说明）
     */
    private String selfStatement;

    /**
     * 技能标签（逗号分隔，如：Python,Java,机器学习）
     */
    private String skillTags;

    /**
     * 简历附件URL
     */
    private String resumeUrl;

    /**
     * 简历文件名
     */
    private String resumeName;

    /**
     * 作品集链接
     */
    private String portfolioUrl;

    /**
     * GitHub地址
     */
    private String githubUrl;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 被邀请次数
     */
    private Integer inviteCount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 最后可见时间（用于排序）
     */
    private LocalDateTime lastVisibleTime;
}