package club.boyuan.official.teammatching.mq.dto;

import club.boyuan.official.teammatching.mq.enums.NotificationCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 异步通知队列中的消息体（JSON 入 Redis List）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationMessage {

    /** 消息唯一标识（便于排查、去重扩展） */
    private String messageId;

    /** 创建时间 Unix 毫秒 */
    private long createTimeMillis;

    /** 通知分类 */
    private NotificationCategory category;

    /** 接收用户 ID */
    private Integer targetUserId;

    /** 标题 */
    private String title;

    /** 正文 / 摘要 */
    private String body;

    /** 业务类型说明，如 project_status_changed */
    private String bizType;

    /** 业务主键，如 projectId */
    private String bizId;

    /** 扩展 JSON（链接、模板参数等） */
    private String extraJson;
}
