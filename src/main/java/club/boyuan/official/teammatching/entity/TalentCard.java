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
 * 人才卡片表
 * </p>
 *
 * @author dhy
 * @since 2026-03-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("talent_card")
@ApiModel(value="TalentCard对象", description="人才卡片表")
public class TalentCard implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "卡片ID，主键")
    @TableId(value = "card_id", type = IdType.AUTO)
    private Integer cardId;

    @ApiModelProperty(value = "用户ID，关联user表")
    private Integer userId;

    @ApiModelProperty(value = "状态：0-关闭（求组队关） 1-开启（求组队开） 2-已组队完成")
    private Integer status;

    @ApiModelProperty(value = "是否可见（F-15隐私设置）")
    private Boolean isVisible;

    @ApiModelProperty(value = "展示姓名（可匿名）")
    private String displayName;

    @ApiModelProperty(value = "专业")
    private String major;

    @ApiModelProperty(value = "年级")
    private String grade;

    @ApiModelProperty(value = "卡片标题（如：寻找大创队友）")
    private String cardTitle;

    @ApiModelProperty(value = "期望方向（如：后端开发/算法/产品）")
    private String targetDirection;

    @ApiModelProperty(value = "期望参赛（大创/挑战杯/互联网+）")
    private String expectedCompetition;

    @ApiModelProperty(value = "期望角色（队员/队长）")
    private String expectedRole;

    @ApiModelProperty(value = "自我陈述（补充说明）")
    private String selfStatement;

    @ApiModelProperty(value = "技能标签（逗号分隔，如：Python,Java,机器学习）")
    private String skillTags;

    @ApiModelProperty(value = "简历附件URL")
    private String resumeUrl;

    @ApiModelProperty(value = "简历文件名")
    private String resumeName;

    @ApiModelProperty(value = "作品集链接")
    private String portfolioUrl;

    @ApiModelProperty(value = "GitHub地址")
    private String githubUrl;

    @ApiModelProperty(value = "浏览次数")
    private Integer viewCount;

    @ApiModelProperty(value = "被邀请次数")
    private Integer inviteCount;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "最后可见时间（用于排序）")
    private LocalDateTime lastVisibleTime;


}
