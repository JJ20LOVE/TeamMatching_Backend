package club.boyuan.official.teammatching.dto.request.team;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新任务状态请求DTO
 */
@Data
@ApiModel(value = "更新任务状态请求")
public class UpdateTaskStatusRequest {
    @ApiModelProperty(value = "状态：0-待办 1-进行中 2-已完成", required = true)
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值非法")
    @Max(value = 2, message = "状态值非法")
    private Integer status;
}
