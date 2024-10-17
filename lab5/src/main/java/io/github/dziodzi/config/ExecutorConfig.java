package io.github.dziodzi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Configuration
@EnableScheduling
public class ExecutorConfig {

    @Value("${custom.thread.pool.size}")
    private int fixedThreadPoolSize;

    @Bean
    public ExecutorService fixedThreadPool() {
        return Executors.newFixedThreadPool(fixedThreadPoolSize, runnable -> {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setName("FTP-" + thread.getId());
            return thread;
        });
    }

    @Bean
    public ScheduledThreadPoolExecutor scheduledThreadPool() {
        return (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
    }
}
