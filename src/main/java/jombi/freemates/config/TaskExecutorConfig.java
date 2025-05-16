package jombi.freemates.config;

import java.util.concurrent.ExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
@EnableAsync
@Configuration
public class TaskExecutorConfig {

  @Bean(name = "applicationTaskExecutor")
  @Primary
  public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(50);
    executor.setQueueCapacity(1000);
    executor.setThreadNamePrefix("AsyncExecutor-");
    executor.initialize();
    return executor;
  }

  @Bean(name = "executorService")
  public ExecutorService executorService() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    // 필요한 설정 (corePoolSize, maxPoolSize 등) 적용
    executor.setMaxPoolSize(50);
    executor.setQueueCapacity(1000);
    executor.setThreadNamePrefix("AsyncExecutor-");
    executor.initialize();
    return executor.getThreadPoolExecutor();
  }
}
