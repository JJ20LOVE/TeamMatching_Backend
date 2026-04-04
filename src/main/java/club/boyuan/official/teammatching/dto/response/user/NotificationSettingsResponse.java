package club.boyuan.official.teammatching.dto.response.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 通知设置响应
 */
@Data
@ApiModel(value = "通知设置响应")
public class NotificationSettingsResponse {

    @ApiModelProperty(value = "新消息通知")
    private boolean messageNotify;

    @ApiModelProperty(value = "项目状态更新通知")
    private boolean projectUpdateNotify;

    @ApiModelProperty(value = "组队邀请通知")
    private boolean invitationNotify;

    @ApiModelProperty(value = "系统通知")
    private boolean systemNotify;
}
