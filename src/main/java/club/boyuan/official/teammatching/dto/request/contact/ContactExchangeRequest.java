package club.boyuan.official.teammatching.dto.request.contact;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发起联系方式交换请求 DTO
 */
@Data
@ApiModel(value = "联系方式交换请求")
public class ContactExchangeRequest {

    @NotNull(message = "会话ID不能为空")
    @ApiModelProperty(value = "会话ID", required = true, example = "501")
    private Integer sessionId;

    @NotNull(message = "接收方ID不能为空")
    @ApiModelProperty(value = "接收方ID", required = true, example = "10002")
    private Integer receiverId;

    @ApiModelProperty(value = "关联项目ID（可选）", example = "201")
    private Integer projectId;
}

