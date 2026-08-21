package com.ruoyi.framework.manager;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.ruoyi.common.utils.Threads;
import com.ruoyi.common.utils.spring.SpringUtils;

/**
 * 异步任务管理器
 * <p>
 * 【架构位置】ruoyi-framework / manager层 —— 全局唯一的异步任务调度入口（单例）。
 * 【用途】把"不重要但要做"的事丢到后台线程执行，不阻塞主请求响应。
 * 典型调用方：LogAspect（操作日志）、SysLoginService（登录日志）→ 通过 AsyncFactory 创建日志任务 → 本类异步落库。
 * <p>
 * 【使用示例】AsyncManager.me().execute(AsyncFactory.recordLogininfor(...));
 *
 * @author ruoyi
 */
public class AsyncManager
{
    /**
     * 操作延迟10毫秒
     * 提交的任务延迟10ms再执行，避开主请求事务未提交就读写同一张库的时序问题
     */
    private final int OPERATE_DELAY_TIME = 10;

    /**
     * 异步操作任务调度线程池
     * 复用 ThreadPoolConfig 中定义的 scheduledExecutorService（守护线程池）
     */
    private ScheduledExecutorService executor = SpringUtils.getBean("scheduledExecutorService");

    /**
     * 单例模式：私有化构造，全局仅一个实例，通过 me() 获取
     */
    private AsyncManager(){}

    private static AsyncManager me = new AsyncManager();

    public static AsyncManager me()
    {
        return me;
    }

    /**
     * 执行任务
     * 
     * @param task 任务
     */
    public void execute(TimerTask task)
    {
        executor.schedule(task, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * 停止任务线程池：应用关闭时由 ShutdownManager 调用，保证正在执行的任务执行完再退出
     */
    public void shutdown()
    {
        Threads.shutdownAndAwaitTermination(executor);
    }
}
