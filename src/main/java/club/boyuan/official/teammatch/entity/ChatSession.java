package club.boyuan.official.teammatch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 会话表实体类
 */
@Data
@TableName("chat_session")
public class ChatSession {

    /**
     * 会话ID，主键
     */
    @TableId(value = "session_id", type = IdType.AUTO)
    private Integer sessionId;

    /**
     * 参与者1，关联user表
     */
    private Integer user1Id;

    /**
     * 参与者2，关联user表
     */
    private Integer user2Id;

    /**
     * 关联项目ID（如果是项目相关聊天）
     */
    private Integer projectId;

    /**
     * 最后一条消息内容
     */
    private String lastMessage;

    /**
     * 最后消息时间
     */
    private LocalDateTime lastMsgTime;

    /**
     * 用户1未读数
     */
    private Integer user1Unread;

    /**
     * 用户2未读数
     */
    private Integer user2Unread;

    /**
     * 状态：1-正常 0-删除
     */
    private Integer status;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}