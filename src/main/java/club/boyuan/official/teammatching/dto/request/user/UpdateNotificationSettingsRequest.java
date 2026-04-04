package club.boyuan.official.teammatching.dto.request.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 更新通知设置请求（字段均可选，至少传一项）
 */
@Data
@ApiModel(value = "更新通知设置请求")
public class UpdateNotificationSettingsRequest {

    @ApiModelProperty(value = "新消息通知")
    private Boolean messageNotify;

    @ApiModelProperty(value = "项目状态更新通知")
    private Boolean projectUpdateNotify;

    @ApiModelProperty(value = "组队邀请通知")
    private Boolean invitationNotify;

    @ApiModelProperty(value = "系统通知")
    private Boolean systemNotify;
}
