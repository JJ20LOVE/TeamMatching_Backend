package club.boyuan.official.teammatching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 技能标签表
 * </p>
 *
 * @author dhy
 * @since 2026-03-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("skill_tag")
@ApiModel(value="SkillTag对象", description="技能标签表")
public class SkillTag implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "标签ID，主键")
    @TableId(value = "tag_id", type = IdType.AUTO)
    private Integer tagId;

    @ApiModelProperty(value = "标签名（如Python、Java）")
    private String tagName;

    @ApiModelProperty(value = "分类（编程语言、框架、工具等）")
    private String category;

    @ApiModelProperty(value = "排序权重")
    private Integer sortOrder;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
