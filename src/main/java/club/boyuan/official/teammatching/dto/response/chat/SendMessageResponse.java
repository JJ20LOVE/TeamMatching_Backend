package club.boyuan.official.teammatching.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 发送消息响应 DTO
 */
@Data
@ApiModel(value = "发送消息响应")
public class SendMessageResponse {

    @ApiModelProperty(value = "消息ID", example = "801")
    private Long messageId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @ApiModelProperty(value = "发送时间", example = "2024-01-01T10:30:00Z")
    private LocalDateTime sendTime;
}

