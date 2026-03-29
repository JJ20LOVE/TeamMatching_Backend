package club.boyuan.official.teammatching.dto.response.contact;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 联系方式交换请求响应 DTO
 */
@Data
@ApiModel(value = "联系方式交换请求响应")
public class ContactExchangeResponse {

    @ApiModelProperty(value = "交换记录ID", example = "901")
    private Integer exchangeId;

    @ApiModelProperty(value = "提示消息", example = "请求已发送，等待对方确认")
    private String message;
}

