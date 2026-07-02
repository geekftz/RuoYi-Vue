package com.ruoyi.framework.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 动态数据源上下文持有者
 * <p>
 * 基于 ThreadLocal 实现线程级的数据源切换。
 * 每个线程可以独立设置当前使用的数据源类型（MASTER/SLAVE），
 * 互不影响，方法执行完成后需调用 clearDataSourceType 清理。
 * </p>
 * <p>
 * 工作原理：DynamicDataSource（继承 AbstractRoutingDataSource）的 determineCurrentLookupKey() 方法
 * 会调用 getDataSourceType() 获取当前线程的数据源类型，从而路由到对应的数据源。
 * </p>
 *
 * @author ruoyi
 */
public class DynamicDataSourceContextHolder
{
    /** 日志记录器 */
    public static final Logger log = LoggerFactory.getLogger(DynamicDataSourceContextHolder.class);

    /**
     * 使用ThreadLocal维护变量，ThreadLocal为每个使用该变量的线程提供独立的变量副本，
     * 所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本。
     */
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程的数据源类型
     *
     * @param dsType 数据源类型名称（MASTER 或 SLAVE）
     */
    public static void setDataSourceType(String dsType)
    {
        log.info("切换到{}数据源", dsType);
        CONTEXT_HOLDER.set(dsType);
    }

    /**
     * 获取当前线程的数据源类型
     * <p>
     * 由 DynamicDataSource.determineCurrentLookupKey() 调用，
     * 返回null时使用默认数据源（主库）。
     * </p>
     *
     * @return 数据源类型名称，未设置时返回null
     */
    public static String getDataSourceType()
    {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清空当前线程的数据源类型（恢复为默认主数据源）
     * <p>
     * 必须在方法执行完成后（finally块中）调用，防止数据源泄漏影响后续请求。
     * </p>
     */
    public static void clearDataSourceType()
    {
        CONTEXT_HOLDER.remove();
    }
}
