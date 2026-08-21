package com.ruoyi.framework.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;

/**
 * 确保应用退出时能关闭后台线程
 * <p>
 * 【架构位置】ruoyi-framework / manager层 —— 优雅停机保障组件。
 * 【解决的问题】若依用后台线程池异步写日志（AsyncManager），如果应用直接kill退出，
 * 线程池中未执行完的任务会丢失（日志丢失）。本类在Spring容器销毁前回调，先关闭线程池并等待任务执行完。
 *
 * @author ruoyi
 */
@Component
public class ShutdownManager
{
    // 使用名为 sys-user 的logger（logback.xml中配置），停机日志写入 sys-user.log
    private static final Logger logger = LoggerFactory.getLogger("sys-user");

    /**
     * 【注解专项】@PreDestroy：Spring销毁Bean之前自动回调本方法，
     * 应用收到停机信号（kill/关闭）时触发，是优雅停机的标准入口
     */
    @PreDestroy
    public void destroy()
    {
        shutdownAsyncManager();
    }

    /**
     * 停止异步执行任务：关闭异步线程池，等待队列中剩余任务执行完毕
     */
    private void shutdownAsyncManager()
    {
        try
        {
            logger.info("====关闭后台任务任务线程池====");
            AsyncManager.me().shutdown();
        }
        catch (Exception e)
        {
            // 停机阶段的异常仅记录日志，不再抛出（避免影响正常退出流程）
            logger.error(e.getMessage(), e);
        }
    }
}
