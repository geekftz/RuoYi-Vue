package com.ruoyi.common.enums;

/**
 * 数据源
 * <p>
 * 【使用者】@DataSource注解的value取值；对应application-druid.yml中spring.datasource.druid.master/slave两个数据源配置。
 * 【使用场景】读写分离：@DataSource(DataSourceType.SLAVE)标在查询Service方法上，SQL路由到从库执行。
 * 不配从库时本枚举只有MASTER被使用，不影响单库运行。
 * 
 * @author ruoyi
 */
public enum DataSourceType
{
    /**
     * 主库
     */
    MASTER,

    /**
     * 从库
     */
    SLAVE
}
