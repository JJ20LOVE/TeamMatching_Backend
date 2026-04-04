package club.boyuan.official.teammatching.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步任务线程池配置
 */
@Configuration
public class AsyncConfig {

    /**
     * 邮箱验证码发送线程池：用于避免 SMTP 发送阻塞主请求线程。
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("email-sender-");
        executor.initialize();
        return executor;
    }
}

