package com.demo.springbootinit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置
 */
@Data
@ConfigurationProperties(prefix = "spring.task.execution.pool")
@Configuration
public class ThreadPoolExecutorConfig {

    private int coreSize;

    private int maxSize;

    private long keepAlive;

    private String threadName = "chart-ai-task-";

    /**
     * 线程池配置
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r);
            thread.setName(threadName + thread.getId());
            return thread;
        };
        return new ThreadPoolExecutor(
                coreSize, maxSize, keepAlive, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(4), threadFactory
        );
    }
}