package club.boyuan.official.teammatching.dto.response.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 通知设置保存结果
 */
@Data
@ApiModel(value = "通知设置保存响应")
public class NotificationSettingsSavedResponse {

    @ApiModelProperty(value = "提示信息")
    private String message;
}
