package club.boyuan.official.teammatching.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话列表项响应 DTO
 */
@Data
@ApiModel(value = "会话列表项")
public class ChatSessionResponse {

    @ApiModelProperty(value = "会话ID", example = "501")
    private Integer sessionId;

    @ApiModelProperty(value = "对方用户")
    private TargetUser targetUser;

    @ApiModelProperty(value = "最后一条消息内容", example = "你好")
    private String lastMessage;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @ApiModelProperty(value = "最后消息时间", example = "2019-08-24T14:15:22Z")
    private LocalDateTime lastMsgTime;

    @ApiModelProperty(value = "未读数", example = "0")
    private Integer unreadCount;

    @ApiModelProperty(value = "招募沟通状态", example = "communicating")
    private String recruitStatus;

    @ApiModelProperty(value = "关联项目ID", example = "201")
    private Integer projectId;

    @ApiModelProperty(value = "关联项目名称", example = "基于AI的校园组队平台")
    private String projectName;

    @Data
    @ApiModel(value = "会话对方用户")
    public static class TargetUser {
        @ApiModelProperty(value = "用户ID", example = "10002")
        private Integer userId;
        @ApiModelProperty(value = "昵称", example = "张三")
        private String nickname;
        @ApiModelProperty(value = "头像", example = "https://example.com/avatar.jpg")
        private String avatar;
    }
}

