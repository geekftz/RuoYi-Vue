package com.ruoyi.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.ruoyi.common.enums.DataSourceType;

/**
 * 自定义多数据源切换注解
 * <p>
 * 【作用】标在Service方法或类上，该方法内的数据库操作走指定数据源（主库MASTER/从库SLAVE，在application-druid.yml配置）。
 * 【处理链路】DataSourceAspect切面拦截本注解 → 把数据源类型存入ThreadLocal（DynamicDataSourceContextHolder）→
 *             DynamicDataSource（继承AbstractRoutingDataSource）执行SQL前读取ThreadLocal决定路由到哪个库 →
 *             方法结束后切面清除ThreadLocal。
 * 【使用示例】@DataSource(DataSourceType.SLAVE) 标在查询方法上实现读写分离。
 * 优先级：先方法，后类，如果方法覆盖了类上的数据源类型，以方法的为准，否则以类上的为准
 *
 * @author ruoyi
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DataSource
{
    /**
     * 切换数据源名称
     */
    public DataSourceType value() default DataSourceType.MASTER;
}
