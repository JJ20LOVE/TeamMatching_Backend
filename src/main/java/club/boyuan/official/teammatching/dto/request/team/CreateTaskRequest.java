package club.boyuan.official.teammatching.dto.request.team;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建任务请求DTO
 */
@Data
@ApiModel(value = "创建任务请求")
public class CreateTaskRequest {
    @ApiModelProperty(value = "任务标题", required = true)
    @NotBlank(message = "任务标题不能为空")
    private String title;

    @ApiModelProperty(value = "任务描述")
    private String description;

    @ApiModelProperty(value = "负责人ID", required = true)
    @NotNull(message = "负责人ID不能为空")
    @Min(value = 1, message = "负责人ID非法")
    private Integer assigneeId;

    @ApiModelProperty(value = "截止日期，格式：yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline;
}
