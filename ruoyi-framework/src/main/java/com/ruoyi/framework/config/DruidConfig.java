package com.ruoyi.framework.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.alibaba.druid.spring.boot.autoconfigure.properties.DruidStatProperties;
import com.alibaba.druid.util.Utils;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.framework.config.properties.DruidProperties;
import com.ruoyi.framework.datasource.DynamicDataSource;

/**
 * Druid 数据源配置类
 * <p>
 * 配置多数据源（主库/从库），基于 Druid 连接池实现动态数据源切换。
 * 同时配置了 Druid 监控页面，并去除监控页面底部的广告信息。
 * </p>
 * <p>
 * 数据源配置流程：
 * 1. masterDataSource：创建主数据源（默认数据源）
 * 2. slaveDataSource：创建从数据源（可选，通过配置开关控制）
 * 3. dataSource：创建动态数据源路由，将主从数据源注册到 AbstractRoutingDataSource
 * 4. removeDruidFilterRegistrationBean：注册过滤器去除监控页面广告
 * </p>
 *
 * @author ruoyi
 */
@Configuration
public class DruidConfig
{
    /**
     * 主数据源配置（默认数据源）
     * <p>
     * 从 application.yml 的 spring.datasource.druid.master 配置项读取连接信息，
     * 创建 Druid 连接池实例并应用通用配置（初始连接数、最大连接数等）。
     * </p>
     *
     * @param druidProperties Druid 连接池通用配置
     * @return 主数据源
     */
    @Bean
    @ConfigurationProperties("spring.datasource.druid.master")
    public DataSource masterDataSource(DruidProperties druidProperties)
    {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidProperties.dataSource(dataSource);
    }

    /**
     * 从数据源配置（可选，通过 spring.datasource.druid.slave.enabled=true 开启）
     * <p>
     * 只有在配置文件中显式启用时才会创建，用于读写分离场景。
     * </p>
     *
     * @param druidProperties Druid 连接池通用配置
     * @return 从数据源
     */
    @Bean
    @ConfigurationProperties("spring.datasource.druid.slave")
    @ConditionalOnProperty(prefix = "spring.datasource.druid.slave", name = "enabled", havingValue = "true")
    public DataSource slaveDataSource(DruidProperties druidProperties)
    {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidProperties.dataSource(dataSource);
    }

    /**
     * 动态数据源配置（核心路由配置）
     * <p>
     * 将主从数据源注册到 DynamicDataSource（继承 AbstractRoutingDataSource），
     * 默认使用主数据源，运行时通过 DynamicDataSourceContextHolder 切换到从数据源。
     * </p>
     *
     * @param masterDataSource 主数据源
     * @return 动态数据源
     */
    @Bean(name = "dynamicDataSource")
    @Primary
    public DynamicDataSource dataSource(DataSource masterDataSource)
    {
        Map<Object, Object> targetDataSources = new HashMap<>();
        // 注册主数据源
        targetDataSources.put(DataSourceType.MASTER.name(), masterDataSource);
        // 注册从数据源（如果存在）
        setDataSource(targetDataSources, DataSourceType.SLAVE.name(), "slaveDataSource");
        return new DynamicDataSource(masterDataSource, targetDataSources);
    }
    
    /**
     * 设置数据源
     * 
     * @param targetDataSources 备选数据源集合
     * @param sourceName 数据源名称
     * @param beanName bean名称
     */
    public void setDataSource(Map<Object, Object> targetDataSources, String sourceName, String beanName)
    {
        try
        {
            DataSource dataSource = SpringUtils.getBean(beanName);
            targetDataSources.put(sourceName, dataSource);
        }
        catch (Exception e)
        {
        }
    }

    /**
     * 去除 Druid 监控页面底部的广告信息
     * <p>
     * 通过注册过滤器拦截 Druid 监控页面的 common.js 资源请求，
     * 将广告相关的 HTML 标签从 JavaScript 中正则移除。
     * </p>
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Bean
    @ConditionalOnProperty(name = "spring.datasource.druid.statViewServlet.enabled", havingValue = "true")
    public FilterRegistrationBean removeDruidFilterRegistrationBean(DruidStatProperties properties)
    {
        // 获取Druid监控页面的Servlet配置
        DruidStatProperties.StatViewServlet config = properties.getStatViewServlet();
        // 计算common.js的请求路径（默认为 /druid/js/common.js）
        String pattern = config.getUrlPattern() != null ? config.getUrlPattern() : "/druid/*";
        String commonJsPattern = pattern.replaceAll("\\*", "js/common.js");
        final String filePath = "support/http/resources/js/common.js";
        // 创建过滤器拦截common.js请求，去除广告内容
        Filter filter = new Filter()
        {
            @Override
            public void init(javax.servlet.FilterConfig filterConfig) throws ServletException
            {
            }
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException
            {
                chain.doFilter(request, response);
                // 重置缓冲区，响应头不会被重置
                response.resetBuffer();
                // 获取common.js
                String text = Utils.readFromResource(filePath);
                // 正则替换banner, 除去底部的广告信息
                text = text.replaceAll("<a.*?banner\"></a><br/>", "");
                text = text.replaceAll("powered.*?shrek.wang</a>", "");
                response.getWriter().write(text);
            }
            @Override
            public void destroy()
            {
            }
        };
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns(commonJsPattern);
        return registrationBean;
    }
}
