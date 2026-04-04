package club.boyuan.official.teammatching.mq.consumer;

import club.boyuan.official.teammatching.common.constants.RedisConstants;
import club.boyuan.official.teammatching.entity.User;
import club.boyuan.official.teammatching.mapper.UserMapper;
import club.boyuan.official.teammatching.mq.dto.NotificationMessage;
import club.boyuan.official.teammatching.mq.dto.NotificationPushDto;
import club.boyuan.official.teammatching.mq.enums.NotificationCategory;
import club.boyuan.official.teammatching.mq.support.NotificationPreferenceUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 从 Redis 队列取出通知，按用户通知开关过滤后推送到 {@code /topic/notify/{userId}}。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private static final int MAX_BATCH_PER_TICK = 50;
    private static final String PUSH_TYPE_NOTIFICATION = "NOTIFICATION";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedDelayString = "${app.notification.consumer.poll-interval-ms:500}")
    public void drainQueue() {
        for (int i = 0; i < MAX_BATCH_PER_TICK; i++) {
            String json = stringRedisTemplate.opsForList().leftPop(RedisConstants.NOTIFICATION_QUEUE_KEY);
            if (json == null) {
                break;
            }
            try {
                processOne(json);
            } catch (Exception e) {
                log.error("处理通知消息失败 raw={}", json, e);
            }
        }
    }

    private void processOne(String json) throws Exception {
        NotificationMessage msg = objectMapper.readValue(json, NotificationMessage.class);
        if (msg.getTargetUserId() == null || msg.getCategory() == null) {
            log.warn("通知消息字段不完整，已丢弃: {}", json);
            return;
        }
        User user = userMapper.selectById(msg.getTargetUserId());
        if (user == null) {
            log.warn("通知接收用户不存在 userId={}", msg.getTargetUserId());
            return;
        }
        if (!isAllowedByUserSettings(user, msg.getCategory())) {
            log.debug("用户已关闭该类通知，跳过 category={} userId={}", msg.getCategory(), msg.getTargetUserId());
            return;
        }

        NotificationPushDto push = new NotificationPushDto();
        push.setPushType(PUSH_TYPE_NOTIFICATION);
        push.setMessageId(msg.getMessageId());
        push.setCreateTimeMillis(msg.getCreateTimeMillis());
        push.setCategory(msg.getCategory().name());
        push.setTitle(msg.getTitle());
        push.setBody(msg.getBody());
        push.setBizType(msg.getBizType());
        push.setBizId(msg.getBizId());
        push.setExtraJson(msg.getExtraJson());

        String dest = "/topic/notify/" + msg.getTargetUserId();
        messagingTemplate.convertAndSend(dest, push);
        log.debug("通知已推送 dest={} category={}", dest, msg.getCategory());
    }

    private static boolean isAllowedByUserSettings(User user, NotificationCategory category) {
        return switch (category) {
            case MESSAGE -> NotificationPreferenceUtils.isChannelEnabled(user.getMessageNotify());
            case PROJECT_UPDATE -> NotificationPreferenceUtils.isChannelEnabled(user.getProjectUpdateNotify());
            case INVITATION -> NotificationPreferenceUtils.isChannelEnabled(user.getInvitationNotify());
            case SYSTEM -> NotificationPreferenceUtils.isChannelEnabled(user.getSystemNotify());
        };
    }
}
