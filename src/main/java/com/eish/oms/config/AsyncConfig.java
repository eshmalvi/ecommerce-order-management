package com.eish.oms.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.boot.task.ThreadPoolTaskExecutorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables {@code @Async} for the post-checkout pipeline. Pool sizes come from
 * {@code spring.task.execution.*} in application.yml.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * When the bounded queue is full, run the task on the calling thread instead of rejecting it.
     * That slows the producer down (backpressure) rather than dropping work or growing memory without limit.
     */
    @Bean
    public ThreadPoolTaskExecutorCustomizer callerRunsWhenSaturated() {
        return executor -> executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
