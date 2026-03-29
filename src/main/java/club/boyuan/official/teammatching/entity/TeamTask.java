package club.boyuan.official.teammatching.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 团队任务表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("team_task")
@ApiModel(value = "TeamTask对象", description = "团队任务表")
public class TeamTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "任务ID，主键")
    @TableId(value = "task_id", type = IdType.AUTO)
    private Integer taskId;

    @ApiModelProperty(value = "项目ID")
    private Integer projectId;

    @ApiModelProperty(value = "任务标题")
    private String title;

    @ApiModelProperty(value = "任务描述")
    private String description;

    @ApiModelProperty(value = "负责人ID")
    private Integer assigneeId;

    @ApiModelProperty(value = "创建者ID")
    private Integer creatorId;

    @ApiModelProperty(value = "截止日期")
    private LocalDate deadline;

    @ApiModelProperty(value = "状态：0-待办 1-进行中 2-已完成")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
