package club.boyuan.official.teammatching.task;

import club.boyuan.official.teammatching.common.constants.RedisConstants;
import club.boyuan.official.teammatching.common.utils.OssUtil;
import club.boyuan.official.teammatching.entity.FileResource;
import club.boyuan.official.teammatching.mapper.FileResourceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 临时文件延迟队列消费者：到期后执行 DB 软删并删除 OSS 物理文件。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TempFileCleanupTask {

    private static final int BATCH_SIZE = 50;

    private final RedisTemplate<String, Object> redisTemplate;
    private final FileResourceMapper fileResourceMapper;
    private final OssUtil ossUtil;

    @Scheduled(fixedDelayString = "${tempfile.cleanup.fixed-delay-ms:60000}")
    public void consumeDueTempFiles() {
        long now = System.currentTimeMillis();

        // 按执行时间（score）拉取到期任务
        Set<Object> dueFileIds = redisTemplate.opsForZSet().rangeByScore(
                RedisConstants.TEMP_FILE_DELAY_QUEUE_KEY,
                Double.NEGATIVE_INFINITY,
                (double) now
        );

        if (dueFileIds == null || dueFileIds.isEmpty()) {
            return;
        }

        int processed = 0;
        for (Object obj : dueFileIds) {
            if (processed >= BATCH_SIZE) {
                break;
            }
            if (obj == null) {
                continue;
            }
            String fileIdStr = obj.toString();
            Long fileId;
            try {
                fileId = Long.valueOf(fileIdStr);
            } catch (NumberFormatException e) {
                // member 异常：直接移除避免死循环
                redisTemplate.opsForZSet().remove(RedisConstants.TEMP_FILE_DELAY_QUEUE_KEY, fileIdStr);
                continue;
            }

            // 先移除队列成员，避免同一轮重复消费
            redisTemplate.opsForZSet().remove(RedisConstants.TEMP_FILE_DELAY_QUEUE_KEY, fileIdStr);

            FileResource fileResource = fileResourceMapper.selectById(fileId);
            if (fileResource == null) {
                continue;
            }

            // 已被关联/已删除则跳过
            if (!Boolean.TRUE.equals(fileResource.getIsTemp()) || Boolean.TRUE.equals(fileResource.getIsDeleted())) {
                continue;
            }

            // 1) DB 软删除
            fileResource.setIsDeleted(true);
            fileResource.setDeletedTime(LocalDateTime.now());
            fileResourceMapper.updateById(fileResource);

            // 2) 删除 OSS 物理文件（幂等处理：失败不影响其他任务）
            String objectName = fileResource.getFileKey();
            if (objectName != null && !objectName.isBlank()) {
                ossUtil.deleteFile(objectName);
            }

            log.info("已清理临时文件：fileId={}", fileId);
            processed++;
        }
    }
}

