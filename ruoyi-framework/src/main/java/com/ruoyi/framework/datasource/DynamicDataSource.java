package com.ruoyi.framework.datasource;

import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态数据源
 * <p>
 * 【架构位置】ruoyi-framework / datasource层 —— 多数据源路由核心。
 * 【实现原理】继承Spring的 AbstractRoutingDataSource（路由数据源）：本身不创建连接，
 * 每次执行SQL前调用 determineCurrentLookupKey() 拿到一个"数据源标识"，再从 targetDataSources 映射中找到真正的数据源用。
 * <p>
 * 【若依多数据源用法】Service方法上标注 @DataSource(DataSourceType.MASTER/SLAVE)，
 * DataSourceAspect 切面拦截后把标识存入 ThreadLocal（DynamicDataSourceContextHolder），本类据此路由。
 * 默认 master 主库；配置从库后可实现读写分离。
 *
 * @author ruoyi
 */
public class DynamicDataSource extends AbstractRoutingDataSource
{
    /**
     * 构造路由数据源
     *
     * @param defaultTargetDataSource 默认数据源（master主库，查不到标识时用它）
     * @param targetDataSources 全部数据源映射（key=DataSourceType枚举名，value=具体Druid数据源）
     */
    public DynamicDataSource(DataSource defaultTargetDataSource, Map<Object, Object> targetDataSources)
    {
        super.setDefaultTargetDataSource(defaultTargetDataSource);
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
    }

    /**
     * 决定本次SQL使用哪个数据源：从ThreadLocal中取当前线程的数据源标识
     * （由 @DataSource 注解的切面在方法执行前设置，执行后清除）
     */
    @Override
    protected Object determineCurrentLookupKey()
    {
        return DynamicDataSourceContextHolder.getDataSourceType();
    }
}