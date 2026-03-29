package club.boyuan.official.teammatching.dto.request.chat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新沟通状态请求 DTO
 */
@Data
@ApiModel(value = "更新沟通状态请求")
public class UpdateRecruitStatusRequest {

    @NotBlank(message = "recruitStatus不能为空")
    @ApiModelProperty(value = "沟通状态：communicating/offer/reject",
            required = true, example = "offer")
    private String recruitStatus;

    @NotBlank(message = "operator不能为空")
    @ApiModelProperty(value = "操作人：captain/student",
            required = true, example = "captain")
    private String operator;
}

