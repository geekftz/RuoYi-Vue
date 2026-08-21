package com.ruoyi.framework.config.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.alibaba.druid.pool.DruidDataSource;

/**
 * druid 配置属性
 * <p>
 * 【架构位置】ruoyi-framework / config层 —— Druid数据库连接池属性装配类。
 * 【数据来源】application-druid.yml 中 spring.datasource.druid.* 各项，通过 @Value 逐一注入。
 * 【使用方】DruidConfig 创建数据源时调用 dataSource() 方法，把连接池参数刷进 DruidDataSource。
 * 【为什么手动装配】Druid部分高级参数（如filter、wall）不被Spring Boot自动配置完整支持，故手动接管。
 *
 * @author ruoyi
 */
@Configuration
public class DruidProperties
{
    /** 初始化时建立的物理连接数（应用启动就预创建，避免首次请求建连慢） */
    @Value("${spring.datasource.druid.initialSize}")
    private int initialSize;

    /** 最小空闲连接数：池中始终保持的最少连接，低于它会自动新建 */
    @Value("${spring.datasource.druid.minIdle}")
    private int minIdle;

    /** 最大连接数：并发上限，超过则新请求排队等待（maxWait超时后报错） */
    @Value("${spring.datasource.druid.maxActive}")
    private int maxActive;

    /** 获取连接时最大等待毫秒数，超时抛异常 */
    @Value("${spring.datasource.druid.maxWait}")
    private int maxWait;

    /** 建立物理连接的超时时间（毫秒） */
    @Value("${spring.datasource.druid.connectTimeout}")
    private int connectTimeout;

    /** 网络读超时：等待数据库返回结果的最长时间（毫秒），防慢SQL把连接挂死 */
    @Value("${spring.datasource.druid.socketTimeout}")
    private int socketTimeout;

    /** 空闲连接检测线程的运行间隔（毫秒） */
    @Value("${spring.datasource.druid.timeBetweenEvictionRunsMillis}")
    private int timeBetweenEvictionRunsMillis;

    /** 连接在池中最小可空闲时间，超过且空闲数>minIdle时被回收 */
    @Value("${spring.datasource.druid.minEvictableIdleTimeMillis}")
    private int minEvictableIdleTimeMillis;

    /** 连接在池中最大生存时间，超过强制回收（防MySQL的wait_timeout单方面断连） */
    @Value("${spring.datasource.druid.maxEvictableIdleTimeMillis}")
    private int maxEvictableIdleTimeMillis;

    /** 检测连接是否有效的SQL，如 SELECT 1 */
    @Value("${spring.datasource.druid.validationQuery}")
    private String validationQuery;

    /** 空闲时检测连接有效性（建议true，性能损耗小） */
    @Value("${spring.datasource.druid.testWhileIdle}")
    private boolean testWhileIdle;

    /** 取连接时检测（影响性能，一般false） */
    @Value("${spring.datasource.druid.testOnBorrow}")
    private boolean testOnBorrow;

    /** 还连接时检测（影响性能，一般false） */
    @Value("${spring.datasource.druid.testOnReturn}")
    private boolean testOnReturn;

    /**
     * 把上述yml配置参数刷入 DruidDataSource 对象
     *
     * @param datasource 待配置的Druid数据源（DruidConfig中new出来的主/从数据源）
     * @return 配置完成的数据源
     */
    public DruidDataSource dataSource(DruidDataSource datasource)
    {
        /** 配置初始化大小、最小、最大 */
        datasource.setInitialSize(initialSize);
        datasource.setMaxActive(maxActive);
        datasource.setMinIdle(minIdle);

        /** 配置获取连接等待超时的时间 */
        datasource.setMaxWait(maxWait);
        
        /** 配置驱动连接超时时间，检测数据库建立连接的超时时间，单位是毫秒 */
        datasource.setConnectTimeout(connectTimeout);
        
        /** 配置网络超时时间，等待数据库操作完成的网络超时时间，单位是毫秒 */
        datasource.setSocketTimeout(socketTimeout);

        /** 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒 */
        datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);

        /** 配置一个连接在池中最小、最大生存的时间，单位是毫秒 */
        datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        datasource.setMaxEvictableIdleTimeMillis(maxEvictableIdleTimeMillis);

        /**
         * 用来检测连接是否有效的sql，要求是一个查询语句，常用select 'x'。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会起作用。
         */
        datasource.setValidationQuery(validationQuery);
        /** 建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。 */
        datasource.setTestWhileIdle(testWhileIdle);
        /** 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。 */
        datasource.setTestOnBorrow(testOnBorrow);
        /** 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。 */
        datasource.setTestOnReturn(testOnReturn);
        return datasource;
    }
}
