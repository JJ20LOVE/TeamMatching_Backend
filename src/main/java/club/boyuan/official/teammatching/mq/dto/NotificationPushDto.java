package club.boyuan.official.teammatching.mq.dto;

import club.boyuan.official.teammatching.dto.response.user.NotificationSettingsResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 通过 STOMP 推送给前端的通知载荷。
 * <ul>
 *   <li>{@code NOTIFICATION}：业务通知（来自异步队列消费）</li>
 *   <li>{@code SETTINGS_SYNC}：用户修改通知设置后的多端同步（不经队列）</li>
 * </ul>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationPushDto {

    /** NOTIFICATION / SETTINGS_SYNC */
    private String pushType;

    private String messageId;
    private long createTimeMillis;

    /** {@link club.boyuan.official.teammatching.mq.enums.NotificationCategory} 名称 */
    private String category;
    private String title;
    private String body;
    private String bizType;
    private String bizId;
    private String extraJson;

    /** 仅 SETTINGS_SYNC 时有值 */
    private NotificationSettingsResponse settings;
}
