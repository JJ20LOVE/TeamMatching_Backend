package club.boyuan.official.teammatching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 消息表
 * </p>
 *
 * @author dhy
 * @since 2026-03-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("message")
@ApiModel(value="Message对象", description="消息表")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "消息ID，主键")
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;

    @ApiModelProperty(value = "会话ID，关联chat_session表")
    private Integer sessionId;

    @ApiModelProperty(value = "发送方ID，关联user表")
    private Integer senderId;

    @ApiModelProperty(value = "接收方ID，关联user表")
    private Integer receiverId;

    @ApiModelProperty(value = "消息内容")
    private String content;

    @ApiModelProperty(value = "类型：1-文字 2-图片 3-系统通知 4-投递卡片 5-邀请卡片")
    private Integer msgType;

    @ApiModelProperty(value = "状态：0-未读 1-已读 2-撤回")
    private Integer status;

    @ApiModelProperty(value = "发送时间")
    private LocalDateTime sendTime;


}
