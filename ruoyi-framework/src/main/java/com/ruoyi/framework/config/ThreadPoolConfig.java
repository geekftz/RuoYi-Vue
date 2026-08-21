package com.ruoyi.framework.config;

import com.ruoyi.common.utils.Threads;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 * <p>
 * 【架构位置】ruoyi-framework / config层 —— 统一管理系统异步线程资源。
 * 【使用场景】
 * 1. threadPoolTaskExecutor：配合 @Async("threadPoolTaskExecutor") 注解做异步任务，如异步记录操作日志（AsyncManager）；
 * 2. scheduledExecutorService：执行周期性/延迟任务，如异步工厂中的延时任务。
 * 【好处】避免每次 new Thread() 造成的资源浪费，线程复用、数量可控、防止高并发下线程爆炸拖垮服务。
 *
 * @author ruoyi
 **/
@Configuration
public class ThreadPoolConfig
{
    // 核心线程池大小：常驻线程数，即使空闲也不会被回收
    private int corePoolSize = 50;

    // 最大可创建的线程数：队列满后允许扩容到的上限
    private int maxPoolSize = 200;

    // 队列最大长度：核心线程都在忙时，新任务先进队列排队等待
    private int queueCapacity = 1000;

    // 线程池维护线程所允许的空闲时间：超过核心数的线程空闲300秒后被回收
    private int keepAliveSeconds = 300;

    /**
     * 通用异步任务线程池（Bean名称 threadPoolTaskExecutor）
     * <p>
     * 【使用示例】在方法上标注 @Async("threadPoolTaskExecutor")，该方法就会提交到本线程池异步执行，
     * 若依记录操作日志/登录日志就是用它异步落库，不阻塞主请求响应
     */
    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(corePoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // 线程池对拒绝任务(无线程可用)的处理策略
        // CallerRunsPolicy：队列和线程全满时，由提交任务的调用线程自己执行该任务（降级兜底，不丢任务）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    /**
     * 执行周期性或定时任务
     * <p>
     * 调度线程池：支持 schedule/scheduleAtFixedRate 等延迟与周期任务，
     * 线程名形如 schedule-pool-1 且为守护线程（JVM退出时自动结束）
     */
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService()
    {
        return new ScheduledThreadPoolExecutor(corePoolSize,
                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build(),
                new ThreadPoolExecutor.CallerRunsPolicy())
        {
            // 每个任务执行完后的钩子：打印异步任务中的异常堆栈，避免异步异常被静默吞掉
            @Override
            protected void afterExecute(Runnable r, Throwable t)
            {
                super.afterExecute(r, t);
                Threads.printException(r, t);
            }
        };
    }
}
