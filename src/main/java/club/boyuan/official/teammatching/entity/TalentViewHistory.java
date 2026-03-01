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
 * 人才浏览记录表
 * </p>
 *
 * @author dhy
 * @since 2026-03-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("talent_view_history")
@ApiModel(value="TalentViewHistory对象", description="人才浏览记录表")
public class TalentViewHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "浏览记录ID")
    @TableId(value = "view_id", type = IdType.AUTO)
    private Long viewId;

    @ApiModelProperty(value = "浏览者ID（通常是队长）")
    private Integer viewerId;

    @ApiModelProperty(value = "被浏览的人才ID")
    private Integer talentId;

    @ApiModelProperty(value = "被浏览的卡片ID")
    private Integer talentCardId;

    @ApiModelProperty(value = "浏览时长（秒）")
    private Integer viewDuration;

    @ApiModelProperty(value = "来源：搜索/推荐/关注列表")
    private String source;

    @ApiModelProperty(value = "浏览时间")
    private LocalDateTime createdTime;


}
