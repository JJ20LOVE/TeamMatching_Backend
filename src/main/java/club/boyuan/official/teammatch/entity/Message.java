package club.boyuan.official.teammatch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息表实体类
 */
@Data
@TableName("message")
public class Message {

    /**
     * 消息ID，主键
     */
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;

    /**
     * 会话ID，关联chat_session表
     */
    private Integer sessionId;

    /**
     * 发送方ID，关联user表
     */
    private Integer senderId;

    /**
     * 接收方ID，关联user表
     */
    private Integer receiverId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 类型：1-文字 2-图片 3-系统通知 4-投递卡片 5-邀请卡片
     */
    private Integer msgType;

    /**
     * 状态：0-未读 1-已读 2-撤回
     */
    private Integer status;

    /**
     * 发送时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime sendTime;
}