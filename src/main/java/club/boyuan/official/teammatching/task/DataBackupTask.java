package club.boyuan.official.teammatching.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 数据备份任务
 */
@Component
public class DataBackupTask {
    
    @Scheduled(cron = "0 0 3 * * ?")
    public void backupData() {
        // 数据备份逻辑
    }
}