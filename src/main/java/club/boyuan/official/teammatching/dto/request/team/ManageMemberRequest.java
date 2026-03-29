package club.boyuan.official.teammatching.dto.request.team;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理成员请求DTO
 */
@Data
@ApiModel(value = "管理成员请求")
public class ManageMemberRequest {
    @ApiModelProperty(value = "成员用户ID", required = true)
    @NotNull(message = "成员用户ID不能为空")
    @Min(value = 1, message = "成员用户ID非法")
    private Integer userId;
}
