package club.boyuan.official.teammatching.dto.request.chat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通过 STOMP 发送消息的请求载荷：
 * - 与 HTTP 接口的 SendMessageRequest 区分开，这里显式携带 senderId。
 */
@Data
@ApiModel(value = "STOMP 发送消息请求")
public class StompSendMessageRequest {

    @NotNull(message = "发送方ID不能为空")
    @ApiModelProperty(value = "发送方ID", required = true, example = "10001")
    private Integer senderId;

    @NotNull(message = "会话ID不能为空")
    @ApiModelProperty(value = "会话ID", required = true, example = "501")
    private Integer sessionId;

    @NotNull(message = "接收方ID不能为空")
    @ApiModelProperty(value = "接收方ID", required = true, example = "10002")
    private Integer receiverId;

    @NotNull(message = "消息内容不能为空")
    @ApiModelProperty(value = "消息内容", required = true, example = "你好，我对你的项目很感兴趣")
    private String content;

    @NotNull(message = "消息类型不能为空")
    @ApiModelProperty(value = "类型：1-文字 2-图片 3-系统通知 4-投递卡片 5-邀请卡片",
            required = true, example = "1")
    private Integer msgType;
}

