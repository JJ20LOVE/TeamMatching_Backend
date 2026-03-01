package club.boyuan.official.teammatch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 技能标签表实体类
 */
@Data
@TableName("skill_tag")
public class SkillTag {

    /**
     * 标签ID，主键
     */
    @TableId(value = "tag_id", type = IdType.AUTO)
    private Integer tagId;

    /**
     * 标签名（如Python、Java）
     */
    private String tagName;

    /**
     * 分类（编程语言、框架、工具等）
     */
    private String category;

    /**
     * 排序权重
     */
    private Integer sortOrder;

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
}