package club.boyuan.official.teammatching.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息历史响应 DTO
 */
@Data
@ApiModel(value = "消息项")
public class ChatMessageResponse {

    @ApiModelProperty(value = "消息ID", example = "801")
    private Long messageId;

    @ApiModelProperty(value = "发送方ID", example = "10001")
    private Integer senderId;

    @ApiModelProperty(value = "发送方昵称", example = "张三")
    private String senderNickname;

    @ApiModelProperty(value = "发送方头像", example = "https://example.com/avatar.jpg")
    private String senderAvatar;

    @ApiModelProperty(value = "消息内容", example = "你好")
    private String content;

    @ApiModelProperty(value = "类型：1-文字 2-图片 3-系统通知 4-投递卡片 5-邀请卡片", example = "1")
    private Integer msgType;

    @ApiModelProperty(value = "状态：0-未读 1-已读 2-撤回", example = "0")
    private Integer status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @ApiModelProperty(value = "发送时间", example = "2019-08-24T14:15:22Z")
    private LocalDateTime sendTime;
}

