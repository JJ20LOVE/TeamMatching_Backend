package club.boyuan.official.teammatching.dto.request.contact;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 处理联系方式交换请求 DTO
 */
@Data
@ApiModel(value = "处理联系方式交换请求")
public class ContactExchangeRespondRequest {

    @NotNull(message = "agree不能为空")
    @ApiModelProperty(value = "true-同意 false-拒绝", required = true, example = "true")
    private Boolean agree;
}

