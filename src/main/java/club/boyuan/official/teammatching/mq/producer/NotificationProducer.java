package club.boyuan.official.teammatching.mq.producer;

import club.boyuan.official.teammatching.common.constants.RedisConstants;
import club.boyuan.official.teammatching.mq.dto.NotificationMessage;
import club.boyuan.official.teammatching.mq.enums.NotificationCategory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 通知消息生产者：将通知写入 Redis 队列，供后续消费者（站内信落库、WebSocket 推送、邮件等）异步处理。
 * <p>
 * 约定：使用 {@link RedisConstants#NOTIFICATION_QUEUE_KEY}，
 * 生产者使用 {@code RPUSH} 追加到队尾；消费者使用 {@code BLPOP} 从队头取出，保证 FIFO。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 投递一条通知到队列
     */
    public void publish(NotificationMessage message) {
        if (message.getTargetUserId() == null) {
            throw new IllegalArgumentException("通知接收用户 targetUserId 不能为空");
        }
        if (message.getCategory() == null) {
            throw new IllegalArgumentException("通知分类 category 不能为空");
        }
        if (message.getMessageId() == null || message.getMessageId().isBlank()) {
            message.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        }
        if (message.getCreateTimeMillis() <= 0) {
            message.setCreateTimeMillis(System.currentTimeMillis());
        }

        try {
            String json = objectMapper.writeValueAsString(message);
            Long size = stringRedisTemplate.opsForList().rightPush(RedisConstants.NOTIFICATION_QUEUE_KEY, json);
            log.debug("通知已入队 category={} targetUserId={} messageId={} queueSize={}",
                    message.getCategory(), message.getTargetUserId(), message.getMessageId(), size);
        } catch (JsonProcessingException e) {
            log.error("通知消息序列化失败 targetUserId={} category={}", message.getTargetUserId(), message.getCategory(), e);
            throw new IllegalStateException("通知消息序列化失败", e);
        }
    }

    public void publishMessage(Integer targetUserId, String title, String body, String bizType, String bizId) {
        publish(NotificationMessage.builder()
                .category(NotificationCategory.MESSAGE)
                .targetUserId(targetUserId)
                .title(title)
                .body(body)
                .bizType(bizType)
                .bizId(bizId)
                .build());
    }

    public void publishProjectUpdate(Integer targetUserId, String title, String body, String bizType, String bizId) {
        publish(NotificationMessage.builder()
                .category(NotificationCategory.PROJECT_UPDATE)
                .targetUserId(targetUserId)
                .title(title)
                .body(body)
                .bizType(bizType)
                .bizId(bizId)
                .build());
    }

    public void publishInvitation(Integer targetUserId, String title, String body, String bizType, String bizId) {
        publish(NotificationMessage.builder()
                .category(NotificationCategory.INVITATION)
                .targetUserId(targetUserId)
                .title(title)
                .body(body)
                .bizType(bizType)
                .bizId(bizId)
                .build());
    }

    public void publishSystem(Integer targetUserId, String title, String body, String bizType, String bizId) {
        publish(NotificationMessage.builder()
                .category(NotificationCategory.SYSTEM)
                .targetUserId(targetUserId)
                .title(title)
                .body(body)
                .bizType(bizType)
                .bizId(bizId)
                .build());
    }
}
