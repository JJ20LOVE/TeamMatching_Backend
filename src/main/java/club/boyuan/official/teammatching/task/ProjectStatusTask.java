package club.boyuan.official.teammatching.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 项目状态更新任务
 */
@Component
public class ProjectStatusTask {
    
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateProjectStatus() {
        // 项目状态更新逻辑
    }
}