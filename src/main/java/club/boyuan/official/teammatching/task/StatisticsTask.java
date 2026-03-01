package club.boyuan.official.teammatching.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 统计数据生成任务
 */
@Component
public class StatisticsTask {
    
    @Scheduled(cron = "0 0 1 * * ?")
    public void generateStatistics() {
        // 统计数据生成逻辑
    }
}