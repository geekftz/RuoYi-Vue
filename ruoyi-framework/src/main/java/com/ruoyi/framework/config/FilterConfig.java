package com.ruoyi.framework.config;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.filter.RefererFilter;
import com.ruoyi.common.filter.RepeatableFilter;
import com.ruoyi.common.filter.XssFilter;
import com.ruoyi.common.utils.StringUtils;

/**
 * 过滤器配置类
 * <p>
 * 注册系统所需的 Servlet 过滤器，包括：
 * - XssFilter：XSS（跨站脚本攻击）防护过滤器，对请求参数中的恶意脚本进行转义
 * - RefererFilter：防盗链过滤器，限制静态资源的访问来源域名
 * - RepeatableFilter：请求体可重复读取过滤器，使 request.getInputStream() 可多次读取
 * </p>
 * <p>
 * 过滤器执行顺序（从高到低）：XssFilter > RefererFilter > RepeatableFilter
 * </p>
 *
 * @author ruoyi
 */
@Configuration
public class FilterConfig
{
    /** XSS 过滤排除路径 */
    @Value("${xss.excludes}")
    private String excludes;

    /** XSS 过滤拦截路径 */
    @Value("${xss.urlPatterns}")
    private String urlPatterns;

    /** 防盗链允许的域名 */
    @Value("${referer.allowed-domains}")
    private String allowedDomains;

    /**
     * XSS 过滤器注册
     * <p>
     * 当配置 xss.enabled=true 时生效。对指定 URL 路径的请求进行 XSS 过滤，
     * 将请求参数中的 HTML 标签转义，防止跨站脚本攻击。
     * </p>
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Bean
    @ConditionalOnProperty(value = "xss.enabled", havingValue = "true")
    public FilterRegistrationBean xssFilterRegistration()
    {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new XssFilter());
        // 设置需要过滤的URL路径（逗号分隔）
        registration.addUrlPatterns(StringUtils.split(urlPatterns, ","));
        registration.setName("xssFilter");
        // 设置最高优先级，确保在其他过滤器之前执行
        registration.setOrder(FilterRegistrationBean.HIGHEST_PRECEDENCE);
        Map<String, String> initParameters = new HashMap<String, String>();
        // 设置排除过滤的路径
        initParameters.put("excludes", excludes);
        registration.setInitParameters(initParameters);
        return registration;
    }

    /**
     * 防盗链过滤器注册
     * <p>
     * 当配置 referer.enabled=true 时生效。对静态资源请求检查 Referer 头，
     * 只允许配置的域名列表访问资源，防止资源被外部网站盗用。
     * </p>
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Bean
    @ConditionalOnProperty(value = "referer.enabled", havingValue = "true")
    public FilterRegistrationBean refererFilterRegistration()
    {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new RefererFilter());
        // 拦截所有静态资源请求（/profile/uploadPath/*）
        registration.addUrlPatterns(Constants.RESOURCE_PREFIX + "/*");
        registration.setName("refererFilter");
        registration.setOrder(FilterRegistrationBean.HIGHEST_PRECEDENCE);
        Map<String, String> initParameters = new HashMap<String, String>();
        // 设置允许访问的域名列表
        initParameters.put("allowedDomains", allowedDomains);
        registration.setInitParameters(initParameters);
        return registration;
    }

    /**
     * 请求体可重复读取过滤器注册
     * <p>
     * Servlet 的 InputStream 默认只能读取一次，导致在过滤器/拦截器中读取请求体后，
     * Controller 无法再获取参数。此过滤器将请求体缓存为字节数组，支持多次读取。
     * </p>
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Bean
    public FilterRegistrationBean someFilterRegistration()
    {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new RepeatableFilter());
        // 拦截所有请求
        registration.addUrlPatterns("/*");
        registration.setName("repeatableFilter");
        // 最低优先级，确保在所有其他过滤器之后执行
        registration.setOrder(FilterRegistrationBean.LOWEST_PRECEDENCE);
        return registration;
    }

}
