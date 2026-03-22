package club.boyuan.official.teammatching.dto.response.contact;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 联系方式响应 DTO
 */
@Data
@ApiModel(value = "联系方式")
public class ContactInfoResponse {

    @ApiModelProperty(value = "手机号", example = "138****1234")
    private String phone;

    @ApiModelProperty(value = "邮箱", example = "zh***@example.com")
    private String email;

    @ApiModelProperty(value = "微信", example = "wechat123")
    private String wechat;

    @ApiModelProperty(value = "QQ", example = "12345678")
    private String qq;
}

